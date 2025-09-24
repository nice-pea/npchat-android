package ru.dsaime.npchat.data.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Database(entities = [Session::class, Host::class], version = 1)
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
    @Query("SELECT * FROM Host")
    suspend fun getAll(): List<Host>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg hosts: Host)

    @Delete
    suspend fun delete(host: Host)
}

@Entity
data class Host(
    @PrimaryKey @ColumnInfo("base_url") val baseUrl: String,
    @ColumnInfo("last_used_at") val lastUsedAt: String = OffsetDateTime.now().toString(),
) {
    val lastUsed: OffsetDateTime
        get() = OffsetDateTime.parse(lastUsedAt)
}
