package com.usefullittleappproduction.palettestudio.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.usefullittleappproduction.palettestudio.R;

public class LoginActivity extends AppCompatActivity  implements View.OnClickListener
{
    private Context mContext = LoginActivity.this;
    EditText edTxtVw_userName, edTxtVw_pass ;
    Button btn_signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edTxtVw_userName = (EditText)findViewById(R.id.edt_loginid);
        edTxtVw_pass = (EditText)findViewById(R.id.edt_loginpass);
        btn_signIn = (Button)findViewById(R.id.btn_login);

        btn_signIn.setOnClickListener(this);

    }

  @Override
    public void onClick(View v) {
        if(v.equals(btn_signIn))
        {
            Intent intt = new Intent(mContext, MainActivity.class);
            startActivity(intt);
            finish();
        }

    }
}
