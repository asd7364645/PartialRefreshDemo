package com.alexleo.partialrefreshdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexleo.partialrefreshdemo.R;
import com.alexleo.partialrefreshdemo.bean.Game;
import com.alexleo.partialrefreshdemo.manager.DownLoadManager;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Alex on 2017/1/17.
 * Alex
 */

public class LvAdapter extends BaseAdapter implements DownLoadManager.DownCallBack{

    private Context context;
    private List<Game> games;
    private LayoutInflater layoutInflater;
    private ListView listView;
    private DownLoadManager downLoadManager;

    public LvAdapter(Context context, ListView listView, List<Game> games) {
        this.context = context;
        this.games = games;
        this.listView = listView;
        layoutInflater = LayoutInflater.from(context);
        downLoadManager = DownLoadManager.getInstance();
        downLoadManager.setDownCallBack(this);
    }

    @Override
    public int getCount() {
        return games.size();
    }

    @Override
    public Object getItem(int position) {
        return games.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setItemView(viewHolder, games.get(position), position);

        return convertView;
    }

    private void setItemView(ViewHolder viewHolder, final Game game, final int position) {

        viewHolder.itemProgress.setMax((int) game.getGameTotalSize());
        viewHolder.itemName.setText(game.getGameName());
        viewHolder.itemSize.setText(game.getGameTotalSize() + "");
        //加载图片
        Glide.with(context).load(game.getGameUrl()).placeholder(R.mipmap.ic_launcher).crossFade(2000).into(viewHolder.itemImg);
        viewHolder.itemDownBtn.setText(getGameState(game));

        if (game.getGameDownSize() > 0 && game.getGameDownSize() < game.getGameTotalSize()) {
            viewHolder.itemProgress.setVisibility(View.VISIBLE);
            viewHolder.itemProgress.setProgress((int) game.getGameDownSize());
        } else {
            viewHolder.itemProgress.setVisibility(View.INVISIBLE);
        }

        viewHolder.itemDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game.getState() == DownLoadManager.DOWN_DOWNLOADING)
                    downLoadManager.pause(position, new DownLoadManager.DownFile(game.getGameTotalSize(), game.getGameDownSize(), game.getState()));
                else
                    downLoadManager.start(position, new DownLoadManager.DownFile(game.getGameTotalSize(), game.getGameDownSize(), game.getState()));
            }
        });

    }

    private String getGameState(Game game) {
        switch (game.getState()) {
            case DownLoadManager.DOWN_DOWNLOADING:
                return "下载中";
            case DownLoadManager.DOWN_FINISH:
                return "已完成";
            case DownLoadManager.DOWN_PAUST:
                return "暂停";
            case DownLoadManager.DOWN_WATE:
                return "等待中";
        }
        return "下载";
    }

    /**
     * 局部刷新
     *
     * @param mListView
     * @param posi
     */
    public void updateSingleRow(ListView mListView, int posi) {
        if (mListView != null) {
            //获取第一个显示的item
            int visiblePos = mListView.getFirstVisiblePosition();
            //计算出当前选中的position和第一个的差，也就是当前在屏幕中的item位置
            int offset = posi - visiblePos;
            int lenth = mListView.getChildCount();
            // 只有在可见区域才更新,因为不在可见区域得不到Tag,会出现空指针,所以这是必须有的一个步骤
            if ((offset < 0) || (offset >= lenth)) return;
            View convertView = mListView.getChildAt(offset);
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            //以下是处理需要处理的控件
            System.out.println("posi = " + posi);
            Game game = games.get(posi);
            if (game.getGameDownSize() > 0 && game.getGameDownSize() < game.getGameTotalSize()) {
                viewHolder.itemProgress.setVisibility(View.VISIBLE);
                viewHolder.itemProgress.setProgress((int) game.getGameDownSize());
            } else {
                viewHolder.itemProgress.setVisibility(View.INVISIBLE);
            }

            viewHolder.itemDownBtn.setText(getGameState(game));
        }
    }

    @Override
    public void update(int posi, DownLoadManager.DownFile downFile) {
        Game game = games.get(posi);
        game.setGameDownSize(downFile.downSize);
        game.setState(downFile.downState);
//            notifyDataSetChanged();
        updateSingleRow(listView, posi);
    }

    private class ViewHolder {

        TextView itemName, itemSize;
        Button itemDownBtn;
        ProgressBar itemProgress;
        ImageView itemImg;

        public ViewHolder(View convertView) {
            itemImg = (ImageView) convertView.findViewById(R.id.itemImg);
            itemName = (TextView) convertView.findViewById(R.id.itemName);
            itemSize = (TextView) convertView.findViewById(R.id.itemSize);
            itemDownBtn = (Button) convertView.findViewById(R.id.itemDownBtn);
            itemProgress = (ProgressBar) convertView.findViewById(R.id.itemProgress);
        }
    }

}
