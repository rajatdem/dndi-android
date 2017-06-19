package com.example.msitese.dndiandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.List;


public class MainActivity extends AppCompatActivity implements DNDIFrameworkListener {

    private static final String TAG = "APP";
    private DNDIFramework dndi;

    private EditText mEditTestInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // config layout and UI components
        setContentView(R.layout.activity_main);
        configUIComponents();

        // initialize the DNDI framework
        dndi = new DNDIFramework(this);
    }

    @Override
    public void onKeywordMatch(List<String> keywords) {
        // TODO: implement the keyword match logic
    }

    private void configUIComponents(){
        mEditTestInput = (EditText) findViewById(R.id.etInput);
    }

    public void onClickPull(View view){
        dndi.pullDataInBatch();
    }

    public void onClickPeriodic(View view){

        int num;

        // convert the input string to integer
        try{
            num = Integer.valueOf(mEditTestInput.getText().toString());
        }
        catch (NumberFormatException ex){
            // if the string is invalid, assign a default value
            num = 5000;
        }
        dndi.configPeriodicMode(num);
    }

    public void onClickEvent(View view){
        dndi.configEventMode();
    }

    // Twitter related application code
    public void onClickTwitterOAuth(View view) {
//        new GetTwitterTokenTask(this).execute();
    }

    public void configTwitterCredential(String token, String secret, String screenName){
//        dndi.configTwitterCredential(token, secret, screenName);
    }

    public void onClickClearPreference(View view){
//        CredentialDAO dao = new CredentialDAO(this);
//        dao.clearTwitterCredential();
    }
}
