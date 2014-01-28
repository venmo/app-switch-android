package com.venmo.android.appswitch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.json.JSONArray;

import java.util.Collection;

public class PaymentRequest {

    public static enum Type {
        PAY("pay"), CHARGE("charge");

        String mValue;

        Type(String value) {
            mValue = value;
        }
    }

    private static final int FIRST_VERSION_SUPPORTED = 85;
    private static final Uri URI_SCHEME_NEW_PAYMENT = Uri.parse("venmosdk://paycharge");

    private static final String VENMO_PACKAGE = "com.venmo";
    private static final String EXTRA_PAY_CHARGE = "extra_pay_charge";
    private static final String EXTRA_APP_ID = "extra_app_id";
    private static final String EXTRA_APP_NAME = "extra_app_name";
    private static final String EXTRA_RECIPIENTS = "extra_recipients";
    private static final String EXTRA_AMOUNT = "extra_amount";
    private static final String EXTRA_NOTE = "extra_note";
    private static final String EXTRA_CALLBACK_SCHEME = "extra_callback_scheme";
    public static final int RESULT_SUCCESS = 200;
    public static final int RESULT_DISCARDED = 300;

    private String mAppId;
    private String mAppName;
    private Type mType = Type.PAY;
    private Collection<String> mRecipients;
    private double mAmount = Double.MIN_VALUE;
    private String mNote;
    private Uri mCallbackScheme;


    /**
     * @param applicationId Your application id
     * @param applicationName Your application name
     * @see <a href="https://venmo.com/api#registering-an-application">Registering an application
     * with Venmo</a>
     */
    public PaymentRequest(String applicationId, String applicationName) {
        Preconditions.checkNotNull(applicationId, "applicationId must not be null");
        Preconditions.checkNotNull(applicationName, "applicationName must not be null");
        mAppId = applicationId;
        mAppName = applicationName;
    }

    public PaymentRequest type(Type type) {
        Preconditions.checkNotNull(type, "Type may not be null");
        mType = type;
        return this;
    }

    //TODO switch this to recipient()
    /**
     *
     * Each item in {@code recipients} should be a Venmo username, email, or phone number
     */
    public PaymentRequest recipients(Iterable<String> recipients) {
        mRecipients = Lists.newArrayList(recipients);
        return this;
    }

    public PaymentRequest amount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0: " + amount);
        }
        mAmount = amount;
        return this;
    }

    public PaymentRequest note(String note) {
        if (TextUtils.isEmpty(note)) {
            throw new IllegalArgumentException("Note must not be empty: " + null);
        }
        mNote = note;
        return this;
    }

    /**
     * An optional way to indicate how to regain focus after the payment is completed.
     */
    public PaymentRequest successCallback(String callbackScheme) {
        mCallbackScheme = Uri.parse(callbackScheme);
        return this;
    }

    public Intent getIntent(Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    VENMO_PACKAGE, 0); // no additional flags desired
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }

        Intent intent;
        if (packageInfo != null && true || packageInfo.versionCode >= FIRST_VERSION_SUPPORTED) {
            intent = new Intent(Intent.ACTION_VIEW, URI_SCHEME_NEW_PAYMENT);
        } else {
            intent = new Intent(context, VenmoSdkWebviewActivity.class);
        }

        intent.putExtra(EXTRA_PAY_CHARGE, mType.mValue);
        intent.putExtra(EXTRA_APP_ID, mAppId);
        intent.putExtra(EXTRA_APP_NAME, mAppName);
        if (mRecipients != null) {
            intent.putExtra(EXTRA_RECIPIENTS, Joiner.on(',').join(mRecipients));
        }
        if (mAmount != Double.MIN_VALUE) {
            intent.putExtra(EXTRA_AMOUNT, mAmount);
        }
        if (!TextUtils.isEmpty(mNote)) {
            intent.putExtra(EXTRA_NOTE, mNote);
        }
        if (mCallbackScheme != null) {
            intent.putExtra(EXTRA_CALLBACK_SCHEME, mCallbackScheme);
        }

        return intent;
    }
}
