package com.speedata.uhf;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class TaskAdapter extends BaseAdapter {
    private List<String> taskList;
    private Context context;

    public TaskAdapter(Context context,List<String> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = View.inflate(context,R.layout.view_taskname,null);
        }
        TextView textView = convertView.findViewById(R.id.task_name);
        textView.setText(taskList.get(position));
        return convertView;
    }
}
