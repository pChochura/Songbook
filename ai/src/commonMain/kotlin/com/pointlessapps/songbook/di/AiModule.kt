package com.pointlessapps.songbook.di

import com.pointlessapps.songbook.Agent
import com.pointlessapps.songbook.G4fAgent
import com.pointlessapps.songbook.GeminiAgent
import com.pointlessapps.songbook.OllamaAgent
import org.koin.core.qualifier.named
import org.koin.dsl.module

val aiModule = module {
    single<Agent>(named("Gemini")) {
        GeminiAgent(key = getProperty("GEMINI_API_KEY"))
    }
    single<Agent>(named("Ollama")) {
        OllamaAgent(key = getProperty("OLLAMA_API_KEY"))
    }
    single<Agent>(named("G4f")) {
        G4fAgent(key = "")
    }
}
