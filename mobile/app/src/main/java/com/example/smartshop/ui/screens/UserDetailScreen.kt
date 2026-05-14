package com.example.smartshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.network.UserProfile
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun UserDetailScreen(user: UserProfile, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBgColor),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = AccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AccentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.full_name.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Name + role badge
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = user.full_name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextColor
                        )
                        if (user.is_admin) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AccentColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Admin",
                                    color = AccentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── PROFILE CARD ───────────────────────────────────────────────────
        item {
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = CardSoftColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(AccentColor, RoundedCornerShape(2.dp))
                        )
                        Text(
                            "Profile Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    InfoRow("Name",    user.full_name)
                    InfoRow("Email",   user.email)
                    InfoRow("Role",    if (user.is_admin) "Administrator" else "Customer")
                    InfoRow("User ID", "#${user.id}")
                }
            }
        }

        // ── LOCATION CARD ──────────────────────────────────────────────────
        if (user.latitude != null && user.longitude != null) {
            item {
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardSoftColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(16.dp)
                                    .background(AccentColor, RoundedCornerShape(2.dp))
                            )
                            Text(
                                "Location",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text("Latitude", fontSize = 10.sp, color = MutedTextColor, fontWeight = FontWeight.SemiBold)
                                Text("%.6f".format(user.latitude), fontSize = 13.sp, color = TextPrimary)
                            }
                            Column {
                                Text("Longitude", fontSize = 10.sp, color = MutedTextColor, fontWeight = FontWeight.SemiBold)
                                Text("%.6f".format(user.longitude), fontSize = 13.sp, color = TextPrimary)
                            }
                        }

                        val userLatLng = remember(user.latitude, user.longitude) {
                            LatLng(user.latitude, user.longitude)
                        }
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(userLatLng, 14f)
                        }
                        Card(
                            shape    = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        ) {
                            GoogleMap(
                                modifier            = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState
                            ) {
                                Marker(
                                    state   = MarkerState(position = userLatLng),
                                    title   = user.full_name,
                                    snippet = "%.4f, %.4f".format(user.latitude, user.longitude)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = CardSoftColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text(
                        "No location data available",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MutedTextColor,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MutedTextColor, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}