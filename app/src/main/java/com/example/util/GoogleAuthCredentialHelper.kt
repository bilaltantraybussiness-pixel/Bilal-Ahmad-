package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object GoogleAuthCredentialHelper {
    private const val TAG = "GoogleAuthCredentialHelper"

    /**
     * Initiates Google Sign-In via Jetpack Credential Manager and Firebase Authentication.
     */
    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onAuthError: (String) -> Unit,
        onAuthCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e(TAG, "Missing default_web_client_id configuration", e)
            onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    
                    val firebaseUser = suspendCancellableCoroutine<FirebaseUser?> { cont ->
                        val auth = FirebaseAuth.getInstance()
                        auth.signInWithCredential(authCredential)
                            .addOnSuccessListener { authResult ->
                                if (cont.isActive) cont.resume(authResult.user)
                            }
                            .addOnFailureListener { exception ->
                                if (cont.isActive) cont.resumeWithException(exception)
                            }
                    }

                    if (firebaseUser != null) {
                        Log.d(TAG, "Firebase Google Auth succeeded: ${firebaseUser.uid}")
                        onAuthSuccess(firebaseUser)
                    } else {
                        onAuthError("Unable to retrieve Firebase user session.")
                    }
                } else {
                    Log.w(TAG, "Unexpected credential type returned: ${credential::class.java.name}")
                    onAuthError("Unexpected credential type received.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onAuthError(e.localizedMessage ?: "Google Sign-In failed. Please try again.")
            }
        }
    }

    /**
     * Signs out of Firebase Auth and clears Credential Manager saved credential state.
     */
    fun signOut(
        context: Context,
        scope: CoroutineScope,
        onSignOutComplete: () -> Unit = {}
    ) {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signOut warning: ${e.message}")
        }

        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                onSignOutComplete()
            }
        }
    }
}
