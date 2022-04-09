package com.gmail.devu.study.mediabrowserservice.ui

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gmail.devu.study.mediabrowserservice.entity.NowPlayingMetadata
import com.gmail.devu.study.mediabrowserservice.media.EMPTY_PLAYBACK_STATE
import com.gmail.devu.study.mediabrowserservice.media.MediaPlaybackServiceClient
import com.gmail.devu.study.mediabrowserservice.media.extensions.*

/**
 * 携带数据的媒体播放控制页ViewModel
 * @property app 上下文
 * @constructor
 * TODO
 *
 * @param mediaPlaybackServiceClient 媒体浏览器客户端
 */
class MediaPlaybackFragmentViewModel(
    private val app: Application,
    mediaPlaybackServiceClient: MediaPlaybackServiceClient
) : ViewModel() {

    companion object {
        private val TAG = MediaPlaybackFragmentViewModel::class.java.simpleName

        //时间间隔
        private const val POSITION_UPDATE_INTERVAL_MILLIS = 100L
    }

    /**
     * 被观察数据-正在播放数据实体
     */
    var nowPlayingMetadata = MutableLiveData<NowPlayingMetadata>()

    /**
     * 观察正在播放数据变化
     */
    private val nowPlayingObserver = Observer<MediaMetadataCompat> {
        Log.v(TAG, "nowPlayingObserver(%s)".format(it?.description))
        if (it?.duration!! > 0L) {
            nowPlayingMetadata.postValue(NowPlayingMetadata.convert(it))
        }
    }

    /**
     * 被观察数据-播放状态
     */
    var playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }

    /**
     * 观察正在播放状态变化
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        Log.v(TAG, "playbackStateObserver(%s)".format(it?.toString()))
        playbackState.postValue(it)
    }

    /**
     * 被观察数据-播放位置
     */
    val playbackPosition = MutableLiveData<Long>().apply {
        postValue(0L)
    }

    /**
     * 是否更新位置
     */
    private var updatePosition = true

    /**
     * 主现场Handler
     */
    private val handler = Handler(Looper.getMainLooper())

    /**
     * 观察client与服务端的数据变化
     */
    private val client = mediaPlaybackServiceClient.also {
        it.nowPlaying.observeForever(nowPlayingObserver)
        it.playbackState.observeForever(playbackStateObserver)
        checkPlaybackPosition()
    }

    /**
     * 轮询更新播放位置
     * @return
     */
    private fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val nowPosition = playbackState.value?.currentPlayBackPosition
        if (playbackPosition.value != nowPosition) {
            playbackPosition.postValue(nowPosition)
        }
        if (updatePosition) {
            checkPlaybackPosition()
        }
    }, POSITION_UPDATE_INTERVAL_MILLIS)

    /**
     * Control APIs
     * 播放
     */
    fun play() {
        client.transportControls.play()
    }

    /**
     * 暂停
     */
    fun pause() {
        client.transportControls.pause()
    }

    /**
     * 下一首
     */
    fun prev() {
        client.transportControls.skipToPrevious()
    }

    /**
     * 上一首
     */
    fun next() {
        client.transportControls.skipToNext()
    }

    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers
        client.nowPlaying.removeObserver(nowPlayingObserver)
        client.playbackState.removeObserver(playbackStateObserver)

        // Stop updating the position
        updatePosition = false

    }

    /**
     * 通过工厂为ViewModel传递参数数据
     *
     * @property app 上下文
     * @property mediaPlaybackServiceClient 媒体浏览器客户端
     */
    class Factory(
        private val app: Application,
        private val mediaPlaybackServiceClient: MediaPlaybackServiceClient
    ) :
        ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            //类型转换
            return MediaPlaybackFragmentViewModel(app, mediaPlaybackServiceClient) as T
        }
    }
}
