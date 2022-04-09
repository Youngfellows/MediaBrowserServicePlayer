package com.gmail.devu.study.mediabrowserservice.entity

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.gmail.devu.study.mediabrowserservice.media.extensions.displaySubtitle
import com.gmail.devu.study.mediabrowserservice.media.extensions.displayTitle
import com.gmail.devu.study.mediabrowserservice.media.extensions.duration
import com.gmail.devu.study.mediabrowserservice.media.extensions.id


/**
 * Utility class used to represent the metadata necessary to display the media item currently being played.
 * 正在播放数据实体
 * @property id 媒体id
 * @property albumArtUri 资源url
 * @property title 名称
 * @property artist 艺术家
 * @property duration 时长
 */
data class NowPlayingMetadata(
    val id: String,
    val albumArtUri: Uri?,
    val title: String?,
    val artist: String?,
    val duration: Long
) {

    companion object {

        /**
         * 将正在播放的数据实体转换
         * @param metadata 播放数据实体
         * @return
         */
        fun convert(metadata: MediaMetadataCompat): NowPlayingMetadata {
            return NowPlayingMetadata(
                metadata.id,
                null,
                metadata.displayTitle,
                metadata.displaySubtitle,
                metadata.duration
            )
        }
    }
}