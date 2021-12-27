package fr.phytok.apps.cachecast.model

import android.net.Uri
import fr.phytok.apps.cachecast.yas.Search
import fr.phytok.apps.cachecast.yas.Thumbnail
import java.time.Duration

data class TrackAppData(
    val id: String,
    val title: String?,
    val duration: Duration,
    val picture: Thumbnail?
)

fun Search.toTrack() : TrackAppData? = items.firstOrNull()?.let {
    TrackAppData(
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