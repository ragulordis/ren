package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.ui.theme.CardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Starts a privacy-preserving Ren conversation. Public property records never
 * expose the owner's email address or phone number.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSellerDialog(
    property: Property,
    onStartChat: (initialMessage: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var message by remember {
        mutableStateOf("Hello ${property.ownerName}, I am interested in ${property.title}. Is it available for a site visit?")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("contact_seller_dialog")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(46.dp)) {
                        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                            Text(property.ownerName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column {
                        Text(property.ownerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${property.ownerType} • Contact through Ren Chat", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                IconButton(onDismiss) { Icon(Icons.Default.Close, "Close") }
            }

            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(property.title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${property.formattedPrice} • ${property.location}", fontSize = 12.sp, color = TextSecondary)
                }
            }

            Text("Your contact details remain private unless you choose to share them in the conversation.", fontSize = 12.sp, color = TextSecondary)
            OutlinedTextField(message, { message = it }, label = { Text("Message") }, minLines = 4, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = { if (message.isNotBlank()) onStartChat(message.trim()) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            ) { Icon(Icons.Default.Chat, null); Text("  Start chat", fontWeight = FontWeight.Bold) }
        }
    }
}
