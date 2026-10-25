package com.craftfolio.patterns

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.Context
import android.graphics.Bitmap
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

/** Explicit screenshot job: skipped in the normal test suite. Captures actual screen pixels. */
@RunWith(AndroidJUnit4::class)
class StoreScreenshotCapture {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun captureVerifiedScreens() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("storeCapture") == "true")
        val locale = args.getString("storeLocale") ?: "en-GB"
        require(locale in AppLanguages.supported)
        val originalLanguage = AppLanguages.selected(ui.activity)
        StoreFixtureTest().seedOriginalPatterns()
        val repo = (ui.activity.application as CraftfolioApp).repository
        val (count, expectedPage) = runBlocking {
            val project = repo.dao.projects().first().first { it.name == "My autumn scarf" }
            repo.dao.counters().first().first { it.projectId == project.id }.value.toString() to project.page + 1
        }
        val originalOrientation = ui.activity.requestedOrientation
        fun orientation(value: Int, expected: Int) {
            ui.runOnUiThread { ui.activity.requestedOrientation = value }
            ui.waitUntil(15000) { ui.activity.resources.configuration.orientation == expected }
            ui.waitForIdle()
        }
        fun capture(name: String, expectedText: String) {
            ui.onNodeWithText(expectedText).assertIsDisplayed()
            if (name == "02-knitting-row-counter" || name == "03-split-view-workspace") {
                val pageDescription = ui.activity.getString(R.string.pattern_page, expectedPage)
                ui.waitUntil(20000) { ui.onAllNodesWithContentDescription(pageDescription).fetchSemanticsNodes().isNotEmpty() }
                ui.onNodeWithContentDescription(pageDescription).assertIsDisplayed()
            }
            ui.runOnUiThread {
                (ui.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(ui.activity.window.decorView.windowToken, 0)
            }
            ui.waitForIdle()
            android.os.SystemClock.sleep(750)
            ui.runOnUiThread { assertTrue("Craftfolio must own the focused window", ui.activity.hasWindowFocus()) }
            val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
            assertEquals(listOf(1200, 1920), listOf(bitmap.width, bitmap.height).sorted())
            val file = File(ui.activity.filesDir, "store-capture/$locale/$name.png")
            file.parentFile!!.mkdirs()
            file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
        }
        try {
            ui.onNodeWithTag("language-selector").performClick()
            ui.onNodeWithTag("language-$locale").performScrollTo().performClick()
            ui.waitUntil(15000) { ui.activity.resources.configuration.locales[0].toLanguageTag() == locale }
            orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
            ui.onNode(hasSetTextAction()).performTextInput("scarf")
            ui.waitUntil(10000) { ui.onAllNodesWithText("Moss stitch scarf").fetchSemanticsNodes().isNotEmpty() }
            capture("01-pattern-library", "Moss stitch scarf")
            ui.onNodeWithText(ui.activity.getString(R.string.projects)).performClick()
            ui.onNodeWithText("My autumn scarf").performClick()
            ui.onNodeWithText(count).assertIsDisplayed()
            capture("02-knitting-row-counter", count)
            orientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_LANDSCAPE)
            capture("03-split-view-workspace", count)
            ui.onNodeWithText(ui.activity.getString(R.string.back)).performClick()
            capture("04-project-tracker", "My autumn scarf")
        } finally {
            AppLanguages.save(ui.activity, originalLanguage)
            ui.runOnUiThread { ui.activity.requestedOrientation = originalOrientation }
            ui.activityRule.scenario.recreate()
        }
    }
}
