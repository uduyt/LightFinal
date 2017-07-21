package com.witcode.light.light.domain;

import android.widget.ImageView;

/**
 * Created by rosety on 30/6/17.
 */

public class Notification {
    private String title, subtitle, image, id, button1Text, button2Text;
    private int type, action, button1Action, button2Action;
    private boolean closeable, fav;

    public static final int CHALLENGE = 1;
    public static final int MARKET = 2;
    public static final int NEW = 3;
    public static final int TWEET = 4;

    public static final int START_ACTIVITY_ACTION = 5;
    public static final int MARKET_ACTION = 6;
    public static final int RANKING_ACTION = 7;
    public static final int NONE = 8;

    public Notification(){

    }

    public Notification(int _type, String _title, String _subtitle, int _action) {

        String _image;
        switch (_type) {
            case CHALLENGE:
                _image = "xxxxxxxx";
                break;
            case MARKET:
                _image = "xxxxxxxx";
                break;
            case NEW:
                _image = "xxxxxxxx";
                break;
            case TWEET:
                _image = "xxxxxxxx";
                break;
            default:
                _image = "xxxxxxxx";
                break;
        }

        initialize(_type, _title, _subtitle, _action, _image );
    }

    public Notification(int _type, String _title, String _subtitle, int _action, String _image) {
        initialize(_type, _title, _subtitle, _action, _image);
    }

    private void initialize(int _type, String _title, String _subtitle, int _action, String _image) {

        type=_type;
        title=_title;
        subtitle=_subtitle;
        action=_action;
        image=_image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    public String getButton1Text() {
        return button1Text;
    }

    public void setButton1Text(String button1Text) {
        this.button1Text = button1Text;
    }

    public String getButton2Text() {
        return button2Text;
    }

    public void setButton2Text(String button2Text) {
        this.button2Text = button2Text;
    }

    public int getButton1Action() {
        return button1Action;
    }

    public void setButton1Action(int button1Action) {
        this.button1Action = button1Action;
    }

    public int getButton2Action() {
        return button2Action;
    }

    public void setButton2Action(int button2Action) {
        this.button2Action = button2Action;
    }
}
