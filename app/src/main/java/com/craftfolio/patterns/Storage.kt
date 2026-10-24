package com.craftfolio.patterns

import android.content.Context
import android.net.Uri
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Entity(indices = [Index("title"), Index("craft")])
data class Pattern(@PrimaryKey val id: String, val title: String, val craft: String,
    val path: String, val mime: String, val bytes: Long, val imported: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val page: Int = 0,
    @ColumnInfo(defaultValue = "1.0") val zoom: Float = 1f,
    @ColumnInfo(defaultValue = "0.0") val panX: Float = 0f,
    @ColumnInfo(defaultValue = "0.0") val panY: Float = 0f)
@Entity(foreignKeys = [ForeignKey(entity = Pattern::class, parentColumns = ["id"], childColumns = ["patternId"], onDelete = ForeignKey.RESTRICT)], indices = [Index("patternId")])
data class Project(@PrimaryKey val id: String, val patternId: String, val name: String,
    val status: String = "ACTIVE", val materials: String = "", val page: Int = 0,
    val zoom: Float = 1f, val panX: Float = 0f, val panY: Float = 0f,
    val touched: Long = System.currentTimeMillis())
@Entity(foreignKeys = [ForeignKey(entity = Project::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("projectId")])
data class Counter(@PrimaryKey val id: String, val projectId: String, val name: String, val value: Int = 0)
@Entity(foreignKeys = [ForeignKey(entity = Counter::class, parentColumns = ["id"], childColumns = ["counterId"], onDelete = ForeignKey.CASCADE)], indices = [Index("counterId")])
data class CounterChange(@PrimaryKey(autoGenerate = true) val id: Long = 0, val counterId: String, val before: Int)
@Entity(foreignKeys = [ForeignKey(entity = Project::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("projectId")])
data class Note(@PrimaryKey val id: String, val projectId: String, val text: String, val created: Long = System.currentTimeMillis())

@Dao
interface CraftDao {
    @Query("SELECT * FROM Pattern ORDER BY imported DESC") fun patterns(): Flow<List<Pattern>>
    @Query("SELECT * FROM Project ORDER BY touched DESC") fun projects(): Flow<List<Project>>
    @Query("SELECT * FROM Counter ORDER BY rowid") fun counters(): Flow<List<Counter>>
    @Query("SELECT * FROM Note ORDER BY created DESC") fun notes(): Flow<List<Note>>
    @Query("SELECT * FROM Project WHERE id=:id") suspend fun project(id: String): Project?
    @Query("SELECT * FROM Counter WHERE id=:id") suspend fun counter(id: String): Counter?
    @Query("SELECT COUNT(*) FROM Pattern") suspend fun patternCount(): Int
    @Query("SELECT COUNT(*) FROM Project WHERE status!='COMPLETED'") suspend fun unfinishedCount(): Int
    @Query("SELECT COUNT(*) FROM Counter WHERE projectId=:id") suspend fun counterCount(id: String): Int
    @Insert suspend fun insert(pattern: Pattern)
    @Insert suspend fun insert(project: Project)
    @Insert suspend fun insert(counter: Counter)
    @Insert suspend fun insert(change: CounterChange)
    @Insert suspend fun insert(note: Note)
    @Update suspend fun update(project: Project)
    @Query("UPDATE Counter SET value=:value WHERE id=:id") suspend fun setCounter(id: String, value: Int)
    @Query("SELECT * FROM CounterChange WHERE counterId=:id ORDER BY id DESC LIMIT 1") suspend fun lastChange(id: String): CounterChange?
    @Query("DELETE FROM CounterChange WHERE id=:id") suspend fun deleteChange(id: Long)
    @Query("UPDATE Project SET touched=:time WHERE id=:id") suspend fun touch(id: String, time: Long)
    @Query("UPDATE Project SET page=:page, zoom=:zoom, panX=:x, panY=:y, touched=:time WHERE id=:id")
    suspend fun position(id: String, page: Int, zoom: Float, x: Float, y: Float, time: Long)
    @Query("UPDATE Pattern SET page=:page, zoom=:zoom, panX=:x, panY=:y WHERE id=:id")
    suspend fun patternPosition(id: String, page: Int, zoom: Float, x: Float, y: Float)
}
@Database(entities = [Pattern::class, Project::class, Counter::class, CounterChange::class, Note::class], version = 2, exportSchema = true)
abstract class CraftDatabase : RoomDatabase() { abstract fun dao(): CraftDao }

val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Pattern ADD COLUMN page INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE Pattern ADD COLUMN zoom REAL NOT NULL DEFAULT 1.0")
        db.execSQL("ALTER TABLE Pattern ADD COLUMN panX REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE Pattern ADD COLUMN panY REAL NOT NULL DEFAULT 0.0")
    }
}

object Rules {
    fun next(value: Int, delta: Int): Int = (value.toLong() + delta).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
    fun premium(until: Long, checked: Long, now: Long): Boolean = checked > 0 && now >= checked && now < until && now - checked < 72 * 60 * 60 * 1000L
}

class Repository(val context: Context, val db: CraftDatabase) {
    val dao = db.dao()
    private val imports = Mutex()
    suspend fun import(uri: Uri, craft: String, premium: Boolean) = withContext(Dispatchers.IO) { imports.withLock {
        check(premium || dao.patternCount() < 20) { "Free library limit reached. Your existing files remain available." }
        val id = UUID.randomUUID().toString()
        val dir = File(context.filesDir, "patterns").apply { mkdirs() }
        val temporary = File(dir, "$id.part")
        val destination = File(dir, id)
        try {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "Cannot read selected file" }
                FileOutputStream(temporary).use { out ->
                    val buffer = ByteArray(65536); var total = 0L
                    while (true) { val n = input.read(buffer); if (n < 0) break
                        total += n; require(total <= 100L * 1024 * 1024) { "Pattern exceeds 100 MB" }; out.write(buffer, 0, n) }
                    out.fd.sync()
                }
            }
            val magic = ByteArray(8).also { bytes -> temporary.inputStream().use { it.read(bytes) } }
            val pdf = magic.take(5).toByteArray().contentEquals("%PDF-".toByteArray())
            val mime = if (pdf) {
                PdfRenderer(ParcelFileDescriptor.open(temporary, ParcelFileDescriptor.MODE_READ_ONLY)).use { require(it.pageCount > 0) }; "application/pdf"
            } else {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(temporary.path, options)
                require(options.outWidth > 0 && options.outHeight > 0 && options.outMimeType in listOf("image/jpeg", "image/png", "image/webp")) { "Choose a valid PDF, JPEG, PNG or WebP pattern" }
                options.outMimeType
            }
            require(temporary.renameTo(destination)) { "Could not preserve imported file" }
            val directoryFd = android.system.Os.open(dir.path, android.system.OsConstants.O_RDONLY, 0)
            try { android.system.Os.fsync(directoryFd) } finally { android.system.Os.close(directoryFd) }
            var title = uri.lastPathSegment?.substringBeforeLast('.')?.take(180)?.ifBlank { "Pattern" } ?: "Pattern"
            if (uri.scheme == "content") context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) title = it.getString(0).substringBeforeLast('.').take(180).ifBlank { "Pattern" }
            }
            dao.insert(Pattern(id, title, craft, destination.path, mime, destination.length()))
        } catch (e: Exception) { temporary.delete(); /* An unregistered final file is retained for recovery. */ throw e }
    } }
    suspend fun create(pattern: Pattern, name: String, materials: String, premium: Boolean): String = db.withTransaction {
        require(name.isNotBlank())
        check(premium || dao.unfinishedCount() < 3) { "Free plan supports three unfinished projects" }
        val id = UUID.randomUUID().toString()
        dao.insert(Project(id, pattern.id, name.trim(), materials = materials))
        dao.insert(Counter(UUID.randomUUID().toString(), id, "Rows")); id
    }
    suspend fun addCounter(projectId: String, name: String, premium: Boolean) = db.withTransaction {
        check(premium || dao.counterCount(projectId) < 1) { "Additional counters require Premium" }
        require(name.isNotBlank()); dao.insert(Counter(UUID.randomUUID().toString(), projectId, name.trim()))
    }
    suspend fun change(id: String, delta: Int) = db.withTransaction {
        val c = requireNotNull(dao.counter(id)); val next = Rules.next(c.value, delta)
        if (next != c.value) { dao.insert(CounterChange(counterId = id, before = c.value)); dao.setCounter(id, next); dao.touch(c.projectId, System.currentTimeMillis()) }
    }
    suspend fun undo(id: String) = db.withTransaction {
        dao.lastChange(id)?.let { dao.setCounter(id, it.before); dao.deleteChange(it.id) }
    }
    suspend fun edit(id: String, name: String, materials: String, status: String) = db.withTransaction {
        require(name.isNotBlank()); require(status in listOf("ACTIVE", "PAUSED", "COMPLETED"))
        val p = requireNotNull(dao.project(id)); dao.update(p.copy(name = name.trim(), materials = materials, status = status, touched = System.currentTimeMillis()))
    }
    suspend fun note(id: String, text: String) { require(text.isNotBlank()); dao.insert(Note(UUID.randomUUID().toString(), id, text.trim())) }
}
