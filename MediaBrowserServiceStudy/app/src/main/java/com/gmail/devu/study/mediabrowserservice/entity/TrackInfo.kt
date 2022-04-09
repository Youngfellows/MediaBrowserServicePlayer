package com.gmail.devu.study.mediabrowserservice.entity

// Sample tracks
// Use the sample music of UAMP (https://github.com/android/uamp)
// https://storage.googleapis.com/uamp/catalog.json
/**
 * 音乐数据实体
 * @property source 播放url
 * @property image 封面图片url
 * @property duration 时长
 */
data class TrackInfo(
    var source: String,
    var image: String,
    var duration: Long
) {

}