package com.example.chk.asyncserver;

import android.content.Context;
import android.os.Handler;

import com.example.chk.asyncserver.handler.AsyncServerRequestCallback;
import com.example.chk.asyncserver.handler.AsyncServerUploadHandler;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;

import java.io.IOException;

/**
 * @author chaman
 */
public class AndroidAsyncServer extends AsyncHttpServer {

    private static final int DEFAULT_PORT = 8080;

    private static final String ROOT_DIR = "wifi_async";
    private static final String DIVIDER = "/";
    private static final String FOLDER_SCRIPTS = "scripts";
    private static final String FOLDER_CSS = "css";
    private static final String FOLDER_IMAGES = "images";
    private static final String INDEX_HTML = "index.html";
    private static final String POST_FILES = "uploadFiles";
    private static String[] WWW_FOLDERS = {FOLDER_SCRIPTS, FOLDER_CSS, FOLDER_IMAGES};

    private Context mContext;
    private Handler mHandler;
    private int mPort;

    public AndroidAsyncServer(Context aContext, Handler aHandler) {
        this(aContext, aHandler, DEFAULT_PORT);
    }

    public AndroidAsyncServer(Context aContext, Handler aHandler, int aPort) {
        mContext = aContext;
        mHandler = aHandler;
        mPort = aPort;
        init();
    }

    /**
     * mapping urls to specific handler
     */
    private void init() {
        this.get(DIVIDER, new AsyncServerRequestCallback(mContext, ROOT_DIR + DIVIDER + INDEX_HTML));
        this.post(DIVIDER + POST_FILES, new AsyncServerUploadHandler(mHandler)); //register the file-upload url
        try {
            for (String folder : WWW_FOLDERS) {
                String assetPath = ROOT_DIR + DIVIDER + folder;
                String wwwPath = DIVIDER + folder + DIVIDER;
                for (String fileName : mContext.getAssets().list(assetPath)) {
                    this.get(wwwPath + fileName, new AsyncServerRequestCallback(mContext, assetPath
                            + DIVIDER + fileName));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在mPort上启动一个HttpServer，
     * 而HttpServer启动一个默认的AsyncServer
     */
    public void start() {
        this.listen(mPort);
    }

    /**
     * 必须重写该方法，因为AsyncHttpServer.stop()只是解除listen的关联，
     * 并未关闭AsyncServer.getDefault()。如果没有关闭AsyncServer，在
     * 下次重新调用start()启动服务器时，启动会失败。
     */
    @Override
    public void stop() {
        super.stop();
        AsyncServer.getDefault().stop();
    }

    public int getPort() {
        return mPort;
    }
}
