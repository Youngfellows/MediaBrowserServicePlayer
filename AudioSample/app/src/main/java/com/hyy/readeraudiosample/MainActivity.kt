package com.hyy.readeraudiosample

import android.content.ComponentName
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButtonToggleGroup
import com.hyy.readeraudiosample.databinding.ActivityMainBinding
import com.hyy.readeraudiosample.model.NowPlayingMetadata

class MainActivity : AppCompatActivity() {

    /**
     * 常量
     */
    companion object {
        const val TAG = "MainActivity"
        const val SPEED_RATE_HALF = 0.5f
        const val SPEED_RATE_NORMAL = 1f
        const val SPEED_RATE_FASTER = 1.5f
        const val SPEED_RATE_FASTEST = 2f
    }

    /**
     * 开始跟踪触摸进度条
     */
    private var startTrackingTouch: Boolean = false

    /**
     * 绑定数据的dataBinding
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * 携带被观察数据的ViewModel
     */
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this, MainViewModel.Factory(
                application,
                MusicServiceConnection.getInstance(
                    applicationContext,
                    ComponentName(applicationContext, ReaderAudioService::class.java)//媒体浏览器服务
                )
            )
        ).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ensureView()
        ensureSubscribe()
    }

    private fun ensureView() {
        /* binding.ibPlay.setOnClickListener {
             viewModel.playMediaId()
         }*/
        /* binding.ibPlay.setOnClickListener(object : View.OnClickListener{
             override fun onClick(v: View?) {
                 viewModel.playMediaId()
             }
         })*/

        binding.ibPlay.setOnClickListener(View.OnClickListener {
            viewModel.playMediaId()//播放
        })

        binding.ibPlayForward.setOnClickListener {
            viewModel.fastForward()//快进
        }
        binding.ibPlayRewind.setOnClickListener {
            viewModel.fastRewind()//快退
        }

        //进度条监听
        binding.sbTimeline.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                startTrackingTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startTrackingTouch = false
                seekBar?.run {
                    viewModel.seekToPosition(progress)
                }
            }

        })

        binding.playerRateGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                R.id.rate_half -> {
                    viewModel.changePlaybackRate(SPEED_RATE_HALF)
                }
                R.id.rate_normal -> {
                    viewModel.changePlaybackRate(SPEED_RATE_NORMAL)
                }
                R.id.rate_one_half -> {
                    viewModel.changePlaybackRate(SPEED_RATE_FASTER)
                }
                R.id.rate_two -> {
                    viewModel.changePlaybackRate(SPEED_RATE_FASTEST)
                }
            }
        }

        /*  binding.playerRateGroup.addOnButtonCheckedListener(object :
              MaterialButtonToggleGroup.OnButtonCheckedListener {
              override fun onButtonChecked(
                  group: MaterialButtonToggleGroup?,
                  checkedId: Int,
                  isChecked: Boolean
              ) {
                  when (checkedId) {
                      R.id.rate_half -> {
                          viewModel.changePlaybackRate(SPEED_RATE_HALF)
                      }
                      R.id.rate_normal -> {
                          viewModel.changePlaybackRate(SPEED_RATE_NORMAL)
                      }
                      R.id.rate_one_half -> {
                          viewModel.changePlaybackRate(SPEED_RATE_FASTER)
                      }
                      R.id.rate_two -> {
                          viewModel.changePlaybackRate(SPEED_RATE_FASTEST)
                      }
                  }
              }
          })*/

    }

    private fun ensureSubscribe() {
        viewModel.mediaButtonRes.observe(this) { res ->
            binding.ibPlay.setImageResource(res)
        }

        viewModel.playbackPosition.observe(this) { position ->
            binding.tvPlayerProgress.text = NowPlayingMetadata.timestampToMSS(this, position)
            if (startTrackingTouch) return@observe
            binding.sbTimeline.progress = position.toSeconds
        }

        viewModel.mediaMetadata.observe(this) { nowPlayingMetaData ->
            binding.tvDuration.text = nowPlayingMetaData.durationStr
            binding.sbTimeline.max = nowPlayingMetaData.totalDuration
        }
    }


    public override fun onResume() {
        super.onResume()
        //设置音频类型-Music
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

}