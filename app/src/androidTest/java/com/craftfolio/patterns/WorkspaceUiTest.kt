package com.craftfolio.patterns

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.room.withTransaction

@RunWith(AndroidJUnit4::class)
class WorkspaceUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    @Test fun importedPatternAppearsAfterPickerLifecycleWithoutRestart() {
        val app = ui.activity.application as CraftfolioApp
        val title = "Live_import_check_${System.nanoTime()}"
        val source = java.io.File(app.cacheDir, "$title.png")
        val bitmap = android.graphics.Bitmap.createBitmap(32, 32, android.graphics.Bitmap.Config.ARGB_8888)
        source.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        try {
            ui.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            runBlocking { app.repository.import(android.net.Uri.fromFile(source), "Other", true) }
            ui.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            ui.waitUntil(15000) { ui.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithText(title).assertIsDisplayed()
        } finally {
            runBlocking { app.repository.dao.patterns().first().firstOrNull { it.title == title }?.let { owned ->
                app.database.withTransaction { app.database.openHelper.writableDatabase.execSQL("DELETE FROM Pattern WHERE id=?", arrayOf(owned.id)) }
                java.io.File(owned.path).delete()
            } }
            source.delete()
        }
    }
    @Test fun resumeAndCounterUndo() {
        StoreFixtureTest().seedOriginalPatterns()
        ui.onNodeWithText("Projects").performClick()
        ui.waitUntil(15000) { ui.onAllNodesWithText("My autumn scarf").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("My autumn scarf").performClick()
        ui.onNodeWithText("42").assertIsDisplayed()
        ui.onNodeWithText("+1").performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("43").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("−1").performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("42").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Undo").performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("43").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Undo").performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("42").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Edit project").assertIsDisplayed()
    }
}
