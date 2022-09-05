package com.yikyaktranslate.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yikyaktranslate.R
import com.yikyaktranslate.model.Language
import com.yikyaktranslate.service.face.ApiResult
import com.yikyaktranslate.service.face.TranslationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TranslateViewModel(application: Application, private val service: TranslationService) : AndroidViewModel(application) {

    // Code for the source language that we are translating from; currently hardcoded to English
    private val sourceLanguageCode: String = application.getString(R.string.source_language_code)

    // List of Languages that we get from the back end
    private val _displayLanguages = MutableStateFlow<UIState<List<Language>>>(UIState.Idle)
    val displayLanguages: LiveData<UIState<List<Language>>> = _displayLanguages.asLiveData()

    // Index within languages/languagesToDisplay that the user has selected
    val targetLanguageIndex = mutableStateOf(0)

    // Text that the user has input to be translated
    private val _textToTranslate = MutableStateFlow(TextFieldValue(""))
    val textToTranslate = _textToTranslate.asLiveData()

    private val _resultLanguage = MutableStateFlow<UIState<String>>(UIState.Idle)
    val resultLanguage = _resultLanguage.asLiveData()

    /**
     * Loads the languages from our service
     */
    private suspend fun loadLanguages() {
        _displayLanguages.value = UIState.Loading
        _displayLanguages.value = when (val apiResult = service.fetchLanguages()) {
            is ApiResult.Success -> UIState.Result(apiResult.data)
            else -> UIState.Failure
        }
    }

    fun translate() {
        val asSuccessResult = _displayLanguages.value as? UIState.Result ?: return
        viewModelScope.launch {
            _resultLanguage.value = UIState.Loading
            val apiResult = service.translate(sourceLanguageCode, asSuccessResult.data[targetLanguageIndex.value].code, _textToTranslate.value.text)
            _resultLanguage.value = when (apiResult) {
                is ApiResult.Success -> UIState.Result(apiResult.data.translatedText)
                else -> UIState.Failure
            }
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
        targetLanguageIndex.value = newLanguageIndex
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
