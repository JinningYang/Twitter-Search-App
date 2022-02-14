package com.company;

public class Tweet {
    private int timestamp;
    private int views;
    private int likes;
    private int retweets;
    private String content;

    public Tweet() {
        this(1, null);
    }

    public Tweet(int timestamp, String content) {
        this(timestamp, content, 1, 1, 1); // Set everything = 1 to help recommendation.
    }

    public Tweet(int timestamp, String content, int views, int likes, int retweets) {
        setTimestamp(timestamp);
        setContent(content);
        setViews(views);
        setLikes(likes);
        setRetweets(retweets);
    }

    public Tweet(String line) {
        assert (line != null);
        String[] args = line.split(",");
        assert (args.length >= 2);
        setTimestamp(Integer.parseInt(args[0]));
        setContent(args[1]);
        if (args.length == 2) {
            setViews(1);
            setLikes(1);
            setRetweets(1);
            return;
        }
        assert (args.length >= 5);
        setViews(Integer.parseInt(args[2]));
        setLikes(Integer.parseInt(args[3]));
        setRetweets(Integer.parseInt(args[4]));
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRetweets() {
        return retweets;
    }

    public void setRetweets(int retweets) {
        this.retweets = retweets;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}
