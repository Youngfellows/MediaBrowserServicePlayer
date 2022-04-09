package com.gmail.devu.study.mediabrowserservice.ui

import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.lifecycle.*
import com.gmail.devu.study.mediabrowserservice.entity.MediaItemData
import com.gmail.devu.study.mediabrowserservice.media.MediaPlaybackServiceClient
import com.gmail.devu.study.mediabrowserservice.media.extensions.id
import com.gmail.devu.study.mediabrowserservice.media.extensions.isPlayEnabled
import com.gmail.devu.study.mediabrowserservice.media.extensions.isPlaying
import com.gmail.devu.study.mediabrowserservice.media.extensions.isPrepared

/**
 * 携带被观察数据的ViewModel
 * @property client 媒体浏览器客户端
 */
class MediaItemFragmentViewModel(private val client: MediaPlaybackServiceClient) : ViewModel() {

    companion object {
        private val TAG = MediaItemFragmentViewModel::class.java.simpleName
    }

    /**
     * 被观察数据-媒体浏览器服务是否已连接
     */
    val isReady: LiveData<Boolean> = Transformations.map(client.isConnected) { isConnected ->
        isConnected
    }

    /**
     * 被观察数据 - 播放列表数据
     */
    private val _mediaItems = MutableLiveData<List<MediaItemData>>()

    /**
     * 被观察数据 - 播放列表数据
     */
    val mediaItems: LiveData<List<MediaItemData>> = _mediaItems

    /**
     * 被观察数据 - 正在播放数据
     */
    private val playingMedia: MutableLiveData<MediaItemData>? = null

    /**
     * Browse media root
     * 订阅媒体浏览器服务
     */
    fun loadMediaItems() {
        Log.v(TAG, "loadMediaItmes()")
        client.subscribe(client.rootMediaId, subscriptionCallback)
    }

    /**
     * 用于接收Server端加载数据的回调
     */
    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        /**
         * Server返回播放列表数据
         * @param parentId
         * @param children 播放列表数据
         */
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            Log.v(TAG, "onChildrenLoaded(%s)".format(parentId))

            //列表转换
            val items: List<MediaItemData> = children.map { child ->
                MediaItemData(
                    child.mediaId!!,
                    child.description.title.toString(),
                    child.description.subtitle.toString()
                )
            }
            //更新播放列表数据
            _mediaItems.postValue(items)
        }
    }

    /**
     * 开始播放
     * Start playback
     * @param mediaItem 将要播放的数据
     */
    fun playMediaItem(mediaItem: MediaItemData) {
        Log.v(TAG, "playMediaItem(%s)".format(mediaItem.id))
        val nowPlaying = client.nowPlaying.value //正在播放的数据
        val transportControls = client.transportControls

        val isPrepared = client.playbackState.value?.isPrepared ?: false
        //同一首歌,则暂停 or 播放
        if (isPrepared && mediaItem.id == nowPlaying?.id) {
            client.playbackState.value?.let { playbackState ->
                when {
                    //正在播放,暂停播放
                    playbackState.isPlaying -> transportControls.pause()
                    //暂停播放,继续播放
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(TAG, "Playable item clicked but neither play nor pause are enabled!")
                    }
                }
            }
        } else {
            Log.v(TAG, "call playFromMediaId(%s)".format(mediaItem.id))
            //根据id播放音乐
            transportControls.playFromMediaId(mediaItem.id, null)
        }

//        if (playingMedia != null) {
//            playingMedia.postValue(mediaItem)
//        }
        //更新当前正在播放数据
        playingMedia?.postValue(mediaItem)
    }

    override fun onCleared() {
        Log.v(TAG, "onCleared()")
        super.onCleared()
        //解除server订阅
        client.unsubscribe(client.rootMediaId, subscriptionCallback)
    }

    /**
     * 通过工厂为ViewModel传递参数
     * @property mediaPlaybackServiceClient 媒体浏览器客户端
     */
    class Factory(private val mediaPlaybackServiceClient: MediaPlaybackServiceClient) :
        ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaItemFragmentViewModel(mediaPlaybackServiceClient) as T
        }
    }
}
