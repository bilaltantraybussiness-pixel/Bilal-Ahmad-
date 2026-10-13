package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ReviewEntity
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    starSize: Int = 24
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { starIndex ->
            val isSelected = starIndex <= rating
            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = "$starIndex Stars",
                tint = if (isSelected) KashmirGold else KashmirTextSecondary.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(starSize.dp)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier
                                .clip(CircleShape)
                                .clickable { onRatingChanged(starIndex) }
                                .padding(2.dp)
                        } else Modifier
                    )
            )
        }
    }
}

@Composable
fun ReviewItemCard(
    review: ReviewEntity,
    onReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(review.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(review.createdAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("review_item_${review.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(KashmirMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.authorName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(review.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirTextPrimary)
                        Text(dateStr, fontSize = 10.sp, color = KashmirTextSecondary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRatingBar(rating = review.rating, starSize = 14)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onReport, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = "Report Review",
                            tint = if (review.isReported) KashmirError else KashmirTextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verified Completed Transaction Badge
            if (review.transactionOrJobRef.isNotBlank()) {
                Surface(
                    color = KashmirBackground,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TaskAlt, null, tint = KashmirEmerald, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verified: ${review.transactionOrJobRef}", fontSize = 10.sp, color = KashmirEmerald, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextPrimary, fontSize = 13.sp)
            )

            if (review.isReported) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⚠️ Flagged for admin review: ${review.reportReason}",
                    fontSize = 11.sp,
                    color = KashmirError
                )
            }
        }
    }
}

@Composable
fun ReviewSubmissionDialog(
    targetType: String, // "WORKER", "JOB", "PRODUCT"
    targetId: Long,
    targetTitle: String,
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var transactionRef by remember { mutableStateOf("Completed Service / Job") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val ratingLabels = listOf("Poor", "Fair", "Good", "Very Good", "Excellent / Master Quality")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("review_submission_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Leave a Verified Review", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Reviewing: $targetTitle", fontSize = 12.sp, color = KashmirTextSecondary)

                Spacer(modifier = Modifier.height(16.dp))

                // Star Rating Picker
                StarRatingBar(
                    rating = rating,
                    onRatingChanged = { rating = it },
                    starSize = 34
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${rating} Stars - ${ratingLabels.getOrElse(rating - 1) { "" }}",
                    fontWeight = FontWeight.Bold,
                    color = KashmirGold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Written Review Input
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Your Honest Feedback") },
                    placeholder = { Text("Describe work quality, punctuality, craft expertise, or transaction...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("review_text_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Transaction / Job Reference
                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("Service or Job Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = KashmirError, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (reviewText.trim().length < 5) {
                            errorMessage = "Please enter at least a few words for your review."
                            return@Button
                        }
                        isSubmitting = true
                        errorMessage = null

                        viewModel.submitReview(
                            targetType = targetType,
                            targetId = targetId,
                            targetTitle = targetTitle,
                            rating = rating,
                            reviewText = reviewText.trim(),
                            transactionRef = transactionRef.trim()
                        ) { success, msg ->
                            isSubmitting = false
                            if (success) {
                                onDismiss()
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("submit_review_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Post Review", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
