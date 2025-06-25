package org.simtag.androidapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Database(entities = [SimTag::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun simTagDao(): SimTagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, coroutineScope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "your-db-name"
                )
                    .addCallback(AppDatabaseCallback(coroutineScope))
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.simTagDao())
                }
            }
        }

        suspend fun populateDatabase(simTagDao: SimTagDao) {
            val existingTags = simTagDao.getAllSimTags().firstOrNull()

            if (existingTags.isNullOrEmpty()) {
                val personalTag = SimTag(
                    tagName = "Personal",
                    fullName = "Your Name",
                    email = "your.email@example.com",
                    isDefaultPersonal = true
                )
                simTagDao.insertSimTag(personalTag)

                val workTag = SimTag(
                    tagName = "Work",
                    fullName = "Your Name",
                    email = "work.email@example.com",
                    isDefaultWork = true
                )
                simTagDao.insertSimTag(workTag)
            }
        }
    }
}