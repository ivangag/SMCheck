package org.symptomcheck.capstone.adapters;

/**
 * Created by Ivan on 06/11/2014.
 */
public class DrawerItem {


    public DrawerItem(String title, int drawable){
        textTitle = title;
        drawableResource = drawable;
    }

    private String textTitle;
    private int drawableResource;

    public DrawerItem(){}

    public String getTextTitle() {
        return textTitle;
    }

    public void setTextTitle(String textTitle) {
        this.textTitle = textTitle;
    }

    public int getDrawableResource() {
        return drawableResource;
    }

    public void setDrawableResource(int drawableResource) {
        this.drawableResource = drawableResource;
    }
}
