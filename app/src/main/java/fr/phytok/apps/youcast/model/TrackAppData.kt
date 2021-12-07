package fr.phytok.apps.youcast.model

import fr.phytok.apps.youcast.yas.Search
import fr.phytok.apps.youcast.yas.Thumbnail
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