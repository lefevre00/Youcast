package fr.phytok.apps.cachecast.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.serverValue).setText(BuildConfig.SERVER)
    }

    // TODO load list of audio file download
}