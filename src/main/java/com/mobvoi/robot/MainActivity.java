package com.mobvoi.robot;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Looper;
import android.renderscript.Sampler;
import android.util.Log;
import android.widget.TextView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import com.mobvoi.robot.HealthCare.HealthCareManager;
import com.mobvoi.speech.SpeechClient;
import com.mobvoi.speech.SpeechClientListener;
import com.mobvoi.speech.TTSListener;
import com.mobvoi.speech.VadType;
import com.mobvoi.speech.annotation.OnlineRecognizerApi;
import com.mobvoi.speech.hotword.HotwordListener;
import com.mobvoi.speech.tts.TTSRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import android.os.Handler;

import java.util.List;
import java.util.Objects;
import java.util.logging.LogRecord;

import com.mobvoi.robot.recipeActivity;
import com.mobvoi.robot.storyActivity;


import static com.mobvoi.robot.R.id.text;

public class MainActivity extends Activity {
    public static final String sDeviceOne = "Huawei-P9";
    public static final String sAppKey = "5CDE56529F8085228FF1414C79738D66";
    public static final String sLocation = "中国,北京市,北京市,北京市,,,39.980739,116.373026";
    private static final String TAG = "SpeechSDKRobotDemo";
    private static final String OUTPUT = "languageOutput";
    private static final String TTSTEXT = "displayText";
    private static final String CONTROL = "control";
    private static final String VOICECONTROL = "voiceControl";
    private static final String STARTVOICE = "start_voice";
    private static final String ENDVOICE = "end";
    private static final String AUDIOFILE = "hello.wav";


    static AlarmManager am;
    static TextView mtextview;

    private static Context mContext;
    public static Handler mainHandler = new Handler();
    static List<String> strList = new ArrayList<String>();
    static List<String> storyAry = new ArrayList<String>();
    //static String[] storyAry = null;
    static int aryCount = 0;

    static String action = "";

    //食譜步驟
    static int recipeStepNum = 1;

    static boolean ncumslAPI = true;
    static int takeCareFreq = 0;
    static int takeCareType = 0;

    static boolean stop = false;

    static HealthCareManager healthCareManager;
    static int diastolic = 0;
    static int systolic = 0;
    static int bpm = 0;
    static int bloodstep = 0;


    static int redhatIndex = 0;


    private HotwordListener hotwordListener = new HotwordListener() {

        @Override
        public void onHotwordDetected() {
            SpeechClient.getInstance().stopHotword();

            AssetManager manager = MainActivity.this.getAssets();
            try {
                AssetFileDescriptor descriptor = manager.openFd(AUDIOFILE);
                WavPlayer.getInstance().play(descriptor, new WavPlayer.Listener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onDone() {
                        Log.i(TAG,"asr");
                        SpeechClient.getInstance().startAsrRecognizer(sDeviceOne);

                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mtextview.setText(" 監聽中");

                            } // This is your code
                        };
                        mainHandler.post(myRunnable);
                    }

                    @Override
                    public void onError() {

                    }
                });

            } catch (IOException e) {
                Log.e(TAG, "Exception", e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mContext = this;
        mtextview = (TextView) findViewById(text);
        healthCareManager = new HealthCareManager(MainActivity.this);


    }

    public void init() {
        SpeechClient.getInstance().init(MainActivity.this, sAppKey, true, false);
        SpeechClient.getInstance().setLocationString(sDeviceOne, sLocation);
        SpeechClient.getInstance().addHotwordListener(hotwordListener);
        //監聽時間 設定為10秒
        SpeechClient.getInstance().enableLocalSilence(false);
        SpeechClient.getInstance().setRemoteVadParams(sDeviceOne, 5000, 1000);
        SpeechClient.getInstance().setClientListener(sDeviceOne, new SpeechClientListenerImpl());
        SpeechClient.getInstance().startHotword();
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mtextview.setText("用你好問問喚醒");

            } // This is your code
        };
        mainHandler.post(myRunnable);

    }


    public static class SpeechClientListenerImpl implements SpeechClientListener {

        @Override
        public void onStartRecord() {
            Log.i(TAG,"on start record");
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    mtextview.setText(" 監聽中");

                } // This is your code
            };
            mainHandler.post(myRunnable);

        }

        @Override
        public void onRemoteSilenceDetected() {

            Log.i(TAG,"on remote silence detected");
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    mtextview.setText(" 用你好問問來喚醒 ");

                } // This is your code
            };
            mainHandler.post(myRunnable);

        }

        @Override
        public void onLocalSilenceDetected() {
            Log.i(TAG,"on local silence detected");
        }

        @Override
        public void onVolume(double volume) {

        }

        @Override
        public void onPartialTranscription(String fixedContent) {
            Log.i(TAG, "partial result: " + fixedContent);
        }

        //抓到回傳回來的語音識別
        @Override
        public void onFinalTranscription(String result) {
            Log.i(TAG, "finalResult" + result);
            Log.i(TAG,"action" + action);
            ncumslAPI = true;

            //動作為吃藥，問什麼時候吃藥
            if (action.equals("TakeCare")){
                String[] AfterSplit = result.split(":");
                action = "";
                ncumslAPI = false;

                Log.i(TAG,"result "+result);
                if(!AfterSplit[0].isEmpty() && !AfterSplit[1].isEmpty()){
                    int hour = Integer.valueOf(AfterSplit[0]);
                    int minute = Integer.valueOf(AfterSplit[1]);
                    Log.i(TAG,"hour" + AfterSplit[0] + "minute" + AfterSplit[1]);
                    String speekstr = healthCareManager.enableAlarm(hour,minute,takeCareFreq,takeCareType);

                    ttsSpeak(speekstr,false);
                }
            }
            //食譜情境
            else if(action.equals("recipe")){
                Log.i(TAG,"recipestr" + String.valueOf(recipeStepNum));
                if (result.equals("播放")){
                    Log.i(TAG,"play");
                    //不要跑 語意 api
                    ncumslAPI = false;
                    recipeActivity.playvideo();
                    SpeechClient.getInstance().startHotword();

                }
                else if(result.equals("上一步")){
                    ncumslAPI = false;
                    recipeStepNum -= 1;
                    recipeStepNum = recipeActivity.stepTTS(recipeStepNum-1);
                }
                else if(result.equals("下一步")){
                    ncumslAPI = false;
                    recipeStepNum += 1;
                    recipeStepNum = recipeActivity.stepTTS(recipeStepNum-1);
                }
                if(recipeStepNum == 0){
                    action = "";
                    SpeechClientListenerImpl.ttsSpeak("已結束食譜教學", false);
                }


            }
            //[長照] 血壓情境
            else if(action.equals("bloodPressure")){
                ncumslAPI = false;
                if(bloodstep == 0){
                    diastolic = Integer.valueOf(result);
                    ttsSpeak("請問舒張壓多少",true);
                    bloodstep +=1;
                }else if(bloodstep == 1){
                    systolic = Integer.valueOf(result);
                    ttsSpeak("請問心率多少",true);
                    bloodstep += 1;
                }else{
                    bpm = Integer.valueOf(result);
                    healthCareManager.setBloodPressure(diastolic,systolic,bpm);
                    action = "";
                    bloodstep = 0;
                    ttsSpeak("完成紀錄",false);
                }


            }

            /*
            if(result.equals("暂停")){
                ncumslAPI = false;
                stop = true;
            }*/

            // 連線MSL語意API
            if(ncumslAPI == true){
                try{
                    //若字串有空白，error
                    result = result.replaceAll(" ", "");
                    Log.i(TAG, "finalResult" + result);
                    String uri = "http://140.115.53.190:8000/todo/api/v1.0/tasks/"+result;
                    Log.i(TAG, uri);

                    HttpGet httpGet = new HttpGet(uri);
                    HttpParams httpParameters = new BasicHttpParams();

                    int timeoutConnection = 3000;
                    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                    int timeoutSocket = 20000;
                    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                    DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
                    HttpResponse mHttpResponse = httpClient.execute(httpGet);


                    Log.i(TAG,String.valueOf(mHttpResponse.getStatusLine().getStatusCode()));
                    if(mHttpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK ){
                        String mJsonText = EntityUtils.toString(mHttpResponse.getEntity());
                        Log.i(TAG,"mjsontext " + mJsonText);

                        if (mJsonText.equals("抱歉無法理解您的意圖,請重新輸入")){
                            //tts 抱歉無法理解你的意圖
                            if(action.equals("recipe")){
                                ttsSpeak("沒聽清楚，請再說一遍",true);
                            }
                            else{
                                ttsSpeak("抱歉，無法理解你的意圖",false);
                            }
                        }
                        else{
                            //readstr : 是否要唸出 mJsonText
                            Boolean readstr = true;

                            //對話情境 TakeCare
                            if(mJsonText.indexOf("請問下次") > -1){
                                action = "TakeCare";
                                String[] splitStr = mJsonText.split(",");

                                takeCareFreq = Integer.valueOf(splitStr[1]);
                                takeCareType = switchType(splitStr[2]);

                                mJsonText = splitStr[0];
                                Log.i(TAG,"freq"+String.valueOf(takeCareFreq)+"Type"+String.valueOf(takeCareType));
                            }


                            //食譜
                            if(mJsonText.indexOf("recipe") > -1){
                                action ="recipe";
                                String[] RecipeSplit = mJsonText.split(",");

                                if(!RecipeSplit[0].isEmpty()){
                                    //食譜初始化
                                    readstr = false;
                                    recipeStepNum = 1;

                                    //開啟食譜頁面
                                    Intent i = new Intent();
                                    i.setClass(mContext,recipeActivity.class);
                                    i.putExtra("type","menu");
                                    i.putExtra("dish",RecipeSplit[0]);
                                    mContext.startActivity(i);
                                }else{
                                    mJsonText = "沒有這個食譜";
                                }
                            }
                            //[食譜]跳到第n步
                            else if(mJsonText.indexOf("step") > -1){
                                Log.i(TAG,mJsonText);
                                String[] StepSplit = mJsonText.split(",");

                                if(action.equals("recipe") && !StepSplit[0].isEmpty()){
                                    readstr = false;
                                    recipeStepNum = Integer.valueOf(StepSplit[0]);
                                    Log.i(TAG,String.valueOf(recipeStepNum));
                                    recipeStepNum = recipeActivity.stepTTS(recipeStepNum-1);
                                }
                                else{
                                    mJsonText = "不在食譜情境";
                                }

                            }
                            //[食譜]原料輸入
                            else if(mJsonText.indexOf("ingredients") > -1){
                                readstr = false;
                                action = "recipe";
                                String[] RecipeSplit = mJsonText.split(",");
                                Log.i(TAG,RecipeSplit.toString());
                                String[] copyRecipeSplit = new String[RecipeSplit.length-1];
                                System.arraycopy(RecipeSplit,0,copyRecipeSplit,0,copyRecipeSplit.length);
                                Log.i(TAG,copyRecipeSplit.toString());

                                Intent i = new Intent();
                                i.setClass(mContext,recipeActivity.class);
                                i.putExtra("type","ingredients");
                                i.putExtra("dish",copyRecipeSplit);
                                mContext.startActivity(i);

                            }
                            //[長照] 紀錄
                            else if(mJsonText.indexOf("action:record") > -1){
                                String[] splitstr = mJsonText.split(":");
                                int firstindex = splitstr[1].indexOf("(");
                                int lastindex = splitstr[1].indexOf(")");

                                if (mJsonText.indexOf("Temperature")>-1 || mJsonText.indexOf("recordbloodGlucose") > -1){
                                    String type = splitstr[1].substring(0,firstindex);
                                    Log.i(TAG,"type"+type+"value"+splitstr[1].substring(firstindex+1,lastindex));
                                    double value = Double.parseDouble(splitstr[1].substring(firstindex+1,lastindex));

                                    writeDB(type,value);
                                    mJsonText = "已幫你記錄";
                                }
                                else if(mJsonText.indexOf("血壓") > -1){
                                    action = "bloodPressure";
                                    mJsonText = "請問你的收縮壓多少";
                                }

                            }
                            //[長照] 顯示
                            else if(mJsonText.indexOf("action:display") > -1){
                                readstr = false;
                                String[] splitstr = mJsonText.split(":");
                                int firstindex = splitstr[1].indexOf("(");
                                int lastindex = splitstr[1].indexOf(")");
                                String type = splitstr[1].substring(firstindex+1,lastindex);
                                Log.i(TAG,"type"+type);
                                showDB(type);

                            }
                            // [長照] 取消鬧鈴
                            else if(mJsonText.indexOf("action:cancle")>-1){
                                readstr = false;
                                String[] splitstr = mJsonText.split(":");
                                int firstindex = splitstr[1].indexOf("(");
                                int lastindex = splitstr[1].indexOf(")");
                                String type = splitstr[1].substring(firstindex+1,lastindex);
                                Log.i(TAG,"type"+type);
                                cancleAlarm(type);

                            }
                            //[童話]
                            else if(mJsonText.indexOf("storycontent")>-1){
                                String[] splitstr = mJsonText.split("storycontent");
                                if (splitstr[0].equals("小紅帽")){
                                    action = "redhat";

                                    readstr = false;
                                    Intent i = new Intent();
                                    i.setClass(mContext,storyActivity.class);
                                    mContext.startActivity(i);

                                }
                                else{
                                    if(splitstr[1].length()>500){

                                        //storyAry= mJsonText.split("。");
                                        //Log.i(TAG,storyAry[0]);
                                        int i;
                                        for(i = 0; (i+1)*500 < splitstr[1].length();i++){
                                            storyAry.add(splitstr[1].substring(i*500, (i+1)*500));
                                            //Log.i(TAG,"story split " + storyAry.get(i));
                                        }

                                        storyAry.add(splitstr[1].substring(i*500, splitstr[1].length()));

                                        readStory(storyAry.get(0));

                                    }
                                }

                            }
                            //[唐詩][開燈]
                            else if(mJsonText.indexOf("poem") > -1 || result.indexOf("灯") > -1){
                                String[] splitstr = mJsonText.split(",");
                                mJsonText = splitstr[0];

                                Intent i = new Intent();
                                i.setClass(mContext,pictureActivity.class);
                                if(result.indexOf("静夜思") > -1){
                                    i.putExtra("type","poem");
                                    mContext.startActivity(i);
                                }else if (result.indexOf("开灯") > -1){
                                    i.putExtra("type","openlight");
                                    mContext.startActivity(i);
                                }else if(result.indexOf("关灯")>-1){
                                    i.putExtra("type","closelight");
                                    mContext.startActivity(i);
                                }


                            }


                            if (readstr == true){
                                if(action.equals("TakeCare") || action.equals("bloodPressure")){
                                    ttsSpeak(mJsonText,true);
                                }
                                //若有抓到結果，直接唸出
                                ttsSpeak(mJsonText,false);
                            }
                        }

                    }
                    else{
                        Log.i(TAG," tom api error");
                        SpeechClient.getInstance().startHotword();
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mtextview.setText(" 用你好問問來喚醒 ");

                            } // This is your code
                        };
                        mainHandler.post(myRunnable);
                    }

                } catch (IOException e1) {
                    Log.i(TAG,e1.toString());
                    e1.printStackTrace();
                    ttsSpeak("連結錯誤",false);

                }

            }
        }

        @Override
        public void onResult(String result) {
            /*
            Log.i(TAG, result);
            String callResult = "";
            try {
                JSONObject jsonObject = JSONObject.parseObject(result);
                String realResult = ((JSONObject) jsonObject.get(OUTPUT)).get(TTSTEXT).toString();
                Log.i(TAG,realResult);

                //要不要念tts
                if(!realResult.equals("為您示範以下這道菜") && action.equals("")){
                    ttsSpeak(realResult,1);
                }

                //回傳回來的json動作
                callResult = ((JSONObject) jsonObject.get("clientAction")).get("action").toString();
                //----------食譜----------
                if(callResult.equals("com.mobvoi.recipe")){
                    String dish = ((JSONObject)((JSONObject) jsonObject.get("clientAction")).get("extras")).get("dish").toString();

                    final String[] separated = dish.split("::");
                    Log.i(TAG,separated[0]);

                    //開啟食譜頁面
                    Intent i = new Intent();
                    i.setClass(mContext,recipeActivity.class);
                    i.putExtra("dish",separated[0]);
                    mContext.startActivity(i);

                }

            } catch (JSONException e) {
                Log.d(TAG, e.toString());
                SpeechClient.getInstance().startHotword();
            }
            */

        }

        @Override
        public void onError(int errorCode) {
            ttsSpeak("結束情境",false);

            Log.i(TAG,"on error");
        }

        @Override
        public void onNoSpeechDetected() {
            Log.i(TAG,"on no speech detected");
        }

        @Override
        public void onSpeechDetected() {
            Log.i(TAG,"on speech detected");
        }

        //語音合成測試
        public static void ttsSpeak(final String str, final boolean continueSpeak){
            Log.i(TAG,"str"+str);
            strList.add(str);

            SpeechClient.getInstance().startTTS(new TTSRequest(str), new TTSListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onError() {
                    SpeechClient.getInstance().startHotword();
                }

                @Override
                public void onDone() {
                    Log.i(TAG,"array size" + String.valueOf(storyAry.size()));
                    Boolean ondoneflag = true;
                    //[童話]
                    if(redhatIndex == 7){
                        redhatIndex = 0;
                        action = "";
                    }
                    else if(action.equals("redhat")){
                        redhatIndex += 1;
                        ondoneflag = false;
                        storyActivity.readNextContent(redhatIndex);
                    }
                    else if(storyAry != null && stop == false){

                        /*
                        if (aryCount < storyAry.length-1){
                            readStory(storyAry[aryCount+1]);
                            Log.i(TAG,storyAry[aryCount+1]);
                            aryCount += 1;

                        }
                        else{
                            storyAry = null;
                            aryCount =0;
                        }*/

                        if(aryCount < storyAry.size()-1){
                            readStory(storyAry.get(aryCount+1));
                            Log.i(TAG,storyAry.get(aryCount+1));
                            aryCount += 1;
                        }
                        else {
                            List<String> storyAry = new ArrayList<String>();
                            aryCount = 0;
                        }

                    }




                    if (continueSpeak && ondoneflag) {
                        if(action.equals("recipe")){
                            SpeechClient.getInstance().setRemoteVadParams(sDeviceOne, 30000, 1000);
                        }
                        else {
                            SpeechClient.getInstance().setRemoteVadParams(sDeviceOne, 5000, 1000);
                        }

                        //返回語音繼續監聽
                        SpeechClient.getInstance().startAsrRecognizer(sDeviceOne);

                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mtextview.setText(" 監聽中");
                            } // This is your code
                        };
                        mainHandler.post(myRunnable);

                    } else if(ondoneflag) {

                        //需用你好問問喚醒
                        SpeechClient.getInstance().startHotword();
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mtextview.setText(" 用你好問問來喚醒 ");

                            } // This is your code
                        };
                        mainHandler.post(myRunnable);
                    }
                }
            });
        }

        private static int readStory(String str){
            ttsSpeak(str,false);

            Log.i(TAG,"end par");
            return 0;
        }

    }

    //Take care 轉換type
    public static int switchType(String Type){
        if(Type.equals("吃藥")){
            return DBHelper.TYPE_MEDICINE;
        }
        else if(Type.equals("量體溫")){
            return DBHelper.TYPE_TEMPERATURE;
        }
        else{
            return 0;
        }
    }

    //[長照] 寫入資料庫
    public static void writeDB(String type, double value){
        if(type.equals("recordTemperature")){
            healthCareManager.setTemperature(value);
        }else if(type.equals("recordbloodGlucose")){
            healthCareManager.setBloodGlucose(value);
        }
    }
    //[長照] 顯示資料庫
    public static void showDB(String type){
        if(type.equals("體溫")){
            healthCareManager.showTemperature();
        }
        else if(type.equals("血糖")){
            healthCareManager.showBloodGlucose();
        }
        else if(type.equals("血壓")){
            healthCareManager.showBloodPressure();
        }
    }
    //[長照]取消鬧鈴
    public static void cancleAlarm(String type){
        if(type.equals("體溫")){
            healthCareManager.disableAlarm(DBHelper.TYPE_TEMPERATURE);
        }
        else if(type.equals("血糖")){
            healthCareManager.disableAlarm(DBHelper.TYPE_BLOOD_GLUCOSE);
        }
        else if(type.equals("血壓")){
            healthCareManager.disableAlarm(DBHelper.TYPE_BLOOD_PRESSURE);
        }
        else if(type.equals("藥")){
            healthCareManager.disableAlarm(DBHelper.TYPE_MEDICINE);
        }
    }

}
