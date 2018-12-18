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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GithubActivity extends AppCompatActivity {

    List<Repo> repos = new ArrayList<>();
    List<Issues> issues = new ArrayList<>();

    Button searchButton ;
    Button addButton;

    TextView searchText;
    TextView token;
    TextView issueTitle;
    TextView issueBody;

    RecyclerView repoView;
    RecyclerView issueView;

    GitAdapter adapter;
    IssueAdapter issueAdapter;

    LinearLayout github ;
    LinearLayout issue;

    Button add;

    final String baseUrl = "https://api.github.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_github);

        github = (LinearLayout)findViewById(R.id.githubll);
        issue = (LinearLayout)findViewById(R.id.issueLinear);

        issueTitle = (TextView)findViewById(R.id.addTitle);
        issueBody = (TextView)findViewById(R.id.addBody);

        repoView = findViewById(R.id.repoRecycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        repoView.setLayoutManager(layoutManager);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext());
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        issueView = findViewById(R.id.issueRecycle);
        issueView.setLayoutManager(layoutManager2);


        adapter = new GitAdapter(GithubActivity.this,repos);
        repoView.setAdapter(adapter);

        issueAdapter = new IssueAdapter(GithubActivity.this,issues);
        issueView.setAdapter(issueAdapter);

        searchText = (TextView)findViewById(R.id.gitSearch);

        searchButton = (Button) findViewById(R.id.gitButton);

        add = (Button)findViewById(R.id.addIssue);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if ((networkInfo == null) || !networkInfo.isConnected()) {
                    Toast.makeText(GithubActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    return ;
                }

                repos.clear();

                OkHttpClient build = new OkHttpClient.Builder()
                        .connectTimeout(2, TimeUnit.SECONDS)
                        .readTimeout(2, TimeUnit.SECONDS)
                        .writeTimeout(2, TimeUnit.SECONDS)
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        // 本次实验不需要自定义Gson
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .client(build)
                        .build();

                final GithubService myService = retrofit.create(GithubService.class);

                final String user = searchText.getText().toString();
                /*Call<List<Repo>> call = myService.getRepo(user);
                call.enqueue(new Callback<List<Repo>>() {
                    @Override
                    public void onResponse(Call<List<Repo>> call, retrofit2.Response<List<Repo>> response) {
                        List<Repo> repoList = response.body();
                        for(int i = 0;i<repoList.size();i++){
                            if(repoList.get(i).has_issues == true)
                                repos.add(repoList.get(i));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Repo>> call, Throwable t) {
                    }
                });*/

                myService.getRepoObservable(user)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<Repo>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(List<Repo> repoList) {
                                for(int i = 0;i<repoList.size();i++){
                                    if(repoList.get(i).has_issues == true)
                                        repos.add(repoList.get(i));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });

                adapter.setOnItemClickListener(new GitAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, final Repo repo) {
                        github.setVisibility(View.INVISIBLE);
                        repoView.setVisibility(View.INVISIBLE);
                        issueView.setVisibility(View.VISIBLE);
                        issue.setVisibility(View.VISIBLE);

                        OkHttpClient build2 = new OkHttpClient.Builder()
                                .connectTimeout(2, TimeUnit.SECONDS)
                                .readTimeout(2, TimeUnit.SECONDS)
                                .writeTimeout(2, TimeUnit.SECONDS)
                                .build();

                        Retrofit retrofit2 = new Retrofit.Builder()
                                .baseUrl(baseUrl)
                                // 本次实验不需要自定义Gson
                                .addConverterFactory(GsonConverterFactory.create())
                                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                .client(build2)
                                .build();

                        GithubService myService2 = retrofit2.create(GithubService.class);

                        /*Call<List<Issues>> listCall = myService.getIssue(user,repo.name);
                        listCall.enqueue(new Callback<List<Issues>>() {
                            @Override
                            public void onResponse(Call<List<Issues>> call, retrofit2.Response<List<Issues>> response) {
                                List<Issues> issueList = response.body();
                                for(int i = 0;i<issueList.size();i++){
                                    issues.add(issueList.get(i));
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Issues>> call, Throwable t) {

                            }
                        });*/

                        myService2.getIssueObservable(user,repo.name)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<List<Issues>>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(List<Issues> issuesList) {
                                        for(int i = 0;i<issuesList.size();i++){
                                            issues.add(issuesList.get(i));
                                        }
                                        issueAdapter.notifyDataSetChanged();
                                    }
                                });

                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Commemt commemt = new Commemt();
                                commemt.body = issueBody.getText().toString();
                                commemt.title = issueTitle.getText().toString();

                                OkHttpClient build = new OkHttpClient.Builder()
                                        .connectTimeout(2, TimeUnit.SECONDS)
                                        .readTimeout(2, TimeUnit.SECONDS)
                                        .writeTimeout(2, TimeUnit.SECONDS)
                                        .build();
                                Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl(baseUrl)
                                        // 本次实验不需要自定义Gson
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                        .client(build)
                                        .build();
                                GithubService githubService = retrofit.create(GithubService.class);
                                Call<Commemt> commemtCall = githubService.addIssue("PeanutADi",repo.name,"0c7df72fc026123d4c3249cf5d8e5020662dffb1",commemt);
                                commemtCall.enqueue(new Callback<Commemt>() {
                                    @Override
                                    public void onResponse(Call<Commemt> call, Response<Commemt> response) {

                                    }

                                    @Override
                                    public void onFailure(Call<Commemt> call, Throwable t) {

                                    }
                                });

                                OkHttpClient build2 = new OkHttpClient.Builder()
                                        .connectTimeout(2, TimeUnit.SECONDS)
                                        .readTimeout(2, TimeUnit.SECONDS)
                                        .writeTimeout(2, TimeUnit.SECONDS)
                                        .build();

                                Retrofit retrofit2 = new Retrofit.Builder()
                                        .baseUrl(baseUrl)
                                        // 本次实验不需要自定义Gson
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                                        .client(build2)
                                        .build();

                                GithubService myService2 = retrofit2.create(GithubService.class);

                        /*Call<List<Issues>> listCall = myService.getIssue(user,repo.name);
                        listCall.enqueue(new Callback<List<Issues>>() {
                            @Override
                            public void onResponse(Call<List<Issues>> call, retrofit2.Response<List<Issues>> response) {
                                List<Issues> issueList = response.body();
                                for(int i = 0;i<issueList.size();i++){
                                    issues.add(issueList.get(i));
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Issues>> call, Throwable t) {

                            }
                        });*/

                                myService2.getIssueObservable(user,repo.name)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<List<Issues>>() {
                                            @Override
                                            public void onCompleted() {

                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                            }

                                            @Override
                                            public void onNext(List<Issues> issuesList) {
                                                for(int i = 0;i<issuesList.size();i++){
                                                    issues.add(issuesList.get(i));
                                                }
                                                issueAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        });

                    }
                });
            }
        });

    }
}
