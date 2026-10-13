package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Kashmir Heritage Shimmer Modifier
 * Provides a gentle, high-performance shimmer sweep across UI placeholders
 * matching the Kashmir color aesthetics.
 */
fun Modifier.shimmerEffect(
    shape: Shape = RoundedCornerShape(8.dp),
    baseColor: Color = KashmirSurfaceVariant.copy(alpha = 0.65f),
    highlightColor: Color = Color.White.copy(alpha = 0.85f),
    durationMillis: Int = 1100
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "KashmirShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "KashmirShimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )

    this
        .clip(shape)
        .background(brush)
}

/**
 * Standard Shimmer Box Placeholder
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier.shimmerEffect(shape = shape)
    )
}

/**
 * Standard Shimmer Text Line Placeholder
 */
@Composable
fun ShimmerTextLine(
    width: Dp = 120.dp,
    height: Dp = 14.dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shimmerEffect(shape = shape)
    )
}

/**
 * Standard Shimmer Circle Placeholder
 */
@Composable
fun ShimmerCircle(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shimmerEffect(shape = CircleShape)
    )
}

/**
 * Standard Shimmer Pill / Badge Placeholder
 */
@Composable
fun ShimmerPill(
    width: Dp = 70.dp,
    height: Dp = 22.dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shimmerEffect(shape = shape)
    )
}

// =========================================================================
// DOMAIN COMPONENT SKELETON SCREENS
// =========================================================================

/**
 * Job Card Skeleton
 * Exactly mirrors JobCardItem in FindJobsScreen and SeasonalWorkScreen
 */
@Composable
fun JobCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("job_card_skeleton"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar + Title & Employer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerTextLine(width = 170.dp, height = 16.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerTextLine(width = 110.dp, height = 12.dp)
                }
                ShimmerPill(width = 65.dp, height = 24.dp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wage & District Tags Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerPill(width = 90.dp, height = 24.dp)
                ShimmerPill(width = 110.dp, height = 24.dp)
                ShimmerPill(width = 75.dp, height = 24.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description preview lines
            ShimmerTextLine(width = 240.dp, height = 12.dp)
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerTextLine(width = 180.dp, height = 12.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerPill(width = 90.dp, height = 20.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBox(modifier = Modifier.size(36.dp), shape = CircleShape)
                    ShimmerBox(modifier = Modifier.size(36.dp), shape = CircleShape)
                    ShimmerBox(modifier = Modifier.width(90.dp).height(36.dp), shape = RoundedCornerShape(8.dp))
                }
            }
        }
    }
}

/**
 * Worker Card Skeleton
 * Exactly mirrors WorkerCardItem in FindWorkersScreen
 */
@Composable
fun WorkerCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("worker_card_skeleton"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar, Name, Profession, Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerCircle(size = 54.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ShimmerTextLine(width = 130.dp, height = 16.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        ShimmerCircle(size = 16.dp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerTextLine(width = 95.dp, height = 12.dp)
                }
                ShimmerPill(width = 80.dp, height = 24.dp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rating & Location Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShimmerPill(width = 65.dp, height = 22.dp)
                ShimmerPill(width = 110.dp, height = 22.dp)
                ShimmerPill(width = 85.dp, height = 22.dp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skills chips placeholder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShimmerBox(modifier = Modifier.width(70.dp).height(20.dp), shape = RoundedCornerShape(6.dp))
                ShimmerBox(modifier = Modifier.width(90.dp).height(20.dp), shape = RoundedCornerShape(6.dp))
                ShimmerBox(modifier = Modifier.width(80.dp).height(20.dp), shape = RoundedCornerShape(6.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (View Profile & Message)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

/**
 * Product Card Skeleton
 * Mirrors MarketProductCard in MarketplaceScreen
 */
@Composable
fun ProductCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("product_card_skeleton"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column {
            // Product Image Banner
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                // Category & Seller pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShimmerPill(width = 80.dp, height = 20.dp)
                    ShimmerPill(width = 60.dp, height = 20.dp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                ShimmerTextLine(width = 200.dp, height = 16.dp)
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerTextLine(width = 140.dp, height = 12.dp)

                Spacer(modifier = Modifier.height(12.dp))

                // Price & Reseller Margin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerTextLine(width = 80.dp, height = 18.dp)
                    ShimmerPill(width = 110.dp, height = 26.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Button
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

/**
 * Service History Card Skeleton
 * Mirrors service records in UserProfileManagerComponent
 */
@Composable
fun ServiceHistoryCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("service_history_skeleton"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerTextLine(width = 160.dp, height = 16.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerTextLine(width = 110.dp, height = 12.dp)
                }
                ShimmerPill(width = 75.dp, height = 22.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Row: Category & Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerPill(width = 90.dp, height = 22.dp)
                ShimmerPill(width = 130.dp, height = 22.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerTextLine(width = 90.dp, height = 16.dp)
                ShimmerPill(width = 50.dp, height = 20.dp)
            }
        }
    }
}

/**
 * Message Conversation Item Skeleton
 * Mirrors chat row in MessagesScreen
 */
@Composable
fun MessageItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("message_item_skeleton"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerCircle(size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShimmerTextLine(width = 130.dp, height = 14.dp)
                    ShimmerTextLine(width = 45.dp, height = 11.dp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerTextLine(width = 180.dp, height = 12.dp)
            }
        }
    }
}

/**
 * Notification Card Skeleton
 * Mirrors notification items in NotificationsScreen
 */
@Composable
fun NotificationCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("notification_skeleton"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerCircle(size = 36.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerTextLine(width = 140.dp, height = 14.dp)
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerTextLine(width = 210.dp, height = 12.dp)
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerTextLine(width = 70.dp, height = 10.dp)
            }
        }
    }
}

/**
 * Kashmiri Literature Book Skeleton
 * Mirrors book / poetry card in KashmiriLiteratureScreen
 */
@Composable
fun LiteratureBookSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("literature_book_skeleton"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover rectangle
            ShimmerBox(
                modifier = Modifier
                    .width(60.dp)
                    .height(84.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                ShimmerTextLine(width = 170.dp, height = 16.dp)
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerTextLine(width = 110.dp, height = 12.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerPill(width = 70.dp, height = 20.dp)
                    ShimmerPill(width = 60.dp, height = 20.dp)
                }
            }
        }
    }
}

/**
 * Stat Card Skeleton for Admin Dashboard
 */
@Composable
fun StatCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("stat_card_skeleton"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            ShimmerBox(modifier = Modifier.size(28.dp), shape = RoundedCornerShape(6.dp))
            Spacer(modifier = Modifier.height(10.dp))
            ShimmerTextLine(width = 50.dp, height = 22.dp)
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerTextLine(width = 80.dp, height = 12.dp)
        }
    }
}

/**
 * Home Screen Feed Skeleton
 * High-fidelity preview when loading home recommendations
 */
@Composable
fun HomeScreenFeedSkeleton(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(2) {
            JobCardSkeleton()
            Spacer(modifier = Modifier.height(8.dp))
        }
        repeat(2) {
            WorkerCardSkeleton()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
