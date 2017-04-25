package com.samonkey.vrplayersdk;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.samonkey.NormalPlayer;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class NormalActivity extends AppCompatActivity {

    private static final String TAG = "NormalActivity";
    private NormalPlayer mNormalPlayer;
    private IMediaPlayer mIMediaPlayer;
    private int mTotalDuration;
    private int mCurrentPosition;
    private Handler mHandler = new Handler();
    private SeekBar mSeekBar;
    private TextView mTextView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_normal);
        mTextView = (TextView) findViewById(R.id.tv_normal);
        mSeekBar = (SeekBar) findViewById(R.id.sb_normal);
        mSeekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        mNormalPlayer = (NormalPlayer) findViewById(R.id.np_normal);
        mIMediaPlayer = mNormalPlayer.getPlayer();
        mIMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                mProgressBar.setVisibility(View.GONE);
                // 开始更新播放进度
                mTotalDuration = (int) (mIMediaPlayer.getDuration() / 1000);
                mSeekBar.setMax(mTotalDuration);
                mHandler.post(new UpdateProgress());
            }
        });
        mIMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
                Toast.makeText(NormalActivity.this, "Error:what=" + what + ",extra=" + extra,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mIMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                finish();
            }
        });
        // 播放网络视频
        mNormalPlayer.openRemoteFile(MainActivity.URL);
        // 播放本地视频
//        mNormalPlayer.openRemoteFile(Environment.getExternalStorageDirectory()+"/iiii.mp4");
    }

    // 播放
    public void play(View view) {
        if (!mIMediaPlayer.isPlaying()) {
            mIMediaPlayer.start();
        }
    }

    // 暂停
    public void pause(View view) {
        if (mIMediaPlayer.isPlaying()) {
            mIMediaPlayer.pause();
        }
    }

    private class UpdateProgress implements Runnable {

        @Override
        public void run() {
            mCurrentPosition = (int) (mIMediaPlayer.getCurrentPosition() / 1000);
            mSeekBar.setProgress(mCurrentPosition);
            mTextView.setText(String.format("%02d:%02d", mCurrentPosition / 60, mCurrentPosition % 60));
            mHandler.postDelayed(this, 500);
        }
    }

    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mIMediaPlayer.seekTo(mSeekBar.getProgress() * 1000);
        }
    }

    // -----------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNormalPlayer.destroy();
        // 取消与mHandler绑定的更新进度任务
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNormalPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNormalPlayer.resume();
    }
}
