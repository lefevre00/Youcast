package fr.phytok.apps.cachecast.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.R
import fr.phytok.apps.cachecast.model.Track
import fr.phytok.apps.cachecast.services.PermissionService
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var permissionService: PermissionService

    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            MdcTheme {
                DrawPage(model)
            }
        }

        onStorageReadable {
            loadLocalTracks()
        }
    }

    override fun onResume() {
        super.onResume()
        loadLocalTracks()
    }

    @Composable
    private fun DrawPage(model: MainViewModel) {

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .border(BorderStroke(2.dp, Color.Black))) {

            Text(
                text = "Main activity",
                style = MaterialTheme.typography.subtitle1,
            )

            Spacer(modifier  = Modifier.padding(10.dp))
            Text( text = BuildConfig.SERVER,
                style = MaterialTheme.typography.subtitle2)


            Spacer(modifier  = Modifier.padding(10.dp))

            val isLoading by remember { model.loading }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                val tracks = model.getTracks()
                LazyColumn(Modifier.fillMaxWidth()) {
                    //header
                    item {
                        Text( text = "${tracks.size} element(s)",
                            style = MaterialTheme.typography.subtitle2)
                    }
                    // body
                    items(tracks) {
                        track -> TrackCard(track)
                    }
                }
            }
        }
    }

    @Composable
    private fun TrackCard(track: Track) {
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            val previewShape = RoundedCornerShape(5)
            // TODO: use track preview, maybe cached
            Image(
                painter = painterResource(android.R.drawable.btn_star_big_on),
                contentDescription = "Track preview",
                modifier = Modifier
                    .size(40.dp)
                    .clip(previewShape)
                    .border(1.5.dp, MaterialTheme.colors.secondary, previewShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = track.title, style = MaterialTheme.typography.h5, overflow = TextOverflow.Ellipsis, maxLines = 1)
                Text(text = "${track.duration}s",style = MaterialTheme.typography.body1)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(android.R.drawable.ic_media_play),
                contentDescription = "Actions",
                modifier = Modifier
                    .size(40.dp)
                    .clip(previewShape)
                    .border(1.5.dp, MaterialTheme.colors.secondary, previewShape)
            )
        }
    }

    private fun onStorageReadable(block: () -> Unit) {
        if (permissionService.canReadStorage()) {
            block()
        } else {
            permissionService.askReadStorage(this)
        }
    }

    private fun loadLocalTracks() {
        model.loadTracks()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionService.handleResponse(requestCode, grantResults) {
            loadLocalTracks()
        }
    }
}
