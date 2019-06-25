package com.example.sx.practicalassistant.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
    public SMSReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals("SENT_SMS_ACTION")){
            int resultCode = getResultCode();
            switch(resultCode){
                case Activity.RESULT_OK:
                Toast.makeText(context,
                        "短信发送成功",Toast.LENGTH_SHORT) .show();
                break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context,
                            "短信发送失败",Toast.LENGTH_SHORT) .show();
                    break;
                }
        }else if(action.equals("DELIVERED_SMS_ACTION")){
            Toast.makeText(context,"收信人已经成功接收",Toast.LENGTH_SHORT).show();
        }
    }
}
