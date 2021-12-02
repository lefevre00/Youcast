package fr.phytok.apps.youcast

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intent?.clipData
            ?.takeIf { it.itemCount>0 }
            ?.let {
                Log.i("BIDON", "Activity onCreate received intent ${it.getItemAt(0)?.text}")
            }
    }
}