package com.example.newsapp.ui.bookmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarksViewHolder> {
    private ArrayList<BookmarkedItem> mBookmarksList;
    private OnBookmarksItemListener mOnBookmarksItemListener;
    private Context mContext;
    private TextView mBookmarkTextView;

    static class BookmarksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView mImageView;
        TextView mTextView1;
        TextView mTextView2;
        ImageButton bookmarkButton;
        TextView mBookmarkText;
        OnBookmarksItemListener onBookmarksItemListener;
        Context thisContext;

        BookmarksViewHolder(@NonNull View itemView, OnBookmarksItemListener onBookmarksItemListener, Context thisContext) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.bookmark_imageView);
            mTextView1 = itemView.findViewById(R.id.bookmark_title);
            mTextView2 = itemView.findViewById(R.id.bookmark_section);
            bookmarkButton = itemView.findViewById(R.id.bookmark_button);
            this.onBookmarksItemListener = onBookmarksItemListener;
            this.thisContext = thisContext;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onBookmarksItemListener.onNewsItemClick(getAdapterPosition());
        }

        public boolean onLongClick(View view) {
            onBookmarksItemListener.onLongClick(getAdapterPosition());
            return true;
        }


    }

    BookmarkAdapter(ArrayList<BookmarkedItem> bookmarksList, OnBookmarksItemListener onBookmarksItemListener, Context context, TextView bookmarkTextView){
        mBookmarksList = bookmarksList;
        mOnBookmarksItemListener = onBookmarksItemListener;
        mContext = context;
        mBookmarkTextView = bookmarkTextView;
    }

    @NonNull
    @Override
    public BookmarksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
        return new BookmarksViewHolder(v,mOnBookmarksItemListener, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final BookmarksViewHolder holder, final int position) {
        final Context context = holder.mImageView.getContext();
        final BookmarkedItem currentItem = mBookmarksList.get(position);
        holder.mTextView1.setText(currentItem.getTitle());
        holder.mTextView2.setText(currentItem.getPublishedDate());
        if(fileExist(currentItem.getId())){
            holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark_24px);
        }
        else {
            holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_24px);
        }

        holder.bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileExist(currentItem.getId())){
                    holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_24px);
                    Toast.makeText(context, "" + currentItem.getTitle() + " was removed from bookmarks", Toast.LENGTH_SHORT).show();
                    String yourFilePath = context.getFilesDir() + "/" + currentItem.getId().hashCode();
                    File yourFile = new File(yourFilePath);
                    mBookmarksList.remove(currentItem);
                    yourFile.delete();

                    if(mBookmarksList.size() > 0){
                        mBookmarkTextView.setVisibility(View.INVISIBLE);
                    }
                    else{
                        mBookmarkTextView.setVisibility(View.VISIBLE);
                    }

                    notifyDataSetChanged();
                }
            }
        });

        Picasso.with(context).load(currentItem.getImageUrl()).fit()
                .into(holder.mImageView);
    }

    public interface OnBookmarksItemListener {
        void onNewsItemClick(int position);
        void onLongClick(int position);
    }

    public boolean fileExist(String fname){
        File file = mContext.getFileStreamPath("" + fname.hashCode());
        return file.exists();
    }

    @Override
    public int getItemCount() {
        return mBookmarksList.size();
    }
}
