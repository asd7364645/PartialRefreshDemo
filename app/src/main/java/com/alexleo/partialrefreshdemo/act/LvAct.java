package com.alexleo.partialrefreshdemo.act;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.alexleo.partialrefreshdemo.R;
import com.alexleo.partialrefreshdemo.adapter.LvAdapter;
import com.alexleo.partialrefreshdemo.bean.Game;
import com.alexleo.partialrefreshdemo.manager.DownLoadManager;

import java.util.ArrayList;
import java.util.List;

public class LvAct extends AppCompatActivity {

    private ListView lvActLv;
    private List<Game> games;
    private LvAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lv);

        lvActLv = (ListView) findViewById(R.id.lvActLv);
        games = new ArrayList<>();
        //模拟数据
        for (int i = 0; i < 40; i++) {
            games.add(new Game("游戏" + i,100));
        }

        adapter = new LvAdapter(this,lvActLv,games);
        lvActLv.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownLoadManager.getInstance().stopAll();
    }
}
