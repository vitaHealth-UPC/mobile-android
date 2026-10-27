package com.vitahealth.tata.intake.presentation.voice

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen
import com.vitahealth.tata.intake.presentation.voice.screens.VoiceConfirmationScreen
import com.vitahealth.tata.intake.presentation.voice.viewmodel.VoiceConfirmationViewModel
import com.vitahealth.tata.intake.presentation.voice.viewmodel.VoicePhase
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab

@Composable
fun VoiceConfirmationRoute(
    factory: VoiceConfirmationViewModel.Factory,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onAdultTab: (AdultTab, String) -> Unit,
) {
    val model: VoiceConfirmationViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val language = if (locale.language == "en") "en-US" else "es-419"
    var speakerReady by remember { mutableStateOf(false) }
    val speaker = remember { TextToSpeech(context) { speakerReady = it == TextToSpeech.SUCCESS } }
    DisposableEffect(speaker) {
        onDispose {
            speaker.stop()
            speaker.shutdown()
        }
    }
    var startupRequested by rememberSaveable { mutableStateOf(false) }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) model.start(language) else model.permissionDenied()
        }
    val start = {
        speaker.stop()
        if (state.dose == null) model.load()
        else if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
            model.start(language)
        else permission.launch(Manifest.permission.RECORD_AUDIO)
    }
    LaunchedEffect(state.dose?.id) {
        if (state.dose != null && !startupRequested) {
            startupRequested = true
            start()
        }
    }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) model.cancelRecording()
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            model.cancelRecording()
        }
    }
    val phrase =
        state.dose?.let { stringResource(R.string.voice_phrase, it.medicationName) }.orEmpty()
    state.confirmation?.let {
        DoseDetailScreen(it, onBack, {}, {}, onHome = onHome, onAdultTab = onAdultTab)
        return
    }
    VoiceConfirmationScreen(
        state,
        onBack = {
            model.cancelRecording()
            onBack()
        },
        onStart = start,
        onReadPhrase = {
            model.cancelRecording()
            if (speakerReady) {
                speaker.language = locale
                speaker.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "intake-phrase")
            }
        },
        canReadPhrase = speakerReady && state.phase != VoicePhase.PROCESSING,
    )
}
