package com.jatin.dropit.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jatin.dropit.R;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<String> imageItems;
    private Context context;

    public ItemAdapter(final List<String> imageItems, final Context context) {
        this.imageItems = imageItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int i) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_list_item, parent,
                false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final String imageUrl = imageItems.get(i);
        final ItemViewHolder holder = (ItemViewHolder) viewHolder;
        holder.mProgress.setVisibility(View.VISIBLE);
        loadImage(imageUrl)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.mProgress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        // image ready, hide progress now
                        holder.mProgress.setVisibility(View.GONE);
                        return false;   // return false if you want Glide to handle everything else.
                    }
                })
                .into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        AppCompatImageView ivImage;
        ProgressBar mProgress;

        public ItemViewHolder(@NonNull final View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            mProgress = itemView.findViewById(R.id.ivProgress);
        }
    }

    private DrawableRequestBuilder<String> loadImage(@NonNull String posterPath) {

        return Glide
                .with(context)
                .load(posterPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL);   // cache both original & resized image
    }

}
