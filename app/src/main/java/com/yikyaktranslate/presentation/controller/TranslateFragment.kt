package com.yikyaktranslate.presentation.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.fragment.app.Fragment
import com.yikyaktranslate.presentation.theme.YikYakTranslateTheme
import com.yikyaktranslate.presentation.view.TranslateView
import com.yikyaktranslate.presentation.viewmodel.TranslateViewModel
import com.yikyaktranslate.presentation.viewmodel.UIState
import org.koin.androidx.viewmodel.ext.android.viewModel

class TranslateFragment : Fragment() {

    private val translateViewModel: TranslateViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                YikYakTranslateTheme {
                    Surface {
                        // Observe fields from view model
                        val inputText by translateViewModel.textToTranslate.observeAsState(
                            TextFieldValue("")
                        )
                        val languages by translateViewModel.displayLanguages.observeAsState(initial = UIState.Idle)
                        val targetLanguageIndex by translateViewModel.toTargetLanguageIndex
                        val fromTargetLanguageIndex by translateViewModel.fromTargetLanguageIndex
                        val resultLanguage by translateViewModel.resultLanguage.observeAsState(initial = UIState.Idle)
                        // Create Compose view
                        TranslateView(
                            inputText = inputText,
                            onInputChange = translateViewModel::onInputTextChange,
                            displayLanguages = languages,
                            toTargetLanguageIndex = targetLanguageIndex,
                            fromTargetLanguageIndex = fromTargetLanguageIndex,
                            toTargetLanguageSelected = translateViewModel::onTargetLanguageChange,
                            fromTargetLanguageSelected = translateViewModel::onFromTargetLanguageChange,
                            onTranslateClick = translateViewModel::translate,
                            translatedText = resultLanguage
                        )
                    }
                }
            }
        }
    }
}