package com.simlelifesolution.colormatch.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.R;

public class MainActivity extends AppCompatActivity
{
//region...... variables declaration
    private Context  mContext = MainActivity.this;
    private Activity  mActivity = MainActivity.this;

    private Toolbar mToolbar;
    Button mCameraBtn, mGalleryBtn, mPalette, mColorPickerBtn;
    TextView mTxtVwColorCode;
    EditText mEtVwAutoCompleteColor;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 103;

    private static int btnPressType = 0 ;

    String[] permissionsRequired = new String[]{    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.SEND_SMS  };

    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    private static final int REQUEST_COLOR_PICKER_ACTIVITY = 9;

    //navigation drawer
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
//        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33B5E5")));

        initialize();

        dl = (DrawerLayout)findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        actionBar.setDisplayHomeAsUpEnabled(true);


        nv = (NavigationView)findViewById(R.id.nv);
        nv.setItemIconTintList(null);

//region ....Navigation drawer item select
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.mn_subscribe:
                        Toast.makeText(mContext, "Susbscribe",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.mn_permissions:
                        func_navItem_permission();
                        break;
                    case R.id.mn_options:
                        Toast.makeText(mContext, "Options",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.mn_uninstall:
                        func_navItem_exit();
                        break;

                    case R.id.mn_about:
                        func_navItem_about();
                        break;
                    case R.id.mn_support:
                        func_navItem_support();
                        break;
                    case R.id.mn_webpage:
                        func_navItem_website();
                        break;
                    default:
                        break;
                }

                return true;
            }
        });
//endregion
    }

    private void initialize() {
        mCameraBtn = (Button)findViewById(R.id.btnCamera);
        mGalleryBtn = (Button)findViewById(R.id.btnGallery);
        mPalette = (Button)findViewById(R.id.btnMyPalette);
        mColorPickerBtn = (Button)findViewById(R.id.btnColorPicker);
        mTxtVwColorCode = (TextView)findViewById(R.id.txtVwColorCode);

      //  mEtVwAutoCompleteColor = (EditText)findViewById(R.id.etAutoColor);
    }


    public void buttonOnClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btnCamera:
                btnPressType = 1;
                func_getPermission();

                break;
            case R.id.btnGallery:
                btnPressType = 2;
                func_getPermission();

                break;
            case R.id.btnColorPicker:
                btnPressType = 3;
                func_getPermission();

                break;
            case R.id.btnMyPalette:
                btnPressType = 4;
                func_getPermission();

               break;
            default:
                break;
        }

    }

    public void func_getPermission()
    {

        /* for single permission
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mContext, Manifest.permission.CAMERA))*/
        if( ActivityCompat.checkSelfPermission(mContext, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContext, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContext, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContext, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED    )
        {
            if( ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissionsRequired[3])    )
            {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(Html.fromHtml("<font color='#3F51B5'>Need multiple Permission</font>"));
                builder.setMessage(Html.fromHtml("<font color='#b6b4b4'>This app needs camera, storage & sms permission .</font>"));

                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                       /* for single permission
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
                         */
                        ActivityCompat.requestPermissions(mActivity, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }

            else if(permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again'- which results in shouldShowRequestPermissionRationale=false and requestPermissions method will do just nothing. So we need to remember whether we ave priorly requested for permission or not, even after the app restarts
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(Html.fromHtml("<font color='#3F51B5'>Some permissions required</font>"));
                builder.setMessage(Html.fromHtml("<font color='#b6b4b4'>This app needs some permissions</font>"));
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(mActivity, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            // the below codes will run everytime until we get permission
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
           // Log.d("Log_pref", String.valueOf(permissionStatus.getBoolean(Manifest.permission.CAMERA,false)));


        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
    }

    private void proceedAfterPermission()
    {
        if(btnPressType < 3)
        {Intent intnt = new Intent(this, ImagePickerActivity.class);
            intnt.putExtra("btnPressed",btnPressType);
            startActivity(intnt);}
        else if(btnPressType == 3){
            Intent colorPkrIntent = new Intent(mContext, ColorPickerActivity.class);
            startActivityForResult(colorPkrIntent, 9);
        }
        else if(btnPressType == 4){
            Intent intentPaletteActivity = new Intent(mContext, PaletteListActivity.class);
            startActivity(intentPaletteActivity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if(allgranted){
                proceedAfterPermission();
            }
            else if(ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(mActivity,permissionsRequired[3])    ){
               // txtPermissions.setText("Permissions Required");
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(Html.fromHtml("<font color='#3F51B5'>Need Multiple Permissions</font>"));
                builder.setMessage(Html.fromHtml("<font color='#b6b4b4'>This app needs Camera and Location permissions.</font>"));
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(mActivity,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     if(requestCode == REQUEST_COLOR_PICKER_ACTIVITY)
        {  /*if(resultCode == Activity.RESULT_OK)
            { String resultColorPkr = data.getStringExtra("resultPkr");
            //    Toast.makeText(this, resultColorPkr, Toast.LENGTH_SHORT).show();
               mTxtVwColorCode.setText(resultColorPkr); } */
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

 //functions used for navigatipn drawer items

 private void func_navItem_about()
 {
    // Toast.makeText(mContext, "ABout",Toast.LENGTH_SHORT).show();
     Intent abtIntent = new Intent(mContext, About.class);
     startActivity(abtIntent);
 }

private void func_navItem_support()
{
    Intent sprtIntent = new Intent(mContext, Support.class);
    startActivity(sprtIntent);
}

private void func_navItem_website()
 {
     Uri uri = Uri.parse("http://www.google.com");
     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
     startActivity(intent);
    }

private void func_navItem_exit()
    {
       /* Intent detailsIntent = new Intent();
       // detailsIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
        detailsIntent.setClass(mContext, MyAccessibilityService.class);

        //ApiLevel greater than or equal to 8
        //detailsIntent.putExtra("pkg", "com.simlelifesolution.colormatch.Activities");

        startActivity(detailsIntent);*/

        finishAffinity();
/*
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);*/

        System.exit(0);

    }

 private void func_navItem_permission()
 {
     Intent intent = new Intent();
     intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
     Uri uri = Uri.fromParts("package", getPackageName(), null);
     intent.setData(uri);
     startActivity(intent);
 }


}
