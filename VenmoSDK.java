package com.drinksonme.util; 

import java.io.*;
import java.util.*;
import java.net.URLEncoder;

import org.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class VenmoSDK
{
	
	//private String myAppID, myAppLocalId, myAppName;
	public VenmoSDK(String myAppId, String myAppLocalId, String myAppName)
	{
		/*this.myAppId = myAppId;
		this.myAppLocalId = myAppLocalId;
		this.myAppName = myAppName;*/
		
	}
	
	/*
	 * Takes the recipients, amount, and note, and returns an Intent object
	 */
	public static Intent openVenmoPayment(String myAppId, String myAppLocalId, String myAppName, String recipients, String amount, String note, String txn)
	{
		String venmo_uri = "venmo://paycharge?txn=" + txn;

    	
    	if (!recipients.equals("")) {
    		try {
    			venmo_uri += "&recipients=" + URLEncoder.encode(recipients, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
				Log.e("venmodemo", "cannot encode recipients");
			}
    	}
    	if (!amount.equals("")) {
    		try {
				venmo_uri += "&amount=" + URLEncoder.encode(amount, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.e("venmodemo", "cannot encode amount");
			}
    	}
    	if (!note.equals("")) {
    		try {
    			venmo_uri += "&note=" + URLEncoder.encode(note, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			Log.e("venmodemo", "cannot encode note");
			}
    	}
    	
    	try {
    		venmo_uri+= "&app_id=" + URLEncoder.encode(myAppId, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmodemo", "cannot encode app ID");
    	}
    	
    	try {
    		venmo_uri+= "&app_name=" + URLEncoder.encode(myAppName, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmodemo", "cannot encode app Name");
    	}
    	
    	try {
    		venmo_uri+= "&app_local_id=" + URLEncoder.encode(myAppLocalId, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmodemo", "cannot encode app local id");
    	}
    	
    	
    	Log.d("VenmoSDK", "URI: " + venmo_uri);
    	
    	venmo_uri = venmo_uri.replaceAll("\\+", "%20"); // use %20 encoding instead of +
    	
    	Intent nativeIntent= new Intent(Intent.ACTION_VIEW, Uri.parse(venmo_uri));
    	
    	Log.v("VenmoSDK", "note: " + note);
    	
		return nativeIntent;
	}
	
	
	/*
	 * Takes the recipients, amount, and note, and returns an Intent object
	 */
	public static Intent openVenmoPaymentInBrowser(String myAppId, String myAppLocalId, String myAppName, String recipients, String amount, String note, String txn)
	{
		String venmo_uri = "https://venmo.com/touch/signup_to_pay?txn=" + txn;

    	if (!recipients.equals("")) {
    		try {
    			venmo_uri += "&recipients=" + URLEncoder.encode(recipients, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
				Log.e("venmodemo", "cannot encode recipients");
			}
    	}
    	if (!amount.equals("")) {
    		try {
				venmo_uri += "&amount=" + URLEncoder.encode(amount, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.e("venmodemo", "cannot encode amount");
			}
    	}
    	if (!note.equals("")) {
    		try {
    			venmo_uri += "&note=" + URLEncoder.encode(note, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			Log.e("venmodemo", "cannot encode note");
			}
    	}
    	
    	try {
    		venmo_uri+= "&app_id=" + URLEncoder.encode(myAppId, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmodemo", "cannot encode app ID");
    	}
    	
    	try {
    		venmo_uri+= "&app_name=" + URLEncoder.encode(myAppName, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmodemo", "cannot encode app Name");
    	}
    	
    	try {
    		venmo_uri+= "&app_local_id=" + URLEncoder.encode(myAppLocalId, "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmodemo", "cannot encode app local id");
    	}
    	
    	try {
    		venmo_uri+= "&client=" + URLEncoder.encode("android", "UTF-8");
    	}
    	catch(UnsupportedEncodingException e)
    	{
    		Log.e("venmodemo", "cannot encode client=android");
    	}
    	
    	
    	Log.d("VenmoSDK", "URI: " + venmo_uri);
    	
    	venmo_uri = venmo_uri.replaceAll("\\+", "%20"); // use %20 encoding instead of +
    	
    	Intent nativeIntent= new Intent(Intent.ACTION_VIEW, Uri.parse(venmo_uri));
    	
		return nativeIntent;
	}
	
	
	public static VenmoResponse validateVenmoPaymentResponse(String signed_payload, String app_secret)
	{
		String[] encodedsig_payload_array = signed_payload.split("\\.");
		String encoded_signature = encodedsig_payload_array[0];
		String payload = encodedsig_payload_array[1];
		String decoded_signature = base64_url_decode(encoded_signature);
		
		Log.d("VenmoSDK", "decoded_signature: " + decoded_signature);
		
		String data;
		
		 // check signature 
		 String expected_sig = hash_hmac(payload, app_secret, "HmacSHA256");
		 
		 Log.d("VenmoSDK", "expected_sig using HmacSHA256:" + expected_sig);
		 
		 VenmoResponse myVenmoResponse;
		 
		 if (decoded_signature.equals(expected_sig))
		 {
			 Log.d("VenmoSDK", "Signature matches!");
			 data = base64_url_decode(payload);
			 Log.v("VenmoSDK", "base64 decoded payload: " + data);
			 //need to json decode data
			 data = base64_url_decode(payload);
			 Log.v("VenmoSDK", "base64 decoded payload: " + data);
			 
			 
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
				 Log.d("VenmoSDK", "Exception caught: " + e.getMessage());
				 myVenmoResponse = new VenmoResponse(null, null, null, "0");
			 } 
		 }
		 else
		 {
			 Log.d("VenmoSDK", "Signature does NOT match");
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
		    Log.d("VenmoSDK Error Message Caught", e.getMessage());
		    return "";
		}
	}
	
	
	private static String base64_url_decode(String payload)
	{
		String payload_modified = payload.replace('-', '+').replace('_', '/').trim();
		String jsonString = new String(Base64.decode(payload_modified, Base64.DEFAULT));
		
		return jsonString;
	}
}