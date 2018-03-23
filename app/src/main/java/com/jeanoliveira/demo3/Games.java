package com.jeanoliveira.demo3;

/**
 * Created by Jean on 3/22/2018.
 */

public class Games {

    private String Title;
    private String image_link;

    public Games(String title, String image_link) {
        Title = title;
        this.image_link = image_link;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getImage_link() {
        return image_link;
    }

    public void setImage_link(String image_link) {
        this.image_link = image_link;
    }
}
