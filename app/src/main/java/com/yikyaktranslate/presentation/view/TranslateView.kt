package com.yikyaktranslate.presentation.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.yikyaktranslate.R
import com.yikyaktranslate.model.Language
import com.yikyaktranslate.presentation.viewmodel.UIState

/**
 * Composable views that create primary translation screen
 */

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TranslateView(
    inputText: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    displayLanguages: UIState<List<Language>>,
    toTargetLanguageIndex: Int,
    fromTargetLanguageIndex: Int,
    toTargetLanguageSelected: (Int) -> Unit,
    fromTargetLanguageSelected: (Int) -> Unit,
    onTranslateClick: () -> Unit,
    translatedText: UIState<String>
) {
    val scrollState = rememberScrollState(0)
    val keyboardContext = LocalSoftwareKeyboardController.current
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
                is UIState.Result ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .border(width = 2.dp, color = MaterialTheme.colors.primary)
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .verticalScroll(scrollState)
                                .padding(5.dp),
                            text = translatedText.data
                        )
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
                is UIState.Idle -> {
                    // Draw nothing!
                }
                else ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(5.dp),
                        text = "Error"
                    )
            }

        }
        // From text options
        LanguageSelection(
            typeText = stringResource(id = R.string.language_selection_from_prompt),
            targetLanguageIndex = fromTargetLanguageIndex,
            displayLanguages = displayLanguages,
            onTargetLanguageSelected = fromTargetLanguageSelected
        )
        // To Text options
        LanguageSelection(
            typeText = stringResource(id = R.string.language_selection_to_prompt),
            targetLanguageIndex = toTargetLanguageIndex,
            displayLanguages = displayLanguages,
            onTargetLanguageSelected = toTargetLanguageSelected
        )
        // Button
        Row {
            // Button to execute the translation
            Button(
                onClick = {
                    onTranslateClick()
                    keyboardContext?.hide()
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(stringResource(R.string.translate_button))
            }
        }

    }
}

@Composable
fun LanguageSelection(
    typeText: String,
    targetLanguageIndex: Int,
    displayLanguages: UIState<List<Language>>,
    onTargetLanguageSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Translate to: " prompt label
        Text(
            modifier = Modifier.weight(1f),
            text = typeText
        )
        Spacer(Modifier.size(5.dp))
        when (displayLanguages) {
            is UIState.Result -> {
                // Creates the dropdown list of languages to select from
                LanguageDropDown(
                    cd = typeText,
                    languages = displayLanguages,
                    targetLanguageIndex = targetLanguageIndex,
                    onTargetLanguageSelected = onTargetLanguageSelected
                )
            }
            is UIState.Loading ->
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(24.dp)
                        .width(24.dp)
                )
            is UIState.Failure -> Text(text = "Failed to fetch languages")
            else -> {
                // NO OP
            }
        }
    }

}

@Composable
fun LanguageDropDown(
    cd: String,
    languages: UIState.Result<List<Language>>,
    targetLanguageIndex: Int,
    onTargetLanguageSelected: (Int) -> Unit
) {

    // Keeps track of whether or not the list of languages is expanded
    var expandLanguageList by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .wrapContentWidth()
            .background(
                color = MaterialTheme.colors.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(5.dp)
            )
            .clickable { expandLanguageList = true },
    ) {

        // Shows currently selected language and opens dropdown menu
        Text(
            modifier = Modifier
                .defaultMinSize(48.dp, 48.dp)
                /**
                 * Setting padding end 24.dp is a cheat to prevent the icon overlapping the text.
                 * I could use a constrain layout instead.
                 */
                .padding(start = 8.dp, end = 24.dp)
                .wrapContentWidth()
                .wrapContentHeight(), // make text center vertical,
            text = languages.data[targetLanguageIndex].name
        )
        Image(
            modifier = Modifier.align(Alignment.CenterEnd),
            painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24),
            contentDescription = "select $cd language"
        )
        // Dropdown menu to select a language to translate to
        DropdownMenu(
            expanded = expandLanguageList,
            onDismissRequest = { expandLanguageList = false }
        ) {
            // Creates a DropdownMenuItem for each language
            languages.data.forEachIndexed { index, language ->
                DropdownMenuItem(
                    onClick = {
                        onTargetLanguageSelected(index)
                        expandLanguageList = false
                    }
                ) {
                    Text(text = language.name, modifier = Modifier.defaultMinSize(48.dp, 48.dp))
                }
            }
        }
    }
}
