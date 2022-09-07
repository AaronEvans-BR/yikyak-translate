package com.yikyaktranslate.presentation.domain

import com.yikyaktranslate.model.Language
import com.yikyaktranslate.presentation.viewmodel.UIState

class TranslateLogic {
    fun areParamsValid(
        text: String,
        target: String,
        source: String,
    ): Boolean = text.isNotBlank()
            && source.isNotBlank()
            && target.isNotBlank()

    fun getDefaultFromLanguageIndex(
        deviceLang: String,
        langs: List<Language>
    ): Int {
        val indexOfDeviceLang = langs.firstOrNull {
            it.code == deviceLang
        }?.let {
            when (val result = langs.indexOf(it)) {
                -1 -> 0  // index of can return -1, prevent out of bounds
                else -> result
            }
        }
        return indexOfDeviceLang ?: 0 // DEFAULT TO INDEX 0
    }

    fun getDefaultToLanguageIndex(
        deviceLang: String,
        langs: List<Language>
    ): Int {
        val indexOfDeviceLang = langs.firstOrNull {
            it.code != deviceLang
        }?.let {
            when (val result = langs.indexOf(it)) {
                -1 -> 0  // index of can return -1, prevent out of bounds
                else -> result // default to the first non matching index of the device lang
            }
        }
        return indexOfDeviceLang ?: 0 // DEFAULT TO INDEX 0
    }

    fun getLangCodeForIndex(index: Int, langs: UIState<List<Language>>): String {
        return (langs as? UIState.Result)?.data?.get(index)?.code.orEmpty()
    }
}