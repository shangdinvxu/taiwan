package com.linkloving.taiwan.logic.UI.main.materialmenu;

/**
 * Created by zkx on 2016/3/7.
 */
public class MenuVO {
    //自定义数据类型
    private int textID;

    private int imgID;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    private int orderId ;

    public int getTextID() {
        return textID;
    }

    public void setTextID(int textID) {
        this.textID = textID;
    }

    public int getImgID() {
        return imgID;
    }

    public void setImgID(int imgID) {
        this.imgID = imgID;
    }
}
