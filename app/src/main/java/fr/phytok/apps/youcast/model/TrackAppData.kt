package fr.phytok.apps.youcast.model

import fr.phytok.apps.youcast.yas.Thumbnail
import java.time.Duration

data class TrackAppData(
    val id: String,
    val title: String,
    val duration: Duration,
    val picture: Thumbnail
)