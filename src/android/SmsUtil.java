package com.imjeffparedes.SmsUtil;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;


/**
 * Created by Jefferson Paredes on July 20, 2019.
 */
public class SmsUtil 
extends CordovaPlugin {
     @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("sendSMS")) {
            Integer simID = args.getInt(0); 
            String toNum = args.getString(1); 
            String smsText = args.getString(2); 
            return this.sendSMS(simID, toNum, smsText, callbackContext);
        }
        return false;
    }

    private boolean sendSMS(int simID, String toNum, String smsText, CallbackContext callbackContext) {
        String name;
        Context ctx = this.cordova.getActivity().getApplicationContext(); 
        try {
            if (simID == 0) {
                name = "isms";
                // for model : "Philips T939" name = "isms0"
            } else if (simID == 1) {
                name = "isms2";
            } else {
                throw new Exception("can not get service which for sim '" + simID + "', only 0,1 accepted as values");
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            if (Build.VERSION.SDK_INT < 18) {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, toNum, null, smsText, null, null);
            } else {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, ctx.getPackageName(), toNum, null, smsText, null, null);
            }

            callbackContext.success("Message sent!");
            return true;
        }catch (Exception e) {
            callbackContext.error("Exception:" + e.getMessage());
            Log.e("apipas", "Exception:" + e.getMessage());
        }
        return false;
    }


}