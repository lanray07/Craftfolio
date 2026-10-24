package com.craftfolio.patterns

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class JourneyTest {
    @Test fun supportedImagesAreCopiedAndRendered() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, CraftDatabase::class.java).build()
        val repo = Repository(context, db)
        try {
            listOf(android.graphics.Bitmap.CompressFormat.JPEG, android.graphics.Bitmap.CompressFormat.PNG, android.graphics.Bitmap.CompressFormat.WEBP).forEachIndexed { i, format ->
                val source = File(context.cacheDir, "image-$i")
                val bitmap = android.graphics.Bitmap.createBitmap(320, 240, android.graphics.Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.GREEN)
                source.outputStream().use { bitmap.compress(format, 90, it) }; bitmap.recycle()
                repo.import(Uri.fromFile(source), "Other", false)
                val pattern = repo.dao.patterns().first().first { it.title == "image-$i" }
                assertArrayEquals(source.readBytes(), File(pattern.path).readBytes())
                assertEquals(320, renderPattern(pattern, 0).bitmap.width)
                source.delete(); File(pattern.path).delete()
            }
        } finally { db.close() }
        Unit
    }
    @Test fun importedOriginalAndProgressSurviveDatabaseReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("journey-test.db")
        var db = Room.databaseBuilder(context, CraftDatabase::class.java, "journey-test.db").build()
        try {
            var repo = Repository(context, db)
            val source = File(context.cacheDir, "Original scarf.pdf")
            val pdf = PdfDocument()
            try {
                repeat(3) { index -> val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, index + 1).create())
                    val paint = Paint().apply { textSize = 26f }
                    page.canvas.drawText("CRAFTFOLIO / MOSS SCARF", 40f, 80f, paint)
                    paint.textSize = 18f
                    page.canvas.drawText("Page ${index + 1}: Knit 1, purl 1. Repeat to end.", 40f, 150f, paint)
                    page.canvas.drawText("Original test pattern. CC0. 200 g wool.", 40f, 200f, paint)
                    pdf.finishPage(page)
                }
                source.outputStream().use { pdf.writeTo(it) }
            } finally { pdf.close() }
            val originalBytes = source.readBytes()
            repo.import(Uri.fromFile(source), "Knitting", false)
            val pattern = repo.dao.patterns().first().single()
            val id = repo.create(pattern, "Moss stitch scarf", "200 g wool · 5 mm needles", false)
            val c = repo.dao.counters().first().single()
            repeat(42) { repo.change(c.id, 1) }; repo.change(c.id, -1); repo.undo(c.id)
            repo.note(id, "Start the next repeat on row 43.")
            repo.dao.position(id, 1, 2f, .1f, .15f, System.currentTimeMillis())
            source.delete()
            db.close()
            db = Room.databaseBuilder(context, CraftDatabase::class.java, "journey-test.db").build(); repo = Repository(context, db)
            assertArrayEquals(originalBytes, File(pattern.path).readBytes())
            assertEquals(42, repo.dao.counter(c.id)!!.value)
            assertEquals(1, repo.dao.project(id)!!.page)
            assertEquals("Start the next repeat on row 43.", repo.dao.notes().first().single().text)
            val render = renderPattern(pattern, 1)
            assertEquals(3, render.pages); assertTrue(render.bitmap.width > 0)
            File(pattern.path).delete()
        } finally { db.close(); context.deleteDatabase("journey-test.db") }
        Unit
    }
    @Test fun corruptImportDoesNotCreateLibraryRecord() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, CraftDatabase::class.java).build()
        try {
            val source = File(context.cacheDir, "invalid.pdf").apply { writeText("not a pattern") }
            val repo = Repository(context, db)
            assertTrue(runCatching { repo.import(Uri.fromFile(source), "Other", false) }.isFailure)
            assertEquals(0, repo.dao.patternCount()); source.delete()
        } finally { db.close() }
        Unit
    }
}
