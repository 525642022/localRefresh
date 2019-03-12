package com.example.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.adapter.MyAdapter;
import com.example.myapplication.adapter.MyDiffAdapter;
import com.example.myapplication.callback.MyCallback;
import com.example.myapplication.model.TextModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
   private List<TextModel> mTextModels;
   private RecyclerView text_rv;
   private TextView ref_rv;
   private MyAdapter myAdapter;
   private MyDiffAdapter myDiffAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_rv = findViewById(R.id.text_rv);
        ref_rv = findViewById(R.id.ref_rv);
        initData();
        initRv();
        setControl();
    }

    private void setControl() {
        ref_rv.setOnClickListener(v->{
            change1();
        });
    }

    private void initRv() {
        text_rv.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter(MainActivity.this);
        myAdapter.setData(mTextModels);
        myDiffAdapter = new MyDiffAdapter(MainActivity.this);
        myDiffAdapter.submitList(mTextModels);
        //这个是使用diffutils
//        text_rv.setAdapter(myAdapter);
        //这个是使用AsyncListDiff
        text_rv.setAdapter(myDiffAdapter);
    }

    private void initData() {
        mTextModels = new ArrayList<>();
        for(int i = 0; i<10;i++){
            TextModel textModel= new TextModel("aa"+i,"bb"+i);
            mTextModels.add(textModel);
        }

    }

    /***
     * 这里使用DiffUtil比对
     */
    public void change() {
        mTextModels.set(1, new TextModel("android", "text"));
        mTextModels.add(new TextModel("androidTitle", "text999"));
        TextModel textModel = mTextModels.get(2);
        mTextModels.remove(2);
        mTextModels.add(textModel);
        List<TextModel> oldList = myAdapter.getData();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MyCallback(oldList, mTextModels), true);
        myAdapter.setData(mTextModels);
        result.dispatchUpdatesTo(myAdapter);
    }
    public void change1() {
        mTextModels = new ArrayList<>();
        for(int i = 0; i<10;i++){
            TextModel textModel= new TextModel("aa"+i,"bb"+i%5);
            mTextModels.add(textModel);
        }
        myDiffAdapter.submitList(mTextModels);
    }
}
