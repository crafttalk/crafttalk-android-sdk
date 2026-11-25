package com.crafttalk.chat.data.local.db

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.crafttalk.chat.data.local.db.database.ChatDatabase
import com.crafttalk.chat.data.local.db.entity.MessageMetaEntity
import com.crafttalk.chat.di.modules.init.DBModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.UUID

class ChatDatabaseMigrationTest {
    @Test fun upgradingFromVersion10KeepsHistoryAndAllowsNewMetadata() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = MigrationTestContext(instrumentation.targetContext)
        val name = "chat.db"
        fun withRoom(readOrWrite: (ChatDatabase) -> Unit) {
            val db = DBModule().provideChatDatabase(context)
            try {
                readOrWrite(db)
            } finally {
                db.close()
            }
        }

        try {
            context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { legacy ->
                val schema = instrumentation.context.assets.open("chat-v10.sql")
                    .bufferedReader().use { it.readText() }
                schema.split(';').filter { it.isNotBlank() }.forEach { legacy.execSQL(it) }
                legacy.execSQL("""
                    INSERT INTO messages (uuid, id, is_reply, message_type, timestamp, message,
                        span_structure_list, replied_message_span_structure_list, replied_message_id,
                        attachment_name, dialog_id) VALUES
                        ('visitor', 'question', 0, 1, 100, 'Question', '[]', '[]', NULL, NULL, 'dialog'),
                        ('visitor', 'answer', 1, 1, 200, 'Answer', '[]', '[]', 'question', 'help.pdf', 'dialog')
                """.trimIndent())
                legacy.execSQL("INSERT INTO persons (person_id, person_preview) VALUES ('agent', 'avatar.png')")
                legacy.execSQL("INSERT INTO files (uuid, file_name) VALUES ('visitor', 'help.pdf')")
                legacy.version = 10
            }

            withRoom { db ->
                val dao = db.messageDao()
                val history = dao.getAllMessages()
                assertEquals(11, db.openHelper.writableDatabase.version)
                assertEquals(listOf("answer", "question"), history.map { it.id })
                assertEquals(listOf("Answer", "Question"), history.map { it.message })
                assertEquals(listOf(200L, 100L), history.map { it.timestamp })
                val (answer, question) = history
                assertTrue(answer.isReply)
                assertFalse(question.isReply)
                assertEquals(question.id, answer.repliedMessageId)
                assertEquals("help.pdf", answer.attachmentName)
                assertEquals("dialog", answer.dialogId)
                assertEquals("visitor", question.uuid)
                assertNull(answer.meta)
                assertNull(question.meta)
                assertEquals("avatar.png", db.personDao().getPersonPreview("agent"))
                assertEquals(listOf("help.pdf"), db.fileDao().getFilesNames())

                dao.insertMessage(answer.copy(id = "new-answer", meta = MessageMetaEntity(skipScore = "true")))
            }
            withRoom { db ->
                assertEquals("true", db.messageDao().getMessageById("new-answer")?.meta?.skipScore)
            }
        } finally {
            SQLiteDatabase.deleteDatabase(context.getDatabasePath(name))
        }
    }
}

private class MigrationTestContext(base: Context) : ContextWrapper(base) {
    private val prefix = "migration-${UUID.randomUUID()}-"

    override fun getApplicationContext(): Context = this
    override fun getDatabasePath(name: String): File = baseContext.getDatabasePath(prefix + name)

    // Older Android versions open SQLite files through the context instead of getDatabasePath.
    override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase =
        openOrCreateDatabase(name, mode, factory, null)

    override fun openOrCreateDatabase(
        name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase = baseContext.openOrCreateDatabase(prefix + name, mode, factory, errorHandler)
}
