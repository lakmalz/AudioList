package lk.lakmalz.audiolist;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by A Lakmal Weerasekara (Lakmalz) on 23/8/17.
 * alrweerasekara@gmail.com
 */

public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.ViewHolder> {
    private static final String TAG = "AudioFilesAdapter";

    public class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {

        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.item_content)
        LinearLayout itemContent;
        @BindView(R.id.btn_play)
        ImageButton imgBtnPlay;
        @BindView(R.id.progress)
        ProgressBar mProgressBar;

        int audioId = -1;
        AudioPlayReceiver receiver;
        boolean isPlaying = false;
        int totDuration = -1;
        int progress = 0;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @OnClick(R.id.btn_play)
        public void onClickButtonPlay(View view) {

            if (isPlaying) {
                mCallback.onClickStop(getAdapterPosition());
                isPlaying = false;
                return;
            }
            mCallback.onClickPlay(progress, getAdapterPosition());
        }

        public void initReceiver(int _audioId) {
            audioId = _audioId;

            String broadcaster = context.getResources().getString(R.string.audio_play_broadcaster);
            IntentFilter filter = new IntentFilter(broadcaster);

            if (receiver != null)
                unregisterReceiver();

            receiver = new AudioPlayReceiver();
            context.registerReceiver(receiver, filter);
        }

        public void unregisterReceiver() {
            context.unregisterReceiver(receiver);
        }

        class AudioPlayReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent args) {
                int status = args.getIntExtra(MainActivity.ARG_AUDIO_STATUS, -1);
                switch (status) {
                    case MainActivity.PLAYING:
                        updateProgress(args);
                        break;

                    case MainActivity.STOPED:
                        updateStop(args);
                        Log.i(TAG, "status" + status);
                        break;

                    case MainActivity.PLAYED_SELECTED:
                        //updatePlayedSelected(args);
                        break;

                    default:
                        break;
                }
            }

            private void updateStop(Intent args) {

                int _audioId = args.getIntExtra(MainActivity.ARG_POSITION, 0);
                int _progress = args.getIntExtra(MainActivity.ARG_PLAY_PROGRESSING, 0);

                if (audioId != -1) {
                    if (audioId == _audioId) {
                        imgBtnPlay.setImageResource(R.drawable.ic_play);
                        progress = _progress;
                        if (getAdapterPosition() != -1) {
                            mDataSet.get(getAdapterPosition()).progress = progress;
                        }
                        mProgressBar.setProgress(progress);
                        isPlaying = false;
                    }
                }
            }

            private void updateProgress(Intent args) {

                int _progress = args.getIntExtra(MainActivity.ARG_PLAY_PROGRESSING, 0);
                int _audioId = args.getIntExtra(MainActivity.ARG_POSITION, 0);
                int _duration = args.getIntExtra(MainActivity.ARG_DURATION, -1);

                if (audioId != -1) {
                    if (audioId == _audioId) {
                        Log.i(TAG, "updateProgress: audioId"+audioId);
                        mProgressBar.setProgress(_progress);
                        imgBtnPlay.setImageResource(R.drawable.ic_pause);
                        isPlaying = true;
                        progress = _progress;
                        Log.i(TAG, "updateProgress: "+getAdapterPosition());
                        if (getAdapterPosition() != -1) {
                            mDataSet.get(getAdapterPosition()).progress = progress;
                        }
                        if (totDuration == -1) {
                            totDuration = _duration;
                            mProgressBar.setMax(totDuration);
                        }
                    }
                }
            }

            private void updatePlayedSelected(Intent args) {

                int _audioId = args.getIntExtra(MainActivity.ARG_POSITION, 0);

                if (audioId != -1) {
                    if (audioId == _audioId) {
                        mProgressBar.setProgress(0);
                        imgBtnPlay.setImageResource(R.drawable.ic_play);
                        isPlaying = false;
                    }
                }
            }

        }
    }

    //-------------
    private Context context;
    private AudioFileAdapterCallback mCallback;
    private final List<AudioFile> mDataSet;
    private int defaultPosition = 0;
    private Activity mActivity;

    public AudioFilesAdapter(Context _context, Activity activity, List<AudioFile> myDataset) {
        mDataSet = myDataset;
        context = _context;
        mActivity = activity;
        mCallback = (AudioFileAdapterCallback) context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_audio_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AudioFile audioFile = mDataSet.get(position);
        holder.tvName.setText(audioFile.name);
        holder.initReceiver(position);
        holder.mProgressBar.setProgress(audioFile.progress);
        Log.i(TAG, "onBindViewHolder: "+holder.progress);
        holder.imgBtnPlay.setImageResource(R.drawable.ic_play);
        holder.tvName.setTextColor(Color.BLACK);
        holder.isPlaying = false;

        if (defaultPosition == position) {
            defaultPosition = -1;
            holder.mProgressBar.setProgress(0);
            //holder.tvName.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    public interface AudioFileAdapterCallback {
        void onClickPlay(int currentProgress, int position);

        void onClickStop(int position);
    }

}
