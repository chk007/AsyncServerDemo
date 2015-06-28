package com.example.chk.asyncserver;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chk.asyncserver.datas.ImageGridViewBean;
import com.example.chk.asyncserver.utils.DrawableDecoder;
import com.example.chk.asyncserver.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class AndroidAsyncMainActivity extends FragmentActivity {
    /** file upload complete message */
    public static final int MSG_FILE_UPLOAD_COMPLETED = 0x01;

    /** file segment upload complete message */
    public static final int MSG_FILE_SEGMENT_UPLOAD_COMPLETED = 0x02;

    /** new file upload message */
    public static final int MSG_NEW_FILE_UPLOAD = 0x03;

    /** hide progress bar message and show success picture after DELAY_TIME */
    public static final int MSG_HIDE_PROGRESSBAR_SHOW_SUCCESS = 0x04;

    /** hide progress and show failed picture after DELAY_TIME*/
    public static final int MSG_HIDE_PROGRESSBAR_SHOW_FAILED = 0x05;

    /** DELAY_TIME , unit (ms)*/
    private final static int DELAY_TIME = 500;

    /** 每次进度条更新的时间 */
    private final static int PROGRESSBAR_INCREAMENT = 10;

    /** grid view item position*/
    private int gridViewItemID = 0;

    /** key: image path in the local. Value: grid view item id*/
    private HashMap<String, Integer> imageItemIDHashMap = new HashMap<String, Integer>();

    /** WIFI server */
    private AndroidAsyncServer wifiServer;

    /** image GridView*/
    private GridView mGridView = null;

    /** GridView adapter*/
    private AndroidASyncGridViewAdapter gridViewAdatper = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_FILE_UPLOAD:
                    onNewFileUpload(msg);
                    break;
                case MSG_FILE_SEGMENT_UPLOAD_COMPLETED:
                    onFileSegmentUploadCompleted(msg);
                    break;
                case MSG_FILE_UPLOAD_COMPLETED:
                    onFileUploadCompleted(msg);
                    break;
                case MSG_HIDE_PROGRESSBAR_SHOW_SUCCESS:
                    onHideProgressBarShowSuccess(msg);
                    break;
                case MSG_HIDE_PROGRESSBAR_SHOW_FAILED:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        TextView tvWifiInfo = (TextView) findViewById(R.id.tvWifiInfo);
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        tvWifiInfo.setText(wifiInfo.getSSID().replace("\"", ""));

        wifiServer = new AndroidAsyncServer(getApplicationContext(), mHandler);
        TextView tvIpInfo = (TextView) findViewById(R.id.tvIpInfo);
        tvIpInfo.setText(NetworkUtils.getLocalIpAddress() + ":" + wifiServer.getPort());
        mGridView = (GridView) findViewById(R.id.wifi_sync_gridview);
        mGridView.setVisibility(View.INVISIBLE);
        gridViewAdatper = new AndroidASyncGridViewAdapter(getApplicationContext(), mHandler, new ArrayList<ImageGridViewBean>());
        mGridView.setAdapter(gridViewAdatper);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start wifi server
        wifiServer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    /**
     * 1. stop wifi server
     * 2. release memory
     */
    private void release() {
        gridViewAdatper = null;
        gridViewItemID = 0;
        mHandler = null;
        if (imageItemIDHashMap != null && imageItemIDHashMap.size() > 0) {
            imageItemIDHashMap.clear();
        }
        if (wifiServer != null) {
            wifiServer.stop();
        }
        if (gridViewAdatper != null) {
            gridViewAdatper.release();
        }
    }

    /**
     * when there is a new file uploaded, there will send a  message: MSG_NEW_FILE_UPLOAD.
     * This function will process this message.
     * @param msg message
     */
    private void onNewFileUpload(Message msg) {
        if (mGridView.getVisibility() == View.INVISIBLE) {
            LinearLayout emptyImageLayout = (LinearLayout) findViewById(R.id.wifiActivity_emptyImageLayout);
            emptyImageLayout.setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.VISIBLE);
        }
        if (msg.obj instanceof String) {
            imageItemIDHashMap.put((String) msg.obj, new Integer(gridViewItemID));
            ImageGridViewBean bean =
                    new ImageGridViewBean(getResources().getDrawable(R.drawable.wifi_empty_upload_image),
                            0, (String) msg.obj);
            gridViewAdatper.addItem(bean);
        }
        gridViewItemID++;
    }

    /**
     * An image may be split into small pieces when uploaded.
     * And if a piece is uploaded successfully, it will send a message : MSG_FILE_SEGMENT_UPLOAD_COMPLETED.
     * This function mainly update the progress.
     * @param msg
     */
    private void onFileSegmentUploadCompleted(Message msg) {
        if (!(msg.obj instanceof String) || imageItemIDHashMap.size() == 0 || gridViewAdatper == null) {
            return;
        }
        String filePath = (String) msg.obj;
        int position = imageItemIDHashMap.get(filePath);
        if (gridViewAdatper.getItem(position) instanceof ImageGridViewBean) {
            ImageGridViewBean bean = (ImageGridViewBean) gridViewAdatper.getItem(position);
            if (bean != null && bean.getProgressStatus() <= 80) {
                bean.setProgressStatus(bean.getProgressStatus() + PROGRESSBAR_INCREAMENT);
            }
        }
        gridViewAdatper.updateItem();
    }

    /**
     * when a file is uploaded successfully, there will send a message : MSG_FILE_UPLOAD_COMPLETED .
     * this function does the follow things:
     * 1. hide the progressbar
     * 2. show the image that has uploaded.
     * @param msg message
     */
    private void onFileUploadCompleted(Message msg) {
        if (!(msg.obj instanceof String) || imageItemIDHashMap.size() == 0 || gridViewAdatper == null) {
            return;
        }
        String filePath = (String) msg.obj;
        if (imageItemIDHashMap == null || !imageItemIDHashMap.containsKey(filePath)) {
            return;
        }
        int position = imageItemIDHashMap.get(filePath);
        if (gridViewAdatper.getItem(position) instanceof ImageGridViewBean) {
            ImageGridViewBean bean = (ImageGridViewBean) gridViewAdatper.getItem(position);
            if (bean != null) {
                bean.setDrawable(DrawableDecoder.decodeAndCompressImage(filePath));
                bean.setProgressStatus(100);
            }
        }
        gridViewAdatper.updateItem();
        //??????????????????????????
        final int itemPosition = position;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = MSG_HIDE_PROGRESSBAR_SHOW_SUCCESS;
                message.obj = itemPosition;
                mHandler.sendMessage(message);
            }
        }, DELAY_TIME);
    }

    /**
     * @param msg
     */
    private void onHideProgressBarShowSuccess(Message msg) {
        if (gridViewAdatper == null) {
            return;
        }
        int position = 0;
        if (msg.obj instanceof Integer) {
            position = (Integer) msg.obj;
        }
        if (position < 0 || position >= gridViewItemID) {
            return;
        }
        if (gridViewAdatper.getItem(position) != null && gridViewAdatper.getItem(position) instanceof ImageGridViewBean) {
                ImageGridViewBean bean = ((ImageGridViewBean) gridViewAdatper.getItem(position));
                bean.setProgressStatus(-1);
        }
        gridViewAdatper.updateItem();
    }

}
