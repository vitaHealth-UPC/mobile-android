package com.vitahealth.tata.intake.presentation.voice

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.presentation.detail.DoseDetailScreen
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*

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

@Composable
fun VoiceConfirmationScreen(
    state: VoiceConfirmationUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onReadPhrase: () -> Unit,
    canReadPhrase: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val inter = FontFamily(Font(R.font.tata_inter))
    val failed =
        state.phase in
            setOf(VoicePhase.NOT_RECOGNIZED, VoicePhase.UNAVAILABLE, VoicePhase.PERMISSION_DENIED)
    Column(
        modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF)))
            )
            .padding(horizontal = 22.dp)
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                IconButton(onBack, Modifier.size(32.dp)) {
                    Text("‹", color = TataNavy, fontSize = 24.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                stringResource(R.string.voice_title),
                fontFamily = FontFamily(Font(R.font.tata_serif)),
                fontSize = 30.sp,
                lineHeight = 36.sp,
                color = TataText,
            )
            Text(
                stringResource(R.string.voice_subtitle),
                fontFamily = inter,
                fontSize = 14.sp,
                color = TataMuted,
            )
            Spacer(Modifier.height(32.dp))
            Box(Modifier.size(242.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(242.dp).background(Color(0xFFEFE8FE), CircleShape))
                Box(Modifier.size(186.dp).background(Color(0xFFE9E0FC), CircleShape))
                Box(
                    Modifier.size(138.dp)
                        .shadow(
                            10.dp,
                            CircleShape,
                            ambientColor = Color(0x1A1A2138),
                            spotColor = Color(0x1A1A2138),
                        )
                        .background(Color.White, CircleShape)
                        .border(2.dp, Color(0xFFD9CEFF), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    TataSvgIcon(R.raw.voice_orb_microphone, Modifier.size(58.dp))
                }
            }
            Spacer(Modifier.height(32.dp))
            val label =
                when (state.phase) {
                    VoicePhase.RECORDING -> R.string.voice_listening
                    VoicePhase.PROCESSING -> R.string.voice_processing
                    VoicePhase.NOT_RECOGNIZED -> R.string.voice_not_recognized
                    VoicePhase.PERMISSION_DENIED -> R.string.voice_permission
                    VoicePhase.UNAVAILABLE -> R.string.voice_unavailable
                    else -> R.string.voice_ready
                }
            Row(
                Modifier.heightIn(min = 38.dp)
                    .background(Color(0xFFF0ECFF), RoundedCornerShape(19.dp))
                    .padding(horizontal = 17.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(label),
                    fontFamily = inter,
                    fontSize = 12.sp,
                    color = if (failed) Color(0xFFAD2E3D) else Color(0xFF696FD1),
                )
                Spacer(Modifier.width(10.dp))
                listOf(10, 18, 26, 18, 10).forEachIndexed { index, height ->
                    Box(
                        Modifier.padding(end = 4.dp)
                            .width(4.dp)
                            .height(height.dp)
                            .background(
                                if (index == 2) Color(0xFF696FD1) else Color(0xFFB6ADEB),
                                RoundedCornerShape(2.dp),
                            )
                    )
                }
            }
            Spacer(Modifier.height(50.dp))
            Column(
                Modifier.fillMaxWidth()
                    .shadow(
                        6.dp,
                        RoundedCornerShape(20.dp),
                        ambientColor = Color(0x141A2138),
                        spotColor = Color(0x141A2138),
                    )
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFFFFF4E5), Color(0xFFFBEBD5))),
                        RoundedCornerShape(20.dp),
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.voice_suggested),
                        fontFamily = inter,
                        fontSize = 12.sp,
                        color = TataMuted,
                        modifier = Modifier.weight(1f),
                    )
                    val readPhraseLabel = stringResource(R.string.voice_read_phrase)
                    IconButton(
                        onReadPhrase,
                        Modifier.semantics { contentDescription = readPhraseLabel }
                            .requiredSize(30.dp),
                        enabled = canReadPhrase && state.dose != null,
                    ) {
                        Box(
                            Modifier.requiredSize(30.dp).background(Color(0xFFF0ECFF), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            TataSvgIcon(R.raw.voice_phrase_speaker, Modifier.size(22.dp))
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.width(3.dp)
                            .height(53.dp)
                            .background(Color(0xFFD5C8F4), RoundedCornerShape(2.dp))
                    )
                    Text(
                        state.dose
                            ?.let { stringResource(R.string.voice_phrase, it.medicationName) }
                            .orEmpty(),
                        fontFamily = inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        lineHeight = 23.sp,
                        color = TataText,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }
            if (failed) {
                Spacer(Modifier.height(8.dp))
                Column(
                    Modifier.fillMaxWidth()
                        .background(Color(0xFFFCE8EB), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        stringResource(R.string.voice_not_saved),
                        fontFamily = inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color(0xFFAD2E3D),
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        stringResource(
                            if (state.phase == VoicePhase.PERMISSION_DENIED)
                                R.string.voice_permission_hint
                            else R.string.voice_retry_hint
                        ),
                        fontFamily = inter,
                        fontSize = 8.5.sp,
                        lineHeight = 12.sp,
                        color = TataMuted,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        val active =
            state.phase == VoicePhase.RECORDING ||
                state.phase == VoicePhase.PROCESSING ||
                state.phase == VoicePhase.LOADING
        Box(
            Modifier.fillMaxWidth().padding(bottom = 56.dp, top = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (active)
                OutlinedButton(
                    onBack,
                    Modifier.widthIn(max = 289.dp).fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color(0xFFE9E8F2)),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = TataNavy,
                        ),
                ) {
                    Text(
                        "×  " + stringResource(R.string.voice_cancel),
                        fontFamily = inter,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            else
                TataButton(
                    stringResource(if (failed) R.string.voice_retry else R.string.voice_start),
                    onStart,
                    Modifier.widthIn(max = 289.dp).fillMaxWidth(),
                )
        }
    }
}
