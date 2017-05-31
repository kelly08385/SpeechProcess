package com.mobvoi.robot;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class pictureActivity extends Activity {
    ImageView pictureImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        Intent intent = this.getIntent();
        String type = intent.getStringExtra("type");
        pictureImg = (ImageView)findViewById(R.id.id_pictureImg);

        if(type.equals("poem")){
            pictureImg.setImageResource(R.drawable.poemmoon);
        }else if(type.equals("openlight")){
            pictureImg.setImageResource(R.drawable.openlight);
        }else if(type.equals("closelight")){
            pictureImg.setImageResource(R.drawable.closelight);
        }
    }
}
