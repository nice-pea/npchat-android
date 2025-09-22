package ru.dsaime.npchat.data.room

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope

class InitialCallback(
    private val koinScope: Scope,
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            koinScope.get<AppDatabase>().hostDao().upsert(
                Host(baseUrl = "http://localhost:8080"),
                Host(baseUrl = "https://api.npchat.dsaime.ru:443"),
            )
        }
    }
}
