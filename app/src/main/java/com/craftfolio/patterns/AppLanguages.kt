package com.craftfolio.patterns

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLanguages {
    val supported = listOf("en-GB", "en-US", "en-AU", "en-IN", "de-DE", "fr-FR", "es-ES", "es-MX", "it-IT", "ja-JP", "nl-NL", "pl-PL", "pt-BR", "pt-PT", "sv-SE", "zh-CN")
    fun selected(context: Context): String = context.getSharedPreferences("app-settings", Context.MODE_PRIVATE)
        .getString("language", "").orEmpty().takeIf { it in supported }.orEmpty()

    fun save(context: Context, tag: String) {
        require(tag.isEmpty() || tag in supported)
        requireUser(context.getSharedPreferences("app-settings", Context.MODE_PRIVATE).edit().putString("language", tag).commit(), R.string.action_failed)
    }

    fun wrap(context: Context): Context {
        val tag = selected(context)
        if (tag.isEmpty()) return context
        val locale = Locale.forLanguageTag(tag)
        return context.createConfigurationContext(Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        })
    }

    fun nativeName(tag: String): String = Locale.forLanguageTag(tag).let { it.getDisplayName(it) }
}
