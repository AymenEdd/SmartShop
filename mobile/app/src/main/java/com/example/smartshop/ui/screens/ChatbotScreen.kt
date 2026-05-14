package com.example.smartshop.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smartshop.viewmodel.ChatbotViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.Locale

private fun startSpeechRecognition(
    speechRecognizer: SpeechRecognizer?,
    onError: (String) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
    }
    try {
        speechRecognizer?.startListening(intent)
    } catch (e: Exception) {
        onError("Impossible de démarrer la reconnaissance vocale")
    }
}

@Composable
fun ChatbotScreen(vm: ChatbotViewModel) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var recording by remember { mutableStateOf(false) }
    var recognitionError by remember { mutableStateOf<String?>(null) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    var detectedLabel by remember { mutableStateOf<String?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var cameraLoading by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) {
            cameraError = "Photo annulée"
            return@rememberLauncherForActivityResult
        }
        cameraLoading = true
        cameraError = null
        detectedLabel = null

        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                val label = labels.maxByOrNull { it.confidence }?.text
                if (!label.isNullOrBlank()) {
                    detectedLabel = label
                    vm.input = label
                    cameraError = null
                    // Auto-send the detected label
                    if (vm.input.trim().isNotEmpty() && !vm.loading) {
                        vm.ask()
                    }
                } else {
                    cameraError = "Aucun objet reconnu"
                }
            }
            .addOnFailureListener {
                cameraError = "Erreur scanner : ${it.message}"
                detectedLabel = null
            }
            .addOnCompleteListener {
                cameraLoading = false
            }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            cameraError = "Permission caméra requise"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            recording = true
            startSpeechRecognition(speechRecognizer) { error -> recognitionError = error }
        } else {
            recognitionError = "Permission microphone requise"
        }
    }

    DisposableEffect(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { recording = false }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}

                    override fun onError(error: Int) {
                        recording = false
                        recognitionError = "Erreur reconnaissance vocale ($error)"
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            vm.input = matches.first()
                            // Auto-send the voice message
                            if (vm.input.trim().isNotEmpty() && !vm.loading) {
                                vm.ask()
                            }
                        }
                        recording = false
                    }
                })
            }
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }

        onDispose {
            speechRecognizer?.destroy()
            tts?.shutdown()
        }
    }

    // Auto-scroll + TTS on new message
    LaunchedEffect(vm.messages.size) {
        if (vm.messages.isNotEmpty()) {
            listState.animateScrollToItem(vm.messages.size - 1)
        }
        val last = vm.messages.lastOrNull()
        if (last != null && !last.isUser) {
            tts?.speak(last.text, TextToSpeech.QUEUE_FLUSH, null, "chatbot_response")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppBgColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Text(
            "AI Assistant",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Ask anything about products or your order",
            color = MutedTextColor,
            fontSize = 13.sp
        )

        // ── Messages list ─────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(vm.messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (msg.isUser) 20.dp else 6.dp,
                            bottomEnd = if (msg.isUser) 6.dp else 20.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.isUser) AccentColor else CardSoftColor
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = if (msg.isUser) Color.White
                            else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Loading bubble
            if (vm.loading) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 20.dp, topEnd = 20.dp,
                                bottomStart = 6.dp, bottomEnd = 20.dp
                            ),
                            colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                                    .size(18.dp),
                                strokeWidth = 2.dp,
                                color = AccentColor
                            )
                        }
                    }
                }
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSoftColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = vm.input,
                    onValueChange = { vm.input = it },
                    placeholder = {
                        Text(
                            "Ask…",
                            color = MutedTextColor,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Camera scan button
                Button(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (granted) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    },
                    enabled = !cameraLoading,
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Scanner",
                        tint = Color.White
                    )
                }

                // Mic button
                Button(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (recording) {
                            speechRecognizer?.stopListening()
                            recording = false
                        } else if (granted) {
                            recording = true
                            recognitionError = null
                            startSpeechRecognition(speechRecognizer) { err ->
                                recognitionError = err
                            }
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recording) ErrorRedColor else AccentColor
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (recording) Icons.Outlined.Close else Icons.Outlined.Call,
                        contentDescription = if (recording) "Stop" else "Mic",
                        tint = Color.White
                    )
                }

                // Send button
                Button(
                    onClick = { vm.ask() },
                    enabled = vm.input.trim().isNotEmpty() && !vm.loading,
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }

        detectedLabel?.let {
            Text(
                "Objet détecté : $it",
                color = AccentColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 6.dp, top = 8.dp)
            )
        }

        if (cameraLoading) {
            Text(
                "Analyse de l'image en cours...",
                color = MutedTextColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            )
        }

        cameraError?.let {
            Text(it, color = ErrorRedColor, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp, top = 4.dp))
        }

        recognitionError?.let {
            Text(it, color = ErrorRedColor, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp, top = 4.dp))
        }
    }
}