package com.example.util

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed interface PhoneAuthState {
    object Idle : PhoneAuthState
    object SendingCode : PhoneAuthState
    data class CodeSent(
        val verificationId: String,
        val phoneNumber: String,
        val token: PhoneAuthProvider.ForceResendingToken? = null
    ) : PhoneAuthState
    object VerifyingCode : PhoneAuthState
    data class Verified(
        val firebaseUser: FirebaseUser?,
        val phoneNumber: String
    ) : PhoneAuthState
    data class Error(val message: String) : PhoneAuthState
}

object PhoneAuthHelper {
    private const val TAG = "PhoneAuthHelper"
    val TEST_OTP = "123456"

    /**
     * Normalizes and formats Indian/Kashmiri phone number to E.164 standard (+91XXXXXXXXXX).
     */
    fun formatPhoneNumber(rawPhone: String): String {
        val cleanDigits = rawPhone.filter { it.isDigit() }
        return when {
            rawPhone.trim().startsWith("+") -> {
                "+" + cleanDigits
            }
            cleanDigits.length == 10 -> {
                "+91$cleanDigits"
            }
            cleanDigits.length == 11 && cleanDigits.startsWith("0") -> {
                "+91" + cleanDigits.substring(1)
            }
            cleanDigits.length == 12 && cleanDigits.startsWith("91") -> {
                "+$cleanDigits"
            }
            else -> {
                if (cleanDigits.isNotEmpty()) "+91$cleanDigits" else ""
            }
        }
    }

    /**
     * Extracts plain 10-digit number for local database matching.
     */
    fun extract10DigitPhone(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() }
        return if (digits.length >= 10) {
            digits.takeLast(10)
        } else {
            digits
        }
    }

    /**
     * Voice-First Helper: Converts spoken numbers (in English, Urdu/Hindi, or Kashmiri)
     * into a plain sequence of numeric digits.
     */
    fun parseSpokenDigits(text: String): String {
        val wordMap = mapOf(
            // English
            "zero" to "0", "oh" to "0", "sifar" to "0", "sifr" to "0", "shunya" to "0",
            "one" to "1", "won" to "1",
            "two" to "2", "to" to "2", "too" to "2",
            "three" to "3", "tree" to "3",
            "four" to "4", "for" to "4",
            "five" to "5",
            "six" to "6", "sex" to "6",
            "seven" to "7",
            "eight" to "8", "ate" to "8",
            "nine" to "9",
            // Urdu / Hindi
            "ek" to "1", "ik" to "1",
            "do" to "2",
            "teen" to "3", "tin" to "3",
            "char" to "4", "chaar" to "4",
            "paanch" to "5", "panch" to "5",
            "chhe" to "6", "cheh" to "6", "che" to "6",
            "saat" to "7", "sat" to "7",
            "aath" to "8", "aat" to "8",
            "nau" to "9", "no" to "9",
            // Kashmiri numerals
            "akh" to "1", "ak" to "1",
            "zı" to "2", "zuh" to "2", "ze" to "2", "zi" to "2",
            "treh" to "3", "tre" to "3", "tra" to "3",
            "tsor" to "4", "chor" to "4", "choor" to "4", "tsour" to "4",
            "paantsh" to "5", "pants" to "5", "pantsh" to "5",
            "sheh" to "6", "shey" to "6", "shi" to "6",
            "sath" to "7",
            "eath" to "8", "aeth" to "8",
            "nav" to "9", "naav" to "9", "now" to "9"
        )

        val clean = text.lowercase(Locale.ROOT)
            .replace(",", " ")
            .replace(".", " ")
            .replace("-", " ")
        val tokens = clean.split("\\s+".toRegex())

        val sb = StringBuilder()
        for (raw in tokens) {
            val token = raw.trim()
            if (token.isEmpty()) continue

            // Direct digit or group of digits
            val digits = token.filter { it.isDigit() }
            if (digits.isNotEmpty()) {
                sb.append(digits)
            } else if (wordMap.containsKey(token)) {
                sb.append(wordMap[token])
            }
        }
        return sb.toString()
    }

    /**
     * Initiates Firebase Phone Number verification.
     */
    fun sendVerificationCode(
        activity: Activity,
        rawPhoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks,
        resendToken: PhoneAuthProvider.ForceResendingToken? = null
    ) {
        val formattedNumber = formatPhoneNumber(rawPhoneNumber)
        val auth = FirebaseAuth.getInstance()

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken)
        }

        try {
            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
            Log.d(TAG, "Phone verification started for $formattedNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating phone verification", e)
            val fakeException = FirebaseException("Verification error: ${e.localizedMessage ?: "Unknown error"}")
            callbacks.onVerificationFailed(fakeException)
        }
    }

    /**
     * Verifies the 6-digit OTP code against the verification ID.
     */
    fun verifyOtp(
        verificationId: String,
        otpCode: String,
        onSuccess: (FirebaseUser?) -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()

        // Development/Test simulated OTP support guarded strictly to DEBUG builds only
        if (com.example.BuildConfig.DEBUG && (verificationId.startsWith("TEST_") || otpCode == TEST_OTP)) {
            Log.d(TAG, "Test OTP used for development verification")
            onSuccess(auth.currentUser)
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        Log.d(TAG, "Firebase phone sign-in successful: ${user?.uid}")
                        onSuccess(user)
                    } else {
                        val ex = task.exception
                        Log.w(TAG, "Firebase phone sign-in failed", ex)
                        val message = when (ex) {
                            is FirebaseAuthInvalidCredentialsException -> "Invalid OTP code. Please check and try again."
                            else -> ex?.localizedMessage ?: "Verification failed. Please try again."
                        }
                        onError(message)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error during OTP credential sign-in", e)
            onError(e.localizedMessage ?: "Authentication error")
        }
    }
}
