package com.yikyaktranslate.presentation.view

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.yikyaktranslate.R
import com.yikyaktranslate.model.Language
import com.yikyaktranslate.presentation.viewmodel.UIState

/**
 * Composable views that create primary translation screen
 */

@Composable
fun TranslateView(
    inputText: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    displayLanguages: UIState<List<Language>>,
    targetLanguageIndex: Int,
    onTargetLanguageSelected: (Int) -> Unit,
    onTranslateClick: () -> Unit,
    translatedText: UIState<String>
) {
    val scrollState = rememberScrollState(0)
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // User inputs text to translate here
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            value = inputText,
            onValueChange = onInputChange,
            placeholder = {
                Text("Input text to translate")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        // User reads their translation here
        Row(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Max)
        ) {
            when (translatedText) {
                is UIState.Result -> Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .border(width = 2.dp, color = MaterialTheme.colors.primary)
                        .padding(5.dp),
                    text = translatedText.data
                )
                is UIState.Idle -> {
                    // Draw nothing!
                }
                is UIState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                        )
                    }
                }
                else -> Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .border(width = 2.dp, color = MaterialTheme.colors.primary)
                        .padding(5.dp),
                    text = "Error"

                )
            }

        }
        // Translate text options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // "Translate to: " prompt label
            Text(stringResource(R.string.language_selection_prompt))
            Spacer(Modifier.size(5.dp))
            when (displayLanguages) {
                is UIState.Result -> {
                    // Creates the dropdown list of languages to select from
                    LanguageDropDown(
                        languages = displayLanguages.data, // TODO: Maybe grab this a different way.
                        targetLanguageIndex = targetLanguageIndex,
                        onTargetLanguageSelected = onTargetLanguageSelected
                    )
                }
                else -> {
                    // Placeholder text if we don't have languages for the dropdown
                    Text(stringResource(R.string.language_selection_placeholder))
                }
            }

        }
        // Button
        Row {
            // Button to execute the translation
            Button(
                onClick = onTranslateClick,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(stringResource(R.string.translate_button))
            }
        }

    }
}

@Composable
fun LanguageDropDown(
    languages: List<Language>,
    targetLanguageIndex: Int,
    onTargetLanguageSelected: (Int) -> Unit
) {
    // Keeps track of whether or not the list of languages is expanded
    var expandLanguageList by remember { mutableStateOf(false) }

    Box {
        // Shows currently selected language and opens dropdown menu
        Text(
            modifier = Modifier.clickable { expandLanguageList = true },
            text = languages[targetLanguageIndex].name
        )

        // Dropdown menu to select a language to translate to
        DropdownMenu(
            expanded = expandLanguageList,
            onDismissRequest = { expandLanguageList = false }
        ) {
            // Creates a DropdownMenuItem for each language
            languages.forEachIndexed { index, language ->
                DropdownMenuItem(
                    onClick = {
                        onTargetLanguageSelected(index)
                        expandLanguageList = false
                    }
                ) {
                    Text(text = language.name)
                }
            }
        }
    }
}
