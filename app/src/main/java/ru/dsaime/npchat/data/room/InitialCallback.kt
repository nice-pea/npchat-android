package ru.dsaime.npchat.data.room

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope
import java.time.Instant

class InitialCallback(
    private val koinScope: Scope,
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val now = Instant.now().epochSecond
            val dao = koinScope.get<AppDatabase>().hostDao()
            listOf(
                SavedHost(baseUrl = "https://api.npchat.dsaime.ru:443", lastUsedAt = now),
                SavedHost(baseUrl = "http://localhost:8080", lastUsedAt = now - 1),
            ).forEach { dao.upsert(it) }
        }
    }
}
