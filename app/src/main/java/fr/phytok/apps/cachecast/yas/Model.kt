package fr.phytok.apps.cachecast.yas

data class Search(
    val items: List<VideoMeta>
)

data class VideoMeta(
    var id: String? = "",
    var snippet: VideoSnippet? = null,
    val contentDetails: VideoDetails? = null,
)

data class VideoSnippet(
    // format "2008-04-20T22:17:15Z"
    var publishedAt: String = "",
    var title: String = "",
    var thumbnails: Map<String, Thumbnail>? = null
)

data class Thumbnail(
    var url: String = "",
    var width: Int = 0,
    var height: Int = 0,
)

data class VideoDetails(
    // format "PT4M49S"
    var duration : String? = null
)
