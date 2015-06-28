package com.example.chk.asyncserver.handler;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.example.chk.asyncserver.AndroidAsyncMainActivity;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class process file upload request.
 * @author chaman
 */
public class AsyncServerUploadHandler implements HttpServerRequestCallback {
    private static final String TAG = "AsyncServerUploadHandler";
    private Handler mHandler;
    /** path of the image uploaded */
    private ArrayList<String> mFilePaths;

    /** the folder of the images uploaded. you can change it as you like!*/
    private final String mImageFolderPath = "/sdcard/AsyncServerImage/";

    public AsyncServerUploadHandler(Handler aHandler) {
        mHandler = aHandler;
        mFilePaths = new ArrayList<String>();
        init();
    }

    private void init() {
        File uploadFileDir = new File(mImageFolderPath);
        if (!uploadFileDir.isDirectory()) {
            uploadFileDir.mkdir();
        }
        if (uploadFileDir.isDirectory()) {
            String[] filePaths = uploadFileDir.list();
            for (String file: filePaths) {
                mFilePaths.add(mImageFolderPath + file);
            }
        }
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
        Log.d(TAG, request.getHeaders().toString());
        final MultipartFormDataBody body = (MultipartFormDataBody) request.getBody();
        body.setMultipartCallback(new MultipartFormDataBody.MultipartCallback() {
            @Override
            public void onPart(Part part) {
                if (part.isFile()) {
                    if (preProcess(part.getFilename())) {
                        UploadFileDataCallback callback = new UploadFileDataCallback(part.getFilename(),
                                response);
                        body.setDataCallback(callback);
                        body.setEndCallback(callback);
                    } else {
                        response.code(200);
                        response.send("This file has been uploaded!");
                        response.end();
                    }
                }
            }
        });
    }

    /**
     * 当一个新的文件请求到来时，判断该文件是否上传过；
     * 如果未上传则向WifiActivity发送消息；否则，提醒用户是否重传;
     * @param fileName 新上传的文件名
     * @return
     */
    private boolean preProcess(String fileName) {
        if (fileName == null || mFilePaths == null) {
            return false;
        }
        String filePath = mImageFolderPath + fileName;
        //第一次上传文件时，向WifiSyncActivity发送消息，在GridView中新建Item并添加默认图片
        mFilePaths.add(filePath);
        Message message = new Message();
        message.what = AndroidAsyncMainActivity.MSG_NEW_FILE_UPLOAD;
        message.obj = filePath;
        mHandler.sendMessage(message);
        return true;
        //TODO 当文件已经上传时
    }
    /**
     * This class is to process the data of uploadFiles request.
     */
    private class UploadFileDataCallback implements DataCallback, CompletedCallback {
        private int mReceivedDataSize;
        private String mFileName;
        private String mFilePath;
        private ByteArrayOutputStream mFileDataStream;
        private AsyncHttpServerResponse mResponse;

        public UploadFileDataCallback(String fileName , AsyncHttpServerResponse aResponse) {
            Log.d(TAG, "fileName=" + fileName);
            mFileName = fileName;
            mFilePath = mImageFolderPath + mFileName;
            mResponse = aResponse;
            mFileDataStream = new ByteArrayOutputStream();
        }

        /**
         * (this is a callback function)
         * If a segment of file has been uploaded successfully,
         * the http server will call this function.
         */
        @Override
        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
            Log.d(TAG, "remaining = " + bb.remaining());
            int remaining = bb.remaining();
            mReceivedDataSize += bb.remaining();
            mFileDataStream.write(bb.getAllByteArray(), 0, remaining);
            mFileDataStream.toByteArray();
            bb.recycle();
            //动态更新进度
            if (mFilePaths.contains(mFilePath)) {
                Message message = new Message();
                message.what = AndroidAsyncMainActivity.MSG_FILE_SEGMENT_UPLOAD_COMPLETED;
                message.obj = mFilePath;
                mHandler.sendMessage(message);
            }
        }

        /**
         * If a file has been uploaded successfully,
         * this function will be called.
         */
        @Override
        public void onCompleted(Exception ex) {
            Log.d(TAG, "mReceivedDataSize=" + mReceivedDataSize);
            if (mReceivedDataSize >= 0 && !TextUtils.isEmpty(mFileName)) {
                Log.d(TAG, "mFileDataStream size＝" + mFileDataStream.size());

                File uploadFileDir = new File(mImageFolderPath);
                if(!uploadFileDir.isDirectory()) {
                    uploadFileDir.mkdir();
                }
                File uploadFile = new File(mFilePath);
                try {
                    FileOutputStream fos = new FileOutputStream(uploadFile);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fos);
                    Log.d(TAG, "toByteArray length=" + mFileDataStream.toByteArray().length);
                    bufferedOutputStream.write(mFileDataStream.toByteArray());
                    bufferedOutputStream.close();
                    fos.close();
                    mFileDataStream.close();
                    mFileDataStream = null;
                    //向WifiSyncActivity发送文件上传完成消息
                    if (mHandler != null) {
                        Message message = new Message();
                        message.what = AndroidAsyncMainActivity.MSG_FILE_UPLOAD_COMPLETED;
                        message.obj = uploadFile.getAbsolutePath();
                        mHandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mHandler != null) {
                        Message message = new Message();
                        message.what = AndroidAsyncMainActivity.MSG_HIDE_PROGRESSBAR_SHOW_FAILED;
                        mHandler.sendMessage(message);
                    }
                    //写入文件时出错，服务器端返回服务器内部错误代码（503）
                    mResponse.code(503);
                    mResponse.send("An error has occurred in Server!");
                    mResponse.end();
                }

                //通知前端上传成功，并关闭该HTML请求通路
                mResponse.code(200);
                mResponse.send("Uploading has done!");
                mResponse.end();
            }
        }
    }
}
