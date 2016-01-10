package com.r.raul.tools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Rulo on 13/12/2015.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void compartirApp(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    protected void contactApp() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + R.string.my_mail));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
        startActivity(Intent.createChooser(emailIntent, this.getResources().getString(R.string.contacto)));
    }


}
