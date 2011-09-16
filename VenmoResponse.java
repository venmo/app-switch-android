package com.drinksonme.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

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