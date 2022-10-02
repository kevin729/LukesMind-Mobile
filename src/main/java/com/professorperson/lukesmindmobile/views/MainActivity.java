package com.professorperson.lukesmindmobile.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


import com.professorperson.lukesmindmobile.R;
import com.professorperson.lukesmindmobile.services.FlashService;

public class MainActivity extends AppCompatActivity {

    private Button scannerBtn;
    private Button flashBtn;
    private Button youtubeBtn;
    private Button tasksBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasksBtn = findViewById(R.id.taskBtn);
        tasksBtn.setOnClickListener(v -> startActivity(new Intent(this, TaskActivity.class)));

        scannerBtn = findViewById(R.id.scannerBtn);
        scannerBtn.setOnClickListener(v -> startActivity(new Intent(this, ScannerActivity.class)));

        flashBtn = findViewById(R.id.flashBtn);
        flashBtn.setOnClickListener(v -> { startService(new Intent(this, FlashService.class));});

        youtubeBtn = findViewById(R.id.youtubeBtn);
        youtubeBtn.setOnClickListener(v -> startActivity(new Intent(this, YoutubeVideos.class)));
    }

    @Override
    public void onBackPressed() {
        
    }
}
