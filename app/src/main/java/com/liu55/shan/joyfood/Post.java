package com.liu55.shan.joyfood;

/**
 * Created by shan on 2018-03-03.
 */

public class Post {

    private String user, name, time, imageUrl, text, ingredient, direction, postID;

    //constructor
    public Post() {
    }

    public Post(String postID) {
        this.postID = postID;
    }

    public Post(String user, String name, String time, String imageUrl, String text, String ingredient, String direction, String postID) {
        this.user = user;
        this.name = name;
        this.time = time;
        this.imageUrl = imageUrl;
        this.text = text;
        this.ingredient = ingredient;
        this.direction = direction;
        this.postID = postID;
    }

    //gettings

    public String getUser() {
        return user;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public String getIngredient() {
        return ingredient;
    }

    public String getDirection() {
        return direction;
    }

    public String getPostID() {
        return postID;
    }

    //settings

    public void setUser(String user) {
        this.user = user;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public void setTime(String time) {
        this.time = time;
    }
}