package com.venmo.android.appswitch;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class TransactionRequest {

    public static enum TransactionType {
        PAY("pay"), CHARGE("charge");

        String mValue;

        TransactionType(String value) {
            mValue = value;
        }

        String getValue() {
            return mValue;
        }
    }

    public static enum Environment {
        /**
         * Default behavior.
         *
         * @see #getIntent(Activity)
         */
        PRODUCTION,

        /**
         * Force {@link #getIntent(Activity)} to always link to the Venmo app. <strong>If the user
         * doesn't have the Venmo app installed, your application will crash if you start this
         * intent.</strong>
         */
        DEBUG_ONLY_APP,

        /**
         * Force {@link #getIntent(Activity)} to always link to the {@link android.webkit.WebView}
         */
        DEBUG_ONLY_WEBVIEW;
    }

    /**
     * Convenience interface to be applied on the {@link Activity} from which you are starting the
     * result of {@link #getIntent}.
     *
     * @see #handleResponse(Intent, OnTransactionListener)
     */
    public static interface OnTransactionListener {
        void onTransactionCancelled();
        void onTransactionSuccess(CompletedTransaction transaction);
    }

    private static final int FIRST_VERSION_SUPPORTED = 85;
    private static final Uri URI_SCHEME_NEW_PAYMENT = Uri.parse("venmosdk://paycharge");
    private static final String VENMO_PACKAGE = "com.venmo";

    public static final String EXTRA_PAY_CHARGE = "extra_pay_charge";
    public static final String EXTRA_APP_ID = "extra_app_id";
    public static final String EXTRA_APP_NAME = "extra_app_name";
    public static final String EXTRA_TARGETS = "extra_targets";
    public static final String EXTRA_AMOUNTS = "extra_amounts";
    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_TRANSACTION_COMPLETED = "extra_transaction_completed";
    public static final char TARGETS_DELIMITER = ',';
    public static final char AMOUNTS_DELIMITER = ',';

    private static Environment sEnvironment = Environment.PRODUCTION;

    private String mAppId;
    private String mAppName;
    private TransactionType mTransactionType = TransactionType.PAY;
    private Map<String, Double> mTargetsAndAmounts = Maps.newLinkedHashMap();
    private String mNote;

    /**
     * @param applicationId Your application id
     * @see <a href="https://venmo.com/api#registering-an-application">Registering an application
     * with Venmo</a>
     */
    public TransactionRequest(String applicationId) {
        Preconditions.checkNotNull(applicationId, "applicationId must not be null");
        mAppId = applicationId;
    }

    public TransactionRequest type(TransactionType transactionType) {
        Preconditions.checkNotNull(transactionType, "Type may not be null");
        mTransactionType = transactionType;
        return this;
    }

    /**
     * @param target Venmo username, phone number, or email
     * @param amount
     */
    public TransactionRequest target(String target, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0: " + amount);
        }

        if (mTargetsAndAmounts.containsKey(target)) {
            throw new IllegalArgumentException(target + " was already added to the request");
        }

        if (mTargetsAndAmounts.size() == 1) {
            throw new IllegalArgumentException("Only one target may be added");
        }

        mTargetsAndAmounts.put(target, amount);
        return this;
    }

    public TransactionRequest note(String note) {
        if (TextUtils.isEmpty(note)) {
            throw new IllegalArgumentException("Note must not be empty: " + null);
        }
        mNote = note;
        return this;
    }

    /**
     * Creates an {@link Intent} that may be started with {@link Activity#startActivity(Intent)} or
     * {@link Activity#startActivityForResult(Intent, int)}. If the user has the Venmo app
     * installed, they will be directed to a Native flow, otherwise a new {@link Activity} will be
     * started with a {@link android.webkit.WebView} that handles the same flow.
     *
     * @param activity the {@link Activity} in which you are creating this {@link Intent}
     * @return
     */
    public Intent getIntent(Activity activity) {
        mAppName = activity.getString(activity.getApplicationInfo().labelRes);

        PackageManager pm = activity.getPackageManager();
        PackageInfo venmoPackageInfo;
        try {
            venmoPackageInfo = pm.getPackageInfo(VENMO_PACKAGE, 0); // no additional flags desired
        } catch (PackageManager.NameNotFoundException e) {
            venmoPackageInfo = null;
        }

        Intent intent;
        switch (sEnvironment) {
            case PRODUCTION:
                if (venmoPackageInfo != null ||
                    venmoPackageInfo.versionCode >= FIRST_VERSION_SUPPORTED) {
                    intent = getNativeIntent();
                } else {
                    intent = getWebviewIntent(activity);
                }
                break;
            case DEBUG_ONLY_APP:
                intent = getNativeIntent();
                break;
            case DEBUG_ONLY_WEBVIEW:
                intent = getWebviewIntent(activity);
                break;
            default:
                throw new IllegalStateException("Intent was not set correctly internally");
        }

        intent.putExtra(EXTRA_PAY_CHARGE, mTransactionType.mValue);
        intent.putExtra(EXTRA_APP_ID, mAppId);
        intent.putExtra(EXTRA_APP_NAME, mAppName);
        if (mTargetsAndAmounts != null) {
            List<String> targets = Lists.newLinkedList();
            List<Double> amounts = Lists.newLinkedList();
            for (Map.Entry<String, Double> entry : mTargetsAndAmounts.entrySet()) {
                targets.add(entry.getKey());
                amounts.add(entry.getValue());
            }
            intent.putExtra(EXTRA_TARGETS, Joiner.on(TARGETS_DELIMITER).join(targets));
            intent.putExtra(EXTRA_AMOUNTS, Joiner.on(AMOUNTS_DELIMITER).join(amounts));
        }
        if (!TextUtils.isEmpty(mNote)) {
            intent.putExtra(EXTRA_NOTE, mNote);
        }

        return intent;
    }

    /**
     * Convenience method to help distinguish between successful transactions and cancelled ones.
     * Call this from {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * <pre>
     * {@code
     *
     *      onActivityResult(int requestCode, int resultCode, Intent data) {
     *          switch (requestCode) {
     *              case VENMO_TRANSACTION_REQUEST_CODE: //specify this in your Activity class
     *                  TransactionRequest.handleResponse(data, new OnTransactionListener() {
     *                      void onTransactionCancelled() {
     *                          // your code here
     *                      }
     *
     *                      void onTransactionSuccess(CompletedTransaction transaction) {
     *                          // your code here
     *                      }
     *                  });
     *                  break;
     *          }
     *      }
     *
     *      // OR
     *
     * public class MyActivity extends Activity implements OnTransactionListener { }
     *      onActivityResult(int requestCode, int resultCode, Intent data) {
     *          switch (requestCode) {
     *              case VENMO_TRANSACTION_REQUEST_CODE: //specify this in your Activity class
     *                  TransactionRequest.handleResponse(data, this);
     *                  break;
     *          }
     *      }
     *      void onTransactionCancelled() {
     *          // your code here
     *      }
     *
     *      void onTransactionSuccess(CompletedTransaction transaction) {
     *          // your code here
     *      }
     *
     * }
     * </pre>
     *
     * @param intent
     * @param listener
     */
    public static void handleResponse(Intent intent, OnTransactionListener listener) {
        Bundle extras = null;
        if (intent == null) {
            extras = new Bundle();
        } else {
            extras = intent.getExtras();
            if (extras == null) {
                extras = new Bundle();
            }
        }

        if (extras.getBoolean(EXTRA_TRANSACTION_COMPLETED, false)) {
            CompletedTransaction.Builder builder = new CompletedTransaction.Builder()
                    .note(extras.getString(EXTRA_NOTE))
                    .transactionType(extras.getString(EXTRA_PAY_CHARGE).equals("pay") ?
                            TransactionType.PAY : TransactionType.CHARGE);

            Iterator<String> targets = Splitter.on(TARGETS_DELIMITER).split(
                    extras.getString(EXTRA_TARGETS)).iterator();
            Iterator<String> amounts = Splitter.on(AMOUNTS_DELIMITER).split(
                    extras.getString(EXTRA_AMOUNTS)).iterator();

            while (targets.hasNext() && amounts.hasNext()) {
                builder.recipient(targets.next(), Double.valueOf(amounts.next()));
            }

            if (targets.hasNext()) {
                throw new IllegalStateException("There were more targets than amounts!");
            } else if (amounts.hasNext()) {
                throw new IllegalStateException("There were more amounts than targets!");
            }

            listener.onTransactionSuccess(builder.build());
        } else {
            listener.onTransactionCancelled();
        }
    }

    private static Intent getNativeIntent() {
        return new Intent(Intent.ACTION_VIEW, URI_SCHEME_NEW_PAYMENT);
    }

    private static Intent getWebviewIntent(Activity activity) {
        return new Intent(activity, VenmoSdkWebviewActivity.class);
    }

    /**
     * Useful for forcing a certain behavior while testing your integration.
     *
     * @see com.venmo.android.appswitch.TransactionRequest.Environment
     */
    @VisibleForTesting
    public static void setEnvironment(Environment environment) {
        Preconditions.checkNotNull(environment, "environment must not be null");
        sEnvironment = environment;
    }
}
