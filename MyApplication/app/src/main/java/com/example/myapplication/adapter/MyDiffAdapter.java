package com.example.myapplication.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.callback.MyItemCallBack;
import com.example.myapplication.model.TextModel;

import java.util.List;

public class MyDiffAdapter  extends RecyclerView.Adapter {
    private Context mContext;
    private AsyncListDiffer<TextModel> mTextDiffl;
    private DiffUtil.ItemCallback<TextModel> diffCallback = new MyItemCallBack();

    public MyDiffAdapter(Context mContext) {
        this.mContext = mContext;
        mTextDiffl = new AsyncListDiffer<>(this, diffCallback);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_view, null);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyAdapter.MyViewHolder myViewHolder = (MyAdapter.MyViewHolder) holder;
        TextModel textModel = getItem(position);
        myViewHolder.tv.setText(textModel.getTextTitle() + "." + textModel.getTextContent());
    }

    @Override
    public int getItemCount() {
        return mTextDiffl.getCurrentList().size();
    }
    public void submitList(List<TextModel> data) {
        mTextDiffl.submitList(data);
    }

    public TextModel getItem(int position) {
        return mTextDiffl.getCurrentList().get(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv;

        MyViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.item_tv);
        }
    }
}
