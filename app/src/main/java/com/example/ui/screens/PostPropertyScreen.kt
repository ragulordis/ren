package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.PostPropertyViewModel
import com.example.viewmodel.QuickNestViewModel

@Composable
fun PostPropertyScreen(postViewModel: PostPropertyViewModel, modifier: Modifier = Modifier) {
    RentalListingForm({ title, description, category, type, rent, location, beds, baths, area, furnishing, deposit, maintenance, included, available, landmark, tenants, amenities, photos ->
        postViewModel.postNewProperty(
            title = title, description = description, category = category, propertyType = type,
            price = rent, marketEstimate = rent, location = location, bedrooms = beds, bathrooms = baths,
            areaSqFt = area, speed = SellingSpeed.NORMAL, features = amenities, furnishing = furnishing,
            securityDeposit = deposit, maintenanceAmount = maintenance, isMaintenanceIncluded = included,
            availableFrom = available, nearbyLandmark = landmark, tenantPreferences = tenants, imageUris = photos
        )
    }, modifier)
}

/** Compatibility entry point used by legacy tests. */
@Composable
fun PostPropertyScreen(viewModel: QuickNestViewModel, modifier: Modifier = Modifier) {
    RentalListingForm({ title, description, category, type, rent, location, beds, baths, area, _, _, _, _, _, _, _, amenities, _ ->
        viewModel.postNewProperty(title, description, category, type, rent, rent, location, beds, baths, area, SellingSpeed.NORMAL, amenities, false)
    }, modifier)
}

@Composable
private fun RentalListingForm(
    onPublish: (String, String, PropertyCategory, String, Long, String, Int, Int, Int, String, Long, Long, Boolean, String, String, List<String>, List<String>, List<Uri>) -> Unit,
    modifier: Modifier
) {
    var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PropertyCategory.RENT) }; var type by remember { mutableStateOf("Apartment") }
    var rent by remember { mutableStateOf("") }; var deposit by remember { mutableStateOf("") }; var maintenance by remember { mutableStateOf("") }
    var maintenanceIncluded by remember { mutableStateOf(false) }; var city by remember { mutableStateOf("Pondicherry") }; var locality by remember { mutableStateOf("Kottakuppam") }
    var landmark by remember { mutableStateOf("") }; var available by remember { mutableStateOf("Available now") }; var beds by remember { mutableStateOf("2") }
    var baths by remember { mutableStateOf("2") }; var area by remember { mutableStateOf("") }; var furnishing by remember { mutableStateOf("Semi-furnished") }
    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }; var amenities by remember { mutableStateOf(setOf("Water", "EB")) }
    var tenants by remember { mutableStateOf(setOf("Family", "Working professionals")) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { picked -> photos = (photos + picked).distinct().take(10) }
    val location = listOf(city.trim(), locality.trim()).filter(String::isNotEmpty).joinToString(", ")
    val isRental = category == PropertyCategory.RENT

    LazyColumn(modifier.fillMaxSize(), PaddingValues(16.dp, 16.dp, 16.dp, 112.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Column { Text(if (isRental) "Post a rental home" else "Post a property", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Text("New listings are reviewed before they go live.", fontSize = 13.sp, color = TextSecondary) } }
        item { FormCard("Listing type") { CategorySelector(category) { category = it; type = if (it == PropertyCategory.RENT) "Apartment" else "Independent House" } } }
        item { FormCard("Property") {
            Field(title, { title = it }, "Listing title", "2 BHK apartment near the beach"); Field(description, { description = it }, "Description", "Describe the home, water, access and rules", 3)
            Choice("Home type", listOf("Apartment", "Independent House", "Villa", "PG", "Room"), type) { type = it }
            Choice("Furnishing", listOf("Furnished", "Semi-furnished", "Unfurnished"), furnishing) { furnishing = it }
            PairFields("BHK", beds, { beds = it }, "Bathrooms", baths, { baths = it }); Field(area, { area = it }, "Area in sq.ft.", keyboard = KeyboardType.Number)
        } }
        item { FormCard(if (isRental) "Rent and deposit" else "Price") {
            Field(rent, { rent = it }, if (isRental) "Monthly rent (₹)" else "Price (₹)", keyboard = KeyboardType.Number)
            if (isRental) { PairFields("Security deposit (₹)", deposit, { deposit = it }, "Maintenance / month (₹)", maintenance, { maintenance = it }); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(maintenanceIncluded, { maintenanceIncluded = it }); Text("Maintenance is included in rent", fontSize = 13.sp, color = TextPrimary) } }
        } }
        item { FormCard("Location and availability") {
            PairFields("City", city, { city = it }, "Area / locality", locality, { locality = it }, false); Field(landmark, { landmark = it }, "Nearby landmark")
            Choice("Availability", listOf("Available now", "Available from date"), available) { available = it }
        } }
        if (isRental) item { FormCard("Tenant preferences") { MultiChoice(listOf("Family", "Bachelor", "Students", "Working professionals", "Pets allowed"), tenants) { tenants = it } } }
        item { FormCard("Amenities") { MultiChoice(listOf("Parking", "Water", "EB", "Lift", "Balcony", "AC", "Wi-Fi", "Gated security"), amenities) { amenities = it } } }
        item { FormCard("Photos") {
            Text("Add up to 10 real photos. The first photo is the cover image.", fontSize = 13.sp, color = TextSecondary)
            Button({ picker.launch("image/*") }, Modifier.fillMaxWidth()) { Icon(Icons.Default.AddPhotoAlternate, null); Text("  Add property photos") }
            if (photos.isNotEmpty()) LazyColumn(Modifier.height(176.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(photos, key = { it.toString() }) { uri -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { AsyncImage(uri, "Selected property photo", Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop); Text(if (uri == photos.first()) "Cover photo" else "Gallery photo", Modifier.weight(1f).padding(horizontal = 12.dp), color = TextPrimary); Icon(Icons.Default.Delete, "Remove photo", Modifier.clickable { photos = photos - uri }) } } }
        } }
        item { Button(
            onClick = { onPublish(title, description, category, type, rent.toLongOrNull() ?: 0, location, beds.toIntOrNull() ?: 0, baths.toIntOrNull() ?: 0, area.toIntOrNull() ?: 0, furnishing, deposit.toLongOrNull() ?: 0, maintenance.toLongOrNull() ?: 0, maintenanceIncluded, available, landmark, tenants.toList(), amenities.toList(), photos) },
            enabled = title.isNotBlank() && (rent.toLongOrNull() ?: 0) > 0 && location.isNotBlank(), modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)
        ) { Text(if (isRental) "Publish rental for review" else "Publish listing for review", fontWeight = FontWeight.Bold) } }
    }
}

@Composable private fun FormCard(title: String, content: @Composable ColumnScope.() -> Unit) = Card(shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary); content() } }
@Composable private fun Field(value: String, onChange: (String) -> Unit, label: String, placeholder: String = "", lines: Int = 1, keyboard: KeyboardType = KeyboardType.Text) = OutlinedTextField(value, onChange, { Text(label) }, placeholder = { Text(placeholder) }, minLines = lines, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboard), modifier = Modifier.fillMaxWidth())
@Composable private fun PairFields(l1: String, v1: String, c1: (String) -> Unit, l2: String, v2: String, c2: (String) -> Unit, numeric: Boolean = true) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(v1, c1, { Text(l1) }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text), modifier = Modifier.weight(1f)); OutlinedTextField(v2, c2, { Text(l2) }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text), modifier = Modifier.weight(1f)) }
@Composable private fun CategorySelector(selected: PropertyCategory, onSelect: (PropertyCategory) -> Unit) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(PropertyCategory.RENT, PropertyCategory.BUY, PropertyCategory.LEASE).forEach { category -> val active = category == selected; Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).clickable { onSelect(category) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) { Text(category.label, color = if (active) Color.White else TextPrimary, fontWeight = FontWeight.Bold) } } }
@Composable private fun Choice(label: String, values: List<String>, selected: String, onSelect: (String) -> Unit) = Column { Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary); values.chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { value -> Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (value == selected) MaterialTheme.colorScheme.primary.copy(.14f) else MaterialTheme.colorScheme.surfaceVariant).clickable { onSelect(value) }.padding(8.dp), contentAlignment = Alignment.Center) { Text(value, fontSize = 11.sp, color = TextPrimary) } } } } }
@Composable private fun MultiChoice(values: List<String>, selected: Set<String>, onChange: (Set<String>) -> Unit) = Column { values.forEach { value -> Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(value in selected, { checked -> onChange(if (checked) selected + value else selected - value) }); Text(value, fontSize = 13.sp, color = TextPrimary) } } }
