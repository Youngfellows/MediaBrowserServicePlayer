package com.gmail.devu.study.mediabrowserservice.entity

import androidx.recyclerview.widget.DiffUtil

/**
 * 分页加载的diffCallback
 * @property id 歌曲ID
 * @property title 歌曲名称
 * @property artist 艺术家
 */
data class MediaItemData(
    val id: String,
    val title: String,
    val artist: String
) {

    companion object {

        /**
         * 静态属性,匿名对象
         */
        val diffCallback = object : DiffUtil.ItemCallback<MediaItemData>() {

            override fun areItemsTheSame(oldItem: MediaItemData, newItem: MediaItemData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MediaItemData,
                newItem: MediaItemData
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}