package com.gmail.devu.study.mediabrowserservice.media

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData


/**
 * 媒体浏览器客户端
 * @constructor
 * TODO
 *
 * @param context 上下文
 */
class MediaPlaybackServiceClient(context: Context) {

    /**
     * Connection status with the MediaPlayback Service
     * 媒体浏览器服务是否已连接
     */
    val isConnected = MutableLiveData<Boolean>().apply {
        postValue(false)
    }

    /**
     * MediaBrowserCompat
     * This value is valid after connecting to MediaPlayback service
     * 可连接媒体服务的rootId
     */
    val rootMediaId: String get() = mediaBrowser.root

    /**
     * 媒体浏览器
     */
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MediaPlaybackService::class.java),//媒体浏览器服务
        MediaBrowserConnectionCallback(context),
        null
    ).apply {
        // Connects to MediaPlayback service@MediaBrowserServiceCompat immediately upon instantiation
        //连接媒体浏览器服务
        connect()
    }

    /**
     * 媒体浏览器服务连接回调
     * @property context 上下文
     */
    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {

        /**
         * Invoked after [MediaBrowserCompat.connect] when the request has successfully completed.
         * 连接成功
         */
        override fun onConnected() {
            Log.v(TAG, "onConnected()")
            // Creates a [MediaControllerCompat] from a session token
            //创建媒体浏览器服务控制器
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            isConnected.postValue(true)
        }

        /**
         * Invoked when a connection to the browser service has been lost.
         * 连接断开
         */
        override fun onConnectionSuspended() {
            Log.v(TAG, "onConnectionSuspended()")
            isConnected.postValue(false)
        }

        /**
         * Invoked when the connection to the media browser service failed.
         * 连接失败
         */
        override fun onConnectionFailed() {
            Log.v(TAG, "onConnectionFailed()")
            isConnected.postValue(false)
        }
    }

    /**
     * Queries for information about the media items that are contained within
     * the specified id and subscribes to receive updates when they change.
     */
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        Log.v(TAG, "subscribe(%s)".format(parentId))
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        Log.v(TAG, "unsubscribe(%s)".format(parentId))
        mediaBrowser.unsubscribe(parentId, callback)
    }

    /**
     *  MediaControllerCompat
     *  The app subscribes to these live data to update metadata and status display
     *  被观察数据-当前正在播放数据
     */
    val nowPlaying = MutableLiveData<MediaMetadataCompat>().apply {
        postValue(NOTHING_PLAYING)
    }

    /**
     * 被观察数据-播放状态
     */
    val playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }

    /**
     * The app use this control for issuing a request of play, pause, etc
     * 应用程序使用此控件发出播放、暂停等请求
     */
    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    /**
     * Initialized when connection to MediaBrowserCompat is established.
     * 在与MediaBrowserCompat建立连接时初始化
     * 媒体浏览器服务控制器
     */
    private lateinit var mediaController: MediaControllerCompat


    /**
     * 媒体浏览器服务数据变化回调
     */
    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        /**
         * 播放数据变化
         * @param metadata 播放数据
         */
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.v(TAG, "onMetadataChanged(%s)".format(metadata?.description))
            nowPlaying.postValue(metadata ?: NOTHING_PLAYING)
        }

        /**
         * 播放状态变化
         * @param state 播放状态
         */
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.v(TAG, "onPlaybackStateChanged(%s)".format(state.toString()))
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        /**
         * 播放列表变化
         * @param queue 播放列表
         */
        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            Log.v(TAG, "onQueueChanged(%s)".format(queue.toString()))
        }

        /**
         * 重写以处理会话所有者发送的自定义事件
         *
         * @param event
         * @param extras
         */
        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            Log.v(TAG, "onSessionEvent(%s)".format(event.toString()))
        }
    }

    companion object {

        private val TAG = MediaPlaybackServiceClient::class.java.simpleName

        // For Singleton instantiation
        @Volatile
        private var instance: MediaPlaybackServiceClient? = null

        /**
         * 单例
         * @param context
         */
        fun getInstance(context: Context) = instance
            ?: synchronized(this) {
                instance
                    ?: MediaPlaybackServiceClient(
                        context
                    )
                        .also { instance = it }
            }
    }
}

/**
 * 当前没有正在播放数据
 */
@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0L)
    .build()

/**
 * 没有播放-空闲状态
 */
@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()
