package com.simplelifesolutions.palettestudio.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.simplelifesolutions.palettestudio.Beans.BeanColor;
import com.simplelifesolutions.palettestudio.Beans.BeanImage;
import com.simplelifesolutions.palettestudio.Beans.BeanMain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper
{
    // Logcat tag
   // private static final String LOG = DatabaseHelper.class.getName();
    private static final String LOG = "dbLog";

    // Database Version
    private static final int DATABASE_VERSION = 1;

//region declare db & table names
    // Database Name
    private static final String DATABASE_NAME = "DbPaletteStudio";

    // Table Names
    private static final String TABLE_MAIN = "tblmainpalette";
    private static final String TABLE_COLOR = "tblcolor";
    private static final String TABLE_IMAGE = "tblimage";

    // Column names - Main table
    private static final String KEY_MAIN_ID_PK = "paletteid_pk";
    private static final String KEY_PALETTE_NAME = "palette_name";
    private static final String KEY_COVERID_FLAG = "coverid_flag";
    private static final String KEY_COVERID = "coverid";
    private static final String KEY_MAIN_UPDATEDT = "update_dt";

    // Column names - Color table
    private static final String KEY_COLORID_PK = "colorid_pk";
    private static final String KEY_MAINID_FK_CLR = "paletteid_fk_clr";
    private static final String KEY_COLOR_CODE = "color_code";
    private static final String KEY_COLOR_NAME = "color_name";
    private static final String KEY_COLOR_UPDATEDT = "update_dt";


    // Column names - Image table
    private static final String KEY_IMAGEID_PK = "imageid_pk";
    private static final String KEY_MAINID_FK_IMG = "paletteid_fk_img";
    private static final String KEY_IMAGE_PATH = "image_path";
    private static final String KEY_THUMB_PATH = "thumbnail_path";
    private static final String KEY_IMAGE_NAME = "image_name";
    private static final String KEY_IMAGE_UPDATEDT = "update_dt";

//endregion

//region create table statements
    // MAIN table create statement
    private static final String CREATE_TABLE_MAIN = "CREATE TABLE "  + TABLE_MAIN + "("
            + KEY_MAIN_ID_PK + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + KEY_PALETTE_NAME + " TEXT,"
            + KEY_COVERID_FLAG + " TEXT," + KEY_COVERID + " INTEGER," + KEY_MAIN_UPDATEDT + " TEXT"
            + ")";

    // COLOR table create statement
    private static final String CREATE_TABLE_COLOR = "CREATE TABLE " + TABLE_COLOR + "("
            + KEY_COLORID_PK + " INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," + KEY_MAINID_FK_CLR + " INTEGER  NOT NULL ," + KEY_COLOR_CODE + " TEXT,"
            + KEY_COLOR_NAME + " TEXT NOT NULL UNIQUE," + KEY_COLOR_UPDATEDT + " TEXT,"
            + " FOREIGN KEY(" + KEY_MAINID_FK_CLR + ") REFERENCES " + TABLE_MAIN +"("+KEY_MAIN_ID_PK+")"
            + ")";

    // IMAGE table create statement
    private static final String CREATE_TABLE_IMAGE = "CREATE TABLE " + TABLE_IMAGE + "("
            + KEY_IMAGEID_PK + " INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," + KEY_MAINID_FK_IMG + " INTEGER  NOT NULL," + KEY_IMAGE_PATH + " TEXT," + KEY_THUMB_PATH + " TEXT,"
            + KEY_IMAGE_NAME + " TEXT NOT NULL UNIQUE," + KEY_IMAGE_UPDATEDT + " TEXT,"
            + " FOREIGN KEY(" + KEY_MAINID_FK_IMG + ") REFERENCES " + TABLE_MAIN +"("+KEY_MAIN_ID_PK+")"
            + ")";



    /*private static final String DATABASE_ALTER_1 = "ALTER TABLE "
            + TABLE_MAIN + " ADD COLUMN " + COLUMN_COACH + " string;";*/

//endregion

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(LOG, "inside constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_MAIN);
        db.execSQL(CREATE_TABLE_COLOR);
        db.execSQL(CREATE_TABLE_IMAGE);

        Log.d(LOG, "\n inside DB create");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        if (oldVersion < 2) {
          //  db.execSQL(DATABASE_ALTER_1);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            db.execSQL("PRAGMA foreign_keys=ON;");
        else
            db.setForeignKeyConstraintsEnabled(true);
    }

/* ************************************** TABLE METHODS *************************************** */

  // create a new palette
    public long createNewPalette(BeanMain objMain)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
            // values.put(KEY_MAIN_ID_PK, "NULL");
            values.put(KEY_PALETTE_NAME, objMain.getPaletteName() );
            values.put(KEY_COVERID_FLAG, objMain.getCoverID_flag() );

            if(objMain.getCoverID_flag().equals("color"))
                values.put(KEY_COVERID, 0);
            else if(objMain.getCoverID_flag().equals("image"))
                values.put(KEY_COVERID, 0);

            values.put(KEY_MAIN_UPDATEDT, getDateTime());

        // insert row
        long mainTblIn_id = db.insert(TABLE_MAIN, null, values);

//        Log.d("dbResult", ""+mainTblIn_id);

        return mainTblIn_id;
    }


    //insert new color
    public long insert_newColor(BeanColor objColor)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
       // values.put(KEY_COLORID_PK, objColor.getColorId());
        values.put(KEY_MAINID_FK_CLR, Integer.valueOf(objColor.getPaletteID()) );
        values.put(KEY_COLOR_CODE, objColor.getColorCode());
        values.put(KEY_COLOR_NAME, objColor.getColorName());
        values.put(KEY_COLOR_UPDATEDT, getDateTime());

        // insert row
        long colorTblIn_id = db.insert(TABLE_COLOR, null, values);

        //        Log.d("dbResult", ""+ colorTblIn_id);

        return colorTblIn_id;
    }

    //insert new image
    public long insert_newImage(BeanImage objImage)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
       // values.put(KEY_IMAGEID_PK, objImage.getimageId());
        values.put(KEY_MAINID_FK_IMG, Integer.valueOf(objImage.getPaletteID()));
        values.put(KEY_IMAGE_PATH, objImage.getimagePath());
        values.put(KEY_THUMB_PATH, objImage.getThumbPath());
        values.put(KEY_IMAGE_NAME, objImage.getimageName());
        values.put(KEY_IMAGE_UPDATEDT, getDateTime());

        // insert row
        long imageTblIn_id = db.insert(TABLE_IMAGE, null, values);

        return imageTblIn_id;
    }


//////////////////////////////////////////////////// ---- For adding cover in palette table

    public long updateCoverInPalette(String pltID, String flagImgOrClr, String imgOrClrID)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

            values.put(KEY_COVERID_FLAG, flagImgOrClr );
            values.put(KEY_COVERID, imgOrClrID );
            values.put(KEY_MAIN_UPDATEDT, getDateTime());

        // insert row
        //long mainTblIn_id = db.insert(TABLE_MAIN, null, values);

        long mainTblIn_id =  db.update(TABLE_MAIN, values, "paletteid_pk = ?", new String[]{pltID});

       Log.d("dbResult", "update value:: "+ mainTblIn_id);

        return mainTblIn_id;
    }


//////////////////////////////////////////////////// fetch from DB

    public BeanMain getPaletteObjFromID(String mPltid )
    {
        BeanMain _pltObj = new BeanMain();

        String selectQuery = "SELECT  * FROM " + TABLE_MAIN + " WHERE paletteid_pk=" + mPltid;

        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                _pltObj.setPaletteID(c.getString((c.getColumnIndex(KEY_MAIN_ID_PK))));
                _pltObj.setPaletteName((c.getString(c.getColumnIndex(KEY_PALETTE_NAME))));
                _pltObj.setCoverID_flag((c.getString(c.getColumnIndex(KEY_COVERID_FLAG))));
                _pltObj.setCoverID(c.getString(c.getColumnIndex(KEY_COVERID)));
                _pltObj.setUpdateTime(c.getString(c.getColumnIndex(KEY_MAIN_UPDATEDT)));
            } while (c.moveToNext());
        }

        return _pltObj;

    }

    public ArrayList<BeanMain> getPaletteList()
    {
        ArrayList<BeanMain> paletteList = new ArrayList<BeanMain>();

        String selectQuery = "SELECT  * FROM " + TABLE_MAIN;

        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                BeanMain td = new BeanMain();
                td.setPaletteID(c.getString((c.getColumnIndex(KEY_MAIN_ID_PK))));
                td.setPaletteName((c.getString(c.getColumnIndex(KEY_PALETTE_NAME))));
                td.setCoverID_flag((c.getString(c.getColumnIndex(KEY_COVERID_FLAG))));
                td.setCoverID(c.getString(c.getColumnIndex(KEY_COVERID)));
                td.setUpdateTime(c.getString(c.getColumnIndex(KEY_MAIN_UPDATEDT)));

                paletteList.add(td);
            } while (c.moveToNext());
        }

        return paletteList;
    }

    public ArrayList<BeanColor> getColorListFromPaletteID(String pltID, int lmt)
    {
        ArrayList<BeanColor> mColorList = new ArrayList<BeanColor>();

        String selectQuery = "";

        if(lmt > 0)
            selectQuery = "SELECT  * FROM " + TABLE_COLOR + " WHERE paletteid_fk_clr=" + Integer.valueOf(pltID) + "  ORDER BY update_dt limit "+lmt;
        else if(lmt == 0)
            selectQuery = "SELECT  * FROM " + TABLE_COLOR + " WHERE paletteid_fk_clr=" + Integer.valueOf(pltID) + "  ORDER BY update_dt";

        Log.d("LogDbPLt", selectQuery + "\n");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                BeanColor mColorObj = new BeanColor();
                mColorObj.setColorId(c.getString(c.getColumnIndex(KEY_COLORID_PK)));
                mColorObj.setPaletteID(c.getString(c.getColumnIndex(KEY_MAINID_FK_CLR)));
                mColorObj.setColorCode(c.getString(c.getColumnIndex(KEY_COLOR_CODE)));
                mColorObj.setColorName(c.getString(c.getColumnIndex(KEY_COLOR_NAME)));
                mColorObj.setUpdateTime(c.getString(c.getColumnIndex(KEY_COLOR_UPDATEDT)));

                mColorList.add(mColorObj);
            } while (c.moveToNext());
        }

        return mColorList;
    }

    public ArrayList<BeanImage> getImageListFromPaletteID(String pltID, int lmt)
    {
         ArrayList<BeanImage> mImageList = new ArrayList<BeanImage>();

        String selectQuery = "";

        if(lmt > 0)
            selectQuery = "SELECT  * FROM " + TABLE_IMAGE + " WHERE paletteid_fk_img=" + Integer.valueOf(pltID) + "  ORDER BY update_dt limit "+lmt;
        else if(lmt == 0)
            selectQuery = "SELECT  * FROM " + TABLE_IMAGE + " WHERE paletteid_fk_img=" + Integer.valueOf(pltID) + "  ORDER BY update_dt";
        Log.d("LogDbPLt", selectQuery + "\n");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                BeanImage mImageObj = new BeanImage();
                mImageObj.setimageId(c.getString(c.getColumnIndex(KEY_IMAGEID_PK)));
                mImageObj.setPaletteID(c.getString(c.getColumnIndex(KEY_MAINID_FK_IMG)));
                mImageObj.setimagePath(c.getString(c.getColumnIndex(KEY_IMAGE_PATH)));
                mImageObj.setThumbPath(c.getString(c.getColumnIndex(KEY_THUMB_PATH)));
                mImageObj.setimageName(c.getString(c.getColumnIndex(KEY_IMAGE_NAME)));
                mImageObj.setUpdateTime(c.getString(c.getColumnIndex(KEY_IMAGE_UPDATEDT)));

                mImageList.add(mImageObj);
            } while (c.moveToNext());
        }

        return mImageList;
      }


//--------------------------- fetch by ID's

    public ArrayList<String> getCoverFromID(String pltID)
    {
        // // return_arr >>> ( CoverID, Flag, Name, Path/code ) ////


        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<String> return_arr = new ArrayList<>(); //( CoverID, Flag, Name, Path/code )

        Cursor c = db.query(TABLE_MAIN, new String[]{KEY_PALETTE_NAME, KEY_COVERID_FLAG, KEY_COVERID},
                KEY_MAIN_ID_PK + "=?", new String[]{String.valueOf(pltID)},
                null, null, null, null);

        if (c != null)
            c.moveToFirst();

        String mCoverID = c.getString(c.getColumnIndex(KEY_COVERID));
        String mCoverFlag = c.getString(c.getColumnIndex(KEY_COVERID_FLAG));


        if(mCoverFlag.equals("image"))
        {
            return_arr.clear();
            if(mCoverID.equals("0"))
                {
                    return_arr.add(mCoverID);
                    return_arr.add("image");
                    return_arr.add("");
                    return_arr.add("");
                    return_arr.add("");
                }
            else {
                Cursor cursor_img = db.query(TABLE_IMAGE, new String[]{KEY_IMAGEID_PK, KEY_IMAGE_PATH, KEY_IMAGE_NAME, KEY_THUMB_PATH},
                        KEY_IMAGEID_PK + "=?", new String[]{mCoverID},
                        null, null, null, null);

                if (cursor_img != null)
                    cursor_img.moveToFirst();

                return_arr.add(mCoverID);
                return_arr.add("image");
                return_arr.add(String.valueOf(cursor_img.getString(cursor_img.getColumnIndex(KEY_IMAGE_NAME))));
                return_arr.add(String.valueOf(cursor_img.getString(cursor_img.getColumnIndex(KEY_IMAGE_PATH))));
                return_arr.add(String.valueOf(cursor_img.getString(cursor_img.getColumnIndex(KEY_THUMB_PATH))));
            }

        }
        else if(mCoverFlag.equals("color"))
        {
            return_arr.clear();

            Cursor cursor_clr = db.query(TABLE_COLOR, new String[]{KEY_COLORID_PK, KEY_COLOR_CODE, KEY_COLOR_NAME },
                    KEY_COLORID_PK + "=?", new String[]{mCoverID},
                    null, null, null, null);

            if (cursor_clr != null)
                cursor_clr.moveToFirst();

            return_arr.add(mCoverID);
            return_arr.add("color");
            return_arr.add(String.valueOf(cursor_clr.getString(cursor_clr.getColumnIndex(KEY_COLOR_NAME))));
            return_arr.add(String.valueOf(cursor_clr.getString(cursor_clr.getColumnIndex(KEY_COLOR_CODE))));
            return_arr.add("");
        }
        return return_arr;

    }


    public ArrayList<String> getCoverObjFromID(String pltID)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<String> return_arr = new ArrayList<>(); //( CoverID, Flag, Name, Path/code )

        Cursor c = db.query(TABLE_MAIN, new String[]{KEY_PALETTE_NAME, KEY_COVERID_FLAG, KEY_COVERID},
                KEY_MAIN_ID_PK + "=?", new String[]{String.valueOf(pltID)},
                null, null, null, null);

        if (c != null)
            c.moveToFirst();

        String mCoverID = c.getString(c.getColumnIndex(KEY_COVERID));
        String mCoverFlag = c.getString(c.getColumnIndex(KEY_COVERID_FLAG));


        if(mCoverFlag.equals("image"))
        {
            return_arr.clear();
            if(mCoverID.equals("0"))
            {
                return_arr.add(mCoverID);
                return_arr.add("image");
                return_arr.add("");
                return_arr.add("");
                return_arr.add("");
            }
            else {
                Cursor cursor_img = db.query(TABLE_IMAGE, new String[]{KEY_IMAGEID_PK, KEY_IMAGE_PATH, KEY_IMAGE_NAME, KEY_THUMB_PATH},
                        KEY_IMAGEID_PK + "=?", new String[]{mCoverID},
                        null, null, null, null);

                if (cursor_img != null)
                    cursor_img.moveToFirst();

                return_arr.add(mCoverID);
                return_arr.add("image");
                return_arr.add(String.valueOf(cursor_img.getString(cursor_img.getColumnIndex(KEY_IMAGE_NAME))));
                return_arr.add(String.valueOf(cursor_img.getString(cursor_img.getColumnIndex(KEY_IMAGE_PATH))));
                return_arr.add(String.valueOf(cursor_img.getString(cursor_img.getColumnIndex(KEY_THUMB_PATH))));
            }

        }
        else if(mCoverFlag.equals("color"))
        {
            return_arr.clear();

            Cursor cursor_clr = db.query(TABLE_COLOR, new String[]{KEY_COLORID_PK, KEY_COLOR_CODE, KEY_COLOR_NAME },
                    KEY_COLORID_PK + "=?", new String[]{mCoverID},
                    null, null, null, null);

            if (cursor_clr != null)
                cursor_clr.moveToFirst();

            return_arr.add(mCoverID);
            return_arr.add("color");
            return_arr.add(String.valueOf(cursor_clr.getString(cursor_clr.getColumnIndex(KEY_COLOR_NAME))));
            return_arr.add(String.valueOf(cursor_clr.getString(cursor_clr.getColumnIndex(KEY_COLOR_CODE))));
            return_arr.add("");
        }
        return return_arr;

    }

    public BeanColor getColorFromColorID(String clr_ID)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TABLE_COLOR, new String[]{KEY_COLORID_PK, KEY_MAINID_FK_CLR, KEY_COLOR_CODE, KEY_COLOR_NAME, KEY_COLOR_UPDATEDT},
                                    KEY_COLORID_PK + "=?", new String[]{String.valueOf(clr_ID)},
                                        null, null, null, null);
        if (c != null)
            c.moveToFirst();

        BeanColor _clrObj = new BeanColor( c.getString(c.getColumnIndex(KEY_COLORID_PK)), c.getString(c.getColumnIndex(KEY_MAINID_FK_CLR)),
                                             c.getString(c.getColumnIndex(KEY_COLOR_CODE)), c.getString(c.getColumnIndex(KEY_COLOR_NAME)), c.getString(c.getColumnIndex(KEY_COLOR_UPDATEDT))  );
        return _clrObj;
    }

    public BeanImage getImageFromImageID(String img_ID)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TABLE_IMAGE, new String[]{KEY_IMAGEID_PK, KEY_MAINID_FK_IMG, KEY_IMAGE_PATH, KEY_THUMB_PATH, KEY_IMAGE_NAME, KEY_IMAGE_UPDATEDT },
                KEY_IMAGEID_PK + "=?", new String[]{String.valueOf(img_ID)},
                null, null, null, null);
        if (c != null)
            c.moveToFirst();

        BeanImage _imgObj = new BeanImage( c.getString(c.getColumnIndex(KEY_IMAGEID_PK)), c.getString(c.getColumnIndex(KEY_MAINID_FK_IMG)), c.getString(c.getColumnIndex(KEY_IMAGE_PATH)),
                                                c.getString(c.getColumnIndex(KEY_THUMB_PATH)), c.getString(c.getColumnIndex(KEY_IMAGE_NAME)), c.getString(c.getColumnIndex(KEY_IMAGE_UPDATEDT)) );
        return _imgObj;
    }

//////////////////////////////////////////////////////method to get now dateTime
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }


///////////////////////////////////////////////////// delete

    public void deleteSinglePalette(String mid)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(TABLE_COLOR, "paletteid_fk_clr=?", new String[]{mid});
            db.delete(TABLE_IMAGE, "paletteid_fk_img=?", new String[]{mid});
            db.delete(TABLE_MAIN, "paletteid_pk=?", new String[]{mid});

            db.setTransactionSuccessful();
        } catch(Exception ex){
            Log.e("DBError", ex.toString());
        }
       finally{
            db.endTransaction();
            db.close();
        }
    }

    public void deletePaletteItem(String m_id, String flg)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows=0;
        Log.d("DBDel", "passedID::" + m_id + " flag::"+ flg);
        db.beginTransaction();
        try {
            if(flg.equals("image"))
                deletedRows = db.delete(TABLE_IMAGE, "imageid_pk=?", new String[]{m_id});
            else if(flg.equals("color"))
                deletedRows= db.delete(TABLE_COLOR, "colorid_pk=?", new String[]{m_id});

            ArrayList<String> arrPltIDsForCover = checkIfItemIsCover(flg, m_id);

            if(arrPltIDsForCover.size() > 0)
            {
                for(String eachPltID: arrPltIDsForCover)
                    updateCoverInPalette(eachPltID, "image", "0");
            }

            db.setTransactionSuccessful();
        } catch(Exception ex){
            Log.e("DBError", ex.toString());
        }
        finally{
            db.endTransaction();
            db.close();
            Log.d("DBDel", "deleted row::" + deletedRows);
        }
    }


//////////////////////////////// checkings

    public boolean checkDuplicatePltName(String newpltName)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] cols = {KEY_PALETTE_NAME};

        Cursor findEntry = db.query(TABLE_MAIN, cols, "palette_name=?", new String[] { newpltName }, null, null, null);

        if(findEntry.getCount() > 0)
            return true;
        else
            return false;
    }

    public boolean checkDuplicateImgName(String newImgName)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] cols = {KEY_IMAGE_NAME};

        Cursor findEntry = db.query(TABLE_IMAGE, cols, "image_name=?", new String[] { newImgName }, null, null, null);

        if(findEntry.getCount() > 0)
            return true;
        else
            return false;
    }

    public boolean checkDuplicateColorName(String newClrName)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] cols = {KEY_COLOR_NAME};

        Cursor findEntry = db.query(TABLE_COLOR, cols, "color_name=?", new String[] { newClrName }, null, null, null);

        if(findEntry.getCount() > 0)
            return true;
        else
            return false;
    }

    public ArrayList<String> checkIfItemIsCover(String _flgClrOrImg, String _id)
    {
        ArrayList<String> strArrPltIdsWithCover = new ArrayList<>();

        if(!_id.equals("0")) {
            String myQuery = "SELECT paletteid_pk  from tblmainpalette where coverid_flag='" + _flgClrOrImg + "' and coverid='" + _id + "'";

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(myQuery, null);



            if (c.moveToFirst()) {
                do {
                    strArrPltIdsWithCover.add(c.getString(c.getColumnIndex("paletteid_pk")));
                }
                while (c.moveToNext());
            }
        }

        return strArrPltIdsWithCover;

    }


    public void getDBVersion_fkCheck()
    {
        Cursor cursor = SQLiteDatabase.openOrCreateDatabase(":memory:", null).rawQuery("select sqlite_version() AS sqlite_version", null);
        String sqliteVersion = "";
        while(cursor.moveToNext()){
            sqliteVersion += cursor.getString(0);
        }

        String selectQuery = "PRAGMA foreign_keys";
        String pragma_result="";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        while(c.moveToNext()){
            pragma_result += c.getString(0);
        }

        Log.d("getDBVersion_fkCheck", "version:: "+ sqliteVersion + "\t-- FK enabled:: " + pragma_result);
    }

}
