package com.example.peanut.webapi2;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface GithubService {
    @GET("/users/{user_name}/repos")
        // 这里的List<Repo>即为最终返回的类型，需要保持一致才可解析
        // 之所以使用一个List包裹是因为该接口返回的最外层是一个数组
    Call<List<Repo>> getRepo(@Path("user_name") String user_name);

    @GET("/users/{user_name}/repos")
    Observable<List<Repo>> getRepoObservable(@Path("user_name") String user_name);

    @GET("/repos/{user_name}/{repo_name}/issues")
    Call<List<Issues>> getIssue(@Path("user_name") String user_name,@Path("repo_name") String repo_name);

    @GET("/repos/{user_name}/{repo_name}/issues")
    Observable<List<Issues>> getIssueObservable(@Path("user_name") String user_name,@Path("repo_name") String repo_name);

    @POST("/repos/{user_name}/{repo_name}/issues")
    Call<Commemt> addIssue(@Path("user_name") String user_name,
                                @Path("repo_name")String repo_name,
                                @Query("access_token") String token,
                                @Body Commemt issue);
}