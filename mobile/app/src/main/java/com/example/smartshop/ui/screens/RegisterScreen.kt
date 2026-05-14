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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.smartshop.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smartshop.viewmodel.AuthViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun RegisterScreen(vm: AuthViewModel, onSuccess: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var locationStatus by remember { mutableStateOf("Location not captured") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var address by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            try {
                val provider = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val lastLocation = provider.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: provider.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (lastLocation != null) {
                        latitude = lastLocation.latitude
                        longitude = lastLocation.longitude
                        locationStatus = "Location captured"
                        address = "Latitude: ${"%.4f".format(latitude)}, Longitude: ${"%.4f".format(longitude)}"
                    } else {
                        locationStatus = "Unable to find your location. Try again."
                    }
                }
            } catch (e: Exception) {
                locationStatus = "Unable to access location"
            }
        } else {
            locationStatus = "Location permission denied"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBgColor)
            .verticalScroll(rememberScrollState()),
    ) {
        RegisterHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                border = BorderStroke(0.5.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RegisterTextField(
                        label = "Full name",
                        value = fullName,
                        onValueChange = { fullName = it },
                        icon = Icons.Outlined.Person,
                        keyboardType = KeyboardType.Text,
                    )
                    RegisterTextField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        icon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email,
                    )
                    RegisterTextField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        icon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                    )

                    OutlinedButton(
                        onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
                    ) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Capture location", fontWeight = FontWeight.SemiBold)
                    }

                    if (latitude != null && longitude != null) {
                        CapturedLocationCard(latitude = latitude, longitude = longitude, address = address)
                    } else {
                        RegisterStatus(text = locationStatus, isError = locationStatus.contains("denied", ignoreCase = true))
                    }

                    vm.registerError?.let { RegisterStatus(text = it, isError = true) }
                }
            }

            Button(
                onClick = {
                    vm.register(
                        email = email.trim(),
                        fullName = fullName.trim(),
                        password = password,
                        latitude = latitude,
                        longitude = longitude,
                        onSuccess = onSuccess,
                    )
                },
                enabled = !vm.authLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
            ) {
                if (vm.authLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Create account", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun RegisterHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFCDE4FF), Color(0xFFF0F6FF)),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 28.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Create account",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                )
                Text(
                    text = "Set up your profile and delivery location.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTextColor,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun RegisterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentColor,
                modifier = Modifier.size(18.dp),
            )
        },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentColor,
            unfocusedBorderColor = BorderColor,
            focusedContainerColor = SurfaceColor,
            unfocusedContainerColor = SurfaceColor,
        ),
    )
}

@Composable
private fun RegisterStatus(text: String, isError: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isError) RedBgColor else AccentBgColor)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Text(
            text = text,
            color = if (isError) ErrorRedColor else AccentColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CapturedLocationCard(latitude: Double?, longitude: Double?, address: String?) {
    if (latitude == null || longitude == null) return

    val position = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 15f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AccentBgColor),
        border = BorderStroke(0.5.dp, BorderColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(18.dp),
                )
                Column {
                    Text(
                        text = "Location captured",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "%.4f, %.4f".format(latitude, longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedTextColor,
                    )
                }
            }

            address?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                cameraPositionState = cameraPositionState,
            ) {
                Marker(
                    state = MarkerState(position = position),
                    title = "Captured location",
                    snippet = "%.4f, %.4f".format(latitude, longitude),
                )
            }
        }
    }
}
