package com.craftfolio.patterns

import android.content.Context
import android.graphics.Paint
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

/** Explicit test-only fixture; never bundled in the release application. Does not clear user data. */
@RunWith(AndroidJUnit4::class)
class StoreFixtureTest {
    @Test fun seedOriginalPatterns() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Context>() as CraftfolioApp
        val repo = app.repository
        val icon = requireNotNull(app.getDrawable(R.drawable.ic_craftfolio))
        listOf(114, 512).forEach { size ->
            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
            icon.setBounds(0, 0, size, size); icon.draw(android.graphics.Canvas(bitmap))
            File(app.filesDir, "store-icon-$size.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        if (repo.dao.patterns().first().any { it.title == "Moss stitch scarf" }) return@runBlocking
        val names = listOf("Moss stitch scarf", "Weekend crochet blanket", "Linen tote bag")
        names.forEachIndexed { index, name ->
            val source = File(app.cacheDir, "$name.pdf")
            val pdf = PdfDocument()
            try {
                repeat(3) { number ->
                    val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, number + 1).create())
                    val p = Paint().apply { color = Color.rgb(49, 93, 80); textSize = 16f }
                    page.canvas.drawText("CRAFTFOLIO  /  THE MAKER'S NOTEBOOK", 44f, 60f, p)
                    p.textSize = 32f; page.canvas.drawText(name, 44f, 120f, p)
                    p.textSize = 17f; p.color = Color.DKGRAY
                    val lines = if(index == 0) listOf("A simple texture. A little time for yourself.", "", "YOU WILL NEED", "200 g worsted wool / 5 mm needles", "", "MOSS STITCH", "Cast on an even number of stitches.", "Row 1: *K1, P1; repeat from * to end.", "Row 2: *P1, K1; repeat from * to end.", "Repeat these two rows to your chosen length.", "Cast off loosely. Weave in ends.")
                    else listOf("A creative project for a quiet weekend.", "", "PLANNING WORKSHEET", "Choose your materials and record them in your project.", "Make a sample before beginning your final piece.", "Record your changes in project notes.")
                    lines.forEachIndexed { n, line -> page.canvas.drawText(line, 44f, 180f + n * 30, p) }
                    p.textSize = 12f; page.canvas.drawText("Original Craftfolio sample - CC0 / Page ${number + 1}", 44f, 790f, p)
                    pdf.finishPage(page)
                }
                source.outputStream().use { pdf.writeTo(it) }
            } finally { pdf.close() }
            repo.import(Uri.fromFile(source), listOf("Knitting", "Crochet", "Sewing")[index], false)
            source.delete()
        }
        val pattern = repo.dao.patterns().first().first { it.title == names[0] }
        val id = repo.create(pattern, "My autumn scarf", "200 g forest green wool\n5 mm knitting needles", false)
        val counter = repo.dao.counters().first().first { it.projectId == id }
        repeat(42) { repo.change(counter.id, 1) }
        repo.note(id, "Finished the first repeat. Start the next row with a purl stitch.")
        repo.dao.position(id, 1, 1f, 0f, 0f, System.currentTimeMillis())
        assertEquals(42, repo.dao.counter(counter.id)!!.value)
    }
}
