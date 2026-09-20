package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.VerificationMethodType
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldVerify
import com.example.ui.theme.FastSaleAmber
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateMutedText
import com.example.ui.theme.SlateSecondaryText
import com.example.ui.theme.UrgencyFlame
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifiedBuyerBottomSheet(
    userProfile: UserProfile?,
    selectedMethod: VerificationMethodType,
    activeOtpCode: String?,
    otpCountdown: Int,
    isVerifying: Boolean,
    verificationMessage: String?,
    verificationError: String?,
    onSelectMethod: (VerificationMethodType) -> Unit,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (phone: String, otp: String) -> Unit,
    onVerifyGovtId: (idType: String, idNumber: String, legalName: String) -> Unit,
    onVerifyFinancials: (budget: String, institution: String, proofType: String) -> Unit,
    onVerifySelfie: (photoUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .testTag("verified_buyer_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NavyPrimary, Color(0xFF2563EB))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Verified Buyer System",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Complete 4 verification trust tiers",
                            fontSize = 12.sp,
                            color = SlateSecondaryText
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("verified_buyer_close_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateSecondaryText)
                }
            }

            // Trust Progress Card
            val level = userProfile?.verificationLevel ?: 0
            val progress = (level / 4f).coerceIn(0f, 1f)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (level > 0) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (level > 0) EmeraldVerify else NavyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = userProfile?.verifiedBadgeText ?: "Unverified Buyer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (level > 0) VerifiedGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Tier $level / 4",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (level > 0) VerifiedGreen else SlateSecondaryText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldVerify,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Level 1: Phone",
                            fontSize = 10.sp,
                            fontWeight = if (level >= 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (level >= 1) EmeraldVerify else SlateMutedText
                        )
                        Text(
                            text = "Level 2: ID",
                            fontSize = 10.sp,
                            fontWeight = if (level >= 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (level >= 2) EmeraldVerify else SlateMutedText
                        )
                        Text(
                            text = "Level 3: Funds",
                            fontSize = 10.sp,
                            fontWeight = if (level >= 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (level >= 3) EmeraldVerify else SlateMutedText
                        )
                        Text(
                            text = "Level 4: Elite ⭐",
                            fontSize = 10.sp,
                            fontWeight = if (level >= 4) FontWeight.Bold else FontWeight.Normal,
                            color = if (level >= 4) EmeraldVerify else SlateMutedText
                        )
                    }
                }
            }

            // Benefits Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BuyerBenefitPill(icon = Icons.Default.Speed, text = "3x Faster Seller Replies")
                BuyerBenefitPill(icon = Icons.Default.VerifiedUser, text = "VIP Visit Scheduling")
                BuyerBenefitPill(icon = Icons.Default.Star, text = "Private Deals Access")
            }

            // Status message / Error banner
            AnimatedVisibility(visible = verificationMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldVerify.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldVerify.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = EmeraldVerify, modifier = Modifier.size(18.dp))
                        Text(
                            text = verificationMessage.orEmpty(),
                            fontSize = 12.sp,
                            color = EmeraldVerify,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            AnimatedVisibility(visible = verificationError != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = UrgencyFlame.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UrgencyFlame.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = UrgencyFlame, modifier = Modifier.size(18.dp))
                        Text(
                            text = verificationError.orEmpty(),
                            fontSize = 12.sp,
                            color = UrgencyFlame,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Method Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VerificationMethodTab(
                    type = VerificationMethodType.PHONE_OTP,
                    isSelected = selectedMethod == VerificationMethodType.PHONE_OTP,
                    isCompleted = userProfile?.isPhoneVerified == true || (userProfile?.verificationLevel ?: 0) >= 1,
                    onClick = { onSelectMethod(VerificationMethodType.PHONE_OTP) }
                )
                VerificationMethodTab(
                    type = VerificationMethodType.GOVERNMENT_ID,
                    isSelected = selectedMethod == VerificationMethodType.GOVERNMENT_ID,
                    isCompleted = userProfile?.isGovtIdVerified == true || (userProfile?.verificationLevel ?: 0) >= 2,
                    onClick = { onSelectMethod(VerificationMethodType.GOVERNMENT_ID) }
                )
                VerificationMethodTab(
                    type = VerificationMethodType.FINANCIAL_PRE_APPROVAL,
                    isSelected = selectedMethod == VerificationMethodType.FINANCIAL_PRE_APPROVAL,
                    isCompleted = userProfile?.isFinancialVerified == true || (userProfile?.verificationLevel ?: 0) >= 3,
                    onClick = { onSelectMethod(VerificationMethodType.FINANCIAL_PRE_APPROVAL) }
                )
                VerificationMethodTab(
                    type = VerificationMethodType.SELFIE_LIVENESS,
                    isSelected = selectedMethod == VerificationMethodType.SELFIE_LIVENESS,
                    isCompleted = userProfile?.isSelfieVerified == true || (userProfile?.verificationLevel ?: 0) >= 4,
                    onClick = { onSelectMethod(VerificationMethodType.SELFIE_LIVENESS) }
                )
            }

            // Active Tab Content
            when (selectedMethod) {
                VerificationMethodType.PHONE_OTP -> {
                    PhoneOtpVerificationSection(
                        userProfile = userProfile,
                        activeOtpCode = activeOtpCode,
                        otpCountdown = otpCountdown,
                        isVerifying = isVerifying,
                        onSendOtp = onSendOtp,
                        onVerifyOtp = onVerifyOtp
                    )
                }
                VerificationMethodType.GOVERNMENT_ID -> {
                    GovernmentIdVerificationSection(
                        userProfile = userProfile,
                        isVerifying = isVerifying,
                        onVerifyGovtId = onVerifyGovtId
                    )
                }
                VerificationMethodType.FINANCIAL_PRE_APPROVAL -> {
                    FinancialVerificationSection(
                        userProfile = userProfile,
                        isVerifying = isVerifying,
                        onVerifyFinancials = onVerifyFinancials
                    )
                }
                VerificationMethodType.SELFIE_LIVENESS -> {
                    SelfieLivenessVerificationSection(
                        userProfile = userProfile,
                        isVerifying = isVerifying,
                        onVerifySelfie = onVerifySelfie
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BuyerBenefitPill(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = EmeraldVerify, modifier = Modifier.size(14.dp))
            Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun VerificationMethodTab(
    type: VerificationMethodType,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> NavyPrimary
            isCompleted -> VerifiedGreen.copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        animationSpec = tween(200)
    )
    val contentColor = when {
        isSelected -> Color.White
        isCompleted -> VerifiedGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NavyPrimary else if (isCompleted) EmeraldVerify.copy(alpha = 0.5f) else CardBorder
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("verification_tab_${type.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = when (type) {
                    VerificationMethodType.PHONE_OTP -> Icons.Default.Phone
                    VerificationMethodType.GOVERNMENT_ID -> Icons.Default.Badge
                    VerificationMethodType.FINANCIAL_PRE_APPROVAL -> Icons.Default.AccountBalance
                    VerificationMethodType.SELFIE_LIVENESS -> Icons.Default.Face
                },
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = type.title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )

            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = if (isSelected) Color.White else VerifiedGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * METHOD 1: PHONE NUMBER VERIFICATION VIA OTP
 */
@Composable
private fun PhoneOtpVerificationSection(
    userProfile: UserProfile?,
    activeOtpCode: String?,
    otpCountdown: Int,
    isVerifying: Boolean,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (phone: String, otp: String) -> Unit
) {
    var phoneNumber by remember(userProfile?.phone) {
        mutableStateOf(userProfile?.verifiedPhone?.ifBlank { userProfile.phone } ?: "")
    }
    var otpInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isAlreadyVerified = userProfile?.isPhoneVerified == true

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Phone, null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "Phone Number OTP Verification",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Level 1: Unlocks direct seller connect & verified buyer badge",
                        fontSize = 11.sp,
                        color = SlateSecondaryText
                    )
                }
            }

            if (isAlreadyVerified) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VerifiedGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = VerifiedGreen, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verified Mobile Number",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedGreen
                            )
                            Text(
                                text = userProfile?.verifiedPhone?.ifBlank { phoneNumber }.orEmpty(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedGreen
                        )
                    }
                }
            }

            // Phone Input Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Mobile Number (with +91 country code)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateSecondaryText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(bottom = 0.dp)
                    ) {
                        Text(
                            text = "🇮🇳 +91",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                        )
                    }

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            if (input.length <= 15) phoneNumber = input
                        },
                        placeholder = { Text("98401 23456", color = SlateMutedText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("buyer_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }

                // Send OTP Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSendOtp(phoneNumber)
                    },
                    enabled = phoneNumber.filter { it.isDigit() }.length >= 10 && !isVerifying && otpCountdown == 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_otp_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    if (isVerifying && activeOtpCode == null) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (otpCountdown > 0) "Resend OTP in ${otpCountdown}s" else if (activeOtpCode != null) "Resend OTP Code" else "Send 6-Digit OTP",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // OTP Box Input & Simulation Alert
            AnimatedVisibility(visible = activeOtpCode != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Demo Simulation Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FastSaleAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FastSaleAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "📲", fontSize = 16.sp)
                                Text(
                                    text = "SMS Sent to $phoneNumber",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "For instant verification, use verification code: ${activeOtpCode.orEmpty()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Enter 6-Digit Verification Code",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateSecondaryText
                    )

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }.take(6)
                            otpInput = clean
                            if (clean.length == 6) {
                                focusManager.clearFocus()
                                onVerifyOtp(phoneNumber, clean)
                            }
                        },
                        placeholder = { Text("e.g. ${activeOtpCode ?: "123456"}", color = SlateMutedText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (otpInput.length == 6) onVerifyOtp(phoneNumber, otpInput)
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_digit_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldVerify,
                            unfocusedBorderColor = CardBorder
                        )
                    )

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onVerifyOtp(phoneNumber, otpInput)
                        },
                        enabled = otpInput.length == 6 && !isVerifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldVerify)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Verify OTP & Unlock Level 1 Badge",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * METHOD 2: GOVERNMENT ID VERIFICATION
 */
@Composable
private fun GovernmentIdVerificationSection(
    userProfile: UserProfile?,
    isVerifying: Boolean,
    onVerifyGovtId: (idType: String, idNumber: String, legalName: String) -> Unit
) {
    val idOptions = listOf("Aadhaar Card", "Driving License", "Passport", "Voter ID")
    var selectedIdType by remember { mutableStateOf(userProfile?.govtIdType?.ifBlank { "Aadhaar Card" } ?: "Aadhaar Card") }
    var legalName by remember(userProfile?.displayName) { mutableStateOf(userProfile?.displayName ?: "") }
    var idNumber by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isAlreadyVerified = userProfile?.isGovtIdVerified == true

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Badge, null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "Government ID Verification",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Level 2: Citizen Trust Shield for genuine buyers",
                        fontSize = 11.sp,
                        color = SlateSecondaryText
                    )
                }
            }

            if (isAlreadyVerified) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VerifiedGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = VerifiedGreen, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Government ID Verified",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedGreen
                            )
                            Text(
                                text = "${userProfile?.govtIdType ?: "National ID"} (${userProfile?.govtIdNumberMasked ?: "••••"})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Level 2 Shield",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedGreen
                        )
                    }
                }
            }

            // ID Type Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Select Document Type",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateSecondaryText
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    idOptions.forEach { type ->
                        FilterChip(
                            selected = selectedIdType == type,
                            onClick = { selectedIdType = type },
                            label = { Text(type, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("id_type_chip_${type.replace(" ", "_").lowercase()}")
                        )
                    }
                }
            }

            // Legal Name
            OutlinedTextField(
                value = legalName,
                onValueChange = { legalName = it },
                label = { Text("Legal Full Name (as per ID)") },
                placeholder = { Text("Rajesh Kumar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("govt_id_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // ID Number
            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("$selectedIdType Number") },
                placeholder = {
                    Text(
                        when (selectedIdType) {
                            "Aadhaar Card" -> "5482 1928 3491 (12 digits)"
                            "Driving License" -> "TN-01-2020-0012345"
                            "Passport" -> "Z3920184"
                            else -> "ABC1234567"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("govt_id_number_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Document Photo Placeholder Preview
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Badge, null, tint = SlateSecondaryText, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-OCR Document Verification",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Encrypted in accordance with UIDAI / Privacy guidelines",
                            fontSize = 11.sp,
                            color = SlateSecondaryText
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onVerifyGovtId(selectedIdType, idNumber, legalName)
                },
                enabled = idNumber.trim().length >= 4 && legalName.isNotBlank() && !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verify_govt_id_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Verify Government ID (Level 2)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * METHOD 3: FINANCIAL PRE-APPROVAL & PURCHASING POWER VERIFICATION
 */
@Composable
private fun FinancialVerificationSection(
    userProfile: UserProfile?,
    isVerifying: Boolean,
    onVerifyFinancials: (budget: String, institution: String, proofType: String) -> Unit
) {
    val budgetTiers = listOf(
        "Under ₹30 Lakhs",
        "₹30L - ₹75 Lakhs",
        "₹75L - ₹1.5 Cr",
        "₹1.5 Cr - ₹3 Cr",
        "₹3 Cr+ (Luxury)"
    )
    val banks = listOf("State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Self-Funded")
    val proofs = listOf("Bank Pre-Approval Letter", "Salary Statements / ITR", "Liquid Mutual Funds / Fixed Deposit")

    var selectedBudget by remember { mutableStateOf(userProfile?.buyerBudgetRange?.ifBlank { budgetTiers[1] } ?: budgetTiers[1]) }
    var selectedBank by remember { mutableStateOf(userProfile?.preApprovalBank?.ifBlank { banks[1] } ?: banks[1]) }
    var selectedProof by remember { mutableStateOf(proofs[0]) }
    val isAlreadyVerified = userProfile?.isFinancialVerified == true

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AccountBalance, null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "Financial Pre-Approval & Budget",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Level 3: Qualifies buyer purchasing capacity",
                        fontSize = 11.sp,
                        color = SlateSecondaryText
                    )
                }
            }

            if (isAlreadyVerified) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VerifiedGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = VerifiedGreen, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pre-Approved Purchasing Power",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedGreen
                            )
                            Text(
                                text = "${userProfile?.buyerBudgetRange ?: "₹50L - ₹1Cr"} via ${userProfile?.preApprovalBank ?: "Bank"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Budget Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Target Budget Band",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateSecondaryText
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    budgetTiers.forEach { tier ->
                        FilterChip(
                            selected = selectedBudget == tier,
                            onClick = { selectedBudget = tier },
                            label = { Text(tier, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("budget_chip_${tier.replace(" ", "_").lowercase()}")
                        )
                    }
                }
            }

            // Bank Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Financial Institution / Source",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateSecondaryText
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    banks.forEach { bank ->
                        FilterChip(
                            selected = selectedBank == bank,
                            onClick = { selectedBank = bank },
                            label = { Text(bank, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Proof Type
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Verification Method",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateSecondaryText
                )

                proofs.forEach { proof ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedProof == proof) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .clickable { selectedProof = proof }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedProof == proof) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (selectedProof == proof) NavyPrimary else SlateMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = proof,
                            fontSize = 12.sp,
                            fontWeight = if (selectedProof == proof) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    onVerifyFinancials(selectedBudget, selectedBank, selectedProof)
                },
                enabled = !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verify_financials_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Confirm Purchasing Capacity (Level 3)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * METHOD 4: BIOMETRIC SELFIE LIVENESS VERIFICATION
 */
@Composable
private fun SelfieLivenessVerificationSection(
    userProfile: UserProfile?,
    isVerifying: Boolean,
    onVerifySelfie: (photoUri: String?) -> Unit
) {
    val isAlreadyVerified = userProfile?.isSelfieVerified == true

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Face, null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "Biometric Liveness Match",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Level 4: ⭐ Elite Verified Buyer Status",
                        fontSize = 11.sp,
                        color = SlateSecondaryText
                    )
                }
            }

            if (isAlreadyVerified) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VerifiedGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = VerifiedGreen, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⭐ Elite Verified Buyer Active",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedGreen
                            )
                            Text(
                                text = "Biometric liveness confirmed & synced to cloud",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Facial Liveness Camera Frame Guide
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(3.dp, if (isAlreadyVerified) EmeraldVerify else NavyPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Facial Frame",
                    tint = if (isAlreadyVerified) EmeraldVerify else SlateSecondaryText,
                    modifier = Modifier.size(80.dp)
                )
            }

            Text(
                text = "Hold device at eye level. Anti-spoofing algorithm checks live motion and matches your legal ID photo.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = SlateSecondaryText
            )

            Button(
                onClick = {
                    onVerifySelfie(null)
                },
                enabled = !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verify_selfie_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAlreadyVerified) EmeraldVerify else NavyPrimary
                )
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isAlreadyVerified) "Re-verify Face Match" else "Snap & Confirm Liveness (Level 4)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
