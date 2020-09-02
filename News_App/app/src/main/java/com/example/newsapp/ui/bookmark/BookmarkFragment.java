package com.example.newsapp.ui.bookmark;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newsapp.R;
import com.example.newsapp.ui.article.DetailedActivity;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BookmarkFragment extends Fragment implements BookmarkAdapter.OnBookmarksItemListener {
    private static final String TAG = "BOOKMARK_FRAGMENT";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private BookmarkFragment fragment;
    private ArrayList<BookmarkedItem> bookmarkedItemsList;
    private Context thiscontext;
    private TextView mBookmarkText;
    View root;

    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.bookmark_fragment, container, false);
        thiscontext = container.getContext();
        fragment = this;
        mBookmarkText = root.findViewById(R.id.bookmark_textView);
        bookmarkedItemsList = getBookmarkedItems(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
//        for (int i = 0; i < bookmarkedItemsList.size(); i++) {
//            if(!fileExist(bookmarkedItemsList.get(i).getId())){
//                bookmarkedItemsList.remove(bookmarkedItemsList.get(i));
//            }
//        }
        bookmarkedItemsList = getBookmarkedItems(root);
        mAdapter.notifyDataSetChanged();
    }

    private ArrayList<BookmarkedItem> getBookmarkedItems(View root) {
        ArrayList<BookmarkedItem> bookmarkedItems = new ArrayList<>();

        File appFilesDirectory = thiscontext.getFilesDir();
        File[] filesList = appFilesDirectory.listFiles();
        Gson gson = new Gson();
        for(File file : filesList){
            String text = "";

            try {
                InputStream inputStream = new FileInputStream(file);
                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    while ((receiveString = bufferedReader.readLine()) != null){
                        stringBuilder.append(receiveString);
                    }
                    inputStream.close();
                    text = stringBuilder.toString();

                    BookmarkedItem item = gson.fromJson(text, BookmarkedItem.class);
                    bookmarkedItems.add(item);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        TextView bookmarkText = root.findViewById(R.id.bookmark_textView);
        if(bookmarkedItems.size() > 0){
            bookmarkText.setVisibility(View.INVISIBLE);
        }
        else{
            bookmarkText.setVisibility(View.VISIBLE);
        }
        mRecyclerView = root.findViewById(R.id.bookmark_recyclerview);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(thiscontext, 2);
        mAdapter = new BookmarkAdapter(bookmarkedItems, fragment, thiscontext, mBookmarkText);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return bookmarkedItems;
    }

    @Override
    public void onNewsItemClick(int position) {
        BookmarkedItem clickedItem = bookmarkedItemsList.get(position);
        String id = clickedItem.getId();
        Log.d(TAG, "onNewsItemClick: " + id);

        Intent intent = new Intent(getActivity(), DetailedActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    public void onLongClick(int position){
        final BookmarkedItem clickedItem = bookmarkedItemsList.get(position);
        String id = clickedItem.getId();
        Log.d(TAG, "onNewsItemClick: " + id);

        LayoutInflater inflater = getLayoutInflater();
        View newView = (View) inflater.inflate(R.layout.custom_dialog, null);

        final Dialog dialog = new Dialog(thiscontext);

        dialog.setContentView(newView);

        TextView titleTextView = dialog.findViewById(R.id.dialog_title);
        titleTextView.setText(clickedItem.getTitle());

        ImageView dialogImageView = newView.findViewById(R.id.dialog_imageView);
        dialog.show();
        Picasso.with(newView.getContext()).load(clickedItem.getImageUrl()).resize(300, 239)
                .into(dialogImageView);


        ImageButton twitterButton = newView.findViewById(R.id.dialog_twitter);
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://twitter.com/intent/tweet?text=Check out this link:&url=" + clickedItem.getWebUrl() + "&hashtags=CSCI571NewsSearch";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        final ImageView bookmarkButton = newView.findViewById(R.id.dialog_bookmark);

        if(fileExist(clickedItem.getId())){
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_24px);
        }

        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileExist(clickedItem.getId())){
                    bookmarkButton.setImageResource(R.drawable.ic_bookmark_border_24px);
                    Toast.makeText(thiscontext, "" + clickedItem.getTitle() + " was removed from bookmarks", Toast.LENGTH_SHORT).show();
                    String yourFilePath = thiscontext.getFilesDir() + "/" + clickedItem.getId().hashCode();
                    File yourFile = new File(yourFilePath);
                    yourFile.delete();
                    bookmarkedItemsList.remove(clickedItem);
                    mAdapter.notifyDataSetChanged();
                    dialog.dismiss();

                    if(bookmarkedItemsList.size() > 0){
                        mBookmarkText.setVisibility(View.INVISIBLE);
                    }
                    else{
                        mBookmarkText.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private boolean fileExist(String fname){
        File file = getActivity().getApplicationContext().getFileStreamPath("" + fname.hashCode());
        return file.exists();
    }
}
