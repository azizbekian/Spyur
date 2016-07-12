package com.azizbekian.spyur.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.listener.IRecyclerLoadingListener;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.model.SearchResponse.SearchItem;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SEARCH_ITEM = 1;
    private static final int TYPE_LOADING_MORE = -1;

    public interface ISearchItemClicked {

        /**
         * Called, when the item has been clicked.
         */
        void onItemClicked(View logo, SearchResponse.SearchItem searchItem, int position);
    }

    private List<SearchItem> mData;
    private RequestManager mGlide;
    private IRecyclerLoadingListener mLoadingListener;
    @Nullable private ISearchItemClicked mSearchItemClicked;

    public SearchAdapter(IRecyclerLoadingListener loadingListener,
                         @Nullable ISearchItemClicked searchItemClicked) {
        mData = Collections.emptyList();
        mGlide = SpyurApplication.getComponent().getGlide();
        mLoadingListener = loadingListener;
        mSearchItemClicked = searchItemClicked;
    }

    public void setData(List<SearchItem> data) {
        mData = data;
    }

    public void addData(List<SearchItem> data) {
        mData.addAll(data);
    }

    public void clear() {
        mData.clear();
    }

    public boolean isEmpty() {
        return mData.size() == 0;
    }

    public int getSize() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (getSize() > 0 && position < getSize()) {
            return TYPE_SEARCH_ITEM;
        }
        return TYPE_LOADING_MORE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {

            case TYPE_SEARCH_ITEM:
                return new MainHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_search_item, parent, false));

            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.infinite_loading, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {

            case TYPE_SEARCH_ITEM:
                bindMainViewHolder(mData.get(position), (MainHolder) holder);
                break;

            case TYPE_LOADING_MORE:
                bindLoadingViewHolder((LoadingMoreHolder) holder);
                break;
        }

    }

    private void bindMainViewHolder(SearchItem item, MainHolder holder) {
        mGlide
                .load(item.getLogo())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .dontTransform()
                .priority(Priority.IMMEDIATE)
                .into(holder.logo);

        holder.title.setText(item.getTitle());
        holder.photo.setVisibility(item.hasAdditionalPlacePhoto() ? View.VISIBLE : View.GONE);
        holder.video.setVisibility(item.hasAdditionalPlaceVideo() ? View.VISIBLE : View.GONE);

        holder.itemView.findViewById(R.id.search_container)
                .setOnClickListener(view ->
                {
                    if (null != mSearchItemClicked)
                        mSearchItemClicked.onItemClicked(holder.logo, item,
                                holder.getAdapterPosition());
                });
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder) {
        holder.progress.setVisibility((holder.getAdapterPosition() > 0
                && mLoadingListener.isDataLoading()) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mData.size() + (mLoadingListener.isDataLoading() ? 1 : 0);
    }

    static class MainHolder extends RecyclerView.ViewHolder {

        public @BindView(R.id.logo) ImageView logo;
        public @BindView(R.id.title) TextView title;
        public @BindView(R.id.photo) View photo;
        public @BindView(R.id.video) View video;

        public MainHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    static class LoadingMoreHolder extends RecyclerView.ViewHolder {

        public ProgressBar progress;

        public LoadingMoreHolder(View itemView) {
            super(itemView);

            progress = (ProgressBar) itemView;
        }
    }
}
