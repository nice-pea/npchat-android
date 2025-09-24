package ru.dsaime.npchat.data.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import ru.dsaime.npchat.model.Host
import java.time.OffsetDateTime

@Database(entities = [Session::class, SavedHost::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    abstract fun hostDao(): HostDao
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM Session LIMIT 1")
    suspend fun last(): Session?

    @Query("SELECT * FROM Session LIMIT 1")
    fun lastFlow(): Flow<Session?>

    @Insert
    suspend fun upsert(session: Session) {
        deleteAll()
        insert(session)
    }

    @Query("DELETE FROM Session")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(session: Session)
}

@Entity
data class Session(
    @PrimaryKey @ColumnInfo("id") val id: String,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("status") val status: String,
    @ColumnInfo("refresh_token") val refreshToken: String,
    @ColumnInfo("refresh_token_expires_at") val refreshTokenExpiresAt: String,
    @ColumnInfo("access_token") val accessToken: String,
    @ColumnInfo("access_token_expires_at") val accessTokenExpiresAt: String,
//    @ColumnInfo("last_used_at") val lastUsedAt: Long = OffsetDateTime.now().toEpochSecond(),
) {
//    val lastUsed: OffsetDateTime
//        get() = Instant.ofEpochSecond(lastUsedAt).atOffset(OffsetDateTime.now().offset)

    fun toModel() =
        ru.dsaime.npchat.model.Session(
            id = id,
            name = name,
            status = status,
            refreshToken = refreshToken,
            refreshTokenExpiresAt = OffsetDateTime.parse(refreshTokenExpiresAt),
            accessToken = accessToken,
            accessTokenExpiresAt = OffsetDateTime.parse(accessTokenExpiresAt),
        )

    constructor(session: ru.dsaime.npchat.model.Session) : this(
        id = session.id,
        name = session.name,
        status = session.status,
        refreshToken = session.refreshToken,
        refreshTokenExpiresAt = session.refreshTokenExpiresAt.toString(),
        accessToken = session.accessToken,
        accessTokenExpiresAt = session.accessTokenExpiresAt.toString(),
    )
}

@Dao
interface HostDao {
    @Query("SELECT * FROM SavedHost")
    suspend fun getAll(): List<SavedHost>

    @Query("SELECT * FROM SavedHost")
    fun getAllFlow(): Flow<List<SavedHost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(host: SavedHost)

    @Query("DELETE FROM SavedHost WHERE base_url = :url")
    suspend fun delete(url: String)
}

@Entity
data class SavedHost(
    @PrimaryKey @ColumnInfo("base_url") val baseUrl: String,
    @ColumnInfo("last_used_at") val lastUsedAt: Long,
    @ColumnInfo("status") val status: String = Host.Status.UNKNOWN.name,
) {
    constructor(host: Host, lastUsedAt: Long) : this(
        baseUrl = host.url,
        lastUsedAt = lastUsedAt,
        status = host.status.name,
    )

    fun toModel() =
        Host(
            url = baseUrl,
            status =
                Host.Status.entries
                    .find { it.name == status }
                    ?: Host.Status.UNKNOWN,
        )
}
