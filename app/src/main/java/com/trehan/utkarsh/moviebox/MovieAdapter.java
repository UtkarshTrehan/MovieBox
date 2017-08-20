package com.trehan.utkarsh.moviebox;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private Context mContext;
    private List<Movie> mMovies;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MovieAdapter(Context context, List<Movie> movies){
        mContext = context;
        mMovies = movies;
    }

    private Context getContext() {
        return mContext;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView posterImaageView;

        public ViewHolder(final View itemView) {
            super(itemView);
            posterImaageView = (ImageView)itemView.findViewById(R.id.moviesCardImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context =  parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View movieView = inflater.inflate(R.layout.movie_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(movieView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieAdapter.ViewHolder viewHolder, int position) {
        ImageView imageView = viewHolder.posterImaageView;
        Movie currentMovie = mMovies.get(position);
        File f = new File("/storage/emulated/0/Android/data/com.trehan.utkarsh.moviebox/images/"+currentMovie.getTile()+".jpeg");
        Picasso.with(getContext()).load(f).into(imageView);
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }
}