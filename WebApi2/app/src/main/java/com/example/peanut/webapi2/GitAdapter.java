package com.example.peanut.webapi2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GitAdapter extends RecyclerView.Adapter<GitAdapter.ViewHolder> implements View.OnClickListener {

    List<Repo> repos = new ArrayList<>();
    Context mContext;

    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , Repo repo);
    }
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    public GitAdapter(Context context, List<Repo> repoList){
        mContext = context;
        repos = repoList;
    }

    public void setRepos(List<Repo> repos) {
        this.repos = repos;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        TextView id;
        TextView issue;
        TextView des;
        public ViewHolder(View view){
            super(view);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_repo,viewGroup,false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(this);
        holder.title = (TextView)view.findViewById(R.id.repoName);
        holder.id = (TextView)view.findViewById(R.id.repoId);
        holder.issue = (TextView)view.findViewById(R.id.repoIssue);
        holder.des = (TextView)view.findViewById(R.id.repoDes);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        viewHolder.itemView.setTag(repos.get(i));
        Repo repo = repos.get(i);
        viewHolder.title.setText("项目名:"+repo.name);
        viewHolder.id.setText("项目id:"+repo.id);
        viewHolder.des.setText("项目描述:"+repo.description);
        viewHolder.issue.setText("存在问题:"+repo.open_issues);
    }

    @Override
    public int getItemCount() {
        return repos.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(Repo)v.getTag());
        }
    }
}
