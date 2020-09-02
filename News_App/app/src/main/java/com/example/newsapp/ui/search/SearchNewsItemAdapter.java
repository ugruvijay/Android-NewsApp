package com.example.newsapp.ui.search;

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

public class SearchNewsItemAdapter extends RecyclerView.Adapter<SearchNewsItemAdapter.SearchNewsViewHolder> {
    private ArrayList<SearchNewsItem> mSearchNewsItemList;
    private OnNewsItemListener mOnNewsItemListener;
    private Context mContext;

    static class SearchNewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView mImageView;
        TextView mTextView1;
        TextView mTextView2;
        ImageButton bookmarkButton;
        OnNewsItemListener onNewsItemListener;
        Context thisContext;

        SearchNewsViewHolder(@NonNull View itemView, OnNewsItemListener onNewsItemListener, Context thisContext) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView);
            mTextView1 = itemView.findViewById(R.id.news_title);
            mTextView2 = itemView.findViewById(R.id.time);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            this.onNewsItemListener = onNewsItemListener;
            this.thisContext = thisContext;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNewsItemListener.onNewsItemClick(getAdapterPosition());
        }

        public boolean onLongClick(View view) {
            onNewsItemListener.onLongClick(getAdapterPosition());
            return true;
        }
    }

    SearchNewsItemAdapter(ArrayList<SearchNewsItem> newsList, OnNewsItemListener onNewsItemListener, Context context){
        mSearchNewsItemList = newsList;
        mOnNewsItemListener = onNewsItemListener;
        mContext = context;
    }

    @NonNull
    @Override
    public SearchNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        return new SearchNewsViewHolder(v, mOnNewsItemListener, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchNewsViewHolder holder, final int position) {
        final Context context = holder.mImageView.getContext();
        final SearchNewsItem currentItem = mSearchNewsItemList.get(position);
        holder.mTextView1.setText(currentItem.getTitle());
        holder.mTextView2.setText(currentItem.getTime());
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
                    yourFile.delete();
                }
                else {
                    holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark_24px);
                    Toast.makeText(context, "" + currentItem.getTitle() + " was added to bookmarks", Toast.LENGTH_SHORT).show();
                    Gson gson = new Gson();
                    String yourObjectJson = gson.toJson(currentItem);
                    writeFile(yourObjectJson, currentItem.getId());
                }
            }
        });

        Picasso.with(context).load(currentItem.getImageUrl()).resize(100, 110).centerCrop()
                .into(holder.mImageView);
    }

    public interface OnNewsItemListener {
        void onNewsItemClick(int position);
        void onLongClick(int position);
    }

    public boolean fileExist(String fname){
        File file = mContext.getFileStreamPath("" + fname.hashCode());
        return file.exists();
    }

    public void writeFile(String jsonObject, String id){
        String yourFilePath = mContext.getFilesDir() + "/" + id.hashCode();
        File yourFile = new File(yourFilePath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(yourFile);
            fileOutputStream.write(jsonObject.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public int getItemCount() {
        return mSearchNewsItemList.size();
    }
}