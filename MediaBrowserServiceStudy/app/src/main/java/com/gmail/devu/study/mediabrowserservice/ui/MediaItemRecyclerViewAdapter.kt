package com.gmail.devu.study.mediabrowserservice.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gmail.devu.study.mediabrowserservice.R
import com.gmail.devu.study.mediabrowserservice.entity.MediaItemData
import com.gmail.devu.study.mediabrowserservice.interfaces.OnListFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_media_item_list_row.view.*

/**
 * RV列表适配器
 * @property mListener 条目点击回调
 */
class MediaItemRecyclerViewAdapter(
    private val mListener: OnListFragmentInteractionListener?
) : ListAdapter<MediaItemData, MediaItemRecyclerViewAdapter.ViewHolder>(MediaItemData.diffCallback) {

    /**
     * item点击回调
     */
    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v: View? ->

            val item = v?.tag as MediaItemData //条目数据
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            //回调点击事件
            mListener?.onClicked(item)
        }
    }

    /**
     * 为条目绑定视图
     * @param parent
     * @param viewType 条目类型
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_media_item_list_row, parent, false)
        return ViewHolder(view)
    }

    /**
     * 为视图绑定数据
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    /**
     * 条目的ViewHolder
     * @property mView 视图
     */
    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {

        private val mIdView: TextView = mView.item_id
        private val mTitleView: TextView = mView.title
        private val mSubtitleView: TextView = mView.subtitle

        /**
         * 为视图绑定数据
         * @param item 播放数据
         */
        fun bind(item: MediaItemData): Unit {
            mIdView.text = item.id
            mTitleView.text = item.title
            mSubtitleView.text = item.artist
            //为item view绑定数据,设置点击事件等
            with(mView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }
    }
}
