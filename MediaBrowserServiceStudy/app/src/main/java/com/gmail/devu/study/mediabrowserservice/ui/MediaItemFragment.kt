package com.gmail.devu.study.mediabrowserservice.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.gmail.devu.study.mediabrowserservice.R
import com.gmail.devu.study.mediabrowserservice.entity.MediaItemData
import com.gmail.devu.study.mediabrowserservice.interfaces.OnListFragmentInteractionListener
import com.gmail.devu.study.mediabrowserservice.media.MediaPlaybackServiceClient

/**
 * 媒体列表页
 */
class MediaItemFragment : Fragment() {

    companion object {
        private val TAG = MediaItemFragment::class.java.simpleName
    }

    /**
     * 携带被观察数据的ViewModel
     */
    private lateinit var viewModel: MediaItemFragmentViewModel

    /**
     * RV列表适配器
     */
    private lateinit var viewAdapter: MediaItemRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate()")

        // Get a ViewModel
        val context = requireActivity()
        //通过工厂为ViewModel传递参数
        val factory =
            MediaItemFragmentViewModel.Factory(MediaPlaybackServiceClient.getInstance(context))
        viewModel =
            ViewModelProvider(context, factory).get(MediaItemFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView()")
        val view = inflater.inflate(R.layout.fragment_media_item, container, false)

        // Set the adapter
        viewAdapter = MediaItemRecyclerViewAdapter(listener)
        if (view is RecyclerView) {
            //设置RV的列表适配器
            with(view) {
                adapter = viewAdapter
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated()")

        //观察View的数据变化 - 媒体浏览器服务播放状态是否已连接
        viewModel.isReady.observe(viewLifecycleOwner, Observer { isReady ->
            if (isReady) {
                // Connected to the MediaBrowserServiceCompat
                // Call subscribe () to browse the media root
                viewModel.loadMediaItems()
            }
        })

        //观察播放列表变化
        viewModel.mediaItems.observe(viewLifecycleOwner, Observer { items ->
            // MediaBrowserCompat.SubscriptionCallback of subscribe() is called
            viewAdapter.submitList(items)
        })
    }

    /**
     * 匿名对象,Item点击回调
     */
    private val listener = object : OnListFragmentInteractionListener {

        /**
         * 点击条目
         * @param item 条目数据
         */
        override fun onClicked(item: MediaItemData) {
            // Item is selected
            //播放音乐
            viewModel.playMediaItem(item)

            //跳转到播放页
            findNavController().navigate(R.id.action_mediaItemFragment_to_mediaPlaybackFragment)
        }
    }
}
