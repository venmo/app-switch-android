Use this SDK to easily add Venmo payments to your Android app!  Just follow these steps:

1) First you need to register your app with Venmo here: https://venmo.com/account/app/new (for a description on what these fields are,
go here: https://venmo.com/api#registering-an-application). After your app has been approved, you will need to get your app credentials here: https://venmo.com/account/settings/developers.  

2) Download the two files listed in this repository, and include them in your directory: VenmoSDK.java and VenmoResponse.java. You'll need to add your package name at the top of each file (e.g. "package com.name;") 

3) You'll also need to download json_simple-1.1.jar from here: http://code.google.com/p/json-simple/.  Add this to your libs directory (create this folder if it doesn't already exist). 

4) You need to add that json jar to your class path.  To do so, in Eclipse go to Project -> Properties, and then click "Java Build Path" on the left.  Click the Libraries tab at the top.  Click "Add Jar" and then find the .jar file you just put in your libs directory.  Select it and click "OK".  See screenshot.png for a screenshot of this. 

5) Now, you're ready to use the SDK!  From your app, include the following code when you want the Venmo app to open:

    Intent sendIntent = VenmoSDK.openVenmoPayment(app_id, local_app_id, app_name, recipient, amount, note, txn);
    try{
        myListActivity.startActivity(sendIntent);
    }
    catch (ActivityNotFoundException e) // Exception thrown when Venmo native app not install on device, so fallback to web version
    {
    	sendIntent = VenmoSDK.openVenmoPaymentInBrowser(app_id, local_app_id, app_name, recipient, amount, note, txn);
    	myListActivity.startActivity(sendIntent);
    }

where all of these parameters are Strings:

* app_id is the app_id you have registered with venmo.com 
* app_local_id is something you make up. An example is "abcd".  
* app_name is the name of your app 
* recipient is the venmo username, phone number, or email address of the person who is being paid or charged 
* amount is the amount to be paid or charged 
* note is the note that will be sent with the payment/charge.  For example, the note might be "for a drink on me!" 
* txn is either "pay" or "charge"


Then, you need to provide a way for the Venmo app to be able to get back to your app after the request goes through.  Here's how: 

6) Add this to your manifest file, inside of your <application> </application> tags: 

	<activity android:name=".URLActivity">
		<intent-filter>
	      <action android:name="android.intent.action.VIEW" />
	      <category android:name="android.intent.category.DEFAULT" />
	      <category android:name="android.intent.category.BROWSABLE" />
	       <data android:scheme="venmo9999abcd" /> 
	    </intent-filter> 
	</activity>

where 9999 is your app_id and abcd is the app_local_id you created above.

7) Create a file named URLActivity.java, which should extend Activity.  This is the activity that is called when control is given back to your app, after a payment has gone through and the user pressed "back to your app".  Inside of the onCreate method, include the following:

	Uri data = getIntent().getData();
	String signed_request = data.getQueryParameter("signed_request");	
	VenmoResponse response = VenmoSDK.validateVenmoPaymentResponse(signed_request, app_secret);

where app_secret is the secret key you were given when you registered your app with Venmo.  

If you want to display the results of the transaction, then the response variable contains public methods you can use to access these variables:

* response.getSuccess()
* response.getNote()
* response.getAmount()
* response.getPaymentId()


And that's it!  Happy coding! 
  
