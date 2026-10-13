package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KashmirCardBorder
import com.example.ui.theme.KashmirDeepGreen
import com.example.ui.theme.KashmirTextPrimary
import com.example.ui.theme.KashmirTextSecondary
import com.example.util.GoogleAuthCredentialHelper
import com.example.viewmodel.RozgarViewModel
import kotlinx.coroutines.launch

@Composable
fun GoogleSignInButton(
    viewModel: RozgarViewModel,
    modifier: Modifier = Modifier,
    buttonText: String = "Sign in with Google",
    onSignInSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = {
                val activity = context as? Activity
                if (activity == null) {
                    Toast.makeText(context, "Activity context unavailable", Toast.LENGTH_SHORT).show()
                    return@OutlinedButton
                }

                isLoading = true
                errorMessage = null

                GoogleAuthCredentialHelper.signInWithGoogle(
                    activity = activity,
                    scope = coroutineScope,
                    onAuthSuccess = { firebaseUser ->
                        isLoading = false
                        viewModel.handleGoogleSignInSuccess(firebaseUser) { success, msg ->
                            if (success) {
                                Toast.makeText(context, "Welcome, ${firebaseUser.displayName ?: "Friend"}!", Toast.LENGTH_SHORT).show()
                                onSignInSuccess()
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    onAuthError = { errorMsg ->
                        isLoading = false
                        errorMessage = errorMsg
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    },
                    onAuthCancelled = {
                        isLoading = false
                    }
                )
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("google_sign_in_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = KashmirTextPrimary
            ),
            border = BorderStroke(1.dp, KashmirCardBorder)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = KashmirDeepGreen,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Connecting to Google...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = KashmirTextSecondary
                    )
                } else {
                    // Google 4-Color "G" Emblem
                    GoogleIconG(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = buttonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KashmirTextPrimary
                    )
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Material-styled Google 4-color "G" icon using Canvas vector drawing.
 */
@Composable
fun GoogleIconG(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Blue right-arm & quadrant
        val bluePath = Path().apply {
            moveTo(cx, cy)
            lineTo(w * 0.95f, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, w, h),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 50f,
                forceMoveTo = false
            )
            lineTo(cx, cy)
            close()
        }
        drawPath(bluePath, Color(0xFF4285F4), style = Fill)

        // Green bottom quadrant
        val greenPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, w, h),
                startAngleDegrees = 50f,
                sweepAngleDegrees = 110f,
                forceMoveTo = false
            )
            lineTo(cx, cy)
            close()
        }
        drawPath(greenPath, Color(0xFF34A853), style = Fill)

        // Yellow bottom-left quadrant
        val yellowPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, w, h),
                startAngleDegrees = 160f,
                sweepAngleDegrees = 65f,
                forceMoveTo = false
            )
            lineTo(cx, cy)
            close()
        }
        drawPath(yellowPath, Color(0xFFFBBC05), style = Fill)

        // Red top quadrant
        val redPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, w, h),
                startAngleDegrees = 225f,
                sweepAngleDegrees = 135f,
                forceMoveTo = false
            )
            lineTo(cx, cy)
            close()
        }
        drawPath(redPath, Color(0xFFEA4335), style = Fill)

        // Inner circle knockout for the "G" cutout
        drawCircle(
            color = Color.White,
            radius = w * 0.28f,
            center = Offset(cx, cy)
        )

        // Right crossbar of the "G"
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(cx, cy - h * 0.1f),
            size = androidx.compose.ui.geometry.Size(w * 0.45f, h * 0.2f)
        )
    }
}
