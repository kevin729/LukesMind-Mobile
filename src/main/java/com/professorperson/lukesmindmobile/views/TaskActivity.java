package com.professorperson.lukesmindmobile.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.google.gson.Gson;
import com.professorperson.lukesmindmobile.R;
import com.professorperson.lukesmindmobile.Http;
import com.professorperson.lukesmindmobile.models.Task;
import com.professorperson.lukesmindmobile.views.adapters.TaskAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {

    ListView taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        taskList = findViewById(R.id.taskList);

        String[] response = new String[1];
        Thread thread = new Thread(() -> {
            response[0] = Http.get("https://lukemind.herokuapp.com/api/get_tasks/1", this);
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {}

        //deserialization
        Gson gson = new Gson();
        Task[] tasks = gson.fromJson(response[0], Task[].class);

        List<Map<String, List<Task>>> mapList = new ArrayList<>();
        Map<String, List<Task>> map = new HashMap();

        Arrays.asList(tasks).forEach((task) -> {
            if (map.containsKey(task.getTaskTitle())) {
                map.get(task.getTaskTitle()).add(task);
            } else {
                List projectTasks = new ArrayList<>();
                projectTasks.add(task);
                map.put(task.getTaskTitle(), projectTasks);
            }
        });

        for (Map.Entry<String, List<Task>> entry : map.entrySet()) {
            Map _map = new HashMap();
            _map.put(entry.getKey(), entry.getValue());
            mapList.add(_map);
        }

        TaskAdapter taskAdapter = new TaskAdapter(this, mapList);
        taskList.setAdapter(taskAdapter);
    }
}