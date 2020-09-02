package com.example.newsapp.ui.headlines;

public class HeadlinesNewsItem {
    private String mImageResource;
    private String title;
    private String time;
    private String id;
    private String webUrl;
    private String publishedDate;

    public HeadlinesNewsItem(String imageUrl, String title, String time, String id, String webUrl, String publishedDate) {
        this.mImageResource = imageUrl;
        this.title = title;
        this.time = time;
        this.id = id;
        this.webUrl = webUrl;
        this.publishedDate = publishedDate;
    }

    public String getImageUrl(){
        return mImageResource;
    }

    public String getTitle(){
        return title;
    }

    public String getTime(){
        return time;
    }

    public String getId() {
        return id;
    }

    public String getWebUrl(){
        return webUrl;
    }

    public String getPublishedDate(){ return publishedDate; }
}
