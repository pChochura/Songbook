package com.pointlessapps.songbook

import com.pointlessapps.songbook.model.OcrRequestBody
import kotlin.io.encoding.Base64

internal fun createOcrPrompt() = """
### Final Refined Prompt

**Role:** Act as a music transcription assistant specializing in multi-document batch processing of song sheets.

**Task:** Extract information from one or more song sheets in a single image. Distinguish between chords synchronized with specific lines of lyrics and general chord notations or repetition symbols written in the margins.

**Extraction Rules:**

1.  **Multi-Song Detection:** Identify each distinct song in the image. Return a JSON array of song objects.
2.  **Title & Artist:** Extract the song title and artist. Sanitize titles by removing leading numbers or punctuation. Use `null` if the artist is missing.
3.  **Sections & Categorization:** Organize the song into sections (e.g., `verse`, `chorus`, `bridge`).
4.  **Sectional Chords & Symbols (Beside Text):** Within each section, include a field `chords_beside`. Capture any chords, progressions, or repetition symbols (e.g., `%`, `x2`, `(bis)`) written in the margins or to the side of that specific section. **Return these as an array of strings.** Use `[]` if none are present.
5.  **Lyrics & Synced Chords:** For each line within a section:
    * `text`: The cleaned lyric line (no leading verse numbers).
    * `chords_above`: Chords written directly over or above the words. **Return these as an array of strings.** Use `[]` if no chords are present.
6.  **Validation Logic:** Cross-reference all extracted "chords" against standard musical notation (A-G, sharps/flats, minor, 7ths, etc.). If a handwritten character is ambiguous (e.g., an '8' that looks like a 'B'), prioritize the musical interpretation.

**Output Format:**

```json
[
  {
    "title": "Song Title",
    "artist": "Artist Name",
    "sections": [
      {
        "type": "verse|chorus|bridge",
        "chords_beside": ["G", "D7", "%", "x2"],
        "lines": [
          {
            "text": "Lyric line here",
            "chords_above": ["G", "C"]
          }
        ]
      }
    ]
  }
]
```

**Constraint:** Return **strictly** the JSON array. No conversational prose or explanations.
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
