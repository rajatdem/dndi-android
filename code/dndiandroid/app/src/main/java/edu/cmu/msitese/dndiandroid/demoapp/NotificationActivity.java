package edu.cmu.msitese.dndiandroid.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import edu.cmu.msitese.dndiandroid.R;


public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "DNDI_APP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        TextView textView = (TextView) findViewById(R.id.textView2);

        Intent intent = getIntent();
        if(intent.hasExtra("match")){
            String topic = intent.getStringExtra("match");
            textView.setText("(" + topic + ")");
        }
        else{
            textView.setText("");
        }
    }

    public void onClickBackBtn(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
