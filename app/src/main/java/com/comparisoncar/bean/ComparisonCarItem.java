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

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(int id) {
        this.id = String.valueOf(id);
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
}
