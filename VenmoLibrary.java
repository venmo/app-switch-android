package com.venmo.demo; //Replace this with the name of your package

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VenmoLibrary {
    private static final String VENMO_PACKAGE = "com.venmo";
    private static final String VENMO_ACTIVITY = "com.venmo.controller.ComposeActivity";

    public VenmoLibrary() {}

    /**
     * Takes the recipients, amount, and note, and returns an Intent object
     */
    public static Intent openVenmoPayment(String myAppId, String myAppName, String recipients,
            String amount, String note, String txn) {
        String venmo_uri = "venmosdk://paycharge?txn=" + txn;

        if (!recipients.equals("")) {
            try {
                venmo_uri += "&recipients=" + URLEncoder.encode(recipients, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e("venmo_library", "cannot encode recipients");
            }
        }
        if (!amount.equals("")) {
            try {
                venmo_uri += "&amount=" + URLEncoder.encode(amount, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e("venmo_library", "cannot encode amount");
            }
        }
        if (!note.equals("")) {
            try {
                venmo_uri += "&note=" + URLEncoder.encode(note, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e("venmo_library", "cannot encode note");
            }
        }

        try {
            venmo_uri += "&app_id=" + URLEncoder.encode(myAppId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("venmo_library", "cannot encode app ID");
        }

        try {
            venmo_uri += "&app_name=" + URLEncoder.encode(myAppName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("venmo_library", "cannot encode app Name");
        }

        try {
            venmo_uri += "&app_local_id=" + URLEncoder.encode("abcd", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("venmo_library", "cannot encode app local id");
        }

        venmo_uri += "&using_new_sdk=true";

        venmo_uri = venmo_uri.replaceAll("\\+", "%20"); // use %20 encoding instead of +

        return new Intent(Intent.ACTION_VIEW, Uri.parse(venmo_uri));
    }

    /**
     * Called once control has been given back to your app - it takes the signed_payload, decodes
     * it, and gives you the response object which gives you details about the transaction -
     * whether it was successful, the note, the amount,etc.
     */
    public VenmoResponse validateVenmoPaymentResponse(String signed_payload, String app_secret) {
        String encoded_signature;
        String payload;
        if (signed_payload == null) {
            return new VenmoResponse(null, null, null, "0");
        }
        try {
            String[] encodedsig_payload_array = signed_payload.split("\\.");
            encoded_signature = encodedsig_payload_array[0];
            payload = encodedsig_payload_array[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return new VenmoResponse(null, null, null, "0");
        }

        String decoded_signature = base64_url_decode(encoded_signature);

        String data;

        // check signature
        String expected_sig = hash_hmac(payload, app_secret, "HmacSHA256");

        VenmoResponse myVenmoResponse;

        if (decoded_signature.equals(expected_sig)) {
            //json decode data
            data = base64_url_decode(payload);
            try {
                JSONArray response = (JSONArray) JSONValue.parse(data);

                JSONObject obj = (JSONObject) response.get(0);

                String payment_id = obj.get("payment_id").toString();
                String note = obj.get("note").toString();
                String amount = obj.get("amount").toString();
                String success = obj.get("success").toString();

                myVenmoResponse = new VenmoResponse(payment_id, note, amount, success);

            } catch (Exception e) {
                myVenmoResponse = new VenmoResponse(null, null, null, "0");
            }
        } else {
            //Signature does NOT match
            myVenmoResponse = new VenmoResponse(null, null, null, "0");
        }

        return myVenmoResponse;

    }

    /**
    * @return a boolean indicating whether or not the Venmo app is installed on a client's device. 
    */
    public static boolean isVenmoInstalled(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(new Intent()
                .setComponent(new ComponentName(VENMO_PACKAGE, VENMO_ACTIVITY)), 0);

        return activities.size() == 1 &&
                VENMO_PACKAGE.equals(activities.get(0).activityInfo.packageName);
    }

    private static String hash_hmac(String payload, String app_secret, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secret = new SecretKeySpec(app_secret.getBytes(), algorithm);
            mac.init(secret);
            byte[] digest = mac.doFinal(payload.getBytes());
            return new String(digest);
        } catch (Exception e) {
            return "";
        }
    }

    private static String base64_url_decode(String payload) {
        String payload_modified = payload.replace('-', '+').replace('_', '/').trim();
        return new String(Base64.decode(payload_modified, Base64.DEFAULT));
    }

    /**
     * This is the object returned to you after a transaction has gone through.
     * It tells you whether it was successful, the amount, te note, and the payment id.
     */
    public class VenmoResponse {
        private String payment_id, note, amount, success;

        public VenmoResponse(String payment_id, String note, String amount, String success) {
            this.payment_id = payment_id;
            this.note = note;
            this.amount = amount;
            this.success = success;
        }

        public String getPaymentId() {
            return payment_id;
        }

        public String getNote() {
            return note;
        }

        public String getAmount() {
            return amount;
        }

        public String getSuccess() {
            return success;
        }
    }
}
