package com.craftfolio.patterns

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import android.app.Application
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class, manifest = Config.NONE)
class PersistenceTest {
    private lateinit var context: Context
    private lateinit var db: CraftDatabase
    private lateinit var repo: Repository
    @Before fun setup() { context = ApplicationProvider.getApplicationContext(); context.deleteDatabase("test.db"); open() }
    private fun open() { db = Room.databaseBuilder(context, CraftDatabase::class.java, "test.db").build(); repo = Repository(context, db) }
    @After fun cleanup() { db.close(); context.deleteDatabase("test.db") }
    @Test fun rapidCountersUndoAndReopen() = runBlocking {
        val pattern = Pattern("p", "Moss stitch scarf", "Knitting", "/private/pattern", "application/pdf", 100)
        repo.dao.insert(pattern)
        val id = repo.create(pattern, "Winter scarf", "200 g wool", false, "Rows")
        val counter = repo.dao.counters().first().single()
        coroutineScope { repeat(100) { launch(Dispatchers.IO) { repo.change(counter.id, 1) } } }
        repo.change(counter.id, -1); repo.undo(counter.id)
        repo.note(id, "Begin ribbing next")
        repo.dao.position(id, 2, 2f, .1f, -.2f, 1000)
        repo.edit(id, "Winter scarf", "200 g wool", "PAUSED")
        db.close(); open()
        assertEquals(100, repo.dao.counter(counter.id)!!.value)
        assertEquals(2, repo.dao.project(id)!!.page)
        assertEquals(2f, repo.dao.project(id)!!.zoom)
        assertEquals("PAUSED", repo.dao.project(id)!!.status)
        assertEquals("Begin ribbing next", repo.dao.notes().first().single().text)
        repo.undo(counter.id); assertEquals(99, repo.dao.counter(counter.id)!!.value)
    }
    @Test fun freeLimitsPreserveExistingProjects() = runBlocking {
        val p = Pattern("p", "Scarf", "Knitting", "/private/p", "application/pdf", 10); repo.dao.insert(p)
        val first = repo.create(p, "One", "", false, "Rows")
        repo.create(p, "Two", "", false, "Rows"); repo.create(p, "Three", "", false, "Rows")
        assertTrue(runCatching { repo.create(p, "Four", "", false, "Rows") }.isFailure)
        assertTrue(runCatching { repo.addCounter(first, "Rounds", false) }.isFailure)
        repo.addCounter(first, "Rounds", true)
        val extra = repo.dao.counters().first().last(); repo.change(extra.id, 1)
        assertEquals(1, repo.dao.counter(extra.id)!!.value)
        repo.edit(first, "One", "", "COMPLETED"); repo.create(p, "Four", "", false, "Rows")
        Unit
    }
}
