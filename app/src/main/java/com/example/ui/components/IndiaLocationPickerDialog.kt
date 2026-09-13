package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BlueCorporate
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CharcoalNavyText
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateSecondaryText
import com.example.ui.theme.VerifiedGreen

val ALL_INDIAN_MAJOR_HUBS = listOf(
    "All Locations",
    "Chennai",
    "Bengaluru",
    "Mumbai",
    "Delhi NCR",
    "Hyderabad",
    "Pune",
    "Kochi",
    "Goa",
    "Pondicherry",
    "Kottakuppam",
    "Coimbatore",
    "Ahmedabad",
    "Jaipur",
    "Kolkata",
    "Chandigarh",
    "Lucknow",
    "Indore",
    "Surat",
    "Visakhapatnam",
    "Madurai",
    "Mysuru",
    "Mangaluru",
    "Nagpur",
    "Dehradun"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IndiaLocationPickerDialog(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var searchInput by remember { mutableStateOf("") }
    var locationError by remember { mutableStateOf<String?>(null) }

    val foreignList = listOf(
        "usa", "united states", "us", "uk", "united kingdom", "london", "dubai", "uae",
        "singapore", "australia", "canada", "new york", "germany", "france", "japan",
        "tokyo", "paris", "california", "texas", "florida", "china", "malaysia", "thailand"
    )

    fun applyCustomLocation(loc: String) {
        val clean = loc.trim()
        if (clean.isBlank() || clean.equals("All", ignoreCase = true) || clean.equals("All Locations", ignoreCase = true)) {
            onLocationSelected("All Locations")
            onDismissRequest()
            return
        }
        val lower = clean.lowercase()
        if (foreignList.any { lower == it || lower.contains(" $it") || lower.contains("$it ") }) {
            locationError = "Ren is currently available exclusively in India 🇮🇳. Foreign country listings are not supported."
            return
        }
        onLocationSelected(clean)
        onDismissRequest()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🇮🇳", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Select Location (India 🇮🇳)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Available across all cities, states & towns in India",
                            fontSize = 11.sp,
                            color = SlateSecondaryText
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateSecondaryText)
                }
            }

            // Custom Location Search Input
            OutlinedTextField(
                value = searchInput,
                onValueChange = {
                    searchInput = it
                    locationError = null
                },
                placeholder = { Text("Search any Indian city, town or locality...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BlueCorporate) },
                trailingIcon = {
                    if (searchInput.isNotBlank()) {
                        IconButton(onClick = { searchInput = "" }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { applyCustomLocation(searchInput) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("location_search_input_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            if (searchInput.isNotBlank()) {
                Button(
                    onClick = { applyCustomLocation(searchInput) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Set Location to \"${searchInput.trim()}\"")
                }
            }

            if (locationError != null) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Text(
                        text = locationError ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // Popular Indian Hubs Chips
            Text(
                text = "Popular Indian Real Estate Hubs",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )

            val filteredHubs = remember(searchInput) {
                if (searchInput.isBlank()) ALL_INDIAN_MAJOR_HUBS
                else ALL_INDIAN_MAJOR_HUBS.filter { it.contains(searchInput, ignoreCase = true) }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredHubs.forEach { city ->
                    val isSelected = selectedLocation.equals(city, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { applyCustomLocation(city) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                }
                                Text(
                                    text = city,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF8FAFC),
                            labelColor = CharcoalNavyText
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
