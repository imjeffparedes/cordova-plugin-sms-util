package com.imjeffparedes;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class SmsUtil
        extends CordovaPlugin {
    private static final String LOGTAG = "SmsUtil";

    public static final String ACTION_SET_OPTIONS = "setOptions";
    private static final String ACTION_START_WATCH = "startWatch";
    private static final String ACTION_STOP_WATCH = "stopWatch";
    private static final String ACTION_ENABLE_INTERCEPT = "enableIntercept";
    private static final String ACTION_LIST_SMS = "listSMS";
    private static final String ACTION_DELETE_SMS = "deleteSMS";
    private static final String ACTION_RESTORE_SMS = "restoreSMS";
    private static final String ACTION_SEND_SMS = "sendSMS";

    public static final String OPT_LICENSE = "license";
    private static final String SEND_SMS_ACTION = "SENT_SMS_ACTION";
    private static final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String SMS_EXTRA_NAME = "pdus";

    public static final String SMS_URI_ALL = "content://sms/";
    public static final String SMS_URI_INBOX = "content://sms/inbox";
    public static final String SMS_URI_SEND = "content://sms/sent";
    public static final String SMS_URI_DRAFT = "content://sms/draft";
    public static final String SMS_URI_OUTBOX = "content://sms/outbox";
    public static final String SMS_URI_FAILED = "content://sms/failed";
    public static final String SMS_URI_QUEUED = "content://sms/queued";

    public static final String BOX = "box";
    public static final String ADDRESS = "address";
    public static final String BODY = "body";
    public static final String READ = "read";
    public static final String SEEN = "seen";
    public static final String SUBJECT = "subject";
    public static final String SERVICE_CENTER = "service_center";
    public static final String DATE = "date";
    public static final String DATE_SENT = "date_sent";
    public static final String STATUS = "status";
    public static final String REPLY_PATH_PRESENT = "reply_path_present";
    public static final String TYPE = "type";
    public static final String PROTOCOL = "protocol";

    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;
    public static final int MESSAGE_IS_NOT_SEEN = 0;
    public static final int MESSAGE_IS_SEEN = 1;

    private static final String SMS_GENERAL_ERROR = "SMS_GENERAL_ERROR";
    private static final String NO_SMS_SERVICE_AVAILABLE = "NO_SMS_SERVICE_AVAILABLE";
    private static final String SMS_FEATURE_NOT_SUPPORTED = "SMS_FEATURE_NOT_SUPPORTED";
    private static final String SENDING_SMS_ID = "SENDING_SMS";
    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";


    private ContentObserver mObserver = null;
    private BroadcastReceiver mReceiver = null;
    private boolean mIntercept = false;
    private String lastFrom = "";
    private String lastContent = "";

    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;
        if (ACTION_SET_OPTIONS.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            this.setOptions(options);
            result = new PluginResult(PluginResult.Status.OK);
        } else if (ACTION_START_WATCH.equals(action)) {
            result = this.startWatch(callbackContext);
        } else if (ACTION_STOP_WATCH.equals(action)) {
            result = this.stopWatch(callbackContext);
        } else if (ACTION_ENABLE_INTERCEPT.equals(action)) {
            boolean on_off = inputs.optBoolean(0);
            result = this.enableIntercept(on_off, callbackContext);
        } else if (ACTION_DELETE_SMS.equals(action)) {
            JSONObject msg = inputs.optJSONObject(0);
            result = this.deleteSMS(msg, callbackContext);
        } else if (ACTION_RESTORE_SMS.equals(action)) {
            JSONArray smsList = inputs.optJSONArray(0);
            result = this.restoreSMS(smsList, callbackContext);
        } else if (ACTION_LIST_SMS.equals(action)) {
            JSONObject filters = inputs.optJSONObject(0);
            result = this.listSMS(filters, callbackContext);
        } else if (ACTION_SEND_SMS.equals(action)) {
            int simId = inputs.optInt(0);
            String address = inputs.optString(1);
            String message = inputs.optString(2);
            result = this.sendSMS(simId, address, message, callbackContext);
        }else {
            Log.d(LOGTAG, String.format("Invalid action passed: %s", action));
            result = new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
        if (result != null) {
            callbackContext.sendPluginResult(result);
        }
        return true;
    }

    public void onDestroy() {
        this.stopWatch(null);
    }

    public void setOptions(JSONObject options) {
        Log.d(LOGTAG, ACTION_SET_OPTIONS);
    }

    protected String __getProductShortName() {
        return "SMS";
    }

    public final String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; ++i) {
                String h = Integer.toHexString(255 & messageDigest[i]);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException digest) {
            return "";
        }
    }

    private PluginResult startWatch(CallbackContext callbackContext) {
        Log.d(LOGTAG, ACTION_START_WATCH);
        if (this.mReceiver == null) {
            this.createIncomingSMSReceiver();
        }
        if (callbackContext != null) {
            callbackContext.success();
        }
        return null;
    }

    private PluginResult stopWatch(CallbackContext callbackContext) {
        Log.d(LOGTAG, ACTION_STOP_WATCH);
        Activity ctx = this.cordova.getActivity();
        if (this.mReceiver != null) {
            ctx.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
            Log.d(LOGTAG, "broadcast receiver unregistered");
        }
        if (this.mObserver != null) {
            ctx.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
            Log.d(LOGTAG, "sms inbox observer unregistered");
        }
        if (callbackContext != null) {
            callbackContext.success();
        }
        return null;
    }

    private PluginResult enableIntercept(boolean on_off, CallbackContext callbackContext) {
        Log.d(LOGTAG, ACTION_ENABLE_INTERCEPT);
        this.mIntercept = on_off;
        if (callbackContext != null) {
            callbackContext.success();
        }
        return null;
    }

    private PluginResult sendSMS(int simId, String address, String text, CallbackContext callbackContext) {
        Log.d(LOGTAG, ACTION_SEND_SMS);
        if (!checkSupport()) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "SMS is not supported"));
            return null;
        }

        Context ctx = this.cordova.getActivity().getApplicationContext();
        SmsManager manager = SmsManager.getDefault();
        final ArrayList<String> parts = manager.divideMessage(text);

        // by creating this broadcast receiver we can check whether or not the SMS was sent
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            boolean anyError = false; //use to detect if one of the parts failed
            String errorMessage = "";//use to identify any error
            int partsCount = parts.size(); //number of parts to send

            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case SmsManager.STATUS_ON_ICC_SENT:
                    case Activity.RESULT_OK:
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        errorMessage = "Unable to send sms. Generic failure cause.";
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        errorMessage = "SMS failed because service is currently unavailable";
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        errorMessage = "SMS Failed because no pdu provided";
                    case SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED:
                        errorMessage = "SMS Failed because the user has denied this app ever send premium short codes.";
                    case SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED:
                        errorMessage = "SMS Failed because user denied the sending of this short code";
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        errorMessage = "SMS Failed because service was explicitly turned off";
                        anyError = true;
                        break;
                }
                // trigger the callback only when all the parts have been sent
                partsCount--;
                if (partsCount == 0) {
                    if (anyError) {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, errorMessage));
                    } else {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,"SMS Sent!"));
                    }
                    cordova.getActivity().unregisterReceiver(this);
                }
            }
        };

        // randomize the intent filter action to avoid using the same receiver
        String intentFilterAction = SENT + java.util.UUID.randomUUID().toString();
        this.cordova.getActivity().registerReceiver(broadcastReceiver, new IntentFilter(intentFilterAction));

        PendingIntent sentIntent = PendingIntent.getBroadcast(this.cordova.getActivity(), 0, new Intent(intentFilterAction), 0);

        // depending on the number of parts we send a text message or multi parts
        if (parts.size() > 1) {
            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            for (int i = 0; i < parts.size(); i++) {
                sentIntents.add(sentIntent);
            }
            //send multipart sms
            manager.sendMultipartTextMessage(address, null, parts, sentIntents, null);
        }
        //send short sms
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx);
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                //get sim info
                SubscriptionInfo simInfo = (SubscriptionInfo) localList.get(simId); //0 or 1
                SmsManager.getSmsManagerForSubscriptionId(simInfo.getSubscriptionId()).sendTextMessage(address, null, text, sentIntent, null);
            }else{
                //send if device one sim
                manager.sendTextMessage(address, null, text, sentIntent, null);
            }
        } else {
            manager.sendTextMessage(address, null, text, sentIntent, null);
        }
        return null;
    }

    private PluginResult listSMS(JSONObject filter, CallbackContext callbackContext) {
        Log.i(LOGTAG, ACTION_LIST_SMS);
        String uri_filter = filter.has(BOX) ? filter.optString(BOX) : "inbox";
        int fread = filter.has(READ) ? filter.optInt(READ) : -1;
        int fid = filter.has("_id") ? filter.optInt("_id") : -1;
        String faddress = filter.optString(ADDRESS);
        String fcontent = filter.optString(BODY);
        int indexFrom = filter.has("indexFrom") ? filter.optInt("indexFrom") : 0;
        int maxCount = filter.has("maxCount") ? filter.optInt("maxCount") : 10;
        JSONArray jsons = new JSONArray();
        Activity ctx = this.cordova.getActivity();
        Uri uri = Uri.parse((SMS_URI_ALL + uri_filter));
        Cursor cur = ctx.getContentResolver().query(uri, (String[])null, "", (String[])null, null);
        int i = 0;
        while (cur.moveToNext()) {
            JSONObject json;
            boolean matchFilter = false;
            if (fid > -1) {
                matchFilter = (fid == cur.getInt(cur.getColumnIndex("_id")));
            } else if (fread > -1) {
                matchFilter = (fread == cur.getInt(cur.getColumnIndex(READ)));
            } else if (faddress.length() > 0) {
                matchFilter = faddress.equals(cur.getString(cur.getColumnIndex(ADDRESS)).trim());
            } else if (fcontent.length() > 0) {
                matchFilter = fcontent.equals(cur.getString(cur.getColumnIndex(BODY)).trim());
            } else {
                matchFilter = true;
            }
            if (! matchFilter) continue;

            if (i < indexFrom) continue;
            if (i >= indexFrom + maxCount) break;
            ++i;

            if ((json = this.getJsonFromCursor(cur)) == null) {
                callbackContext.error("failed to get json from cursor");
                cur.close();
                return null;
            }
            jsons.put((Object)json);
        }
        cur.close();
        callbackContext.success(jsons);
        return null;
    }

    private JSONObject getJsonFromCursor(Cursor cur) {
        JSONObject json = new JSONObject();

        int nCol = cur.getColumnCount();
        String keys[] = cur.getColumnNames();

        try {
            for(int j=0; j<nCol; j++) {
                switch(cur.getType(j)) {
                    case Cursor.FIELD_TYPE_NULL:
                        json.put(keys[j], null);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        json.put(keys[j], cur.getLong(j));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        json.put(keys[j], cur.getFloat(j));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        json.put(keys[j], cur.getString(j));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        json.put(keys[j], cur.getBlob(j));
                        break;
                }
            }
        } catch (Exception e) {
            return null;
        }

        return json;
    }

    private void fireEvent(final String event, JSONObject json) {
        final String str = json.toString();
        Log.d(LOGTAG, "Event: " + event + ", " + str);

        cordova.getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
                String js = String.format("javascript:cordova.fireDocumentEvent(\"%s\", {\"data\":%s});", event, str);
                webView.loadUrl( js );
            }
        });
    }

    private void onSMSArrive(JSONObject json) {
        String from = json.optString(ADDRESS);
        String content = json.optString(BODY);
        if (from.equals(this.lastFrom) && content.equals(this.lastContent)) {
            return;
        }
        this.lastFrom = from;
        this.lastContent = content;
        this.fireEvent("onSMSArrive", json);
    }


    protected void createIncomingSMSReceiver() {
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(SMS_RECEIVED)) {
                    // Create SMS container
                    SmsMessage smsmsg = null;
                    // Determine which API to use
                    if (Build.VERSION.SDK_INT >= 19) {
                        try {
                            SmsMessage[] sms = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                            smsmsg = sms[0];
                        } catch (Exception e) {
                            Log.d(LOGTAG, e.getMessage());
                        }
                    } else {
                        Bundle bundle = intent.getExtras();
                        Object pdus[] = (Object[]) bundle.get("pdus");
                        try {
                            smsmsg = SmsMessage.createFromPdu((byte[]) pdus[0]);
                        } catch (Exception e) {
                            Log.d(LOGTAG, e.getMessage());
                        }
                    }
                    // Get SMS contents as JSON
                    if(smsmsg != null) {
                        JSONObject jsms = SmsUtil.this.getJsonFromSmsMessage(smsmsg);
                        SmsUtil.this.onSMSArrive(jsms);
                        Log.d(LOGTAG, jsms.toString());
                    }else{
                        Log.d(LOGTAG, "smsmsg is null");
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(SMS_RECEIVED);
        try {
            webView.getContext().registerReceiver(this.mReceiver, filter);
        } catch (Exception e) {
            Log.d(LOGTAG, "error registering broadcast receiver: " + e.getMessage());
        }
    }


    protected void createContentObserver() {
        Activity ctx = this.cordova.getActivity();
        this.mObserver = new ContentObserver(new Handler()){

            public void onChange(boolean selfChange) {
                this.onChange(selfChange, null);
            }

            public void onChange(boolean selfChange, Uri uri) {
                ContentResolver resolver = cordova.getActivity().getContentResolver();
                Log.d(LOGTAG, ("onChange, selfChange: " + selfChange + ", uri: " + (Object)uri));
                int id = -1;
                String str;
                if (uri != null && (str = uri.toString()).startsWith(SMS_URI_ALL)) {
                    try {
                        id = Integer.parseInt(str.substring(SMS_URI_ALL.length()));
                        Log.d(LOGTAG, ("sms id: " + id));
                    }
                    catch (NumberFormatException var6_6) {
                        // empty catch block
                    }
                }
                if (id == -1) {
                    uri = Uri.parse(SMS_URI_INBOX);
                }
                Cursor cur = resolver.query(uri, null, null, null, "_id desc");
                if (cur != null) {
                    int n = cur.getCount();
                    Log.d(LOGTAG, ("n = " + n));
                    if (n > 0 && cur.moveToFirst()) {
                        JSONObject json;
                        if ((json = SmsUtil.this.getJsonFromCursor(cur)) != null) {
                            onSMSArrive(json);
                        } else {
                            Log.d(LOGTAG, "fetch record return null");
                        }
                    }
                    cur.close();
                }
            }
        };
        ctx.getContentResolver().registerContentObserver(Uri.parse(SMS_URI_INBOX), true, this.mObserver);
        Log.d(LOGTAG, "sms inbox observer registered");
    }

    private PluginResult deleteSMS(JSONObject filter, CallbackContext callbackContext) {
        Log.d(LOGTAG, ACTION_DELETE_SMS);
        String uri_filter = filter.has(BOX) ? filter.optString(BOX) : "inbox";
        int fread = filter.has(READ) ? filter.optInt(READ) : -1;
        int fid = filter.has("_id") ? filter.optInt("_id") : -1;
        String faddress = filter.optString(ADDRESS);
        String fcontent = filter.optString(BODY);
        Activity ctx = this.cordova.getActivity();
        int n = 0;
        try {
            Uri uri = Uri.parse((SMS_URI_ALL + uri_filter));
            Cursor cur = ctx.getContentResolver().query(uri, (String[])null, "", (String[])null, null);
            while (cur.moveToNext()) {
                int id = cur.getInt(cur.getColumnIndex("_id"));
                boolean matchId = fid > -1 && fid == id;
                int read = cur.getInt(cur.getColumnIndex(READ));
                boolean matchRead = fread > -1 && fread == read;
                String address = cur.getString(cur.getColumnIndex(ADDRESS)).trim();
                boolean matchAddr = faddress.length() > 0 && address.equals(faddress);
                String body = cur.getString(cur.getColumnIndex(BODY)).trim();
                boolean matchContent = fcontent.length() > 0 && body.equals(fcontent);
                if (!matchId && !matchRead && !matchAddr && !matchContent) continue;
                ctx.getContentResolver().delete(uri, "_id=" + id, (String[])null);
                ++n;
            }
            callbackContext.success(n);
        }
        catch (Exception e) {
            callbackContext.error(e.toString());
        }
        return null;
    }

    private JSONObject getJsonFromSmsMessage(SmsMessage sms) {
        JSONObject json = new JSONObject();

        try {
            json.put( ADDRESS, sms.getOriginatingAddress() );
            json.put( BODY, sms.getMessageBody() ); // May need sms.getMessageBody.toString()
            json.put( DATE_SENT, sms.getTimestampMillis() );
            json.put( DATE, System.currentTimeMillis() );
            json.put( READ, MESSAGE_IS_NOT_READ );
            json.put( SEEN, MESSAGE_IS_NOT_SEEN );
            json.put( STATUS, sms.getStatus() );
            json.put( TYPE, MESSAGE_TYPE_INBOX );
            json.put( SERVICE_CENTER, sms.getServiceCenterAddress());

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return json;
    }

    private ContentValues getContentValuesFromJson(JSONObject json) {
        ContentValues values = new ContentValues();
        values.put( ADDRESS, json.optString(ADDRESS) );
        values.put( BODY, json.optString(BODY));
        values.put( DATE_SENT,  json.optLong(DATE_SENT));
        values.put( READ, json.optInt(READ));
        values.put( SEEN, json.optInt(SEEN));
        values.put( TYPE, json.optInt(TYPE) );
        values.put( SERVICE_CENTER, json.optString(SERVICE_CENTER));
        return values;
    }
    private PluginResult restoreSMS(JSONArray array, CallbackContext callbackContext) {
        ContentResolver resolver = this.cordova.getActivity().getContentResolver();
        Uri uri = Uri.parse(SMS_URI_INBOX);
        int n = array.length();
        int m = 0;
        for (int i = 0; i < n; ++i) {
            JSONObject json;
            if ((json = array.optJSONObject(i)) == null) continue;
            String str = json.toString();
            Log.d(LOGTAG, str);
            Uri newuri = resolver.insert(uri, this.getContentValuesFromJson(json));
            Log.d(LOGTAG, ("inserted: " + newuri.toString()));
            ++m;
        }
        if (callbackContext != null) {
            callbackContext.success(m);
        }
        return null;
    }

    private boolean checkSupport() {
        return this.cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

}

