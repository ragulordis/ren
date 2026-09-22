package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Ren Luxury Button with tactile spring scale micro-interaction (0.97 on press).
 */
@Composable
fun RenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    height: Dp = 52.dp,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ren_button_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        shape = shape,
        modifier = modifier
            .height(height)
            .scale(scale)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Ren Architectural Outlined Button with subtle border.
 */
@Composable
fun RenOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    height: Dp = 48.dp,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ren_outlined_scale"
    )

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        border = BorderStroke(1.dp, borderColor),
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
        modifier = modifier
            .height(height)
            .scale(scale)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Animated Favorite Heart Button with spring scale feedback.
 */
@Composable
fun RenFavoriteButton(
    isSaved: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "favorite_button"
) {
    val scale by animateFloatAsState(
        targetValue = if (isSaved) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heart_scale"
    )

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(RenSurfaceWhite.copy(alpha = 0.92f))
            .border(1.dp, RenBorder.copy(alpha = 0.6f), CircleShape)
            .clickable(onClick = onToggle)
            .scale(scale)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isSaved) "Remove from saved" else "Save to favorites",
            tint = if (isSaved) RenError else RenPrimaryNavy,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Dominant numeric price component with architectural typography hierarchy.
 * e.g. "₹18,000" (bold 18sp) + " / month" (muted 12sp)
 */
@Composable
fun RenPriceText(
    formattedPrice: String,
    modifier: Modifier = Modifier,
    priceFontSize: TextUnit = 18.sp,
    periodFontSize: TextUnit = 12.sp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        val parts = formattedPrice.split("/")
        Text(
            text = parts[0].trim(),
            fontSize = priceFontSize,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = (-0.3).sp
        )
        if (parts.size > 1) {
            Text(
                text = " / ${parts[1].trim()}",
                fontSize = periodFontSize,
                fontWeight = FontWeight.Normal,
                color = RenTextSecondary,
                modifier = Modifier.padding(bottom = 1.5.dp, start = 2.dp)
            )
        }
    }
}

/**
 * Compact, elegant Location row with pin icon.
 */
@Composable
fun RenLocationRow(
    location: String,
    modifier: Modifier = Modifier,
    textColor: Color = RenTextSecondary,
    fontSize: TextUnit = 12.5.sp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = RenTextSecondary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = location,
            fontSize = fontSize,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Subtle Luxury Badge for listing status (FOR RENT, FOR SALE, FAST SALE, VERIFIED).
 */
@Composable
fun RenBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = RenPrimaryNavy.copy(alpha = 0.88f),
    contentColor: Color = RenSurfaceWhite,
    borderColor: Color = Color.Transparent,
    icon: ImageVector? = null
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(12.dp))
            }
            Text(
                text = text.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = contentColor
            )
        }
    }
}

/**
 * Architectural Filter Chip / Category Pill (Rent / Lease / Buy).
 */
@Composable
fun RenCategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) RenPrimaryNavy else RenSecondaryIvory,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) RenSurfaceWhite else RenPrimaryNavy,
        label = "pill_text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) RenPrimaryNavy else RenBorder,
        label = "pill_border"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

/**
 * Editorial Section Header with clean spacing and subtle counter/sub-caption.
 */
@Composable
fun RenSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = RenTextSecondary
                )
            }
        }
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            TextButton(onClick = onActionClick, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text = actionText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RenPrimaryNavy
                )
            }
        }
    }
}

/**
 * Architectural Empty State with generous whitespace and clear call-to-action.
 */
@Composable
fun RenEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    testTag: String = "empty_state"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(RenSecondaryIvory)
                    .border(1.dp, RenBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RenPrimaryNavy,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = message,
            fontSize = 13.5.sp,
            color = RenTextSecondary,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(6.dp))
            RenButton(
                text = actionText,
                onClick = onActionClick,
                height = 46.dp
            )
        }
    }
}
