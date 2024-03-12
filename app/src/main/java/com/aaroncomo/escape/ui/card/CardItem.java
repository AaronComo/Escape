package com.aaroncomo.escape.ui.card;

public class CardItem {
    private Object image;
    public Boolean saved = false;
    private String hint;

    public CardItem(Object image, String hint) {
        this.image = image;
        this.hint = hint;
    }

    public String getImageURL() {
        return (String) image;
    }

    // 只有在图像是drawable资源时才可调用
    public int getImageID() {
        return (int) image;
    }

    public String getHint() {
        return hint;
    }
}
