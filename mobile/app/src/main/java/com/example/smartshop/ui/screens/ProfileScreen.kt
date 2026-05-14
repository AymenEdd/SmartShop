package com.example.smartshop.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.smartshop.viewmodel.ProfileViewModel
import com.example.smartshop.viewmodel.UiState

@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    // ── NOT LOGGED IN GATE ──────────────────────────────────────────────────
    if (!vm.isLoggedIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBgColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(HeroColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = AccentColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "You're not logged in",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Log in to access your profile, orders and personalized recommendations.",
                    color = MutedTextColor,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Log in", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
        return
    }

    // ── LOGGED IN ───────────────────────────────────────────────────────────
    LaunchedEffect(Unit) { vm.loadProfile() }

    val context = LocalContext.current
    val selectedLatitude = remember { mutableStateOf<Double?>(null) }
    val selectedLongitude = remember { mutableStateOf<Double?>(null) }
    val locationStatus = remember { mutableStateOf("") }
    val saveMessage = remember { mutableStateOf<String?>(null) }
    val saving = remember { mutableStateOf(false) }

    LaunchedEffect(vm.profile) {
        vm.profile?.let {
            selectedLatitude.value = it.latitude
            selectedLongitude.value = it.longitude
            saveMessage.value = null
        }
    }

    fun updateLocationFields(lat: Double, lng: Double) {
        selectedLatitude.value = lat
        selectedLongitude.value = lng
        locationStatus.value = "Localisation capturée"
    }

    fun locateFromDevice() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (loc != null) updateLocationFields(loc.latitude, loc.longitude)
        else locationStatus.value = "Impossible de trouver la localisation. Réessayez."
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) locateFromDevice()
        else locationStatus.value = "Permission GPS refusée"
    }

    fun requestDeviceLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) locateFromDevice()
        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun saveLocation() {
        val lat = selectedLatitude.value
        val lng = selectedLongitude.value
        if (lat == null || lng == null) { saveMessage.value = "Aucune localisation à enregistrer"; return }
        saving.value = true
        vm.saveProfileLocation(lat, lng) { success, message ->
            saving.value = false
            saveMessage.value = if (success) "Localisation enregistrée" else message
            if (success) locationStatus.value = "Localisation sauvegardée"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBgColor),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── HERO HEADER ────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFCDE4FF), Color(0xFFF0F6FF))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(AccentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vm.profile?.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = vm.profile?.full_name?.ifBlank { null } ?: "My Account",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        vm.profile?.email?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedTextColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AccentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "✦  AI personalization on",
                                color = AccentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // ── LOCATION CARD ──────────────────────────────────────────────────
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Localisation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            tint = AccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    val profile = vm.profile
                    val latitude = selectedLatitude.value ?: profile?.latitude
                    val longitude = selectedLongitude.value ?: profile?.longitude

                    if (latitude != null && longitude != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text("Latitude", fontSize = 10.sp, color = MutedTextColor, fontWeight = FontWeight.SemiBold)
                                Text("${"%.6f".format(latitude)}", fontSize = 13.sp, color = TextPrimary)
                            }
                            Column {
                                Text("Longitude", fontSize = 10.sp, color = MutedTextColor, fontWeight = FontWeight.SemiBold)
                                Text("${"%.6f".format(longitude)}", fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 14f)
                        }
                        LaunchedEffect(latitude, longitude) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 14f)
                        }
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(state = MarkerState(position = LatLng(latitude, longitude)), title = "Votre position")
                        }
                    } else {
                        Text("Aucune localisation enregistrée.", color = MutedTextColor, fontSize = 13.sp)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { requestDeviceLocation() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text("Use device location", fontSize = 12.sp, color = AccentColor)
                        }
                        Button(
                            onClick = { saveLocation() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                        ) {
                            Text(if (saving.value) "Saving…" else "Save location", fontSize = 12.sp, color = Color.White)
                        }
                    }
                    if (locationStatus.value.isNotBlank()) Text(locationStatus.value, color = MutedTextColor, fontSize = 12.sp)
                    saveMessage.value?.let { Text(it, color = AccentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }

        // ── RECOMMENDED ────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionTitle("Recommended for you")
                if (vm.personalized.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("No recommendations yet", Modifier.padding(14.dp), color = MutedTextColor, fontSize = 13.sp)
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(vm.personalized.take(6)) { p ->
                            Card(
                                modifier = Modifier.width(140.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(p.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, color = TextPrimary)
                                    Text("${"%.2f".format(p.price)} EUR", color = AccentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── ORDER HISTORY ──────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionTitle("Order history")
                when (val state = vm.ordersState) {
                    UiState.Loading -> CircularProgressIndicator(color = AccentColor)
                    is UiState.Error -> Text(state.message, color = ErrorRedColor)
                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("No orders yet", Modifier.padding(14.dp), color = MutedTextColor, fontSize = 13.sp)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.data.forEach { order ->
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text("Order #${order.id}", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                                Text("View details", color = MutedTextColor, fontSize = 12.sp)
                                            }
                                            Text("${"%.2f".format(order.total_amount)} EUR", color = AccentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        // ── LOGOUT ─────────────────────────────────────────────────────────
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ErrorRedColor.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRedColor)
            ) {
                Text("Log out", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

// ── Shared section title with accent bar ──────────────────────────────────────
@Composable
private fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentColor)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}