package com.example.chk.asyncserver.handler;

import android.content.Context;
import android.util.Log;

import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author chaman
 */
public class AsyncServerRequestCallback implements HttpServerRequestCallback {
	private static final String TAG = "AsyncServerRequestCallback";
	private static final String MIME_CSS = ".css";
	private static final String TYPE_CSS = "text/css";
	private Context mContext;
	private String mFileName;

	public AsyncServerRequestCallback(Context context, String fileName) {
		mContext = context;
		mFileName = fileName;
		Log.d(TAG, fileName);
	}

	@Override
	public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
		try {
			Log.d(TAG, mFileName);
			InputStream isAssetFile = mContext.getAssets().open(mFileName);
			if (mFileName.endsWith(MIME_CSS)) {
				response.setContentType(TYPE_CSS);
			}
			response.sendStream(isAssetFile, isAssetFile.available());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
