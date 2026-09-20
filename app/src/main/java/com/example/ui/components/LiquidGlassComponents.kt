package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(12.dp),
    fillColor: Color = Color.Transparent,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp,
    showSpecular: Boolean = true,
    elevation: Dp = 0.dp
) = this

enum class GlassButtonVariant {
    Primary,
    Outlined,
    Neutral,
    Danger,
    Success
}

@Composable
fun LiquidGlassBackdrop(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        content = content
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    fillColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    borderWidth: Dp = 1.dp,
    showSpecular: Boolean = true,
    elevation: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = shape,
        color = fillColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun LiquidGlassBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GlassButtonVariant = GlassButtonVariant.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val mVariant = when (variant) {
        GlassButtonVariant.Primary -> MarklifyButtonVariant.Primary
        GlassButtonVariant.Outlined -> MarklifyButtonVariant.Outlined
        GlassButtonVariant.Neutral -> MarklifyButtonVariant.Neutral
        GlassButtonVariant.Danger -> MarklifyButtonVariant.Danger
        GlassButtonVariant.Success -> MarklifyButtonVariant.Primary
    }
    MarklifyButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        variant = mVariant,
        icon = icon,
        enabled = enabled
    )
}

@Composable
fun LiquidGlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    backgroundColor: Color = color,
    borderColor: Color = Color.Transparent
) {
    MarklifyBadge(
        text = text,
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = textColor
    )
}

@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    MarklifyDialog(
        onDismissRequest = onDismissRequest,
        content = content
    )
}

@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = if (label.isNotBlank()) { { Text(label) } } else null,
        placeholder = if (placeholder.isNotBlank()) { { Text(placeholder) } } else null,
        singleLine = singleLine,
        leadingIcon = leadingIcon?.let { { Icon(imageVector = it, contentDescription = null) } },
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun LiquidGlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    LiquidGlassTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder
    )
}

@Composable
fun LiquidGlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    MarklifyChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier
    )
}

@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    MarklifySwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
    )
}

@Composable
fun LiquidGlassFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
fun LiquidGlassEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    description: String = subtitle,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val textSubtitle = if (subtitle.isNotBlank()) subtitle else description
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = textSubtitle,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            MarklifyButton(text = actionText, onClick = onActionClick)
        }
    }
}

@Composable
fun LiquidGlassErrorCard(
    message: String = "",
    onRetry: () -> Unit = {},
    title: String = "Error",
    retryText: String = "Retry",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (message.isNotBlank()) message else title,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text(text = retryText, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
            }
        }
    }
}
