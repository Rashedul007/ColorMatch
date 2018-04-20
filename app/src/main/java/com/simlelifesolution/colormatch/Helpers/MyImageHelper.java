package com.simlelifesolution.colormatch.Helpers;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import android.content.ContentResolver;

import com.simlelifesolution.colormatch.BuildConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import static android.media.ThumbnailUtils.extractThumbnail;

public class MyImageHelper {

    public MyImageHelper(){}

    public static String getRealPathFromURI(Context ctx, Uri uri) {
        Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static Uri getImageFileUriByOsVersion(Context ctx, File file)
    {
        Log.d("myCamTag", "getImageFileUriByOsVersion:: "+ Build.VERSION.SDK_INT );

        Uri ret_uri = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            ret_uri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID + ".provider", file);
        else
            ret_uri = Uri.fromFile(file);

        return ret_uri;
    }

    public static Bitmap getBitmapFromPath(String path)
    {
        Bitmap myBitmap= null;

        File imgFile = new File(path);

        if(imgFile.exists()){
                    myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }

       return myBitmap;
    }

    public static File func_makeFolderForImage(Context ctx, String folderName, String img_prefix, String img_middleTIme, String img_ext)
    {
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        String pathDir = baseDir +File.separator + folderName;
        File mydir = new File(pathDir);

        if (!mydir.exists())
        {  if (!mydir.mkdirs()) { Log.d("myCamTag", "failed to create directory");
            return null;}      }


        String imageFileName = img_prefix + img_middleTIme + img_ext;

        File outputImageFile = new File(pathDir + File.separator + imageFileName);

        try {outputImageFile.createNewFile();}
        catch(Exception exp){
            String errmsg = "Inside func_MakeFolderForImage():: \t"+exp;
            Log.d("TAG_LOG_TAKE_PICTURE", errmsg);
            Toast.makeText(ctx, errmsg, Toast.LENGTH_LONG).show();}

        return outputImageFile;

    }

    public static InputStream func_createThumbs_ReturnInStream(Bitmap btmap)
    {
        //Bitmap mThumbBitmap = extractThumbnail(btmap,100,100, options);
        Bitmap mThumbBitmap = extractThumbnail(btmap,150,150);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (mThumbBitmap != null) {
            bos = new ByteArrayOutputStream();
            mThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 10, bos);
            //return new ByteArrayInputStream(bos.toByteArray());
        }

        return new ByteArrayInputStream(bos.toByteArray());
    }

    public static String func_giveBitmap_aFileName(Context ctx, Bitmap yourBitmap, String imgTime)
    {
        File mThumbFile = func_makeFolderForImage(ctx, "ColorApp_Thumb", "colorAppThumb_", imgTime, ".jpg");
        //String folderName, String img_prefix, String img_middleTIme,String img_ext

        try {
            yourBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(mThumbFile));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return mThumbFile.getAbsolutePath();
    }


    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }


    }

}
