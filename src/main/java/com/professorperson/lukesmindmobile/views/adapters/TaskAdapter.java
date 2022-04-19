package com.professorperson.lukesmindmobile.views.adapters;
import com.professorperson.lukesmindmobile.R;
import com.professorperson.lukesmindmobile.models.Task;

import android.content.Context;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends ArrayAdapter<Map<String, List<Task>>> {

    Context context;

    public TaskAdapter(@NonNull Context context, List resource) {
        super(context, R.layout.task_rows, resource);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.task_rows, parent, false);

        TextView taskTitle = (TextView) view.findViewById(R.id.taskTitle);
        ListView taskList = view.findViewById(R.id.taskCheckList);

        Map<String, List<Task>> projects = getItem(position);
        for (Map.Entry<String, List<Task>> entry : projects.entrySet()) {
            //set project name
            taskTitle.setText(entry.getKey());

            entry.getValue().sort(Comparator.comparingInt(Task::getPriority).reversed());
            //set tasks
            TaskCheckAdapter adapter = new TaskCheckAdapter(getContext(), entry.getValue(), taskList);
            taskList.setAdapter(adapter);
        }

        return view;
    }
}
