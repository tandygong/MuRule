package net.zyc.ss.myrule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MyRuleView.ScrollStopListener {

    private MyRuleView myRuleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myRuleView = (MyRuleView) findViewById(R.id.myRuleView);
        myRuleView.setScrollStopListener(this);
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i=0;i<30;i++) {
            arrayList.add(i+1+"号");
        }

        myRuleView.setDataList(arrayList);
    }

    @Override
    public void onScrollStop(String rule, int pointPosition, String pointValue) {
        Log.e("rule", pointValue);
    }

    public void pointTo(View view) {
        int pointPos = myRuleView.getPointPos();
        myRuleView.setPointPos(2+pointPos,false);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
