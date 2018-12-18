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

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {
    List<Issues> issues = new ArrayList<>();
    Context mContext;

    public void setIssues(List<Issues> issues) {
        this.issues = issues;
    }

    public IssueAdapter(Context context, List<Issues> issueList){
        mContext = context;
        issues = issueList;
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
    public IssueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_repo,viewGroup,false);
        ViewHolder holder = new IssueAdapter.ViewHolder(view);

        holder.title = (TextView)view.findViewById(R.id.repoName);
        holder.id = (TextView)view.findViewById(R.id.repoId);
        holder.issue = (TextView)view.findViewById(R.id.repoIssue);
        holder.des = (TextView)view.findViewById(R.id.repoDes);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final IssueAdapter.ViewHolder viewHolder, int i) {
        Issues issue = issues.get(i);
        viewHolder.title.setText("Title："+issue.title);
        viewHolder.id.setText("创建时间："+issue.created_at);
        viewHolder.issue.setText("问题状态："+issue.state);
        viewHolder.des.setText("问题描述:"+issue.body);
    }

    @Override
    public int getItemCount() {
        return issues.size();
    }
}
