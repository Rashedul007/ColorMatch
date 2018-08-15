package com.usefullittleappproduction.palettestudio.Beans;

import android.os.Parcel;
import android.os.Parcelable;

public class BeanColor implements Parcelable
{
    private String colorId;
    private String paletteID;
    private String colorCode;
    private String colorName;
    private String updateTime;

    public BeanColor(){}

    public BeanColor(String _colorID, String _palateID, String _colorCode, String _colorName, String _updateTime)
    {
        this.colorId = _colorID;
        this.paletteID = _palateID;
        this.colorCode = _colorCode;
        this.colorName = _colorName;
        this.updateTime = _updateTime;
    }

 //region ...Parcalable methods
 //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.colorId);
        dest.writeString(this.paletteID);
        dest.writeString(this.colorCode);
        dest.writeString(this.colorName);
        dest.writeString(this.updateTime);
    }

    protected BeanColor(Parcel in) {
        this.colorId = in.readString();
        this.paletteID = in.readString();
        this.colorCode = in.readString();
        this.colorName = in.readString();
        this.updateTime = in.readString();
    }

    public static final Parcelable.Creator<BeanColor> CREATOR = new Parcelable.Creator<BeanColor>() {
        public BeanColor createFromParcel(Parcel source) {
            return new BeanColor(source);
        }
        public BeanColor[] newArray(int size) {
            return new BeanColor[size];
        }
    };
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

//endregion
    public String getColorId() {
        return colorId;
    }

    public void setColorId(String colorId) {
        this.colorId = colorId;
    }

    public String getPaletteID() {
        return paletteID;
    }

    public void setPaletteID(String paletteID) {
        this.paletteID = paletteID;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

}
