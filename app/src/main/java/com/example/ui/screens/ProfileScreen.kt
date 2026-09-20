package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VerificationMethodType
import com.example.ui.components.VerifiedBuyerBottomSheet
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FastSaleAmber
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NormalGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyFlame
import com.example.ui.theme.UrgencyFlameContainer
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenContainer
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.QuickNestViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val userRole by profileViewModel.userRole.collectAsStateWithLifecycle()
    val currentUserProfile by profileViewModel.currentUserProfile.collectAsStateWithLifecycle()
    val showVerificationDialog by profileViewModel.showVerificationDialog.collectAsStateWithLifecycle()
    val selectedMethod by profileViewModel.selectedVerificationMethod.collectAsStateWithLifecycle()
    val activeOtpCode by profileViewModel.activeOtpCode.collectAsStateWithLifecycle()
    val otpCountdown by profileViewModel.otpCountdown.collectAsStateWithLifecycle()
    val isVerifying by profileViewModel.isVerifying.collectAsStateWithLifecycle()
    val verificationMessage by profileViewModel.verificationMessage.collectAsStateWithLifecycle()
    val verificationError by profileViewModel.verificationError.collectAsStateWithLifecycle()

    ProfileScreen(
        userRole = userRole,
        currentUserProfile = currentUserProfile,
        onSetUserRole = { profileViewModel.setUserRole(it) },
        onFeedback = { mainViewModel.showFeedback(it) },
        onLogout = { profileViewModel.logout() },
        onOpenVerification = { profileViewModel.openVerificationDialog(it) },
        modifier = modifier
    )

    if (showVerificationDialog) {
        VerifiedBuyerBottomSheet(
            userProfile = currentUserProfile,
            selectedMethod = selectedMethod,
            activeOtpCode = activeOtpCode,
            otpCountdown = otpCountdown,
            isVerifying = isVerifying,
            verificationMessage = verificationMessage,
            verificationError = verificationError,
            onSelectMethod = { profileViewModel.selectVerificationMethod(it) },
            onSendOtp = { profileViewModel.sendPhoneOtp(it) },
            onVerifyOtp = { phone, otp -> profileViewModel.verifyPhoneOtp(phone, otp) },
            onVerifyGovtId = { type, num, name -> profileViewModel.verifyGovernmentId(type, num, name) },
            onVerifyFinancials = { budget, bank, proof -> profileViewModel.verifyFinancials(budget, bank, proof) },
            onVerifySelfie = { uri -> profileViewModel.verifySelfie(uri) },
            onDismiss = { profileViewModel.closeVerificationDialog() }
        )
    }
}

@Composable
fun ProfileScreen(
    viewModel: QuickNestViewModel,
    modifier: Modifier = Modifier
) {
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    ProfileScreen(
        userRole = userRole,
        currentUserProfile = currentUserProfile,
        onSetUserRole = { viewModel.setUserRole(it) },
        onFeedback = { viewModel.triggerFeedback(it) },
        onLogout = { viewModel.logout() },
        onOpenVerification = { /* No-op in legacy ViewModel */ },
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    userRole: String,
    currentUserProfile: com.example.data.model.UserProfile?,
    onSetUserRole: (String) -> Unit,
    onFeedback: (String) -> Unit,
    onLogout: () -> Unit,
    onOpenVerification: (VerificationMethodType) -> Unit = {},
    modifier: Modifier = Modifier
) {

    val roles = listOf("Individual", "Property Owner", "Broker", "Builder", "Investor")

    val profile = currentUserProfile
    val displayName = profile?.displayName?.ifBlank { "User Account" } ?: "Guest User"
    val email = profile?.email ?: "Not signed in"
    val verificationLevel = profile?.verificationLevel ?: 0
    val uid = profile?.uid ?: ""

    val initials = remember(displayName) {
        displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifBlank { "U" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_content"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(MaterialTheme.colorScheme.primary, Color(0xFF4F378B))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (verificationLevel > 0) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = "Verified",
                                        tint = VerifiedGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (email.isNotBlank() && email != "Not signed in") email else if (uid.isNotBlank()) "UID: ${uid.take(12)}" else "Browsing as Guest",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (verificationLevel > 0) VerifiedGreenContainer else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (verificationLevel > 0) "Level $verificationLevel Verified" else if (profile != null) "Pending Verification" else "Guest Mode",
                                        color = if (verificationLevel > 0) VerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = userRole,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Persona / Role Switcher (Section 4 of Blueprint)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Switch Account Persona",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Customize dashboard metrics and matching recommendations",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        roles.forEach { role ->
                            val isSel = userRole == role
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onSetUserRole(role) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. SELLER & BROKER PERFORMANCE DASHBOARD (Section 24)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Icon(Icons.Default.Insights, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Analytics & Lead Pipeline",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Last 30 Days",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Stat Grid (2 x 3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox("2,450", "Property Views", modifier = Modifier.weight(1f))
                        StatBox("130", "Saved by Buyers", modifier = Modifier.weight(1f))
                        StatBox("45", "Direct Inquiries", modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox("12", "Site Visits Booked", modifier = Modifier.weight(1f))
                        StatBox("18", "QuickMatch Matches", modifier = Modifier.weight(1f), highlight = true)
                        StatBox("2", "Active Listings", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 4. TRUST & VERIFICATION LADDER (Section 16)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            Icon(Icons.Default.Shield, null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Verified Buyer System",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (profile?.isVerifiedBuyer == true) VerifiedGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { onOpenVerification(VerificationMethodType.PHONE_OTP) }
                                .testTag("verified_buyer_badge_pill")
                        ) {
                            Text(
                                text = profile?.verifiedBadgeText ?: "Unverified",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profile?.isVerifiedBuyer == true) VerifiedGreen else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress bar
                    val vLevel = profile?.verificationLevel ?: 0
                    LinearProgressIndicator(
                        progress = { (vLevel / 4f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = VerifiedGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // 4 Interactive Rows
                    val phoneVerified = profile?.isPhoneVerified == true || vLevel >= 1
                    VerificationRow(
                        title = "Level 1: Phone OTP Verification",
                        subtitle = if (phoneVerified) "Completed (${profile?.verifiedPhone?.ifBlank { profile?.phone }?.takeIf { it.isNotBlank() } ?: "Verified Mobile"})" else "Tap to verify 10-digit mobile number via SMS OTP",
                        isDone = phoneVerified,
                        onClick = { onOpenVerification(VerificationMethodType.PHONE_OTP) }
                    )

                    val govtIdVerified = profile?.isGovtIdVerified == true || vLevel >= 2
                    VerificationRow(
                        title = "Level 2: Government ID",
                        subtitle = if (govtIdVerified) "${profile?.govtIdType?.ifBlank { "National ID" } ?: "National ID"} (${profile?.govtIdNumberMasked?.ifBlank { "••••" } ?: "••••"}) Verified" else "Tap to verify Aadhaar / Driving License / Passport",
                        isDone = govtIdVerified,
                        onClick = { onOpenVerification(VerificationMethodType.GOVERNMENT_ID) }
                    )

                    val financialVerified = profile?.isFinancialVerified == true || vLevel >= 3
                    VerificationRow(
                        title = "Level 3: Financial Pre-Approval",
                        subtitle = if (financialVerified) "Verified: ${profile?.buyerBudgetRange ?: "Budget"} (${profile?.preApprovalBank ?: "Bank"})" else "Tap to pre-approve purchasing capacity",
                        isDone = financialVerified,
                        onClick = { onOpenVerification(VerificationMethodType.FINANCIAL_PRE_APPROVAL) }
                    )

                    val selfieVerified = profile?.isSelfieVerified == true || vLevel >= 4
                    VerificationRow(
                        title = "Level 4: ⭐ Elite Biometric Match",
                        subtitle = if (selfieVerified) "Biometric Liveness Match Confirmed" else "Tap for facial liveness match to unlock Elite Buyer status",
                        isDone = selfieVerified,
                        onClick = { onOpenVerification(VerificationMethodType.SELFIE_LIVENESS) }
                    )

                    // Button CTA
                    if (vLevel < 4) {
                        Button(
                            onClick = {
                                val nextMethod = when {
                                    !phoneVerified -> VerificationMethodType.PHONE_OTP
                                    !govtIdVerified -> VerificationMethodType.GOVERNMENT_ID
                                    !financialVerified -> VerificationMethodType.FINANCIAL_PRE_APPROVAL
                                    else -> VerificationMethodType.SELFIE_LIVENESS
                                }
                                onOpenVerification(nextMethod)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_verification_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NavyPrimary
                            )
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (vLevel == 0) "Get Verified Buyer Badge" else "Upgrade Trust Badge to Level ${vLevel + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. MONETIZATION & BOOST PLANS (Section 25)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Speed, null, tint = UrgencyFlame, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Boost Property Visibility",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    BoostPlanCard(
                        "🔥 Urgent Boost",
                        "Top 1 on Urgent Deals + 50 Instant Buyer SMS",
                        "₹299 / 7 Days",
                        UrgencyFlame,
                        onClick = { onFeedback("Urgent Boost selected! Top placement activated for your active listings.") }
                    )
                    BoostPlanCard(
                        "⭐ Featured Listing",
                        "Highlighted with gold badge on Home & Explore",
                        "₹99 / 3 Days",
                        FastSaleAmber,
                        onClick = { onFeedback("Featured Listing activated! Gold badge will appear on your properties.") }
                    )
                    BoostPlanCard(
                        "💼 Broker Pro Subscription",
                        "Unlimited listings + CRM Lead Manager + Verified Broker Tag",
                        "₹1,499 / Month",
                        MaterialTheme.colorScheme.primary,
                        onClick = { onFeedback("Broker Pro subscription requested! Relationship manager assigned.") }
                    )
                }
            }
        }

        // 6. ANTI-FRAUD & SCAM PROTECTION STATUS (Section 29)
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Security, null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Ren Scam & Duplicate Shield Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "• AI Duplicate photo detector enabled\n• Watermark protection on all uploaded media\n• Direct owner contact mask protection",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sign Out / Switch Account Button
            androidx.compose.material3.OutlinedButton(
                onClick = { onLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = androidx.compose.ui.graphics.Color(0xFFDC2626)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFECACA))
            ) {
                Text(
                    text = "Sign Out from Ren",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (highlight) UrgencyFlameContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) UrgencyFlame else TextPrimary
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun VerificationRow(
    title: String,
    subtitle: String,
    isDone: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDone) VerifiedGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = null,
            tint = if (isDone) VerifiedGreen else TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 10.sp, color = TextSecondary)
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDone) VerifiedGreenContainer else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Text(
                text = if (isDone) "Verified" else "Verify",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) VerifiedGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun BoostPlanCard(
    title: String,
    desc: String,
    price: String,
    accentColor: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(desc, fontSize = 11.sp, color = TextSecondary)
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.12f)
        ) {
            Text(
                text = price,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
