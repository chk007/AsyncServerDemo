package com.example.chk.asyncserver;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.chk.asyncserver.datas.ImageGridViewBean;

import java.util.List;

/**
 * @author chaman
 */
public final class AndroidASyncGridViewAdapter extends BaseAdapter {
    /** */
    private static final String TAG = "AndroidASyncGridViewAdapter";

    /** 存放GridView所有Item对应的数据 */
    private List<ImageGridViewBean> mDatas;

    /** GridView所在的Context */
    private Context mContext;

    /** WifiActivity handler */
    private Handler mHandler;

    /** 上传完成 */
    private static final int PROGRESS_FINISHED = 100;

    public AndroidASyncGridViewAdapter(Context aContext, Handler aHanlder, List<ImageGridViewBean> aDatas) {
        mContext = aContext;
        mHandler = aHanlder;
        mDatas = aDatas;
    }

    @Override
    public int getCount() {
        if (mDatas == null) {
            return 0;
        }
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        if (mDatas == null) {
            return null;
        }

        return mDatas.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_gridview_item, parent, false);
            holder = new ViewHolder();
            holder.mImageView = (ImageView) convertView.findViewById(R.id.gridView_item_image);
            holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.gridView_item_progressBar);
            holder.mItemLayout = (ViewGroup) convertView.findViewById(R.id.gridView_item_layout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageGridViewBean bean = ((ImageGridViewBean) getItem(position));
        holder.mImageView.setImageDrawable(bean.getDrawable());
        if (bean.getProgressStatus() >= 0 && bean.getProgressStatus() <= PROGRESS_FINISHED) {
            holder.mProgressBar.setVisibility(View.VISIBLE);
            holder.mProgressBar.setProgress(bean.getProgressStatus());
        } else {
            holder.mProgressBar.setVisibility(View.INVISIBLE);
        }
        final int itemPosition = position;
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
        return convertView;
    }

    /**
     * 向GridView中添加新的Item
     * @param aBean 新添加的Item持有的数据
     */
    public void addItem(ImageGridViewBean aBean) {
        Log.d(TAG, aBean.toString());
        mDatas.add(aBean);
        notifyDataSetChanged();
    }

    public void updateItem() {
        notifyDataSetChanged();
    }

    /**
     * 使用内部类来持有数据
     */
    private class ViewHolder {
        private ImageView mImageView;
        private ProgressBar mProgressBar;
        private ViewGroup mItemLayout;
    }

    public void release() {
        if (mDatas != null && mDatas.size() > 0) {
            mDatas.clear();
            mDatas = null;
        }
        mHandler = null;
    }
}
