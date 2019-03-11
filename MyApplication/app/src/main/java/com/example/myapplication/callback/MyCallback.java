package com.example.myapplication.callback;

import android.support.v7.util.DiffUtil;

import com.example.myapplication.model.TextModel;

import java.util.List;

public class MyCallback extends DiffUtil.Callback {

    private List<TextModel> old_students, new_students;

    public MyCallback(List<TextModel> data, List<TextModel> students) {
        this.old_students = data;
        this.new_students = students;
    }

    @Override
    public int getOldListSize() {
        return old_students.size();
    }

    @Override
    public int getNewListSize() {
        return new_students.size();
    }

    // 判断Item是否已经存在
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return old_students.get(oldItemPosition).getTextTitle() == new_students.get(newItemPosition).getTextTitle();
    }

    // 如果Item已经存在则会调用此方法，判断Item的内容是否一致
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return old_students.get(oldItemPosition).getTextContent().equals(new_students.get(newItemPosition).getTextContent());
    }
}