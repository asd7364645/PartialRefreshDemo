package com.alexleo.partialrefreshdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.alexleo.partialrefreshdemo.act.LvAct;

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
                Intent intent = new Intent(this, LvAct.class);
                startActivity(intent);
                break;
            case R.id.mRvBtn:
                break;
        }
    }
}
