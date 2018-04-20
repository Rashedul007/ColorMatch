package com.simlelifesolution.colormatch.Helpers;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;

import java.io.File;


public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false))
        {
            // <---- run your one time code here
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            String pathDir1 = baseDir + File.separator + "ColorApp_Thumb";
            String pathDir2 = baseDir +File.separator + "ColorappImgs";


       /*     deleteRecursive(new File(pathDir1));
            deleteRecursive(new File(pathDir2));*/

            // mark first time has runned.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }
    }


    void deleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
