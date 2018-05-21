package com.simlelifesolution.colormatch.Helpers;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import java.io.FileWriter;
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
        File outputImageFile = null;
        try {
            File root = new File(ctx.getExternalFilesDir(null), folderName);

            if (!root.exists()) {
                if (!root.mkdirs()) {
                    Log.d("myCamTag", "failed to create directory");
                    return null;
                }    }


            String imageFileName = img_prefix + img_middleTIme + img_ext;

             outputImageFile = new File(root, imageFileName);
//            FileWriter writer = new FileWriter(outputImageFile);

        }
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
            mThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
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


    public static void deleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

//region........... rotate the image in imageview


    public static Bitmap rotateImageFromURI(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
//endregion
}
