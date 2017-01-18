package com.alexleo.partialrefreshdemo.act;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alexleo.partialrefreshdemo.R;
import com.alexleo.partialrefreshdemo.adapter.RvAdapter;
import com.alexleo.partialrefreshdemo.bean.Game;
import com.alexleo.partialrefreshdemo.manager.DownLoadManager;

import java.util.ArrayList;
import java.util.List;

public class RvAct extends AppCompatActivity {

    private RecyclerView rvActRv;
    private List<Game> games;
    private RvAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv);
        rvActRv = (RecyclerView) findViewById(R.id.rvActRv);

        games = new ArrayList<>();
        //模拟数据
        for (int i = 0; i < 40; i++) {
            games.add(new Game("游戏" + i,100));
        }

        adapter = new RvAdapter(this,games);

        rvActRv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        rvActRv.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownLoadManager.getInstance().stopAll();
    }
}
