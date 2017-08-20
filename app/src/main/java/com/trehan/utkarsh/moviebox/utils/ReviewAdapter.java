package com.trehan.utkarsh.moviebox.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trehan.utkarsh.moviebox.R;
import com.trehan.utkarsh.moviebox.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> mReviews;
    private Context mContext;

    public ReviewAdapter(Context context, List<Review> reviews) {
        mReviews = reviews;
        mContext = context;
    }
    private Context getContext() {
        return mContext;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView authorTextView;
        public TextView contentTextView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            authorTextView = (TextView) itemView.findViewById(R.id.review_author);
            contentTextView = (TextView) itemView.findViewById(R.id.review_content);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View movieView = inflater.inflate(R.layout.item_review, parent, false);
        ViewHolder viewHolder = new ViewHolder(movieView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ViewHolder holder, int position) {
        Review review = mReviews.get(position);

        // Set item views based on your views and data model
        TextView authorTextView = holder.authorTextView;
        authorTextView.setText(review.getAuthor());
        TextView contentTextView = holder.contentTextView;
        contentTextView.setText(review.getContent());

    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

}
