package com.pointlessapps.songbook

import com.pointlessapps.songbook.model.OcrRequestBody
import kotlin.io.encoding.Base64

internal fun createOcrPrompt() = """
Act as a music transcription assistant. I will provide you with an image of a song sheet that contains printed lyrics and potentially handwritten chords. Your task is to extract this information and return it **strictly** as a structured JSON object.

**Extraction Rules:**

1. **Title:** Extract the song title. Remove any leading numbers or punctuation (e.g., "3. Song Name" becomes "Song Name").
2. **Lyrics:** Extract all verses. For each line, remove any leading verse numbers (e.g., "1. Lyrics here" becomes "Lyrics here").
3. **Chords:** Look for handwritten letters (like D, G, A7) written above or beside the text. Map these chords to the corresponding lines of text. Return them as a comma-separated string. If no chords are present for a line, return `null`.
4. **Structure:** Organize the content into a list of sections, identifying each as a 'verse', 'chorus', or 'bridge'.

**Output Format:**

```json
{
  "title": "the title of the song",
  "sections": [
    {
      "type": "verse|chorus|bridge",
      "lines": [
        {
          "text": "the cleaned text of the specific line",
          "chords": "comma, separated, chords" 
        }
      ]
    }
  ]
}
```

**Constraint:** Do not include any conversational prose or explanations. Return only the JSON block.
""".trimIndent()

internal fun createOcrRequestBody(
    prompt: String,
    bytes: ByteArray,
    mimeType: String,
) = OcrRequestBody(
    contents = listOf(
        OcrRequestBody.Content(
            parts = listOf(
                OcrRequestBody.Content.Part(
                    inlineData = OcrRequestBody.Content.Part.InlineData(
                        mimeType = mimeType,
                        data = Base64.encode(bytes),
                    ),
                ),
                OcrRequestBody.Content.Part(
                    text = prompt,
                ),
            ),
        ),
    ),
    generationConfig = OcrRequestBody.GenerationConfig(
        temperature = 0.1,
        topP = 1.0,
        topK = 1,
        maxOutputTokens = 8000,
    ),
)
