package com.incentive.yellowpages.ui.main


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.incentive.yellowpages.R
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.misc.LoadMoreListener
import com.incentive.yellowpages.ui.base.BaseApplication
import com.incentive.yellowpages.misc.ImageLoader
import kotlinx.android.synthetic.main.infinite_loading.view.*
import kotlinx.android.synthetic.main.row_search_item.view.*

class MainAdapter(private val loadingListener: LoadMoreListener,
                  private val searchItemClicked: MainAdapter.ISearchItemClicked)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val TYPE_SEARCH_ITEM = 1
        private val TYPE_LOADING_MORE = -1
    }

    interface ISearchItemClicked {
        fun onItemClicked(logo: View, searchItem: SearchResponse.SearchItem, position: Int)
    }

    val imageLoader: ImageLoader = BaseApplication.appComponent.imageLoader()

    var data: MutableList<SearchResponse.SearchItem> = mutableListOf()

    fun addData(newData: List<SearchResponse.SearchItem>) = data.addAll(newData)

    fun clear() = data.clear()

    val isEmpty: Boolean
        get() = data.size == 0

    val size: Int
        get() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {

        when (viewType) {
            TYPE_SEARCH_ITEM -> return MainHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_search_item, parent, false))

            TYPE_LOADING_MORE -> return LoadingMoreHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.infinite_loading, parent, false))
        }

        return null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(holder.adapterPosition)) {
            TYPE_SEARCH_ITEM -> (holder as MainHolder).bind(data[position])
            TYPE_LOADING_MORE -> (holder as LoadingMoreHolder).bind()
        }
    }

    override fun getItemCount(): Int = data.size + if (loadingListener.isDataLoading()) 1 else 0

    override fun getItemViewType(position: Int): Int {
        if (position == itemCount - 1 && loadingListener.isDataLoading())
            return TYPE_LOADING_MORE
        return TYPE_SEARCH_ITEM
    }

    inner class MainHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: SearchResponse.SearchItem) {
            itemView.apply {
                imageLoader.load(item.logo, logo)

                logo.tag = adapterPosition
                title.text = item.title
                photo.visibility = if (item.hasAdditionalPlacePhoto) View.VISIBLE else View.GONE
                video.visibility = if (item.hasAdditionalPlaceVideo) View.VISIBLE else View.GONE

                findViewById(R.id.searchContainer)
                        .setOnClickListener {
                            searchItemClicked.onItemClicked(logo, item,
                                    adapterPosition)
                        }
            }
        }
    }

    inner class LoadingMoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.progressBarAdapter.visibility = if (loadingListener.isDataLoading()) View.VISIBLE
            else View.GONE
        }
    }

}
