package com.craftfolio.patterns

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Test fun versionOneRecordsAndHistorySurvive() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("migration-test.db")
        val schema = InstrumentationRegistry.getInstrumentation().context.assets.open("schema1.json").bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        context.openOrCreateDatabase("migration-test.db", 0, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                old.execSQL(entity.getString("createSql").replace("$" + "{TABLE_NAME}", entity.getString("tableName")))
                val indices = entity.getJSONArray("indices")
                for (j in 0 until indices.length()) old.execSQL(indices.getJSONObject(j).getString("createSql").replace("$" + "{TABLE_NAME}", entity.getString("tableName")))
            }
            val queries = schema.getJSONArray("setupQueries")
            for (i in 0 until queries.length()) old.execSQL(queries.getString(i))
            old.execSQL("INSERT INTO Pattern VALUES ('p','Scarf','Knitting','/private/original','application/pdf',100,1)")
            old.execSQL("INSERT INTO Project VALUES ('project','p','My scarf','PAUSED','200 g wool',2,2.0,0.1,0.2,1)")
            old.execSQL("INSERT INTO Counter VALUES ('counter','project','Rows',42)")
            old.execSQL("INSERT INTO CounterChange VALUES (1,'counter',41)")
            old.execSQL("INSERT INTO Note VALUES ('note','project','Continue ribbing',1)")
            old.version = 1
        }
        val db = Room.databaseBuilder(context, CraftDatabase::class.java, "migration-test.db").addMigrations(MIGRATION_1_2).build()
        try {
            val repo = Repository(context, db)
            assertEquals("/private/original", repo.dao.patterns().first().single().path)
            assertEquals(0, repo.dao.patterns().first().single().page)
            assertEquals(2, repo.dao.project("project")!!.page)
            assertEquals("PAUSED", repo.dao.project("project")!!.status)
            assertEquals("Continue ribbing", repo.dao.notes().first().single().text)
            assertEquals(42, repo.dao.counter("counter")!!.value)
            repo.undo("counter"); assertEquals(41, repo.dao.counter("counter")!!.value)
            repo.dao.patternPosition("p", 3, 2f, .1f, .2f)
            assertEquals(3, repo.dao.patterns().first().single().page)
        } finally { db.close(); context.deleteDatabase("migration-test.db") }
        Unit
    }
}
