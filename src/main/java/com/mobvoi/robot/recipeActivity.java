package com.mobvoi.robot;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import com.mobvoi.robot.MainActivity.SpeechClientListenerImpl;

public class recipeActivity extends Activity {

    private static int stepi = 0;
    ImageView img;
    String title;
    TextView text;
    TextView stepView;
    WebView myWebView;
    String resultstr = new String();
    String imagestr = new String();
    String steptext = new String();
    String videourl = new String();

    static JSONArray stepAry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        Intent intent = this.getIntent();

        text = (TextView) findViewById(R.id.textView4);
        stepView = (TextView) findViewById(R.id.textView3);
        //final Button videobnt = (Button)findViewById(R.id.button2);

        img = (ImageView) findViewById(R.id.imageView);


        myWebView = (WebView) findViewById(R.id.web);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.requestFocus();
        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.toString());
                return false;
            }
        });

        String dish = intent.getStringExtra("dish");
        Log.i("SpeechSDKRobotDemo",dish);
        api(dish);


    }

    private void api(final String dish){
        Log.i("SpeechSDKRobotDemo","in the func api");
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                String uri = "http://60.251.236.15:5000/search_conv?key="+dish;
                String stringTemp = new String();
                Log.i("response",uri);

                HttpClient mHttpClient = new DefaultHttpClient();
                HttpGet mHttpGet = new HttpGet(uri);
                HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
                Log.i("response", String.valueOf(mHttpResponse.getStatusLine().getStatusCode()));
                if(mHttpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK ){
                    // 將資料實體化

                    String mJsonText = EntityUtils.toString(mHttpResponse.getEntity());
                    //Log.i("123",mJsonText);
                    JSONArray mTitle = new JSONArray(mJsonText);
                    System.out.println("*****JARRAY*****"+ mTitle.length());


                    Log.i("123","!!");
                    JSONArray json_data = mTitle.getJSONArray(0);
                    org.json.JSONObject jsonObj = json_data.getJSONObject(0);
                    Log.i("jsonObj",jsonObj.toString());

                    //text.setText(jsonObj.getString("title"));


                    Log.i("log_tag", "title" + jsonObj.getString("title"));
                    title = jsonObj.getString("title");
                    stringTemp = title;

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
                    resultstr = "為您示範以下這道菜"+ title + "原料" + material+ "步驟" + stepAry.getString(0);
                    final String resultText = "步驟\n"+ steptext;
                    Log.i("123",resultstr);

                    JSONArray imagesAry = jsonObj.getJSONArray("images");
                    imagestr = imagesAry.getString(0);
                    Log.i("123",videourl);
                    final String video_id = videourl.substring(videourl.indexOf("?v=") + 3,videourl.length());

                    Handler mainHandler = new Handler(getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            text.setText(title);
                            stepView.setText(resultText);
                            myWebView.loadUrl("http://www.youtube.com/embed/"+video_id+"?autoplay=1&vq=small");
                        } // This is your code
                    };

                    mainHandler.post(myRunnable);
                    new recipeActivity.DownloadImageTask((ImageView) img)
                            .execute(imagestr);

                    //SpeechClientListenerImpl test = new SpeechClientListenerImpl();
                    //test.selfTTS("123");

                    SpeechClientListenerImpl.ttsSpeak(resultstr,true);

                }
            } catch (Exception e){
            }
            }

        }).start();



    }

    public static void stepTTS(){
        Log.i("SpeechSDKRobotDemo","stepTTS");
        if(stepi < stepAry.length()){
            stepi +=1;
            try {
                SpeechClientListenerImpl.ttsSpeak(stepAry.getString(stepi),true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            stepi = 0;
        }
    }


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

}
