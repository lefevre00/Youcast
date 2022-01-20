package fr.phytok.apps.cachecast.db

import android.content.Context
import androidx.room.*
import java.time.Instant
import java.util.*

@Database(entities = [Video::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase() : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "cachecast"
                    ).build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}

@Entity
data class Video(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "video_id") val videoId: String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    @ColumnInfo(name = "percent_read") val percentRead: Int = 0,
    @ColumnInfo(name = "creation_date") val creationDate: Date = Date.from(Instant.now()),
    @ColumnInfo(name = "play_date") val playDate: Date? = null
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}


@Dao
interface VideoDao {

    @Query("SELECT * FROM video")
    fun getAll(): List<Video>

    @Insert
    fun insert(video: Video)

    @Query("SELECT * FROM video WHERE video_id = :videoId limit 1")
    fun firstByVideoId(videoId: String): Video?

}