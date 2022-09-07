package com.yikyaktranslate.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yikyaktranslate.model.Language
import com.yikyaktranslate.presentation.domain.TranslateLogic
import com.yikyaktranslate.service.face.ApiResult
import com.yikyaktranslate.service.face.TranslationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TranslateViewModel(
    application: Application,
    private val service: TranslationService,
    private val translateLogic: TranslateLogic
) : AndroidViewModel(application) {

    // Code for the source language that we are translating from; currently mapped to the device locale/language
    private val sourceLanguageCode: String get() = Locale.current.language

    // List of Languages that we get from the back end
    private val _displayLanguages = MutableStateFlow<UIState<List<Language>>>(UIState.Idle)
    val displayLanguages: LiveData<UIState<List<Language>>> = _displayLanguages.asLiveData()

    // Text that the user has input to be translated
    private val _textToTranslate = MutableStateFlow(TextFieldValue(""))
    val textToTranslate = _textToTranslate.asLiveData()

    private val _resultLanguage = MutableStateFlow<UIState<String>>(UIState.Idle)
    val resultLanguage = _resultLanguage.asLiveData()

    // Index within languages/languagesToDisplay that the user has selected
    val toTargetLanguageIndex = mutableStateOf(0)

    val fromTargetLanguageIndex = mutableStateOf(0)

    /**
     * Loads the languages from our service
     */
    private suspend fun loadLanguages() {
        _displayLanguages.value = UIState.Loading
        _displayLanguages.value = when (val apiResult = service.fetchLanguages()) {
            is ApiResult.Success -> {
                toTargetLanguageIndex.value = translateLogic.getDefaultToLanguageIndex(sourceLanguageCode, apiResult.data)
                fromTargetLanguageIndex.value = translateLogic.getDefaultFromLanguageIndex(sourceLanguageCode, apiResult.data)
                UIState.Result(apiResult.data)
            }
            else -> UIState.Failure
        }
    }

    fun translate() {
        val source = translateLogic.getLangCodeForIndex(fromTargetLanguageIndex.value, _displayLanguages.value)
        val target = translateLogic.getLangCodeForIndex(toTargetLanguageIndex.value, _displayLanguages.value)
        val text = _textToTranslate.value.text

        if (translateLogic.areParamsValid(text, source, target)) {
            // Valid parameter state
            _resultLanguage.value = UIState.Loading
            viewModelScope.launch {
                _resultLanguage.value =
                    when (val apiResult = service.translate(
                        text = _textToTranslate.value.text,
                        sourceLanguage = sourceLanguageCode,
                        targetLanguage = target,
                    )) {
                        is ApiResult.Success -> UIState.Result(apiResult.data.translatedText)
                        else -> UIState.Failure
                    }
            }
        } else {
            // Invalid Parameter state
        }
    }

    /**
     * Updates the data when there's new text from the user
     *
     * @param newText TextFieldValue that contains user input we want to keep track of
     */
    fun onInputTextChange(newText: TextFieldValue) {
        _textToTranslate.value = newText
    }

    /**
     * Updates the selected target language when the user selects a new language
     *
     * @param newLanguageIndex Represents the index for the chosen language in the list of languages
     */
    fun onTargetLanguageChange(newLanguageIndex: Int) {
        toTargetLanguageIndex.value = newLanguageIndex
    }

    fun onFromTargetLanguageChange(newLanguageIndex: Int) {
        fromTargetLanguageIndex.value = newLanguageIndex
    }

    init {
        viewModelScope.launch {
            loadLanguages()
        }
    }
}

sealed class UIState<out UI_DATA : Any> {
    object Idle : UIState<Nothing>()
    object Loading : UIState<Nothing>()
    object Failure : UIState<Nothing>()
    data class Result<out UI_DATA : Any>(val data: UI_DATA) : UIState<UI_DATA>()
}
