package com.speedata.uhf;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TaskSelectAdapter extends BaseAdapter {
    private List<String> taskList;
    private Context context;
    private List<String> checkBoxList;

    public TaskSelectAdapter(Context context) {
        this.context = context;
        checkBoxList = new ArrayList<>();
    }

    public List<String> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<String> taskList) {
        this.taskList = taskList;
    }

    public List<String> getCheckBoxList() {
        return checkBoxList;
    }

    public void setCheckBoxList(List<String> checkBoxList) {
        this.checkBoxList = checkBoxList;
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
            convertView = View.inflate(context,R.layout.view_select_taskname,null);
        }
        TextView textView = convertView.findViewById(R.id.select_task_name);
        textView.setText(taskList.get(position));
        final CheckBox checkBox = convertView.findViewById(R.id.select_task_checBox);
        checkBox.setText(taskList.get(position));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(context,isChecked+"bbbbbbbbbbbb",Toast.LENGTH_SHORT).show();
                buttonView.setChecked(isChecked);
                if (isChecked){
                    checkBoxList.add(buttonView.getText().toString());
                }else {
                    checkBoxList.remove(buttonView.getText().toString());
                }
            }
        });
        return convertView;
    }
}
