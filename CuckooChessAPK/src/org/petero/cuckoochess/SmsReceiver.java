package org.petero.cuckoochess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.petero.cuckoochess.CuckooChess.SmsHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	SmsHandler handler;
	
	public SmsReceiver(SmsHandler handler) {
		this.handler = handler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		//---get the SMS message passed in---
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String str = "";            
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);  
                if(msgs[i]==null) {
            		continue;
                }
//                Log.i("",msgs[i].getDisplayMessageBody());
                Pattern p = Pattern.compile("##ChessTouch## ",Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher m = p.matcher(msgs[i].getDisplayMessageBody());
            	if(m.find()) {
                    handler.sendMessage(handler.obtainMessage(0, msgs[i]));
            		this.abortBroadcast();
            	}
            }
        }
	}

}
