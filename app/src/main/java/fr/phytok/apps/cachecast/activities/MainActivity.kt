package fr.phytok.apps.cachecast.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.LocalTrackRepository
import fr.phytok.apps.cachecast.R
import fr.phytok.apps.cachecast.model.Track
import fr.phytok.apps.cachecast.services.PermissionService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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
                drawPage(model)
            }
        }

        onStorageReadable {
            loadLocalTracks()
        }
    }

    @Composable
    private fun drawPage(model: MainViewModel) {

        Column(modifier = Modifier
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
                LazyColumn {
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

    private @Composable
    fun TrackCard(track: Track) {
        Row {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Track preview",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(5))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = track.name, style = MaterialTheme.typography.h5)
                Text(text = "${track.duration}s",style = MaterialTheme.typography.body1)
            }
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

@HiltViewModel
class MainViewModel
    @Inject constructor(
        private val localTrackRepository: LocalTrackRepository
    ) : ViewModel() {

    val loading = mutableStateOf(true)

    private val myTracks = mutableStateListOf<Track>()

    fun loadTracks() {
        Executors.newScheduledThreadPool(1) // schedule another request for 2 seconds later
            .schedule({
                Log.d(TAG, "Start loading")
                myTracks.clear()
                myTracks.addAll(localTrackRepository.searchTrack())
                Log.d(TAG, "Found ${myTracks.size} track(s)")
                loading.value = false
                Log.d(TAG, "Finish loading")
        }, 2, TimeUnit.SECONDS)
    }

    fun getTracks(): List<Track> = myTracks

    companion object {
        private const val TAG = "MainViewModel"
    }
}
