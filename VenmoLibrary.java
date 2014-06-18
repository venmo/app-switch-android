package com.venmo.demo; //Replace this with the name of your package 

import java.io.*;
import java.net.URLEncoder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

public class VenmoLibrary
{
	public VenmoLibrary()
	{
		
	}
	/*
	 * Takes the recipients, amount, and note, and returns an Intent object
	 */
	public static Intent openVenmoPayment(String myAppId, String myAppName, String recipients, String amount, String note, String txn)
	{
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
    		venmo_uri+= "&app_id=" + URLEncoder.encode(myAppId, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmo_library", "cannot encode app ID");
    	}
    	
    	try {
    		venmo_uri+= "&app_name=" + URLEncoder.encode(myAppName, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmo_library", "cannot encode app Name");
    	}
    	
    	try {
    		venmo_uri+= "&app_local_id=" + URLEncoder.encode("abcd", "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmo_library", "cannot encode app local id");
    	}
    	
    	venmo_uri += "&using_new_sdk=true";
    	
    	
    	venmo_uri = venmo_uri.replaceAll("\\+", "%20"); // use %20 encoding instead of +
    	
    	Intent nativeIntent= new Intent(Intent.ACTION_VIEW, Uri.parse(venmo_uri));
    	
    	
		return nativeIntent;
	}
	
	
	/*
	 * Takes the recipients, amount, and note, and returns a String representing the URL to visit to complete the transaction
	 */
	public static String openVenmoPaymentInWebView(String myAppId, String myAppName, String recipients, String amount, String note, String txn)
	{
		String venmo_uri = "https://venmo.com/touch/signup_to_pay?txn=" + txn;
        
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
    		venmo_uri+= "&app_id=" + URLEncoder.encode(myAppId, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmo_library", "cannot encode app ID");
    	}
    	
    	try {
    		venmo_uri+= "&app_name=" + URLEncoder.encode(myAppName, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmo_library", "cannot encode app Name");
    	}
    	
    	try {
    		venmo_uri+= "&app_local_id=" + URLEncoder.encode("abcd", "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmo_library", "cannot encode app local id");
    	}
    	
    	try {
    		venmo_uri+= "&client=" + URLEncoder.encode("android", "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmo_library", "cannot encode client=android");
    	}
    	
    	
    	
    	venmo_uri = venmo_uri.replaceAll("\\+", "%20"); // use %20 encoding instead of +
    	
		return venmo_uri;
	}
	
	//Called once control has been given back to your app - it takes the signed_payload, decodes it, and gives you the response object which 
	//gives you details about the transaction - whether it was successful, the note, the amount, etc. 
	public VenmoResponse validateVenmoPaymentResponse(String signed_payload, String app_secret)
	{
		String encoded_signature;
		String payload;
		if(signed_payload == null) {
			VenmoResponse myVenmoResponse = new VenmoResponse(null, null, null, "0");
			return myVenmoResponse;
		}
		try {
			String[] encodedsig_payload_array = signed_payload.split("\\.");
			encoded_signature = encodedsig_payload_array[0];
			payload = encodedsig_payload_array[1];
		}
		catch(ArrayIndexOutOfBoundsException e) {
			VenmoResponse myVenmoResponse = new VenmoResponse(null, null, null, "0");
			return myVenmoResponse;
		}
		
		String decoded_signature = base64_url_decode(encoded_signature);
		
		String data;
		
        // check signature 
        String expected_sig = hash_hmac(payload, app_secret, "HmacSHA256");
        
        VenmoResponse myVenmoResponse;
        
        if (decoded_signature.equals(expected_sig))
        {
            data = base64_url_decode(payload);
            //need to json decode data
            data = base64_url_decode(payload);
            
            
            //need to json decode data
            try
            {
                JSONArray response = (JSONArray)JSONValue.parse(data);
                
                JSONObject obj = (JSONObject)response.get(0);
                
                String payment_id = obj.get("payment_id").toString();
                String note = obj.get("note").toString();
                String amount = obj.get("amount").toString();
                String success = obj.get("success").toString();
                
                myVenmoResponse = new VenmoResponse(payment_id, note, amount, success);
                
            }
            catch(Exception e)
            {
                myVenmoResponse = new VenmoResponse(null, null, null, "0");
            } 
        }
        else
        {
            //Signature does NOT match
            myVenmoResponse = new VenmoResponse(null, null, null, "0");
        }
		
        return myVenmoResponse;
		
	}
	
	
	private static String hash_hmac(String payload, String app_secret, String algorithm)
	{
		try 
		{
		    Mac mac = Mac.getInstance(algorithm);
		    SecretKeySpec secret = new SecretKeySpec(app_secret.getBytes(), algorithm);
		    mac.init(secret);
		    byte[] digest = mac.doFinal(payload.getBytes());
		    String enc = new String(digest);
		    return enc;
		} 
		catch (Exception e) 
		{
		    return "";
		}
	}
	
	
	private static String base64_url_decode(String payload)
	{
		String payload_modified = payload.replace('-', '+').replace('_', '/').trim();
		String jsonString = new String(Base64.decode(payload_modified, Base64.DEFAULT));
		
		return jsonString;
	}
	
	
	//This is the object returned to you after a transaction has gone through.
	//It tells you whether it was successful, the amount, te note, and the payment id. 
	public class VenmoResponse
	{
		private String payment_id, note, amount, success;
		public VenmoResponse(String payment_id, String note, String amount, String success)
		{
			this.payment_id = payment_id;
			this.note = note;
			this.amount= amount;
			this.success = success;
		}
		public String getPaymentId()
		{
			return payment_id;
		}
		public String getNote()
		{
			return note;
		}
		public String getAmount()
		{
			return amount;
		}
		public String getSuccess()
		{
			return success;
		}
	}
	
	
}
