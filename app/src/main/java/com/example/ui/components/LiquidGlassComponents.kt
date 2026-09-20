package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

        content()
    }
}

/**
 * Modifier that applies the signature Achromatic Liquid Glass surface look:
 * - Pure neutral white translucency (0xFFFFFF) with low opacity (8–15%)
 * - Crisp refractive rim light (1–2dp border gradient with specular top-left edge)
 * - Clean static specular sheen across the upper edge
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

/**
 * Frosted Glass Modal Dialog Container.
 * Overlays the entire screen with a subtle 35% dark backdrop veil and presents
 * a visionOS-inspired Liquid Glass panel with 24dp rounded corners, refractive rim, and depth.
 */
@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.50f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // prevent dismissing when tapping inside
                    )
                    .liquidGlassSurface(
                        shape = RoundedCornerShape(26.dp),
                        fillColor = Color(0x2812162A),
                        elevation = 16.dp,
                        showSpecular = true
                    )
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = content
                )
            }
        }
    }
}

/**
 * Liquid Glass Text Input Field.
 * Clean, translucent container with high-contrast text and specular rim light.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String? = null,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(TextPrimary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassFillElevated)
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    Color.White.copy(alpha = 0.20f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp)
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }

                    if (trailingIcon != null) {
                        trailingIcon()
                    }
                }
            }
        )
    }
}

/**
 * Liquid Glass Search Bar with integrated search icon and clear button.
 */
@Composable
fun LiquidGlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    LiquidGlassTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Liquid Glass Filter / Option Chip.
 */
@Composable
fun LiquidGlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val fill = if (selected) GlassFillActive else GlassFillSubtle
    val border = if (selected) GlassBorderBright else GlassBorderSubtle
    val textCol = if (selected) TextPrimary else TextSecondary

    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(fill)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
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
                    tint = textCol,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 5.dp)
                )
            }
            Text(
                text = label,
                color = textCol,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Liquid Glass Toggle Switch with glowing glass thumb.
 */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 3.dp,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )
    val trackBg by animateColorAsState(
        targetValue = if (checked) Color(0x384338CA) else GlassFillSubtle,
        animationSpec = tween(durationMillis = 200),
        label = "trackBg"
    )
    val trackBorder by animateColorAsState(
        targetValue = if (checked) Color(0x80818CF8) else GlassBorderSubtle,
        animationSpec = tween(durationMillis = 200),
        label = "trackBorder"
    )

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackBg)
            .border(1.dp, trackBorder, RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) TextPrimary else Color(0xCCFFFFFF))
                .border(1.dp, Color.White, CircleShape)
        )
    }
}

/**
 * Segmented Liquid Glass Tab Row.
 */
@Composable
fun LiquidGlassTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassFillSubtle)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) GlassFillElevated else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) GlassBorderBright else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Floating Liquid Glass Action Button (FAB).
 */
@Composable
fun LiquidGlassFAB(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(GlassFillActive)
            .liquidGlassSurface(shape = CircleShape, fillColor = GlassFillActive, elevation = 12.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(26.dp)
        )
    }
}

/**
 * Liquid Glass Empty State Component.
 */
@Composable
fun LiquidGlassEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        fillColor = GlassFill,
        showSpecular = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GlassFillElevated)
                    .border(1.dp, GlassBorderBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            if (!actionButtonText.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                LiquidGlassButton(
                    text = actionButtonText,
                    onClick = onActionClick,
                    variant = GlassButtonVariant.Primary
                )
            }
        }
    }
}

/**
 * Liquid Glass Error Card.
 */
@Composable
fun LiquidGlassErrorCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    retryText: String? = null,
    onRetry: (() -> Unit)? = null
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        fillColor = ErrorRedBg,
        borderColor = ErrorRedBorder,
        showSpecular = true
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = message,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (!retryText.isNullOrBlank() && onRetry != null) {
                LiquidGlassButton(
                    text = retryText,
                    onClick = onRetry,
                    variant = GlassButtonVariant.Danger
                )
            }
        }
    }
}
