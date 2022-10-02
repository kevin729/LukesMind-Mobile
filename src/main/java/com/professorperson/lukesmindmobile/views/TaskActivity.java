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
import java.util.stream.Collectors;

public class TaskActivity extends AppCompatActivity {

    ListView taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        taskList = findViewById(R.id.taskList);
        setTaskList();
    }

    public String getTasks() {
        String[] response = new String[1];
        Thread thread = new Thread(() -> {
            response[0] = Http.get("https://lukemind.herokuapp.com/api/get_tasks/1", this);
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {}

        return response[0];
    }

    public List<Map<String, List<Task>>> setTaskList() {
        String json = getTasks();
        Gson gson = new Gson();
        Task[] response = gson.fromJson(json, Task[].class);
        List<Map<String, List<Task>>> projects = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();
        Map<String, List<Task>> taskMap = new HashMap();

        for (Task task : response) {
            if (taskMap.containsKey(task.getTaskTitle())) {
                taskMap.get(task.getTaskTitle()).add(task);
            } else {
                List projectTasks = new ArrayList<>();
                projectTasks.add(task);
                taskMap.put(task.getTaskTitle(), projectTasks);
            }
            tasks.add(task);
        }

        projects.add(taskMap);

//        Arrays.stream(tasks).forEach(task -> {
//            Map<String, List<Task>> map = new HashMap<>();
//            map.put(task.getTaskTitle(), Arrays.stream(tasks)
//                    .filter(filter -> filter.getTaskTitle() == task.getTaskTitle())
//                    .collect(Collectors.toList()));
//
//            mapList.add(map);
//        });

        TaskAdapter taskAdapter = new TaskAdapter(this, projects);
        taskList.setAdapter(taskAdapter);

        return projects;
    }
}