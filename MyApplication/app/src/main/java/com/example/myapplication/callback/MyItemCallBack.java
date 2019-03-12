package com.example.myapplication.callback;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;
import android.util.Log;

import com.example.myapplication.model.TextModel;

public class MyItemCallBack extends DiffUtil.ItemCallback<TextModel> {
    @Override
    public boolean areItemsTheSame(@NonNull TextModel oldItem, @NonNull TextModel newItem) {
        return TextUtils.equals(oldItem.getTextTitle(), newItem.getTextTitle());
    }

    @Override
    public boolean areContentsTheSame(@NonNull TextModel oldItem, @NonNull TextModel newItem) {
        return TextUtils.equals(oldItem.getTextContent(), newItem.getTextContent());
    }
}
