package net.zyc.ss.myrule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements MyRuleView.ScrollStopListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyRuleView myRuleView = (MyRuleView) findViewById(R.id.myRuleView);
        myRuleView.setScrollStopListener(this);
    }

    @Override
    public void onScrollStop(String rule, int pointPosition, String pointValue) {
        Log.e("rule", pointValue);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
