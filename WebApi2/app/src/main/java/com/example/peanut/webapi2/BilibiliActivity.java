package com.example.peanut.webapi2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BilibiliActivity extends AppCompatActivity {

    List<Item> items = new ArrayList<>();

    Button button;
    EditText editText;

    ItemAdapter adapter;

    String module="https://space.bilibili.com/ajax/top/showTop?mid=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilibili);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        button = (Button)findViewById(R.id.button);
        editText = (EditText)findViewById(R.id.editText);

        RecyclerView rv = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);

        adapter = new ItemAdapter(BilibiliActivity.this,items);
        rv.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if ((networkInfo == null) || !networkInfo.isConnected()) {
                    Toast.makeText(BilibiliActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    return ;
                }

                String id = editText.getText().toString();
                if(id == null || id.equals("") || isNumeric(id) == false){
                    Toast.makeText(BilibiliActivity.this, "需要整数类型数据", Toast.LENGTH_SHORT).show();
                    return ;
                }

                int i=Integer.parseInt(id);
                if(i<=0){
                    Toast.makeText(BilibiliActivity.this,"需要大于0的user_id",Toast.LENGTH_SHORT).show();
                }

                final String web = module + id;

                Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {

                        String message="";
                        try {
                            URL url=new URL(web);
                            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(5*1000);
                            connection.connect();
                            InputStream inputStream=connection.getInputStream();
                            StringBuffer sb=new StringBuffer();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                message += line ;
                            }
                            reader.close();
                            inputStream.close();
                            connection.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        char index = message.charAt(10);
                        if(index=='f'){
                            subscriber.onNext("false");
                        }
                        else{
                            subscriber.onNext(message);
                        }
                        subscriber.onCompleted();
                    }
                });

                Observer<String> observer = new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        if(s.equals("false")){
                            Toast.makeText(BilibiliActivity.this, "数据库中不存在记录", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Item item1 = new Gson().fromJson((String) s, Item.class);
                        items.add(item1);
                        adapter.notifyDataSetChanged();
                    }
                };

                observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);

            }
        });
    }

    public static boolean isNumeric(String str)
    {
        for (int i = 0; i < str.length(); i++)
            if (!Character.isDigit(str.charAt(i)))
            {
                return false;
            }
        return true;
    }

}
