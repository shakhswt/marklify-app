package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Rich, Vivid Backdrop.
 * Creates a high-contrast, deeply saturated space (saturated indigo, royal blue,
 * electric violet, magenta, and cyan light pools) over a deep midnight void.
 * On Android 12+ (API 31+), the light pools are optically blurred into a silky refraction field.
 */
@Composable
fun LiquidGlassBackdrop(
    modifier: Modifier = Modifier,
    animated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val isApi31Plus = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VividBackdropBase)
    ) {
        // Vivid Light Pools Canvas Layer
        val blurModifier = if (isApi31Plus) {
            Modifier.blur(48.dp)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(blurModifier)
                .drawBehind {
                    // Pool 1: Deep Saturated Electric Indigo (Top-Left)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividIndigoPool.copy(alpha = 0.70f),
                                VividIndigoPool.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.20f, size.height * 0.15f),
                            radius = size.width * 0.85f
                        )
                    )

                    // Pool 2: Vivid Royal Blue (Right-Center)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividRoyalBluePool.copy(alpha = 0.65f),
                                VividRoyalBluePool.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.38f),
                            radius = size.width * 0.75f
                        )
                    )

                    // Pool 3: Vivid Electric Violet (Bottom-Left)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividVioletPool.copy(alpha = 0.60f),
                                VividVioletPool.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.25f, size.height * 0.75f),
                            radius = size.width * 0.80f
                        )
                    )

                    // Pool 4: Vivid Saturated Magenta (Bottom-Right)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividMagentaPool.copy(alpha = 0.55f),
                                VividMagentaPool.copy(alpha = 0.20f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.90f, size.height * 0.88f),
                            radius = size.width * 0.70f
                        )
                    )

                    // Pool 5: Vivid Electric Cyan Highlight (Center Accent)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividCyanPool.copy(alpha = 0.40f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.50f, size.height * 0.52f),
                            radius = size.width * 0.50f
                        )
                    )
                }
        )

        // Screen Foreground Content
        content()
    }
}

/**
 * Modifier that applies the signature Achromatic Liquid Glass surface look:
 * - Pure neutral white translucency (0xFFFFFF) with low opacity (8–15%)
 * - Crisp refractive rim light (1–2dp border gradient with specular top-left edge)
 * - Tightly focused specular light catch along the top rim
 * - Clean static specular sheen (no distracting continuous shimmering)
 * - Soft ambient depth drop shadow
 */
fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(22.dp),
    fillColor: Color = GlassFill,
    borderColor: Color? = null,
    showSpecular: Boolean = true,
    elevation: Dp = 6.dp
): Modifier = this
    .clip(shape)
    .background(fillColor, shape)
    .drawWithContent {
        drawContent()

        if (showSpecular) {
            // Clean static specular sheen across the top corner (no continuous shimmer motion)
            val sweepBrush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                start = Offset.Zero,
                end = Offset(size.width * 0.85f, size.height * 0.45f)
            )
            drawRect(brush = sweepBrush, blendMode = BlendMode.Screen)

            // Physical Edge Refraction / Top Rim Light Catch
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.50f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                start = Offset(12f, 1.2f),
                end = Offset(size.width - 12f, 1.2f),
                strokeWidth = 1.5f
            )
        }
    }
    .border(
        width = 1.2.dp,
        brush = if (borderColor != null) {
            SolidColor(borderColor)
        } else {
            // Refractive glass rim: bright specular edge at top-left bending into ambient glass
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.75f),
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.45f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        },
        shape = shape
    )

/**
 * Liquid Glass Panel / Card Container with Static Specular Highlight
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    fillColor: Color = GlassFill,
    borderColor: Color? = null,
    showSpecular: Boolean = true,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .liquidGlassSurface(
                shape = shape,
                fillColor = fillColor,
                borderColor = borderColor,
                showSpecular = showSpecular,
                elevation = elevation
            )
            .then(clickableModifier)
    ) {
        content()
    }
}

/**
 * Frosted Glass Top App Bar
 */
@Composable
fun LiquidGlassTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FrostedBarBackground)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f))
                ),
                shape = RectangleShape
            )
            .padding(top = statusBarPadding)
            .height(56.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                }
                Box(modifier = Modifier.padding(start = if (navigationIcon != null) 4.dp else 12.dp)) {
                    ProvideTextStyle(
                        value = TextStyle(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    ) {
                        title()
                    }
                }
            }

            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}

/**
 * Frosted Glass Bottom Action Bar
 */
@Composable
fun LiquidGlassBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FrostedBarBackground)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                ),
                shape = RectangleShape
            )
            .padding(bottom = navBarPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

enum class GlassButtonVariant {
    Neutral,
    Primary,
    Success,
    Warning,
    Danger
}

/**
 * Achromatic Liquid Glass Button.
 * Translucent pure white body with high-contrast specular rim.
 */
@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GlassButtonVariant = GlassButtonVariant.Neutral,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(18.dp)
) {
    val (fillColor, borderColor, contentColor) = when (variant) {
        GlassButtonVariant.Neutral -> Triple(
            GlassFillElevated,
            Color.White.copy(alpha = 0.55f),
            TextPrimary
        )
        GlassButtonVariant.Primary -> Triple(
            GlassFillActive,
            Color.White.copy(alpha = 0.80f),
            TextPrimary
        )
        GlassButtonVariant.Success -> Triple(
            SuccessGreenBg,
            SuccessGreenBorder,
            SuccessGreen
        )
        GlassButtonVariant.Warning -> Triple(
            WarningAmberBg,
            WarningAmberBorder,
            WarningAmber
        )
        GlassButtonVariant.Danger -> Triple(
            ErrorRedBg,
            ErrorRedBorder,
            ErrorRed
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) fillColor else GlassFillSubtle)
            .drawWithContent {
                drawContent()
                if (enabled) {
                    // Crisp specular sweep on top half of button
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            )
                        ),
                        size = size.copy(height = size.height * 0.55f),
                        blendMode = BlendMode.Screen
                    )

                    // Top-edge light catch
                    drawLine(
                        color = Color.White.copy(alpha = 0.70f),
                        start = Offset(8f, 1f),
                        end = Offset(size.width - 8f, 1f),
                        strokeWidth = 1.5f
                    )
                }
            }
            .border(
                width = 1.2.dp,
                brush = if (enabled) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.75f),
                            borderColor
                        )
                    )
                } else {
                    SolidColor(GlassBorderSubtle)
                },
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) contentColor else TextMuted,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 6.dp)
                )
            }
            Text(
                text = text,
                color = if (enabled) contentColor else TextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Frosted Glass Badge / Chip.
 * Pure neutral white translucent styling by default; semantic color only for status.
 */
@Composable
fun LiquidGlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: GlassButtonVariant = GlassButtonVariant.Neutral,
    icon: ImageVector? = null,
    textColor: Color? = null,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    shape: Shape = RoundedCornerShape(14.dp)
) {
    val (defaultFill, defaultBorder, defaultContent) = when (variant) {
        GlassButtonVariant.Neutral -> Triple(GlassFillSubtle, GlassBorderSubtle, TextSecondary)
        GlassButtonVariant.Primary -> Triple(GlassFillElevated, GlassBorderBright, TextPrimary)
        GlassButtonVariant.Success -> Triple(SuccessGreenBg, SuccessGreenBorder, SuccessGreen)
        GlassButtonVariant.Warning -> Triple(WarningAmberBg, WarningAmberBorder, WarningAmber)
        GlassButtonVariant.Danger -> Triple(ErrorRedBg, ErrorRedBorder, ErrorRed)
    }

    val finalFill = backgroundColor ?: defaultFill
    val finalBorder = borderColor ?: defaultBorder
    val finalContent = textColor ?: defaultContent

    Box(
        modifier = modifier
            .clip(shape)
            .background(finalFill)
            .border(width = 1.dp, color = finalBorder, shape = shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = finalContent,
                    modifier = Modifier
                        .size(13.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                color = finalContent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}
