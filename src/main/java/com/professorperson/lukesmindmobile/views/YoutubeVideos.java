package com.professorperson.lukesmindmobile.views;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.professorperson.lukesmindmobile.R;
import com.professorperson.lukesmindmobile.Http;

public class YoutubeVideos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_videos);
        RelativeLayout layout =  findViewById(R.id.youtubeLayout);
        YouTubePlayerView previousView = null;

        String json = Http.get("https://lukesmind.herokuapp.com/api/get_youtube_videos", this);
        String[] videos = new Gson().fromJson(json, String[].class);

        for (int i = 0; i < videos.length; i++) {
            final String video = videos[i];

            YouTubePlayerView youtubeView = new YouTubePlayerView(this);
            youtubeView.setId(i+1);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (previousView != null) {
                params.addRule(RelativeLayout.BELOW, previousView.getId());
            }
            youtubeView.setLayoutParams(params);
            previousView = youtubeView;

            youtubeView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    super.onReady(youTubePlayer);
                    youTubePlayer.cueVideo(video, 0);
                }
            });

            layout.addView(youtubeView);
        }

    }
}