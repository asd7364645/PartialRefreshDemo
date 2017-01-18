package com.alexleo.partialrefreshdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alexleo.partialrefreshdemo.R;
import com.alexleo.partialrefreshdemo.bean.Game;
import com.alexleo.partialrefreshdemo.manager.DownLoadManager;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Alex on 2017/1/18.
 * Alex
 */

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.MyViewHolder> implements DownLoadManager.DownCallBack{

    private Context context;
    private List<Game> games;
    private LayoutInflater layoutInflater;
    private DownLoadManager downLoadManager;

    public RvAdapter(Context context, List<Game> games) {
        this.context = context;
        this.games = games;
        layoutInflater = LayoutInflater.from(context);
        downLoadManager = DownLoadManager.getInstance();
        downLoadManager.setDownCallBack(this);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = layoutInflater.inflate(R.layout.item,parent,false);
        return new MyViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        final Game game = games.get(position);
        if (payloads.isEmpty()){
            holder.itemProgress.setMax((int) game.getGameTotalSize());
            holder.itemName.setText(game.getGameName());
            holder.itemSize.setText(game.getGameTotalSize() + "");
            //加载图片
            Glide.with(context).load(game.getGameUrl()).placeholder(R.mipmap.ic_launcher).crossFade(2000).into(holder.itemImg);
            holder.itemDownBtn.setText(getGameState(game));

            if (game.getGameDownSize() > 0 && game.getGameDownSize() < game.getGameTotalSize()) {
                holder.itemProgress.setVisibility(View.VISIBLE);
                holder.itemProgress.setProgress((int) game.getGameDownSize());
            } else {
                holder.itemProgress.setVisibility(View.INVISIBLE);
            }

            holder.itemDownBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (game.getState() == DownLoadManager.DOWN_DOWNLOADING)
                        downLoadManager.pause(position, new DownLoadManager.DownFile(game.getGameTotalSize(), game.getGameDownSize(), game.getState()));
                    else
                        downLoadManager.start(position, new DownLoadManager.DownFile(game.getGameTotalSize(), game.getGameDownSize(), game.getState()));
                }
            });
        }else {
            if (game.getGameDownSize() > 0 && game.getGameDownSize() < game.getGameTotalSize()) {
                holder.itemProgress.setVisibility(View.VISIBLE);
                holder.itemProgress.setProgress((int) game.getGameDownSize());
            } else {
                holder.itemProgress.setVisibility(View.INVISIBLE);
            }

            holder.itemDownBtn.setText(getGameState(game));
        }
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

    @Override
    public int getItemCount() {
        return games.size();
    }

    @Override
    public void update(int posi, DownLoadManager.DownFile downFile) {
        Game game = games.get(posi);
        game.setGameDownSize(downFile.downSize);
        game.setState(downFile.downState);
//        notifyItemChanged(posi,1);
        notifyItemChanged(posi);
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView itemName, itemSize;
        Button itemDownBtn;
        ProgressBar itemProgress;
        ImageView itemImg;

        MyViewHolder(View itemView) {
            super(itemView);
            itemImg = (ImageView) itemView.findViewById(R.id.itemImg);
            itemName = (TextView) itemView.findViewById(R.id.itemName);
            itemSize = (TextView) itemView.findViewById(R.id.itemSize);
            itemDownBtn = (Button) itemView.findViewById(R.id.itemDownBtn);
            itemProgress = (ProgressBar) itemView.findViewById(R.id.itemProgress);
        }
    }

}
