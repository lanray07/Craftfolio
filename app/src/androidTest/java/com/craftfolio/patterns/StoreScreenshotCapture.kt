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

/** Explicit screenshot job: skipped in the normal test suite. Captures actual screen pixels. */
@RunWith(AndroidJUnit4::class)
class StoreScreenshotCapture {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun captureVerifiedScreens() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("storeCapture") == "true")
        val locale = args.getString("storeLocale") ?: "en-GB"
        require(locale in listOf("en-GB", "en-US"))
        StoreFixtureTest().seedOriginalPatterns()
        val originalOrientation = ui.activity.requestedOrientation
        fun orientation(value: Int, expected: Int) {
            ui.runOnUiThread { ui.activity.requestedOrientation = value }
            ui.waitUntil(15000) { ui.activity.resources.configuration.orientation == expected }
            ui.waitForIdle()
        }
        fun capture(name: String, expectedText: String) {
            ui.onNodeWithText(expectedText).assertIsDisplayed()
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
            orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
            ui.onNode(hasSetTextAction()).performTextInput("scarf")
            ui.waitUntil(10000) { ui.onAllNodesWithText("Moss stitch scarf").fetchSemanticsNodes().isNotEmpty() }
            capture("01-pattern-library", "Moss stitch scarf")
            ui.onNodeWithText("Projects").performClick()
            ui.onNodeWithText("My autumn scarf").performClick()
            ui.onNodeWithText("42").assertIsDisplayed()
            capture("02-knitting-row-counter", "42")
            orientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_LANDSCAPE)
            capture("03-split-view-workspace", "42")
            ui.onNodeWithText("Back").performClick()
            capture("04-project-tracker", "My autumn scarf")
        } finally { ui.runOnUiThread { ui.activity.requestedOrientation = originalOrientation } }
    }
}
