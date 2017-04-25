package com.samonkey;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;

import tv.danmaku.ijk.media.player.IMediaPlayer;


/**
 * Created on 2017/4/20
 *
 * @author saker
 */

public class VRPlayer extends FrameLayout {

    private Activity mActivity;
    private GLSurfaceView mSurfaceView;
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();
    private MDVRLibrary mVRLibrary;
    private VRPreparedListener mVRPreparedListener;
    private VRClickListener mVRClickListener;

    public VRPlayer(Context activity) {
        this(activity, null);
    }

    public VRPlayer(Context activity, AttributeSet attrs) {
        super(activity, attrs);
        init();
    }

    public void switchPinchEnabled(boolean enable) {
        mVRLibrary.switchPinchEnabled(enable);
    }

    public void switchDisplayMode(int mode) {
        mVRLibrary.switchDisplayMode(mActivity, mode);
    }

    public void switchInteractiveMode(int mode) {
        mVRLibrary.switchInteractiveMode(mActivity, mode);
    }

    public IMediaPlayer getPlayer() {
        return mMediaPlayerWrapper.getPlayer();
    }

    public void setVRClickListener(VRClickListener listener) {
        mVRClickListener = listener;
    }

    public void setVRPreparedListener(VRPreparedListener listener) {
        mVRPreparedListener = listener;
    }

    private void init() {
        // 获取View所在的Activity
        if (this.getContext() instanceof Activity) {
            mActivity = (Activity) this.getContext();
        }
        mSurfaceView = new GLSurfaceView(mActivity);
        this.addView(mSurfaceView);
        // 添加水印
        TextView textWatermark = new TextView(mActivity);
        textWatermark.setText("三目VR");
        textWatermark.setTextColor(Color.WHITE);
        int px = Utils.dip2px(mActivity, 10);
        textWatermark.setPadding(0, px, px, 0);
        textWatermark.setGravity(Gravity.RIGHT);
        textWatermark.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textWatermark.setTypeface(Typeface.createFromAsset(mActivity.getAssets(), "fontzipMin.ttf"));
        this.addView(textWatermark);
        // init VR Library
        mVRLibrary = createVRLibrary();
        mMediaPlayerWrapper.init();
        initEvent();
    }

    private void initEvent() {
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                if (mVRLibrary != null) {
                    mVRLibrary.notifyPlayerChanged();
                }
                if (mVRPreparedListener != null) {
                    mVRPreparedListener.onPrepared(iMediaPlayer);
                }
            }
        });
        getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height,
                                           int sar_num, int sar_den) {
                mVRLibrary.onTextureResize(width, height);
            }
        });
    }

    /**
     * reset后即可openUri重新播放
     * 该方法暂不对外开放
     */
    private void reset() {
        mMediaPlayerWrapper.pause();
        mMediaPlayerWrapper.destroy();
        mMediaPlayerWrapper.init();
    }

    public void openUri(Uri uri) {
        if (uri == null) {
            Toast.makeText(mActivity, "Uri is null!", Toast.LENGTH_SHORT).show();
        } else {
            mMediaPlayerWrapper.openRemoteFile(uri.toString());
            mMediaPlayerWrapper.prepare();
        }
    }

    private MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(mActivity)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(mActivity, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(1.0f).setMax(8.0f).setDefaultValue(0.1f))
                .pinchEnabled(false)// 捏合默认为false
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        return MD360Director.builder().setPitch(-90).build();
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(0.95f))
                .listenGesture(new MDVRLibrary.IGestureListener() {// 单击事件
                    @Override
                    public void onClick(MotionEvent e) {
                        if (mVRClickListener != null) {
                            mVRClickListener.onClick(e);
                        }
                    }
                })
                .build(mSurfaceView);
    }

    public void resume() {
        mVRLibrary.onResume(mActivity);
        mMediaPlayerWrapper.resume();
    }

    public void pause() {
        mVRLibrary.onPause(mActivity);
        mMediaPlayerWrapper.pause();
    }

    public void destroy() {
        mVRLibrary.onDestroy();
        mMediaPlayerWrapper.destroy();
    }

    public void configurationChanged() {
        mVRLibrary.onOrientationChanged(mActivity);
    }

}
