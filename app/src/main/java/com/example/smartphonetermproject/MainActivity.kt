package com.example.smartphonetermproject

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.smartphonetermproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var titleMusic: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        if (titleMusic == null) {
            titleMusic = MediaPlayer.create(this, R.raw.title)?.apply {
                isLooping = true
                start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        titleMusic?.release()
        titleMusic = null
    }

    fun onStartGameClicked(view: View) {
        Log.d(javaClass.simpleName, "Start Game")
        startActivity(Intent(this, SkyBlasterActivity::class.java))
    }
}