package com.luxwallet.app.integration

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.luxwallet.app.core.database.LuxDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DatabaseMigrationTest {
    @Test fun versionOneDataSurvivesMigration() = migrate(1)
    @Test fun versionTwoDataSurvivesMigration() = migrate(2)
    private fun migrate(version: Int) = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val stream = javaClass.classLoader!!.getResourceAsStream("com.luxwallet.app.core.database.LuxDatabase/$version.json")!!
        val schema = stream.bufferedReader().use { Json.parseToJsonElement(it.readText()).jsonObject["database"]!!.jsonObject }
        val config = SupportSQLiteOpenHelper.Configuration.builder(context).name(name).callback(object : SupportSQLiteOpenHelper.Callback(version) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                schema["entities"]!!.jsonArray.forEach { element ->
                    val entity = element.jsonObject
                    val table = entity["tableName"]!!.jsonPrimitive.content
                    db.execSQL(entity["createSql"]!!.jsonPrimitive.content.replace("\${TABLE_NAME}", table))
                    entity["indices"]!!.jsonArray.forEach { index ->
                        db.execSQL(index.jsonObject["createSql"]!!.jsonPrimitive.content.replace("\${TABLE_NAME}", table))
                    }
                }
                schema["setupQueries"]!!.jsonArray.forEach { db.execSQL(it.jsonPrimitive.content) }
                db.execSQL("INSERT INTO notification_observations (id,sourceApp,packageName,notificationKey,title,text,postedAt,receivedAt,rawPayloadHash,parserVersion,parseStatus) VALUES (1,'MYBCA','com.bca.mybca','key','Catatan Finansial','Pengeluaran IDR 3.00',100,100,'hash',1,'PENDING')")
            }
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }).build()
        FrameworkSQLiteOpenHelperFactory().create(config).let { helper -> helper.writableDatabase; helper.close() }
        val db = Room.databaseBuilder(context, LuxDatabase::class.java, name).addMigrations(LuxDatabase.MIGRATION_1_2, LuxDatabase.MIGRATION_2_3).allowMainThreadQueries().build()
        try {
            val observation = db.notificationObservationDao().getById(1)!!
            assertEquals("Pengeluaran IDR 3.00", observation.text)
            assertNull(observation.eventTime)
            assertNull(observation.contentHash)
            assertTrue(db.paydayPlanDao().getAllOnce().isEmpty())
            assertEquals(3, db.openHelper.readableDatabase.version)
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
