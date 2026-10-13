package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * SpeechToTextManager: High-level helper class managing Android's SpeechRecognizer
 * for voice-based interaction, voice search, and natural language command navigation
 * across Rozgar Kashmir.
 */
class SpeechToTextManager(private val context: Context) {

    private val tag = "SpeechToTextManager"
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var activeResultCallback: ((String) -> Unit)? = null

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Start listening for voice input.
     * @param languageCode Locale code such as "en-IN", "ur-IN", "hi-IN", or default device locale.
     * @param onResult Optional completion callback with the final recognized text string.
     */
    fun startListening(
        languageCode: String = "en-IN",
        onResult: ((String) -> Unit)? = null
    ) {
        mainHandler.post {
            try {
                activeResultCallback = onResult
                _errorMessage.value = null
                _recognizedText.value = ""
                _partialText.value = ""
                _rmsDb.value = 0f

                if (!isRecognitionAvailable()) {
                    val msg = "Speech recognition is not supported or not enabled on this device."
                    _errorMessage.value = msg
                    _isListening.value = false
                    Log.w(tag, msg)
                    return@post
                }

                // Clean up any existing recognizer instance first
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
                Log.d(tag, "Started speech recognition listening in $languageCode")
            } catch (e: Exception) {
                Log.e(tag, "Failed to start speech recognition", e)
                _errorMessage.value = "Voice recognition error: ${e.localizedMessage ?: "Unknown error"}"
                _isListening.value = false
            }
        }
    }

    /**
     * Stop listening and finalize results.
     */
    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(tag, "Error stopping speech recognizer", e)
            } finally {
                _isListening.value = false
            }
        }
    }

    /**
     * Cancel ongoing recognition without returning results.
     */
    fun cancel() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.e(tag, "Error canceling speech recognizer", e)
            } finally {
                _isListening.value = false
                _rmsDb.value = 0f
            }
        }
    }

    /**
     * Release all recognizer resources.
     */
    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e(tag, "Error destroying speech recognizer", e)
            } finally {
                _isListening.value = false
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _errorMessage.value = null
            }

            override fun onBeginningOfSpeech() {
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                _rmsDb.value = rmsdB.coerceAtLeast(0f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _rmsDb.value = 0f
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client-side recognition error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout while processing voice"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak again clearly."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Voice server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Listening timed out."
                    else -> "Voice recognition encountered error ($error)"
                }
                Log.w(tag, "Speech recognition error: $message (code $error)")
                // If it was just NO_MATCH or SPEECH_TIMEOUT, user can retry without scary red errors
                _errorMessage.value = message
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                _rmsDb.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val finalSpeech = matches?.firstOrNull() ?: ""
                if (finalSpeech.isNotBlank()) {
                    _recognizedText.value = finalSpeech
                    _partialText.value = finalSpeech
                    activeResultCallback?.invoke(finalSpeech)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { partial ->
                    if (partial.isNotBlank()) {
                        _partialText.value = partial
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    companion object {
        /**
         * Helper parser for Kashmir Voice Navigation Commands:
         * Parses speech transcription into app action or query.
         */
        fun parseVoiceCommand(text: String): VoiceAction {
            val clean = text.trim().lowercase(Locale.ROOT)
            return when {
                clean.contains("job") || clean.contains("kaam") || clean.contains("rozgar") || clean.contains("vacancy") -> {
                    VoiceAction.NavigateToJobs
                }
                clean.contains("worker") || clean.contains("mistri") || clean.contains("carpenter") ||
                clean.contains("plumber") || clean.contains("artisan") || clean.contains("labor") ||
                clean.contains("mazdoor") -> {
                    VoiceAction.NavigateToWorkers
                }
                clean.contains("seasonal") || clean.contains("apple") || clean.contains("orchard") ||
                clean.contains("saffron") || clean.contains("harvest") -> {
                    VoiceAction.NavigateToSeasonal
                }
                clean.contains("market") || clean.contains("bazaar") || clean.contains("product") ||
                clean.contains("shawl") || clean.contains("pashmina") || clean.contains("carpet") ||
                clean.contains("buy") || clean.contains("sell") -> {
                    VoiceAction.NavigateToMarketplace
                }
                clean.contains("hire") || clean.contains("post job") || clean.contains("post a job") -> {
                    VoiceAction.NavigateToPostJob
                }
                clean.contains("chat") || clean.contains("message") || clean.contains("inbox") -> {
                    VoiceAction.NavigateToMessages
                }
                clean.contains("profile") || clean.contains("account") || clean.contains("my card") -> {
                    VoiceAction.NavigateToProfile
                }
                clean.contains("admin") || clean.contains("dashboard") -> {
                    VoiceAction.NavigateToAdmin
                }
                clean.contains("donate") || clean.contains("charity") || clean.contains("zakat") || clean.contains("sadqah") -> {
                    VoiceAction.NavigateToDonate
                }
                clean.startsWith("search") || clean.startsWith("find") || clean.startsWith("dhoondo") -> {
                    val query = clean.removePrefix("search").removePrefix("find").removePrefix("dhoondo").trim()
                    VoiceAction.Search(query)
                }
                else -> VoiceAction.GeneralQuery(text)
            }
        }
    }
}

sealed interface VoiceAction {
    data object NavigateToJobs : VoiceAction
    data object NavigateToWorkers : VoiceAction
    data object NavigateToSeasonal : VoiceAction
    data object NavigateToMarketplace : VoiceAction
    data object NavigateToPostJob : VoiceAction
    data object NavigateToMessages : VoiceAction
    data object NavigateToProfile : VoiceAction
    data object NavigateToAdmin : VoiceAction
    data object NavigateToDonate : VoiceAction
    data class Search(val query: String) : VoiceAction
    data class GeneralQuery(val rawText: String) : VoiceAction
}
