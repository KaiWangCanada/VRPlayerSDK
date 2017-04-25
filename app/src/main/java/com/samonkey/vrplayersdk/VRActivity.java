package com.samonkey.vrplayersdk;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asha.vrlib.MDVRLibrary;
import com.samonkey.VRClickListener;
import com.samonkey.VRPlayer;
import com.samonkey.VRPreparedListener;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class VRActivity extends AppCompatActivity {

    private static final String TAG = "VRActivity";
    private VRPlayer mVRPlayer;
    private ProgressBar mProgressBar;
    private SeekBar mSeekBar;
    private TextView mTextView;
    private IMediaPlayer mIMediaPlayer;
    private int mTotalDuration;
    private int mCurrentPosition;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_vr);
        mTextView = (TextView) findViewById(R.id.tv_normal);
        mSeekBar = (SeekBar) findViewById(R.id.sb_normal);
        mSeekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        mVRPlayer = (VRPlayer) findViewById(R.id.vp_vr);
        mIMediaPlayer = mVRPlayer.getPlayer();
        mVRPlayer.setVRPreparedListener(new VRPreparedListener() {
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
                Toast.makeText(VRActivity.this, "Error:what=" + what + ",extra=" + extra,
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
        // 播放器的单击事件
        mVRPlayer.setVRClickListener(new VRClickListener() {
            @Override
            public void onClick(MotionEvent e) {
                Toast.makeText(VRActivity.this, "clicked", Toast.LENGTH_SHORT).show();
            }
        });
        // 设置显示模式 默认MDVRLibrary.DISPLAY_MODE_NORMAL
        mVRPlayer.switchDisplayMode(MDVRLibrary.DISPLAY_MODE_NORMAL);
        // 设置交互模式 默认MDVRLibrary.INTERACTIVE_MODE_MOTION
        mVRPlayer.switchInteractiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION);
        // 开启捏合手势
        mVRPlayer.switchPinchEnabled(true);
        // 播放网络视频
        mVRPlayer.openUri(Uri.parse(MainActivity.URL));
        // 播放本地视频
//        mVRPlayer.openUri(Uri.parse(Environment.getExternalStorageDirectory()+"/iiii.mp4"));
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

    public void showDisplayDialog(View view) {
        String[] items = {"正常", "分屏"};
        new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mVRPlayer.switchDisplayMode(MDVRLibrary.DISPLAY_MODE_NORMAL);
                        break;
                    case 1:
                        mVRPlayer.switchDisplayMode(MDVRLibrary.DISPLAY_MODE_GLASS);
                        break;
                }
                dialog.dismiss();
            }
        }).show();
    }

    public void showInteractiveDialog(View view) {
        String[] items = {"陀螺仪", "手势", "陀螺仪&手势"};
        new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mVRPlayer.switchInteractiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION);
                        break;
                    case 1:
                        mVRPlayer.switchInteractiveMode(MDVRLibrary.INTERACTIVE_MODE_TOUCH);
                        break;
                    case 2:
                        mVRPlayer.switchInteractiveMode
                                (MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH);
                        break;
                }
                dialog.dismiss();
            }
        }).show();
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
        mVRPlayer.destroy();
        // 取消与mHandler绑定的更新进度任务
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVRPlayer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRPlayer.pause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRPlayer.configurationChanged();
    }
}
