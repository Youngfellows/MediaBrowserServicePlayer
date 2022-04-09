package com.gmail.devu.study.mediabrowserservice

import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // The volume controls should adjust the music volume while in the app.
        //设置音频播放类型-音乐
        volumeControlStream = AudioManager.STREAM_MUSIC
    }
}
