package com.professorperson.lukesmindmobile.views.adapters;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.professorperson.lukesmindmobile.R;
import com.professorperson.lukesmindmobile.Http;
import com.professorperson.lukesmindmobile.models.Task;

import java.util.List;

public class TaskCheckAdapter extends ArrayAdapter<Task> {

    ListView taskList;

    public TaskCheckAdapter(@NonNull Context context, List resource, ListView taskList) {
        super(context, R.layout.taskcheck, resource);
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.taskcheck, parent, false);

        Task task = getItem(position);

        //complete
        CheckBox box = view.findViewById(R.id.checkbox);
        if ("DONE".equals(task.getStatus())) {
            box.setChecked(true);
        }

        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                task.setStatus("DONE");
            } else {
                task.setStatus("BACKLOG");
            }

            Thread thread = new Thread(() -> {
                Http.put("https://lukesmind.herokuapp.com/api/modify_task", new Gson().toJson(task), getContext());
            });

            thread.start();
        });

        //priority
        NumberPicker np = view.findViewById(R.id.priorityNumber);
        np.setEnabled(true);
        np.setMinValue(0);
        np.setMaxValue(3);
        np.setValue(task.getPriority());

        np.setOnValueChangedListener((picker, oldVal, newVal) -> {

            np.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int priority = newVal;
                    task.setPriority(priority);
                    Toast.makeText(getContext(), Integer.toString(priority), Toast.LENGTH_SHORT).show();
                    Thread thread = new Thread(() -> {
                        Http.put("https://lukesmind.herokuapp.com/api/modify_task", new Gson().toJson(task), getContext());
                    });

                    thread.start();
                }

                return false;
            });

        });

        //note
        TextView taskView = view.findViewById(R.id.checkText);
        taskView.setText(task.getTaskNote());

        return view;
    }
}
