package com.venmo.android.appswitch.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.common.collect.Lists;
import com.venmo.android.appswitch.PaymentRequest;
import com.venmo.android.appswitch.PaymentRequest.Type;
import com.venmo.android.appswitch.sample.R;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String APP_SECRET = "TetADaxbQnD8s9NwaRWgxYSde3XN7NJs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Hello, world!");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "starting delayed task");
                Collection<String> recipients = new ArrayList<String>();
                recipients.add("jesse");
                Intent paymentRequest = new PaymentRequest("1136", "xyear-dev")
                        .amount(12.34d)
                        .note("Hello, AppSwitchWorld!")
                        .recipients(recipients)
                        .type(Type.CHARGE)
                        .getIntent(getApplicationContext());
                startActivity(paymentRequest);
            }
        }, 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_switch_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.appswitch_action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
