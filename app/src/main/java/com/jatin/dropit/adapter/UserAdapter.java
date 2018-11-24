package com.jatin.dropit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jatin.dropit.util.ItemOffsetDecoration;
import com.jatin.dropit.R;
import com.jatin.dropit.model.UsersItem;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private List<UsersItem> userResults;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;

    public UserAdapter(Context context) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) context;
        userResults = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.item_list, parent, false);
                viewHolder = new UserVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        UsersItem item = userResults.get(position);
        switch (getItemViewType(position)) {
            case ITEM:
                final UserVH userVH = (UserVH) viewHolder;
                userVH.tvUser.setText(item.getName());
                userVH.mProgress.setVisibility(View.VISIBLE);
                final ItemAdapter adapter = new ItemAdapter(item.getItems(),context);
                ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(context, R.dimen.item_offset);
                userVH.rvUser.addItemDecoration(itemDecoration);
                GridLayoutManager manager = new GridLayoutManager(context, 2);
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return (position % 3 == 0 ? 2 : 1);
                    }
                });
                userVH.rvUser.setLayoutManager(manager);

                userVH.rvUser.setAdapter(adapter);
                loadImage(item.getImage())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                userVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // image ready, hide progress now
                                userVH.mProgress.setVisibility(View.GONE);
                                return false;   // return false if you want Glide to handle everything else.
                            }
                        })
                        .into(userVH.ivUser);
                break;
            case LOADING:
                LoadingVH loadingVH = (LoadingVH) viewHolder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return userResults == null ? 0 : userResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == userResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    private class UserVH extends ViewHolder {

        private ImageView ivUser;
        private TextView tvUser;
        private ProgressBar mProgress;
        private RecyclerView rvUser;

        public UserVH(final View itemView) {
            super(itemView);

            ivUser = itemView.findViewById(R.id.ivUser);
            tvUser = itemView.findViewById(R.id.tvUser);
            mProgress = itemView.findViewById(R.id.ivProgress);
            rvUser = itemView.findViewById(R.id.rvUser);

        }
    }

    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        public LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = (ImageButton) itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = (TextView) itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = (LinearLayout) itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(userResults.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    private DrawableRequestBuilder<String> loadImage(@NonNull String posterPath) {

        return Glide
                .with(context)
                .load(posterPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL);   // cache both original & resized image
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new UsersItem());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = userResults.size() - 1;
        UsersItem result = getItem(position);

        if (result != null) {
            userResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void add(UsersItem r) {
        userResults.add(r);
        notifyItemInserted(userResults.size() - 1);
    }

    public void addAll(List<UsersItem> moveResults) {
        for (UsersItem result : moveResults) {
            add(result);
        }
    }

    public UsersItem getItem(int position) {
        return userResults.get(position);
    }

}
