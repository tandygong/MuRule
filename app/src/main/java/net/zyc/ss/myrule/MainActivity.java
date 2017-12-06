package net.zyc.ss.myrule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MyRuleView.ScrollStopListener {

    private MyRuleView myRuleView;
  /*  private MyRuleView myRuleView1;
    private MyRuleView myRuleView2;
    private MyRuleView myRuleView3;
    private MyRuleView myRuleView4;
    private MyRuleView myRuleView5;
    private MyRuleView myRuleView6;
    private MyRuleView myRuleView7;
    private MyRuleView myRuleView8;
    private MyRuleView myRuleView9;
    private MyRuleView myRuleView10;
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myRuleView = (MyRuleView) findViewById(R.id.myRuleView);
       /* myRuleView1 = (MyRuleView) findViewById(R.id.myRuleView1);
        myRuleView2 = (MyRuleView) findViewById(R.id.myRuleView2);
        myRuleView3 = (MyRuleView) findViewById(R.id.myRuleView3);
        myRuleView4 = (MyRuleView) findViewById(R.id.myRuleView4);
        myRuleView5 = (MyRuleView) findViewById(R.id.myRuleView5);
        myRuleView6 = (MyRuleView) findViewById(R.id.myRuleView6);
        myRuleView7 = (MyRuleView) findViewById(R.id.myRuleView7);
        myRuleView8 = (MyRuleView) findViewById(R.id.myRuleView8);
        myRuleView9 = (MyRuleView) findViewById(R.id.myRuleView9);
        myRuleView10 = (MyRuleView) findViewById(R.id.myRuleView10);*/
        myRuleView.setScrollStopListener(this);
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i=0;i<30;i++) {
            arrayList.add(i+1+"号");
        }

        myRuleView.setDataList(arrayList);
    /*    myRuleView1.setDataList(arrayList);
        myRuleView2.setDataList(arrayList);
        myRuleView3.setDataList(arrayList);
        myRuleView4.setDataList(arrayList);
        myRuleView5.setDataList(arrayList);
        myRuleView6.setDataList(arrayList);
        myRuleView7.setDataList(arrayList);
        myRuleView8.setDataList(arrayList);
        myRuleView9.setDataList(arrayList);
        myRuleView10.setDataList(arrayList);*/

      myRuleView.setPointPos(2);
      myRuleView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Log.e("sdad","111");
          }
      });
    }

    @Override
    public void onScrollStop(MyRuleView rule, int pointPosition, String pointValue) {
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
