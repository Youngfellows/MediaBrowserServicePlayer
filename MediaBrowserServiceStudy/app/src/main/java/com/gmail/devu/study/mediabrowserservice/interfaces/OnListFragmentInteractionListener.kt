package com.gmail.devu.study.mediabrowserservice.interfaces

import com.gmail.devu.study.mediabrowserservice.entity.MediaItemData

/**
 * RV 条目点击回调
 */
interface OnListFragmentInteractionListener {

    /**
     * 条目点击回调
     * @param item 播放数据
     */
    fun onClicked(item: MediaItemData)
}