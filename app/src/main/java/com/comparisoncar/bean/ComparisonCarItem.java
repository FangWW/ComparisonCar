/*
 * mail:1065680448@qq.com
 */
package com.comparisoncar.bean;

/**
 * @Author FangJW
 * @Date 12/4/16
 */
public class ComparisonCarItem {
    private String name;
    private String id;
    private String rowTitle;
    private String color;
    private String rankUrl;
    private int imgBackgroud = -1;
    /**
     * 是否显示一行的头
     */
    private boolean isHeader = true;
    /**
     * 这一行的值  是否相等
     */
    private boolean isSame;

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRankUrl() {
        return rankUrl;
    }

    public void setRankUrl(String rankUrl) {
        this.rankUrl = rankUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public boolean isSame() {
        return isSame;
    }

    public void setSame(boolean same) {
        isSame = same;
    }

    public String getRowTitle() {
        return rowTitle;
    }

    public void setRowTitle(String rowTitle) {
        this.rowTitle = rowTitle;
    }

    public int getImgBackgroud() {
        return imgBackgroud;
    }

    public void setImgBackgroud(int imgBackgroud) {
        this.imgBackgroud = imgBackgroud;
    }
}
