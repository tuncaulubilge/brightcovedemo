package com.example.brightcovedemo;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.brightcove.ima.GoogleIMAEventType;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.media.DeliveryType;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.brightcove.vmap.VMAPComponent;
import com.google.ads.interactivemedia.v3.api.AdsManager;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    private BrightcoveMediaController mediaController;

    public BrightcoveExoPlayerVideoView brightcoveVideoView;
    private EventEmitter eventEmitter;

    private VMAPComponent vmapComponent;
    private String adRulesURL = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Init Video with media controllers
        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcoveVideoView);
        initMediaController();
        adjustPlayerSize();

        vmapComponent = new VMAPComponent(brightcoveVideoView);
        eventEmitter = brightcoveVideoView.getEventEmitter();
        setupUI();

        Button button = (Button) findViewById(R.id.addVideosButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                initVideos();
            }
        });
    }

    private void initVideos() {
        List<String> urls = new ArrayList<>();
        urls.add("http://www.sample-videos.com/video/mp4/480/big_buck_bunny_480p_5mb.mp4");
        urls.add("http://download.wavetlan.com/SVV/Media/HTTP/H264/Other_Media/H264_test5_voice_mp4_480x360.mp4");
        urls.add("http://techslides.com/demos/sample-videos/small.mp4");

        bindVideoData(urls);
        brightcoveVideoView.start();
    }

    private void bindVideoData(List<String> videoUrls) {
        List<com.brightcove.player.model.Video> videos = new ArrayList<>();

        for (String url : videoUrls) {
            Video video = Video.createVideo(url, DeliveryType.MP4);
            video.getProperties().put(VMAPComponent.VMAP_URL, adRulesURL);
            videos.add(video);
        }

        brightcoveVideoView.addAll(videos);
    }

    private void initMediaController() {
        mediaController = new BrightcoveMediaController(brightcoveVideoView);

        // Add "Ad Markers" where the Ads Manager says ads will appear.
        mediaController.addListener(GoogleIMAEventType.ADS_MANAGER_LOADED, new EventListener() {
            @Override
            public void processEvent(Event event) {
                AdsManager manager = (AdsManager) event.properties.get("adsManager");
                List<Float> cuepoints = manager.getAdCuePoints();
                for (int i = 0; i < cuepoints.size(); i++) {
                    Float cuepoint = cuepoints.get(i);
                    mediaController.getBrightcoveSeekBar().addMarker((int) (cuepoint * DateUtils.SECOND_IN_MILLIS));
                }
            }
        });

        brightcoveVideoView.setMediaController(mediaController);
    }

    private void setupUI() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void adjustPlayerSize() {
        ViewGroup.LayoutParams layoutParams = brightcoveVideoView.getLayoutParams();
        layoutParams.height = (int) (getWidth() * 9.0 / 16);
        brightcoveVideoView.setLayoutParams(layoutParams);
    }

    private double getWidth() {
        Point size = new Point();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        return size.x;
    }

}
