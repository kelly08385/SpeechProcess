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
import com.mobvoi.speech.SpeechClient;
import com.mobvoi.speech.SpeechClientListener;
import com.mobvoi.speech.TTSListener;
import com.mobvoi.speech.VadType;
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
import java.util.logging.LogRecord;

import com.mobvoi.robot.recipeActivity;


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
    static int recipeStepNum = 0;

    static boolean ncumslAPI = true;
    static int eatmedicinefreq = 0;
    static int tempfreq = 0;
    static boolean stop = false;

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

                        SpeechClient.getInstance().startAsrRecognizer(sDeviceOne);
                        Log.i(TAG,"asr");
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
    }

    public void init() {
        SpeechClient.getInstance().init(MainActivity.this, sAppKey, true, false);
        SpeechClient.getInstance().setLocationString(sDeviceOne, sLocation);
        SpeechClient.getInstance().addHotwordListener(hotwordListener);
        //監聽時間 設定為10秒
        SpeechClient.getInstance().setVad(sDeviceOne, VadType.DNNBasedVad, 50, 500);
        SpeechClient.getInstance().setClientListener(sDeviceOne, new SpeechClientListenerImpl());
    }


    public static class SpeechClientListenerImpl implements SpeechClientListener {
        static Boolean goset = false;
        static int hour = 0;
        static int minute = 0;

        @Override
        public void onStartRecord() {

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
            if (action.equals("eatmedicine")){
                String[] AfterSplit = result.split(":");
                action = "";

                result = eatmedicinefreq+"medicineaskingtwo"+result;

                Log.i(TAG,"result "+result);
                if(!AfterSplit[0].isEmpty() && !AfterSplit[1].isEmpty()){
                    goset = true;
                    hour = Integer.valueOf(AfterSplit[0]);
                    minute = Integer.valueOf(AfterSplit[1]);
                    Log.i(TAG,"hour" + AfterSplit[0] + "minute" + AfterSplit[1]);
                }
            }
            else if (action.equals("temperature")){
                String[] AfterSplit = result.split(":");
                action = "";

                result = tempfreq+"medicineaskingtwo"+result;

                Log.i(TAG,"result "+result);
                if(!AfterSplit[0].isEmpty() && !AfterSplit[1].isEmpty()){
                    goset = true;
                    hour = Integer.valueOf(AfterSplit[0]);
                    minute = Integer.valueOf(AfterSplit[1]);
                    Log.i(TAG,"hour" + AfterSplit[0] + "minute" + AfterSplit[1]);
                }
            }
            else if(action.equals("recipe")){
                if (result.equals("播放")){
                    Log.i(TAG,"play");
                    //不要跑 語意 api
                    ncumslAPI = false;
                    recipeActivity.playvideo();
                    SpeechClient.getInstance().startHotword();

                }
                else if(result.equals("下一步")){
                    ncumslAPI = false;
                    recipeStepNum += 1;
                    recipeStepNum = recipeActivity.stepTTS(recipeStepNum);
                }

            }

            // 鬧鈴結束
            if(result.equals("结束提醒")){
                closeAlarm(mContext);
                ncumslAPI = false;
                ttsSpeak("鬧鈴結束",false);
            }

            else if(result.equals("暂停")){
                ncumslAPI = false;
                stop = true;
            }

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
                    int timeoutSocket = 10000;
                    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                    DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
                    HttpResponse mHttpResponse = httpClient.execute(httpGet);


                    Log.i(TAG,String.valueOf(mHttpResponse.getStatusLine().getStatusCode()));
                    if(mHttpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK ){
                        String mJsonText = EntityUtils.toString(mHttpResponse.getEntity());
                        Log.i(TAG,"mjsontext " + mJsonText);


                        if (mJsonText.equals("抱歉無法理解您的意圖,請重新輸入")){
                            //tts 抱歉無法理解你的意圖
                            ttsSpeak("抱歉，無法理解你的意圖",false);
                        }
                        else{
                            //readstr : 是否要唸出 mJsonText
                            Boolean readstr = true;

                            //對話情境
                            if(mJsonText.indexOf("請問下次吃藥的時間是幾點幾分") > -1){
                                action = "eatmedicine";

                                int firstindex = mJsonText.indexOf("(");
                                int lastindex = mJsonText.indexOf(")");
                                System.out.println(mJsonText.substring(firstindex+1,lastindex));
                                eatmedicinefreq = Integer.parseInt(mJsonText.substring(firstindex+1,lastindex));

                                Log.i(TAG,"frequence "+ String.valueOf(eatmedicinefreq));
                                mJsonText = "請問下次吃藥的時間是幾點幾分";
                            }
                            else if(mJsonText.indexOf("請問下次量體溫的時間是幾點幾分") > -1){
                                action = "temperature";

                                int firstindex = mJsonText.indexOf("(");
                                int lastindex = mJsonText.indexOf(")");
                                System.out.println(mJsonText.substring(firstindex+1,lastindex));
                                tempfreq = Integer.parseInt(mJsonText.substring(firstindex+1,lastindex));

                                //Log.i(TAG,"frequence "+ String.valueOf(eatmedicinefreq));
                                mJsonText = "請問下次量體溫的時間是幾點幾分";

                            }

                            //食譜
                            if(mJsonText.indexOf("recipe") > -1){
                                action ="recipe";
                                String[] RecipeSplit = mJsonText.split(",");

                                if(!RecipeSplit[0].isEmpty()){
                                    //食譜初始化
                                    readstr = false;
                                    recipeStepNum = 0;

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
                                    recipeActivity.stepTTS(recipeStepNum-1);
                                }
                                else{
                                    mJsonText = "不在食譜情境";
                                }

                            }
                            //[食譜]原料輸入
                            else if(mJsonText.indexOf("ingredients") > -1){
                                action = "recipe";
                                String[] RecipeSplit = mJsonText.split(",");

                                Intent i = new Intent();
                                i.setClass(mContext,recipeActivity.class);
                                i.putExtra("type","ingredients");
                                i.putExtra("dish",RecipeSplit);
                                mContext.startActivity(i);

                            }

                            //[童話]若念的字串過大，分段唸出
                            Log.i(TAG,String.valueOf(mJsonText.length()));

                            if(mJsonText.length()>500){

                                //storyAry= mJsonText.split("。");
                                //Log.i(TAG,storyAry[0]);
                                int i;
                                for(i = 0; (i+1)*500 < mJsonText.length();i++){
                                    storyAry.add(mJsonText.substring(i*500, (i+1)*500));
                                    //Log.i(TAG,"story split " + storyAry.get(i));
                                }

                                storyAry.add(mJsonText.substring(i*500, mJsonText.length()));

                                readStory(storyAry.get(0));

                            }else if (readstr == true){
                                if(action.equals("eatmedicine")){
                                    ttsSpeak(mJsonText,true);
                                }
                                if(action.equals("temperature")){
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
            SpeechClient.getInstance().startHotword();
        }

        @Override
        public void onNoSpeechDetected() {

        }

        @Override
        public void onSpeechDetected() {

        }

        @Override
        public void onReady() {
            SpeechClient.getInstance().startHotword();
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    mtextview.setText(" 用你好問問來喚醒 ");

                } // This is your code
            };
            mainHandler.post(myRunnable);
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
                    if(storyAry != null && stop == false){
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

                    Log.i(TAG,String.valueOf(continueSpeak));


                    if (continueSpeak) {
                        //返回語音繼續監聽
                        SpeechClient.getInstance().startAsrRecognizer(sDeviceOne);
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                mtextview.setText(" 監聽中");

                            } // This is your code
                        };
                        mainHandler.post(myRunnable);

                    } else {
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

            //設定吃藥鬧鐘
            if(eatmedicinefreq > 0 && goset == true){
                setAlarm(mContext,hour,minute);
                eatmedicinefreq = 0;
                goset = false;
                action = "";

            }

        }

        private static int readStory(String str){
            ttsSpeak(str,false);

            Log.i(TAG,"end par");
            return 0;
        }

    }

    //設定吃藥鬧鐘
    public static void setAlarm(Context c,int hour, int minute){

        am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(c, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(c, 0, intent, 0);

        // We want the alarm to go off 10 seconds from now.
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());

        //calendar.add(Calendar.SECOND, 10);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Log.i(TAG,"frequence"+eatmedicinefreq);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
        //am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 10 * eatmedicinefreq * 1000, sender);

    }

    //關閉鬧鐘
    public static void closeAlarm(Context c){
        Intent intent = new Intent(c, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(c, 0, intent, 0);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);


    }


}
