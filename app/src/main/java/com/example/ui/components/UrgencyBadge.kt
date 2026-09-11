package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SellingSpeed
import com.example.ui.theme.FastSaleAmber
import com.example.ui.theme.FastSaleAmberContainer
import com.example.ui.theme.NormalGreen
import com.example.ui.theme.NormalGreenContainer
import com.example.ui.theme.PrivateSaleDark
import com.example.ui.theme.UrgencyFlame
import com.example.ui.theme.UrgencyFlameContainer
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenContainer

@Composable
fun SellingSpeedBadge(
    speed: SellingSpeed,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (speed) {
        SellingSpeed.URGENT -> Triple(UrgencyFlame, Color.White, "URGENT SALE")
        SellingSpeed.FAST -> Triple(FastSaleAmber, Color.White, "FAST DEAL")
        SellingSpeed.NORMAL -> Triple(VerifiedGreenContainer, VerifiedGreen, "VERIFIED")
        SellingSpeed.PRIVATE -> Triple(PrivateSaleDark, Color.White, "PRIVATE SALE")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun UrgencyScoreRow(
    score: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(score.coerceIn(1, 5)) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Urgency Flame",
                tint = UrgencyFlame,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = when (score) {
                5 -> "Very High"
                4 -> "High"
                3 -> "Medium"
                else -> "Moderate"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = UrgencyFlame,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun VerificationBadge(
    level: Int,
    modifier: Modifier = Modifier
) {
    val (label, color, bgColor) = when {
        level >= 3 -> Triple("Trusted Owner", VerifiedGreen, VerifiedGreenContainer)
        level >= 2 -> Triple("ID Verified", Color(0xFF6750A4), Color(0xFFEADDFF))
        else -> Triple("Phone Verified", Color(0xFF49454F), Color(0xFFF3EDF7))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OpportunityPill(
    savingsText: String,
    modifier: Modifier = Modifier
) {
    if (savingsText.isBlank()) return
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(UrgencyFlameContainer)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = UrgencyFlame,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "Save $savingsText",
            color = UrgencyFlame,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
