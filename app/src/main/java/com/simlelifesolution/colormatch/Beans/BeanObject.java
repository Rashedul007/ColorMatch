package com.simlelifesolution.colormatch.Beans;


import android.os.Parcel;
import android.os.Parcelable;

public class BeanObject implements Parcelable {

    Object _anyObj;
    String _flagimgOrClr;
    String _dtime;

    public BeanObject(){}

    public BeanObject(Object obj, String flgImgClr, String dt)
    {
        this._anyObj = obj;
        this._flagimgOrClr = flgImgClr;
        this._dtime = dt;
    }

    //region ...Parcalable methods
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) this._anyObj, flags);
        dest.writeString(this._flagimgOrClr);
        dest.writeString(this._dtime);
    }

    protected BeanObject(Parcel in) {
        this._anyObj = in.readParcelable(Object.class.getClassLoader());
        this._flagimgOrClr = in.readString();
        this._dtime = in.readString();
    }

    public static final Creator<BeanObject> CREATOR = new Creator<BeanObject>() {
        public BeanObject createFromParcel(Parcel source) {
            return new BeanObject(source);
        }
        public BeanObject[] newArray(int size) {
            return new BeanObject[size];
        }
    };
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//endregion

    public Object getAnyObjLst() {
        return _anyObj;
    }

    public void setAnyObjLst(Object anyObjLst) {
        this._anyObj = anyObjLst;
    }

    public String getDtime() {
        return _dtime;
    }

    public void setDtime(String dtime) {
        this._dtime = dtime;
    }

    public String getFlag_imgOrClr() {
        return _flagimgOrClr;
    }

    public void setFlag_imgOrClr(String flag_imgOrClr) {
        this._flagimgOrClr = flag_imgOrClr;
    }

}
