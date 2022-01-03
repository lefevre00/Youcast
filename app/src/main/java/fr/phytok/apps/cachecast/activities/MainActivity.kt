package fr.phytok.apps.cachecast.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.LocalTrackRepository
import fr.phytok.apps.cachecast.R
import fr.phytok.apps.cachecast.activities.MainActivity.Companion.TAG
import fr.phytok.apps.cachecast.model.Track
import fr.phytok.apps.cachecast.services.PermissionService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var localTrackRepository: LocalTrackRepository
    @Inject
    lateinit var permissionService: PermissionService

    private val trackList = mutableListOf<Track>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val model: MainViewModel by viewModels()


        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            MdcTheme {
                drawPage(model)
            }
        }

        // TODO permission: ShareUrlActivity should controle write permission
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

            val size = model.getTracks().value?.size ?: 0

            Spacer(modifier  = Modifier.padding(10.dp))

            val isLoading by remember { model.loading }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text( text = "$size elements found",
                    style = MaterialTheme.typography.subtitle2)
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
        trackList.clear()
        trackList.addAll(localTrackRepository.searchTrack())
        Log.d(TAG, "Found ${trackList.size} tracks")
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

    companion object {
        const val TAG = "MainActivity"
    }
}

class MainViewModel : ViewModel() {

    val loading = mutableStateOf(true)

    private val tracks: MutableLiveData<List<Track>> by lazy {
        MutableLiveData<List<Track>>().also {
            loadTracks()
        }
    }

    private fun loadTracks() {
        Executors.newScheduledThreadPool(1) // schedule another request for 2 seconds later
            .schedule({
                Thread.sleep(3000)
                Log.d(TAG, "Finish loading")
                loading.value = false
        }, 2, TimeUnit.SECONDS)
    }

    fun getTracks(): LiveData<List<Track>> = tracks
}
