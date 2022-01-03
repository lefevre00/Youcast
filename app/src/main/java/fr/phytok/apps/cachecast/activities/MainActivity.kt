package fr.phytok.apps.cachecast.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.LocalTrackRepository
import fr.phytok.apps.cachecast.R
import fr.phytok.apps.cachecast.model.Track
import fr.phytok.apps.cachecast.services.PermissionService
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

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            MdcTheme {
                drawPage()
            }
        }

        // TODO permission: ShareUrlActivity should controle write permission
        onStorageReadable {
            loadLocalTracks()
        }
    }

    @Composable
    private fun drawPage() {
        Column(modifier = Modifier
            .padding(10.dp)
            .border(BorderStroke(2.dp, Color.Black))) {

            Text(text = "Main activity",
                style = MaterialTheme.typography.subtitle1,
            )
            Spacer(modifier  = Modifier.padding(10.dp))
            Text( text = BuildConfig.SERVER,
                style = MaterialTheme.typography.subtitle2)
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
