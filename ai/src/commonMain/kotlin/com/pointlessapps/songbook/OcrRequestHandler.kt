package com.pointlessapps.songbook

import com.pointlessapps.songbook.model.G4fRequestBody
import com.pointlessapps.songbook.model.OcrRequestBody
import com.pointlessapps.songbook.model.OllamaRequestBody
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.io.encoding.Base64

internal fun createOcrPrompt() = """
**Role:** Expert Music Transcriptionist specializing in Songbook Pro/ChordPro formatting.

**Objective:** Extract song data from one or more song sheets in a single image. Return a JSON array of song objects.

**Extraction Logic & Rules:**

1.  **Multi-Song Detection:** Treat each distinct song as a separate object in the array.
2.  **Metadata:**
    * `title`: Cleaned title (no leading numbers).
    * `artist`: Artist name or `null`.
    * `chords_beside`: Extract marginal chords or repetition symbols (e.g., `%`, `x2`) appearing to the side of the main text.
3.  **Songbook Pro Content:** Format the `content` field as a continuous string:
    * **Sections:** Headers in brackets, e.g., `[Verse]`, `[Chorus]`, `[Bridge]`.
    * **Solo Annotations:** Treat any "Solo" or "Instrumental" markings as section headers (e.g., `[Solo]`).
    * **Inline Chords:** Place chords inside brackets `[C]` immediately before or within the lyric line where they are synchronized.
    * **Cleaning:** Remove leading line numbers or verse markers from the lyrics.
4.  **Musical Heuristics:** Correct OCR errors using musical context (e.g., prioritize `B7` over `87`).

**Output Format:**
Return **strictly** a JSON array. No conversational prose.

```json
[
  {
    "title": "Song Title",
    "artist": "Artist Name",
    "chords_beside": ["G", "D7", "%"],
    "content": "[Intro]\n[G] [D]\n\n[Verse]\nThis is a [G]lyric line with [C]chords\n\n[Solo]\n[Am] [F] [C] [G]"
  }
]
```
""".trimIndent()

internal fun createG4fRequestBody(
    prompt: String,
    bytes: ByteArray,
) = G4fRequestBody(
    model = "gpt-4o",
    messages = listOf(
        G4fRequestBody.Message(
            role = "user",
            content = prompt,
        ),
    ),
    images = listOf(
        listOf("data:image/jpeg;base64,${Base64.encode(bytes)}", "image.jpg"),
    ),
)

internal fun createOllamaRequestBody(
    prompt: String,
    bytes: ByteArray,
) = OllamaRequestBody(
    model = "qwen3-vl:235b-cloud",
    prompt = prompt,
    stream = false,
    think = false,
    images = listOf(Base64.encode(bytes)),
    format = buildJsonObject {
        put("type", "array")
        putJsonObject("items") {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("title") {
                    putJsonArray("type") {
                        add("string")
                        add("null")
                    }
                }
                putJsonObject("artist") {
                    putJsonArray("type") {
                        add("string")
                        add("null")
                    }
                }
                putJsonObject("sections") {
                    put("type", "array")
                    putJsonObject("items") {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("type") {
                                putJsonArray("enum") {
                                    add("verse")
                                    add("chorus")
                                    add("bridge")
                                    add("outro")
                                    add("intro")
                                }
                            }
                            putJsonObject("chords_beside") {
                                put("type", "array")
                                putJsonObject("items") {
                                    put("type", "string")
                                }
                            }
                            putJsonObject("lines") {
                                put("type", "array")
                                putJsonObject("items") {
                                    put("type", "object")
                                    putJsonObject("properties") {
                                        putJsonObject("text") {
                                            put("type", "string")
                                        }
                                        putJsonObject("chords_above") {
                                            put("type", "array")
                                            putJsonObject("items") {
                                                put("type", "string")
                                            }
                                        }
                                    }
                                    putJsonArray("required") {
                                        add("text")
                                        add("chords_above")
                                    }
                                }
                            }
                        }
                        putJsonArray("required") {
                            add("type")
                            add("chords_beside")
                            add("lines")
                        }
                    }
                }
            }
            putJsonArray("required") {
                add("title")
                add("artist")
                add("sections")
            }
        }
    },
)

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
