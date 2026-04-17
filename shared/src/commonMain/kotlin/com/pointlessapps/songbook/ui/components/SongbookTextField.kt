package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density

@Composable
internal fun SongbookTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    onImeAction: (() -> Unit)? = null,
    onTextLayout: (Density.(() -> TextLayoutResult?) -> Unit)? = null,
    textFieldStyle: SongbookTextFieldStyle = defaultSongbookTextFieldStyle(),
) {
    BasicTextField(
        modifier = modifier.fillMaxWidth(),
        state = state,
        keyboardOptions = textFieldStyle.keyboardOptions,
        onKeyboardAction = { onImeAction?.invoke() },
        inputTransformation = textFieldStyle.inputTransformation,
        outputTransformation = textFieldStyle.outputTransformation,
        textStyle = textFieldStyle.typography.copy(
            color = textFieldStyle.textColor,
            textAlign = textFieldStyle.textAlign,
        ),
        cursorBrush = SolidColor(textFieldStyle.cursorColor),
        lineLimits = textFieldStyle.lineLimits,
        onTextLayout = onTextLayout,
        readOnly = textFieldStyle.readOnly,
        decorator = { innerTextField ->
            if (state.text.isEmpty()) {
                SongbookText(
                    modifier = Modifier.fillMaxWidth(),
                    text = textFieldStyle.placeholder,
                    textStyle = defaultSongbookTextStyle().copy(
                        typography = textFieldStyle.typography,
                        textColor = textFieldStyle.placeholderColor,
                        textAlign = textFieldStyle.textAlign,
                        maxLines = when (val limits = textFieldStyle.lineLimits) {
                            is TextFieldLineLimits.SingleLine -> 1
                            is TextFieldLineLimits.MultiLine -> limits.maxHeightInLines
                        },
                    ),
                )
            }
            innerTextField()
        },
    )
}

@Composable
internal fun defaultSongbookTextFieldStyle() = SongbookTextFieldStyle(
    keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
    ),
    inputTransformation = null,
    outputTransformation = null,
    placeholder = "",
    typography = MaterialTheme.typography.bodyLarge,
    textColor = MaterialTheme.colorScheme.onSurface,
    textAlign = TextAlign.Start,
    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = Int.MAX_VALUE),
    placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.onSurfaceVariant,
    readOnly = false,
)

internal data class SongbookTextFieldStyle(
    val keyboardOptions: KeyboardOptions,
    val inputTransformation: InputTransformation?,
    val outputTransformation: OutputTransformation?,
    val placeholder: String,
    val typography: TextStyle,
    val textColor: Color,
    val textAlign: TextAlign,
    val lineLimits: TextFieldLineLimits,
    val placeholderColor: Color,
    val cursorColor: Color,
    val readOnly: Boolean,
)
