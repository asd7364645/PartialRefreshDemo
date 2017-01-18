package com.alexleo.partialrefreshdemo.manager;

import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownLoadManager {

    public interface DownCallBack{
        void update(int posi,DownFile downFile);
    }

    private DownCallBack downCallBack;

    public DownCallBack getDownCallBack() {
        return downCallBack;
    }

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