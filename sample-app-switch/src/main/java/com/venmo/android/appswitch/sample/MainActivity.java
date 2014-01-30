package com.venmo.android.appswitch.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.venmo.android.appswitch.CompletedTransaction;
import com.venmo.android.appswitch.TransactionRequest;
import com.venmo.android.appswitch.TransactionRequest.Environment;
import com.venmo.android.appswitch.TransactionRequest.OnTransactionListener;
import com.venmo.android.appswitch.TransactionRequest.TransactionType;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MainActivity extends Activity implements OnTransactionListener {

    private static final int RESULT_VENMO_TRANSACTION_REQUEST = 45678;
    // set this to whatever you want

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_switch_activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_switch_main, menu);
        return true;
    }

    public void onDefaultClick(View view) {
        TransactionRequest.setEnvironment(Environment.DEBUG_ONLY_APP);
        onButtonClicked(view);
    }

    public void onForceWebviewClick(View view) {
        TransactionRequest.setEnvironment(Environment.DEBUG_ONLY_WEBVIEW);
        onButtonClicked(view);
    }

    public void onForceAppClick(View view) {
        TransactionRequest.setEnvironment(Environment.DEBUG_ONLY_APP);
        onButtonClicked(view);
    }

    private void onButtonClicked(View view) {
        Intent paymentRequest = new TransactionRequest("1136")
                .note("Hello, AppSwitchWorld!")
                .target("jesse", .01)
                .type(TransactionType.CHARGE)
                .getIntent(this);
        startActivityForResult(paymentRequest, RESULT_VENMO_TRANSACTION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_VENMO_TRANSACTION_REQUEST) {
            TransactionRequest.handleResponse(data, this);
        }
    }

    @Override
    public void onTransactionCancelled() {
        new AlertDialog.Builder(this).setTitle("Cancelled!")
                                     .setMessage("The transaction was cancelled!")
                                     .show();
    }

    @Override
    public void onTransactionSuccess(CompletedTransaction transaction) {
        StringBuilder message = new StringBuilder();
        message.append("You ");
        if (transaction.getTransactionType() == TransactionType.PAY) {
            message.append("paid");
        } else {
            message.append("charged");
        }
        message.append(' ');

        Iterator<Entry<String, Double>> iterator =
                transaction.getTargetsAndAmounts().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            message.append(entry.getKey() + " $" + entry.getValue());

            if (iterator.hasNext()) {
                message.append(", ");
            }
        }
        message.append(". The note left was: " + transaction.getNote());

        new AlertDialog.Builder(this).setTitle("Success!")
                                     .setMessage(message.toString())
                                     .show();
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
