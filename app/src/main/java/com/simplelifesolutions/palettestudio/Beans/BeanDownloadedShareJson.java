package com.simplelifesolutions.palettestudio.Beans;

import java.util.ArrayList;

public class BeanDownloadedShareJson
{
    String paletteName;
    String paletteCover_flag;
    String palette_CoverPathOrCode;

    ArrayList<BeanDownloadedShare_details> array_paletteDetail ;

    public BeanDownloadedShareJson(){}

    public BeanDownloadedShareJson(String _pltName, String _pltFlag, String _pltCoverPathOrCode, ArrayList<BeanDownloadedShare_details> _arrayDetails)
    {
        this.paletteName = _pltName;
        this.paletteCover_flag = _pltFlag;
        this.palette_CoverPathOrCode = _pltCoverPathOrCode;
        this.array_paletteDetail  = _arrayDetails;
    }


    public String getPaletteName() {
        return paletteName;
    }

    public void setPaletteName(String paletteName) {
        this.paletteName = paletteName;
    }

    public String getPaletteCover_flag() {
        return paletteCover_flag;
    }

    public void setPaletteCover_flag(String paletteCover_flag) {
        this.paletteCover_flag = paletteCover_flag;
    }

    public String getPalette_CoverPathOrCode() {
        return palette_CoverPathOrCode;
    }

    public void setPalette_CoverPathOrCode(String palette_CoverPathOrCode) {
        this.palette_CoverPathOrCode = palette_CoverPathOrCode;
    }

    public ArrayList<BeanDownloadedShare_details> getArray_paletteDetail() {
        return array_paletteDetail;
    }

    public void setArray_paletteDetail(ArrayList<BeanDownloadedShare_details> array_paletteDetail) {
        this.array_paletteDetail = array_paletteDetail;
    }

}
