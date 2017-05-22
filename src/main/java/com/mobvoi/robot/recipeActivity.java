package com.mobvoi.robot;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;

import com.android.internal.http.multipart.MultipartEntity;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.mobvoi.robot.MainActivity.SpeechClientListenerImpl;
import com.google.android.youtube.player.YouTubePlayerView;




public class recipeActivity extends Activity implements YouTubePlayer.OnInitializedListener {

    ImageView img;
    String title;
    TextView text;
    TextView stepView;
    String resultstr = new String();
    String imagestr = new String();
    String steptext = new String();
    String videourl = new String();

    private static final int RECOVERY_REQUEST = 1;
    MyPlayerStateChangeListener playerStateChangeListener;
    MyPlaybackEventListener playbackEventListener;
    private static YouTubePlayer player;
    public static final String YOUTUBE_API_KEY = "AIzaSyACjKYt_6DAifk5NKnjKjhjiqn0meKQvVk";
    public static String VIDEO_ID = "fhWaJi1Hsfo";
    private YouTubePlayerFragment youTubePlayerFragment;

    //食譜步驟
    static JSONArray stepAry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("SpeechSDKRobotDemo","recipe start");
        setContentView(R.layout.activity_recipe);
        Intent intent = this.getIntent();


        text = (TextView) findViewById(R.id.textView4);
        stepView = (TextView) findViewById(R.id.textView3);
        //final Button videobnt = (Button)findViewById(R.id.button2);

        img = (ImageView) findViewById(R.id.imageView);

        String type = intent.getStringExtra("type");
        String dish = intent.getStringExtra("dish");
        Log.i("SpeechSDKRobotDemo",dish);

        stepAry = null;
        if(type.equals("menu")){
            apiget(dish);
            //apipost(dish);
        }else if (type.equals("ingredients")){
            apipost(dish);
        }

        youTubePlayerFragment = (YouTubePlayerFragment)getFragmentManager()
                .findFragmentById(R.id.youtubeplayerfragment);
        youTubePlayerFragment.initialize(YOUTUBE_API_KEY, this);

        playerStateChangeListener = new MyPlayerStateChangeListener();
        playbackEventListener = new MyPlaybackEventListener();


    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer yplayer, boolean wasRestored) {

        player = yplayer;
        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);
        if (!wasRestored) {
            player.cueVideo(VIDEO_ID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if ( errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {

        }
    }


    private void apiget(final String dish){
        Log.i("SpeechSDKRobotDemo","in the func api");
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                String uri = "http://60.251.236.15:5000/search_conv?key="+dish;

                Log.i("response",uri);

                HttpClient mHttpClient = new DefaultHttpClient();
                HttpGet mHttpGet = new HttpGet(uri);
                HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
                Log.i("response", String.valueOf(mHttpResponse.getStatusLine().getStatusCode()));
                if(mHttpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK ){
                    // 將資料實體化

                    String mJsonText = EntityUtils.toString(mHttpResponse.getEntity());
                    //Log.i("123",mJsonText);
                    jsonprocess(mJsonText);


                }
            } catch (Exception e){
            }

            }

        }).start();
    }

    private void jsonprocess(String mJsonText){
        try{
            JSONArray mTitle = new JSONArray(mJsonText);
            System.out.println("*****JARRAY*****"+ mTitle.length());


            Log.i("123","!!");
            JSONArray json_data = mTitle.getJSONArray(0);
            org.json.JSONObject jsonObj = json_data.getJSONObject(0);
            Log.i("jsonObj",jsonObj.toString());


            Log.i("log_tag", "title" + jsonObj.getString("title"));
            title = jsonObj.getString("title");
            String id = jsonObj.getString("id");
            Log.i("jsonid",id);


            org.json.JSONObject contentObj = jsonObj.getJSONObject("content");
            Log.i("content",contentObj.toString());
            videourl = jsonObj.getString("video");
            String material = contentObj.getString("原料");
            Log.i("content",material);
            steptext = "";
            stepAry = contentObj.getJSONArray("步驟");

            for(int j = 0;j<stepAry.length();j++){
                steptext = steptext + stepAry.getString(j) + '\n';
            }

            // tts 播報
            resultstr = "為您示範以下這道菜"+ title + "原料" + material+ "步驟" + stepAry.getString(0);

            //畫面顯示步驟
            final String resultText = "步驟\n"+ steptext;

            JSONArray imagesAry = jsonObj.getJSONArray("images");
            imagestr = imagesAry.getString(0);
            Log.i("123",videourl);
            VIDEO_ID = videourl.substring(videourl.indexOf("?v=") + 3,videourl.length());
            player.cueVideo(VIDEO_ID);

            Handler mainHandler = new Handler(getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    text.setText(title);
                    stepView.setText(resultText);

                } // This is your code
            };
            mainHandler.post(myRunnable);

            new recipeActivity.DownloadImageTask((ImageView) img)
                    .execute(imagestr);

            SpeechClientListenerImpl.ttsSpeak(resultstr,true);

        }catch (Exception e){

        }

    }

    //利用原料找
    private void apipost(final String ingredients){
        Log.i("123",ingredients);
        final HttpURLConnection[] httpcon = {null};
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                String result = "";
                try {
                    //instantiates httpclient to make request
                    // 1. create HttpClient
                    HttpClient httpclient = new DefaultHttpClient();

                    // 2. make POST request to the given URL
                    HttpPost httpPost = new HttpPost("http://60.251.236.15:5000/related_ingredients");
                    String json = "";

                    // 3. build jsonObject
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("ingredients", new JSONArray(new Object[] { "麵粉", "蛋", "高麗菜"} ));

                    // 4. convert JSONObject to JSON to String
                    json = jsonObject.toString();

                    // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                    // ObjectMapper mapper = new ObjectMapper();
                    // json = mapper.writeValueAsString(person);

                    // 5. set json to StringEntity
                    StringEntity se = new StringEntity(json);

                    // 6. set httpPost Entity
                    httpPost.setEntity(se);

                    // 7. Set some headers to inform server about the type of the content

                    httpPost.setHeader("Content-type", "application/json");


                    // 8. Execute POST request to the given URL
                    HttpResponse httpResponse = httpclient.execute(httpPost);

                    // 9. receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();

                    // 10. convert inputstream to string
                    if(inputStream != null){
                        result = convertStreamToString(inputStream);
                        jsonprocess(result);
                    }

                    else
                        result = "Did not work!";

                    Log.i("123",result);

                }catch (Exception e){

                }

            }
        }).start();

    }

    //將inputStream 轉成 result
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    // 下一步
    public static int stepTTS(int recipeStepNum){
        Log.i("SpeechSDKRobotDemo","stepTTS");
        if(recipeStepNum < stepAry.length()){
            try {
                SpeechClientListenerImpl.ttsSpeak(stepAry.getString(recipeStepNum),true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            SpeechClientListenerImpl.ttsSpeak("已結束食譜教學",false);
            recipeStepNum = 0;
        }
        return recipeStepNum;
    }

    public static void playvideo(){
        player.play();
    }

    // download 圖片
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {
        private void updateLog(String prompt){

        };
        @Override
        public void onAdStarted() {
            updateLog("onAdStarted()");
        }
        @Override
        public void onError(com.google.android.youtube.player.YouTubePlayer.ErrorReason arg0) {
            updateLog("onError(): " + arg0.toString());
        }
        @Override
        public void onLoaded(String arg0) {
            updateLog("onLoaded(): " + arg0);
        }
        @Override
        public void onLoading() {
            updateLog("onLoading()");
        }
        @Override
        public void onVideoEnded() {
            updateLog("onVideoEnded()");
        }
        @Override
        public void onVideoStarted() {
            updateLog("onVideoStarted()");
        }
    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {

        private void updateLog(String prompt){

        };
        @Override
        public void onBuffering(boolean arg0) {
            updateLog("onBuffering(): " + String.valueOf(arg0));
        }
        @Override    public void onPaused() {
            updateLog("onPaused()");
        }
        @Override
        public void onPlaying() {
            updateLog("onPlaying()");
        }
        @Override
        public void onSeekTo(int arg0) {
            updateLog("onSeekTo(): " + String.valueOf(arg0));
        }
        @Override
        public void onStopped() {
            updateLog("onStopped()");
        }
    }

}
