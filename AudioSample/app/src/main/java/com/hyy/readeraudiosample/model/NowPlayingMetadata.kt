package com.hyy.readeraudiosample.model

import android.content.Context
import android.net.Uri
import com.hyy.readeraudiosample.R

/**
 * 正在播放的元数据
 */
data class NowPlayingMetadata(
    val id: String,
    val albumArtUri: Uri?,
    val title: String?,
    val subtitle: String?,
    val durationStr: String,
    val totalDuration: Int//totalSeconds
) {
    companion object {
        /**
         * 将毫秒转换为分秒显示的实用方法
         * Utility method to convert milliseconds to a display of minutes and seconds
         */
        fun timestampToMSS(context: Context, position: Long): String {
            val totalSeconds = Math.floor(position / 1E3).toInt() //将毫秒转换为秒,除以1000
            val minutes = totalSeconds / 60 //分
            val remainingSeconds = totalSeconds - (minutes * 60) //剩下多少秒
            return if (position < 0) context.getString(R.string.duration_unknown)
            else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
        }
    }
}
