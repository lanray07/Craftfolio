package com.craftfolio.patterns

import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class LocalizationTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private var original = ""
    @Before fun rememberLanguage() { original = AppLanguages.selected(ui.activity) }
    @After fun restoreLanguage() {
        AppLanguages.save(ui.activity, original)
        ui.activityRule.scenario.recreate()
    }

    @Test fun languageChoiceUpdatesRealUiAndSurvivesRecreation() {
        val labels = mapOf("de-DE" to "Anleitungen", "fr-FR" to "Modèles", "es-ES" to "Patrones", "es-MX" to "Patrones", "it-IT" to "Schemi", "ja-JP" to "編み図・型紙", "nl-NL" to "Patronen", "pl-PL" to "Wzory", "pt-BR" to "Receitas", "pt-PT" to "Receitas", "sv-SE" to "Mönster", "zh-CN" to "图解与纸样")
        for (tag in AppLanguages.supported) {
            val library = labels[tag] ?: "Patterns"
            ui.onNodeWithTag("language-selector").performClick()
            ui.onNodeWithTag("language-$tag").performScrollTo().performClick()
            ui.waitUntil(15000) { ui.onAllNodesWithText(library).fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithText(library).assertIsDisplayed()
            assertEquals(tag, AppLanguages.selected(ui.activity))
            ui.activityRule.scenario.recreate()
            ui.onNodeWithText(library).assertIsDisplayed()
            assertEquals(Locale.forLanguageTag(tag), ui.activity.resources.configuration.locales[0])
        }
    }

    @Test fun translatedMessagesAndRegionalEnglishResolveWithoutRawExceptionText() {
        val expected = mapOf("de-DE" to "Stricken", "fr-FR" to "Tricot", "es-ES" to "Punto", "es-MX" to "Tejido con agujas", "it-IT" to "Maglia", "ja-JP" to "棒針編み", "nl-NL" to "Breien", "pl-PL" to "Na drutach", "pt-BR" to "Tricô", "pt-PT" to "Tricot", "sv-SE" to "Stickning", "zh-CN" to "棒针编织")
        for ((tag, knitting) in expected) {
            val localized = ui.activity.createConfigurationContext(Configuration(ui.activity.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) })
            assertEquals(knitting, localized.getString(R.string.knitting))
            assertTrue(localized.getString(R.string.import_craft, knitting).contains(knitting))
            assertNotEquals("Free library limit reached. Your existing files remain available.", localized.userMessage(UserFacingFailure(R.string.free_library_limit)))
            assertFalse(localized.userMessage(Exception("raw platform exception")).contains("raw platform"))
        }
        for ((tag, word) in listOf("en-GB" to "Organise", "en-US" to "Organize", "en-AU" to "Organise", "en-IN" to "Organise")) {
            val localized = ui.activity.createConfigurationContext(Configuration(ui.activity.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) })
            assertTrue(localized.getString(R.string.empty_library).startsWith(word))
        }
    }
}
