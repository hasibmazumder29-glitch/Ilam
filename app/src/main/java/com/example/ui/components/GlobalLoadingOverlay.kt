package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.theme.*

/**
 * Global Loading Overlay Component
 *
 * Provides a high-fashion, luxury visual feedback overlay during asynchronous
 * operations such as security authentication, digital payment processing,
 * order placement, coupon validation, or network data synchronizations.
 *
 * Features:
 * - Full-screen non-blocking pointer shield (prevents duplicate submissions)
 * - Gold-accented indeterminate spinner with glowing brand emblem
 * - Context-aware status icons (Auth Lock, Bag, Sync, Diamond)
 * - Bilingual support for English and Bengali
 * - Animated pulsing micro-indicators
 * - Fully testable with testTags and accessibility semantics
 */
@Composable
fun GlobalLoadingOverlay(
    isVisible: Boolean,
    message: String? = null,
    subtitle: String? = null,
    language: AppLanguage = AppLanguage.EN,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(220)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(animationSpec = tween(180)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(180)
        ),
        modifier = modifier
    ) {
        val displayMessage = message ?: Strings.loading(language)
        val displaySubtitle = subtitle ?: Strings.pleaseWait(language)

        // Select context icon if not explicitly provided
        val resolvedIcon = icon ?: when {
            displayMessage.contains("PIN", ignoreCase = true) ||
            displayMessage.contains("Auth", ignoreCase = true) ||
            displayMessage.contains("পাসকোড", ignoreCase = true) ||
            displayMessage.contains("নিরাপত্তা", ignoreCase = true) -> Icons.Default.Lock

            displayMessage.contains("Order", ignoreCase = true) ||
            displayMessage.contains("অর্ডার", ignoreCase = true) ||
            displayMessage.contains("Payment", ignoreCase = true) ||
            displayMessage.contains("পেমেন্ট", ignoreCase = true) -> Icons.Default.ShoppingBag

            displayMessage.contains("Update", ignoreCase = true) ||
            displayMessage.contains("Sync", ignoreCase = true) ||
            displayMessage.contains("তথ্য", ignoreCase = true) -> Icons.Default.Sync

            else -> Icons.Default.Diamond
        }

        // Fullscreen Modal Scrim (absorbs pointer events)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC08080A))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    // Modal barrier: Absorb taps to prevent double clicks during async operations
                }
                .semantics {
                    contentDescription = "$displayMessage. $displaySubtitle"
                }
                .testTag("global_loading_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Elegant Elevated Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IlamCardDark,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            IlamGold.copy(alpha = 0.65f),
                            IlamGoldDark.copy(alpha = 0.25f)
                        )
                    )
                ),
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 340.dp)
                    .padding(horizontal = 24.dp)
                    .testTag("global_loading_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Brand Badge Header
                    Text(
                        text = Strings.brandName(language).uppercase(),
                        color = IlamGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Animated Spinner with Center Emblem
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp)
                    ) {
                        // Outer subtle ring
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = IlamGold.copy(alpha = 0.15f),
                            strokeWidth = 3.dp,
                        )

                        // Indeterminate spinning gold ring
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("global_loading_spinner"),
                            color = IlamGold,
                            strokeWidth = 3.dp,
                            trackColor = Color.Transparent
                        )

                        // Center Icon with soft pulsing background
                        PulsingEmblemContainer(icon = resolvedIcon)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Feedback Message
                    Text(
                        text = displayMessage,
                        color = IlamWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.testTag("global_loading_message")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle / Helper
                    Text(
                        text = displaySubtitle,
                        color = IlamTextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("global_loading_subtitle")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Micro Dots
                    LoadingMicroDots(color = IlamGold)
                }
            }
        }
    }
}

@Composable
private fun PulsingEmblemContainer(icon: ImageVector) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblemAlpha"
    )

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(IlamSurfaceDark),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IlamGold.copy(alpha = alpha),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LoadingMicroDots(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    @Composable
    fun dotAlpha(delay: Int): Float {
        val anim by infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    0.25f at 0
                    1.0f at (delay + 250)
                    0.25f at (delay + 500)
                    0.25f at 1000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "dot"
        )
        return anim
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = dotAlpha(0)))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = dotAlpha(150)))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = dotAlpha(300)))
        )
    }
}
