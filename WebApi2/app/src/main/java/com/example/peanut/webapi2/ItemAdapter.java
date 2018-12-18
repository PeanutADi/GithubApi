package com.example.peanut.webapi2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> itemList = new ArrayList<>();
    private Context mContext;
    List<ImagePiece> ip;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView Cover;
        TextView Create;
        TextView Title;
        TextView Content;
        ProgressBar progressBar;
        TextView Play;
        TextView Comment;
        TextView Duration;
        SeekBar seekBar;
        Bitmap bmp;
        public ViewHolder(View view){
            super(view);
        }
    }

    public ItemAdapter(Context context,List<Item> items){
        mContext = context;
        itemList = items;
    }

    @Override
    public  ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout,parent,false);
        ViewHolder holder = new ViewHolder(view);

        holder.Cover=view.findViewById(R.id.cover);
        holder.Create = (TextView) view.findViewById(R.id.create);
        holder.Title = (TextView) view.findViewById(R.id.title);
        holder.Content = (TextView) view.findViewById(R.id.content);
        holder.progressBar=view.findViewById(R.id.pb);
        holder.Play = (TextView) view.findViewById(R.id.play);
        holder.Comment = (TextView) view.findViewById(R.id.comment);
        holder.Duration = (TextView) view.findViewById(R.id.duration);
        holder.seekBar = (SeekBar) view.findViewById(R.id.sb);

        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        final Item item = itemList.get(position);
        viewHolder.Create.setText("创建时间: "+item.data.create);
        viewHolder.Title.setText(item.data.title);
        viewHolder.Content.setText(item.data.content);
        viewHolder.Play.setText("播放： "+item.data.play);
        viewHolder.Comment.setText("评论： "+item.data.video_review);
        viewHolder.Duration.setText("时长： "+item.data.duration);


        Observable.create(new Observable.OnSubscribe<Bitmap >() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                String urlpath = item.data.cover;
                StringBuilder sb = new StringBuilder(urlpath);
                sb.insert(4, "s");
                String url = sb.toString();
                Bitmap bm = getInternetPicture(url);

                subscriber.onNext(bm);
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread())//回调在主线程
                .subscribeOn(Schedulers.io())//执行在io线程
                .subscribe(
        new Observer<Bitmap>() {
            @Override
            public void onCompleted() {
                viewHolder.progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Bitmap bm) {
                viewHolder.bmp=bm;
                viewHolder.Cover.setImageBitmap(bm);
            }
        });

        Observable.create(new Observable.OnSubscribe<Bitmap >() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                String message="";
                final String u="https://api.bilibili.com/pvideo?aid="+item.data.aid;
                try {
                    URL url=new URL(u);
                    HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5*1000);
                    connection.connect();
                    InputStream inputStream=connection.getInputStream();
                    byte[] data=new byte[1024];
                    StringBuffer sb=new StringBuffer();
                    int length=0;
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line = null;
                    while ((line = reader.readLine()) != null) { // 循环从流中读取
                        message += line ;
                    }
                    reader.close(); // 关闭流

                    inputStream.close();
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ImageCover image=new Gson().fromJson((String) message, ImageCover.class);
                String url=image.data.image[0];
                final Bitmap bm = getInternetPicture(url);
                subscriber.onNext(bm);
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread())//回调在主线程
                .subscribeOn(Schedulers.io())//执行在io线程
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onNext(Bitmap s) {
                        ip=ImageSplitter.split(s,10,10);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });


        viewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int i=100*seekBar.getProgress()/seekBar.getMax();

                int select=i%20;

                viewHolder.Cover.setImageBitmap(ip.get(select).bitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(0);
                viewHolder.Cover.setImageBitmap(viewHolder.bmp);
            }
        });

    }
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public Bitmap getInternetPicture(String UrlPath) {
        Bitmap bm = null;
        String urlpath = UrlPath;
        try {
            URL uri = new URL(urlpath);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                File file = new File(mContext.getCacheDir(), getFileName(urlpath));
                FileOutputStream fos = new FileOutputStream(file);
                int len = 0;
                byte[] b = new byte[1024];
                while ((len = is.read(b)) != -1) {
                    fos.write(b, 0, len);
                }
                fos.close();
                is.close();
                bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            } else {
                bm = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;

    }

    public String getFileName(String path) {
        int index = path.lastIndexOf("/");
        return path.substring(index + 1);
    }


}
