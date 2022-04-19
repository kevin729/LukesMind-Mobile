package com.professorperson.lukesmindmobile.views;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.professorperson.lukesmindmobile.R;
import com.professorperson.lukesmindmobile.Http;

public class YoutubeVideos extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_videos);
        RelativeLayout layout =  findViewById(R.id.youtubeLayout);

        String json = Http.get("https://lukesmind.herokuapp.com/api/get_youtube_videos", this);
        String[] videos = new Gson().fromJson(json, String[].class);

        for (int i = 0; i < videos.length; i++) {
            YouTubePlayerView youtubeView = new YouTubePlayerView(this);
            youtubeView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(youtubeView);
        }

    }
}