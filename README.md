Android ListView与RecyclerView局部刷新
=================================
----------
##一、ListView
	
之前写过一篇关于ListView局部刷新的博客，这部分对其进行完善
，之前的链接为：[Android模拟ListView点击下载和局部刷新](http://blog.csdn.net/asd7364645/article/details/51559647)

平时在写ListView的时候需要更改某些数据，这种情况我们一般会调用
notifyDataSetChanged()方法进行刷新，调用notifydatasetchange其实会导致adpter的getView方法被多次调用（画面上能显示多少就会被调用多少次），并且在有获取网络图片的情况下会可能造成大量闪动或卡顿，极大的影响用户体验（图片重新加载并闪动在ImageLoader框架中会出现，在glide框架中没有出现）。

所以我们需要做单行刷新来进行优化

这个是Google官方给出的解决方案：

```
private void updateSingleRow(ListView listView, long id) {  

        if (listView != null) {  
            int start = listView.getFirstVisiblePosition();  
            for (int i = start, j = listView.getLastVisiblePosition(); i <= j; i++)  
                if (id == ((Messages) listView.getItemAtPosition(i)).getId()) {  
                    View view = listView.getChildAt(i - start);  
                    getView(i, view, listView);  
                    break;  
                }  
        }  
    }
```

对于这个方法可以参考这个博客：[android ListView 单条刷新方法实践及原理解析](http://blog.csdn.net/yuyuanhuang/article/details/43198107)

可以看出来谷歌的方案是通过listview的getView方法将单行的所有内容都刷新一遍，但是这样如果是有加载网络图片的话可能也会造成闪动重新加载，所以我们需要单独刷新某个item中的某个控件来实现局部刷新

所以我们在Adapter中添加一个局部刷新的方法

```
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
            //以下是处理需要处理的控件方法。。。。。
        }
    }
```
举个例子，简单的模拟在列表中点击下载并更新列表的demo
1.首先定义一个Bean对象
这里有一个图片url供Glide加载

```
public class Game {

    private String gameName;
    private long gameTotalSize;
    private long gameDownSize;
    private String gameUrl;
    private int state;

    public Game(String gameName, long gameTotalSize) {
        this.gameName = gameName;
        this.gameTotalSize = gameTotalSize;
        gameUrl = "http://dynamic-image.yesky.com/600x-/uploadImages/upload/20140912/upload/201409/smkxwzdt1n1jpg.jpg";
        this.gameDownSize = 0;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public String getGameName() {
        return gameName;
    }

    public long getGameTotalSize() {
        return gameTotalSize;
    }

    public long getGameDownSize() {
        return gameDownSize;
    }

    public void setGameDownSize(long gameDownSize) {
        this.gameDownSize = gameDownSize;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
```

2.实现一个下载的模拟器（单例类）

```
public class DownLoadManager {

    public interface DownCallBack{
        void update(int posi,DownFile downFile);
    }

    private DownCallBack downCallBack;

    public void setDownCallBack(DownCallBack downCallBack) {
        this.downCallBack = downCallBack;
    }

    /**
     * 下载文件类，不过在正常的项目中应该有一个ID或者url，
     * 用来判断文件，在这个demo中就用列表的position来判断了
     */
    public static class DownFile {
        public long total;
        public long downSize;
        public int downState;

        public DownFile(long total, long downSize, int downState) {
            this.total = total;
            this.downSize = downSize;
            this.downState = downState;
        }
    }

    public static final int DOWN_WATE = 0X02;
    public static final int DOWN_PAUST = 0X03;
    public static final int DOWN_FINISH = 0X04;
    public static final int DOWN_DOWNLOADING = 0X05;

    private ExecutorService executorService;
    private SparseArray<DownFile> downFileSparseArray;
    private SparseArray<DownTask> downTaskSparseArray;
    private static volatile DownLoadManager singleton;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int posi = msg.arg1;
            DownLoadManager.DownFile downFile = (DownLoadManager.DownFile) msg.obj;
            if (downCallBack!=null){
                downCallBack.update(posi,downFile);
            }
        }
    };

    private DownLoadManager() {
        executorService = Executors.newFixedThreadPool(3);
        downFileSparseArray = new SparseArray<>();
        downTaskSparseArray = new SparseArray<>();
    }

    public static DownLoadManager getInstance() {
        if (singleton == null) {
            synchronized (DownLoadManager.class) {
                if (singleton == null) {
                    singleton = new DownLoadManager();
                }
            }
        }
        return singleton;
    }

    public void start(int posi, DownFile downFile) {
        if (downFile.downState == DOWN_WATE||downFile.downState == DOWN_FINISH){
            return;
        }
        //首先设置为排队中的状态
        downFile.downState = DOWN_WATE;
        update(posi, downFile);
        downFileSparseArray.put(posi, downFile);
        DownTask downTask = new DownTask(posi);
        downTaskSparseArray.put(posi, downTask);
        executorService.submit(downTask);
    }

    public void pause(int posi, DownFile downFile) {
        downTaskSparseArray.get(posi).stop();
        downTaskSparseArray.remove(posi);
        downFile.downState = DOWN_PAUST;
        update(posi, downFile);
    }

    public void update(int posi, DownFile downFile) {
        Message msg = handler.obtainMessage();
        msg.obj = downFile;
        msg.arg1 = posi;
        msg.sendToTarget();
    }

    public void stopAll(){
        for (int i = 0;i<downTaskSparseArray.size();i++) {
            downTaskSparseArray.valueAt(i).stop();
        }
        downTaskSparseArray.clear();
    }


    private class DownTask implements Runnable {

        private int posi;
        private boolean isWorking;
        private DownFile downFile;

        public DownTask(int posi) {
            this.posi = posi;
            isWorking = true;
            downTaskSparseArray.put(posi, this);
        }

        public void stop() {
            this.isWorking = false;
        }

        @Override
        public void run() {

            //一旦成功进入到线程里就变为下载中状态
            downFile = downFileSparseArray.get(posi);
            downFile.downState = DOWN_DOWNLOADING;
            while (isWorking) {
                update(posi, downFile);
                if (downFile.downSize < downFile.total) {
                    ++downFile.downSize;
                } else {
                    downFile.downState = DOWN_FINISH;
                    downFileSparseArray.remove(posi);
                    downTaskSparseArray.remove(posi);
                    isWorking = false;
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    downFile.downState = DOWN_PAUST;
                    downFileSparseArray.remove(posi);
                    downTaskSparseArray.remove(posi);
                    isWorking = false;
                }
            }
        }
    }

}
```

3.创建adapter

```
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
```

基本代码就是这些。
下面是notifyDataSetChanged后刷新的效果：由于不断的刷新界面中所有的item，所以在下载的时候点击按钮没有任何的反应，只有在刷新的间隙时间里点击才可以执行下载

![这里写图片描述](http://img.blog.csdn.net/20170118223921704?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

可以看到，这里我点击了好多回没有效果，偶尔会成功，这简直没法用了~

局部刷新的效果：

![这里写图片描述](http://img.blog.csdn.net/20170118224522227?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

##一、RecyclerView

看到RecyclerView局部刷新可能大家会说，RecyclerView 中不是有notifyItemChanged（position）么，没错，的确是用这个方法可以刷新一个item，但是这个方法也是有个坑，同样会刷新item中所有东西，并且在不断刷新的时候会执行刷新Item的动画，导致滑动的时候会错开一下，还可能会造成图片重新加载或者不必要的消耗。

下面讲解一下我的方法：
一般我们刷新某个item的方法为

```
notifyItemChanged(position);
```

所以我们就从这里入手，
首先查看源码
![这里写图片描述](http://img.blog.csdn.net/20170118180657874?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
这个方法里执行了notifyItemRangeChanged方法，继续跟踪
![这里写图片描述](http://img.blog.csdn.net/20170118180746626?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
发现这里执行了另一个重载方法

最后一个参数为null？继续跟进
![这里写图片描述](http://img.blog.csdn.net/20170118180925658?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
发现这里的参数为Object payload，然后我看到了notifyItemChanged的另一个重载方法，这里也有一个Object payload参数，看一下源码：
![这里写图片描述](http://img.blog.csdn.net/20170118181347211?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

payload 的解释为：如果为null，则刷新item全部内容
那言外之意就是不为空就可以局部刷新了~！
继续从payload跟踪，发现在RecyclerView中有另一个onBindViewHolder的方法，多了一个参数，payload！！！这个不就是我要找的么~~！

看一下源码：
![这里写图片描述](http://img.blog.csdn.net/20170118181718122?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
发现它调用了另一个重载方法，而另一个重载方法就是我们在写adapter中抽象的方法，那我们就可以直接从这里入手了。

1.重写onBindViewHolder(VH holder, int position, List<Object> payloads)这个方法

```
@Override
    public void onBindViewHolder(MyViewHolder holder, final int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads.isEmpty()){  
        //全部刷新
        }else {
        //局部刷新
        }
    }
```
2.执行两个参数的notifyItemChanged，第二个参数随便什么都行，只要不让它为空就OK~，这样就可以实现只刷新item中某个控件了！！！

这个是用一般的刷新item方法的效果图：

![这里写图片描述](http://img.blog.csdn.net/20170118222628324?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

虽然录制的效果不是特别好，但是可以看见明显的浮动错位现象

这个是用这个方法刷新的效果图：

![这里写图片描述](http://img.blog.csdn.net/20170118222834981?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYXNkNzM2NDY0NQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

这样就恢复正常了

RecyclerView adapter默认的使用我就不在这里多说了。主要是局部刷新。

今天的博客就说到这里，有什么增加的日后再补充~~
博客原文：http://blog.csdn.net/asd7364645/article/details/54581920
希望喜欢的小伙伴给个屎蛋（star）~~

----------

我是Alex-蜡笔小刘，一个android小白~！
有失误或者错误希望有大神能纠正~！也希望与其他“小白”共同进步~！



