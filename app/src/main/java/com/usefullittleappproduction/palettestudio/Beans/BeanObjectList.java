package com.usefullittleappproduction.palettestudio.Beans;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BeanObjectList implements  Parcelable
{
    BeanMain _paletteObj;
    ArrayList<BeanObject> _imgOrClrObjLst;


    public BeanObjectList(){}

    public BeanObjectList(BeanMain pltObj, ArrayList<BeanObject> nObj)
    {
        _paletteObj = pltObj;
        _imgOrClrObjLst = nObj;
    }

    //region ...Parcalable methods
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this._paletteObj, flags);

        dest.writeTypedList(this._imgOrClrObjLst);
    }


    protected BeanObjectList(Parcel in) {
        this._paletteObj = in.readParcelable(BeanMain.class.getClassLoader());

        in.readTypedList(this._imgOrClrObjLst, BeanObject.CREATOR);
    }

    public static final Parcelable.Creator<BeanObjectList> CREATOR = new Parcelable.Creator<BeanObjectList>() {
        public BeanObjectList createFromParcel(Parcel source) {
            return new BeanObjectList(source);
        }
        public BeanObjectList[] newArray(int size) {
            return new BeanObjectList[size];
        }
    };


  //*************************************************************************************************//


    public BeanMain get_paletteObj() {
        return _paletteObj;
    }

    public void set_paletteObj(BeanMain _paletteObj) {
        this._paletteObj = _paletteObj;
    }

    public ArrayList<BeanObject> get_imgOrClrObjLst() {
        return _imgOrClrObjLst;
    }

    public void set_imgOrClrObjLst(ArrayList<BeanObject> _imgOrClrObjLst) {
        this._imgOrClrObjLst = _imgOrClrObjLst;
    }



}
