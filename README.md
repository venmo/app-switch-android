Venmo App Switch Android Library
==============================

Use this library to easily add Venmo payments to your Android app!  Just follow these steps:

Register your app with Venmo here: 

https://venmo.com/account/app/new 

![Create new application](https://dl.dropboxusercontent.com/s/ffo01uzr65y9kzw/GbalC.png)

For more information on creating a new application, go here: 

https://venmo.com/api#registering-an-application.

2. Download the following two files and include them in your directory: VenmoLibrary.java and VenmoWebViewActivity. You'll need to edit your manifest to add VenmoWebViewActivity as an activity, and you'll need to add your package name at the top of each file (e.g. "package com.name;").  Also, make sure your manifest includes the internet permission: "android.permission.INTERNET".

3. Download venmo_webview.xml and add it to your res/layout directory.  

4. You'll also need to download json_simple-1.1.jar from here: http://code.google.com/p/json-simple/.  Add this to your libs directory (create this folder if it doesn't already exist). You'll need to add that json jar to your class path.  To do so, in Eclipse go to Project -> Properties, and then click "Java Build Path" on the left.  Click the Libraries tab at the top.  Click "Add Jar" and then find the .jar file you just put in your libs directory.  Select it and click "OK".  See screenshot.png for a screenshot of this. 

5. Now, you're ready to use the library!  From the activity in your app where you want to open the Venmo app, include the following code:

```java
    try {
        Intent venmoIntent = VenmoLibrary.openVenmoPayment(app_id, app_name, recipient, amount, note, txn);
        startActivityForResult(venmoIntent, 1); //1 is the requestCode we are using for Venmo. Feel free to change this to another number. 
    }
    catch (android.content.ActivityNotFoundException e) //Venmo native app not install on device, so let's instead open a mobile web version of Venmo in a WebView
    {
        Intent venmoIntent = new Intent(MainActivity.this, VenmoWebViewActivity.class);
        String venmo_uri = VenmoLibrary.openVenmoPaymentInWebView(app_id, app_name, recipient, amount, note, txn);
        venmoIntent.putExtra("url", venmo_uri);
        startActivityForResult(venmoIntent, 1);
    }
```

where all of these parameters are Strings:

* app_id is the app_id you have registered with venmo.com 
* app_name is the name of your app 
* recipient is the venmo username, phone number, or email address of the person who is being paid or charged 
* amount is the amount to be paid or charged 
* note is the note that will be sent with the payment/charge.  For example, the note might be "for a drink on me!" 
* txn is either "pay" or "charge"

This will open the Venmo app's pay/charge screen if the user has the Venmo app installed on the phone.  If they don't have it installed, it will instead send them to the activity you added - VenmoWebViewActivity - which displays a mobile web version of Venmo in a WebView.  This will allow the user to enter his credit card information and complete the transaction. (Or, if they have an account but don't have the app for whatever reason, it will allow them to login and complete the transaction in the WebView.) 

If you look at the previous step, you'll see that the Venmo activity that allows the transaction to be completed is opened using the "startActivityForResult" method, which means that once the activity is finished, control will be yielded back to your activity.  To handle the response (i.e. to know whether the payment was completed successfully), implement Android's onActivityResult method in the same activity where you wrote the code in step 5.  This will look like the following: 

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode) {
            case 1: { //1 is the requestCode we picked for Venmo earlier when we called startActivityForResult
                if(resultCode == RESULT_OK) {
                    String signedrequest = data.getStringExtra("signedrequest");
                    if(signedrequest != null) {
                        VenmoResponse response = (new VenmoLibrary()).validateVenmoPaymentResponse(signedrequest, app_secret);
                        if(response.getSuccess().equals("1")) {
                            //Payment successful.  Use data from response object to display a success message
                            String note = response.getNote();
                            String amount = response.getAmount();
                        }
                    }
                    else {
                        String error_message = data.getStringExtra("error_message");
                        //An error ocurred.  Make sure to display the error_message to the user
                    }                               
                }
                else if(resultCode == RESULT_CANCELED) {
                    //The user cancelled the payment
                }
            break;
            }           
        }
    }

You'll need to add VenmoLibrary.VenmoResponse to your imports, like this: import com.venmo.demo.VenmoLibrary.VenmoResponse; (where com.venmo.demo is your package name)

Make sure you display the results of the transaction after it is completed.  The response variable above contains public methods you can use to access these variables:

* response.getSuccess()
* response.getNote()
* response.getAmount()
* response.getPaymentId()


And that's it!  Happy coding! 
  
