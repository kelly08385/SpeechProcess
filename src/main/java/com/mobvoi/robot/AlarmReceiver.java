package com.mobvoi.robot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.mobvoi.robot.MainActivity.SpeechClientListenerImpl;


/**
 * Created by kelly on 2017/5/6.
 */

//每當鬧鐘觸發時，便會調用這個接收器，並執行onReceiver中的程序。
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"時間到",Toast.LENGTH_LONG).show();

        SpeechClientListenerImpl.ttsSpeak("提醒您，該吃藥了",false);
    }
}
