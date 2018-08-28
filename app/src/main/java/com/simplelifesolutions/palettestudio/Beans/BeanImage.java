package com.simplelifesolutions.palettestudio.Beans;

import android.os.Parcel;
import android.os.Parcelable;

public class BeanImage implements Parcelable
{
    private String imageId;
    private String paletteID;
    private String imagePath;
    private String thumbPath;
    private String imageName;
    private String updateTime;

    public BeanImage(){}

    public BeanImage(String _imageID, String _palateID, String _imagepth, String _thumbpth, String _imageName, String _updateTime)
    {
        this.imageId = _imageID;
        this.paletteID = _palateID;
        this.imagePath = _imagepth;
        this.thumbPath = _thumbpth;
        this.imageName = _imageName;
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
        dest.writeString(this.imageId);
        dest.writeString(this.paletteID);
        dest.writeString(this.imagePath);
        dest.writeString(this.thumbPath);
        dest.writeString(this.imageName);
        dest.writeString(this.updateTime);
    }

    protected BeanImage(Parcel in) {
        this.imageId = in.readString();
        this.paletteID = in.readString();
        this.imagePath = in.readString();
        this.thumbPath = in.readString();
        this.imageName = in.readString();
        this.updateTime = in.readString();
    }

    public static final Creator<BeanImage> CREATOR = new Creator<BeanImage>() {
        public BeanImage createFromParcel(Parcel source) {
            return new BeanImage(source);
        }
        public BeanImage[] newArray(int size) {
            return new BeanImage[size];
        }
    };
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//endregion

    public String getimageId() {
        return imageId;
    }

    public void setimageId(String imageId) {
        this.imageId = imageId;
    }

    public String getPaletteID() {
        return paletteID;
    }

    public void setPaletteID(String paletteID) {
        this.paletteID = paletteID;
    }

    public String getimagePath() {
        return imagePath;
    }

    public void setimagePath(String imageCode) {
        this.imagePath = imageCode;
    }

    public String getimageName() {
        return imageName;
    }

    public void setimageName(String imageName) {
        this.imageName = imageName;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
}
