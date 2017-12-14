package lk.lakmalz.audiolist;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AudioFilesAdapter.AudioFileAdapterCallback {

    public static final String ARG_PLAY_PROGRESSING = "PROGRESSING";
    public static final String ARG_POSITION = "AUDIO_POSITION";
    public static final String ARG_DURATION = "AUDIO_DURATION";
    public static final String ARG_AUDIO_STATUS = "AUDIO_STATUS";

    public static final int PLAYING = 200;
    public static final int PLAYED_SELECTED = 300;
    public static final int STOPED = 400;

    SeekBar mSeekBar;

    MediaPlayer mMediaPlayer;

    Handler mHandler;

    Runnable mRunnable;

    ImageButton imgBtnPlay;

    RecyclerView rvList;

    private AudioFilesAdapter mAdapter;
    private String broadcaster;


    private void initRecyclerView() {

        mAdapter = new AudioFilesAdapter(this, this, getListItems());
        rvList.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        rvList.setHasFixedSize(true);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(mAdapter);

    }

    private int getAlarm(int position) {
        int rawVal = R.raw.alarm1;
        switch (position) {
            case 0:
                rawVal = R.raw.alarm1;
                break;
            case 1:
                rawVal = R.raw.alarm2;
                break;
            case 2:
                rawVal = R.raw.alarm3;
                break;
            case 3:
                rawVal = R.raw.alarm4;
                break;
            case 4:
                rawVal = R.raw.alarm5;
                break;
        }

        return rawVal;
    }

    private List<AudioFile> getListItems() {

        List<AudioFile> itemList = new ArrayList<>();
        List<String> fileNames = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.alarm_file_names)));

        for (int i = 0; i < fileNames.size(); i++) {
            AudioFile item = new AudioFile();
            item.id = i;
            item.name = fileNames.get(i);
            item.fileName = "";
            item.progress = 0;
            itemList.add(item);
        }

        return itemList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        broadcaster = getResources().getString(R.string.audio_play_broadcaster);
        mHandler = new Handler();
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        imgBtnPlay = (ImageButton) findViewById(R.id.img_btn_play);
        rvList = (RecyclerView) findViewById(R.id.rv_list);
        initRecyclerView();
        onClick();

        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.let_me_love);

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mSeekBar.setMax(mMediaPlayer.getDuration());


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if (input) {
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void onClick() {
        imgBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying()) {
                    imgBtnPlay.setImageResource(R.drawable.ic_play);
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                    playCycle();
                    imgBtnPlay.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mHandler.removeCallbacks(mRunnable);
    }

    int currentPosition = -1;

    private void playCycle() {
        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
        if (currentPosition != -1) {
            updateAdapter(currentPosition, mMediaPlayer.getCurrentPosition(), PLAYING, mMediaPlayer.getDuration());
        }
        if (mMediaPlayer.isPlaying()) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            mHandler.postDelayed(mRunnable, 10);
        } else {
            imgBtnPlay.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    public void onClickPlay(int currentProgress, int position) {
        if (currentPosition != -1) {
            updateAdapter(currentPosition, mMediaPlayer.getCurrentPosition(), STOPED, 0);
            mAdapter.notifyDataSetChanged();
        }

        startPlay(position, currentProgress);
    }

    @Override
    public void onClickStop(int position) {
        updateAdapter(position, mMediaPlayer.getCurrentPosition(), STOPED, mMediaPlayer.getDuration());
        mMediaPlayer.pause();
        currentPosition = -1;
    }

    private void startPlay(int position, int currentProgress) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = MediaPlayer.create(this, getAlarm(position));

        mMediaPlayer.start();

        mMediaPlayer.seekTo(currentProgress);

        playCycle();

        currentPosition = position;

        updateAdapter(position, 0, PLAYING, mMediaPlayer.getDuration());
    }


    private void updateAdapter(final int position, int progress, int status, int duration) {
        Intent bordCaster = new Intent();
        bordCaster.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        bordCaster.setAction(broadcaster);
        bordCaster.putExtra(ARG_AUDIO_STATUS, status);
        bordCaster.putExtra(ARG_PLAY_PROGRESSING, progress);
        bordCaster.putExtra(ARG_POSITION, position);
        bordCaster.putExtra(ARG_DURATION, duration);
        sendBroadcast(bordCaster);
    }
}
