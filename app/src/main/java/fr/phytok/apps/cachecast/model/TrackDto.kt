package fr.phytok.apps.cachecast.model

import android.net.Uri
import fr.phytok.apps.cachecast.db.Video
import fr.phytok.apps.cachecast.yas.Search
import fr.phytok.apps.cachecast.yas.Thumbnail
import java.time.Duration

data class TrackDto(
    val id: String,
    val title: String?,
    val duration: Duration,
    val picture: Thumbnail?
) {
    fun toDbTrack() = Video(videoId = id, thumbnailUrl = picture?.url)
}

fun Search.toTrack() : TrackDto? = items.firstOrNull()?.let {
    TrackDto(
        id = it.id!!,
        title = it.snippet?.title,
        Duration.parse(it.contentDetails?.duration),
        it.snippet?.thumbnails?.asIterable()?.firstOrNull()?.value
    )
}


// Container for information about each tracks.
data class Track(val uri: Uri,
                 val name: String,
                 val duration: Int,
                 val size: Int
)