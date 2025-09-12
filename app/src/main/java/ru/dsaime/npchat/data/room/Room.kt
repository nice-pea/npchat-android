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
import java.time.OffsetDateTime

@Database(entities = [Session::class, Host::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    abstract fun hostDao(): HostDao
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM Session")
    suspend fun getAll(): List<Session>

    @Query("SELECT * FROM Session WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<Session>

    @Insert
    suspend fun insertAll(vararg sessions: Session)

    @Delete
    suspend fun delete(session: Session)
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
)

@Dao
interface HostDao {
    @Query("SELECT * FROM Host")
    suspend fun getAll(): List<Host>

//    @Query("SELECT * FROM Host WHERE base_url IN (:ids)")
//    fun loadAllByIds(ids: IntArray): List<Host>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg hosts: Host)

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
