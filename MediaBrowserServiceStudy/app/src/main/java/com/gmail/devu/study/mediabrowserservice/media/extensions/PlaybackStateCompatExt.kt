package com.gmail.devu.study.mediabrowserservice.media.extensions

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat

/**
 * Useful extension methods for [PlaybackStateCompat].
 */

/**
 * 播放状态 - 是否已经准备完毕
 */
inline val PlaybackStateCompat.isPrepared
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
            (state == PlaybackStateCompat.STATE_PLAYING) ||
            (state == PlaybackStateCompat.STATE_PAUSED)

/**
 * 播放状态 - 是否正在播放中
 */
inline val PlaybackStateCompat.isPlaying
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
            (state == PlaybackStateCompat.STATE_PLAYING)

/**
 * 播放状态 - 是否能播放
 */
inline val PlaybackStateCompat.isPlayEnabled
    get() = (actions and PlaybackStateCompat.ACTION_PLAY != 0L) ||
            ((actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) &&
                    (state == PlaybackStateCompat.STATE_PAUSED))

/**
 * 播放状态 - 是否正在下一首
 */
inline val PlaybackStateCompat.isSkipToNextEnabled
    get() = actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT != 0L

/**
 * 播放状态 - 是否正在上一首
 */
inline val PlaybackStateCompat.isSkipToPreviousEnabled
    get() = actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS != 0L

/**
 * Calculates the current playback position based on last update time along with playback
 * state and speed.
 * 计算当前播放位置,根据播放速度和上次播放位置
 */
inline val PlaybackStateCompat.currentPlayBackPosition: Long
    get() = if (state == PlaybackStateCompat.STATE_PLAYING) {
        val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        (position + (timeDelta * playbackSpeed)).toLong()
    } else {
        position
    }
