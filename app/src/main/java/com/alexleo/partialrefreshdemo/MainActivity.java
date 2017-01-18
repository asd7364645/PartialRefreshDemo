package com.alexleo.partialrefreshdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.alexleo.partialrefreshdemo.act.LvAct;
import com.alexleo.partialrefreshdemo.act.RvAct;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mLvBtn,mRvBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLvBtn = (Button) findViewById(R.id.mLvBtn);
        mRvBtn = (Button) findViewById(R.id.mRvBtn);
        mLvBtn.setOnClickListener(this);
        mRvBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mLvBtn:
                Intent intent1 = new Intent(this, LvAct.class);
                startActivity(intent1);
                break;
            case R.id.mRvBtn:
                Intent intent2 = new Intent(this, RvAct.class);
                startActivity(intent2);
                break;
        }
    }
}
