package com.usefullittleappproduction.palettestudio.Beans;

import android.os.Parcel;
import android.os.Parcelable;

public class BeanMain implements Parcelable {

    private String paletteID;
    private String paletteName;
    private String coverID_flag;
    private String coverID;
    private String updateTime;

    public BeanMain() { }

    public BeanMain(String _paletteID, String _paletteName, String _coverIDFlag, String _coverID, String _updateTime)
    {
        this.paletteID = _paletteID;
        this.paletteName = _paletteName;
        this.coverID_flag = _coverIDFlag;
        this.coverID = _coverID;
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
        dest.writeString(this.paletteID);
        dest.writeString(this.paletteName);
        dest.writeString(this.coverID_flag);
        dest.writeString(this.coverID);
        dest.writeString(this.updateTime);
    }

    protected BeanMain(Parcel in) {
        this.paletteID = in.readString();
        this.paletteName = in.readString();
        this.coverID_flag = in.readString();
        this.coverID = in.readString();
        this.updateTime = in.readString();
    }

    public static final Creator<BeanMain> CREATOR = new Creator<BeanMain>() {
        public BeanMain createFromParcel(Parcel source) {
            return new BeanMain(source);
        }
        public BeanMain[] newArray(int size) {
            return new BeanMain[size];
        }
    };
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//endregion

    public String getPaletteID() {
        return paletteID;
    }

    public void setPaletteID(String paletteID) {
        this.paletteID = paletteID;
    }

    public String getPaletteName() {
        return paletteName;
    }

    public void setPaletteName(String paletteName) {
        this.paletteName = paletteName;
    }

    public String getCoverID() {
        return coverID;
    }

    public void setCoverID(String coverID) {
        this.coverID = coverID;
    }

    public String getCoverID_flag() {
        return coverID_flag;
    }

    public void setCoverID_flag(String coverID_flag) {
        this.coverID_flag = coverID_flag;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

}