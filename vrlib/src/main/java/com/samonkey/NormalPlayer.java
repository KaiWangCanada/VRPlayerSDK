package com.samonkey;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created on 2017/4/20
 *
 * @author saker
 */

public class NormalPlayer extends FrameLayout implements TextureView.SurfaceTextureListener {

    private static final String TAG = "NormalPlayer";
    private Context mContext;
    private TextureView mTextureView;
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();
    private Surface surface;

    public NormalPlayer(Context context) {
        this(context, null);
    }

    public NormalPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public IMediaPlayer getPlayer() {
        return mMediaPlayerWrapper.getPlayer();
    }

    public void openRemoteFile(String url) {
        mMediaPlayerWrapper.openRemoteFile(url);
        mMediaPlayerWrapper.prepare();
    }

    private void init() {
        mTextureView = new TextureView(mContext);
        this.addView(mTextureView);
        mTextureView.setSurfaceTextureListener(this);

        mMediaPlayerWrapper.init();
    }

    public void resume() {
        mMediaPlayerWrapper.resume();
    }

    public void pause() {
        mMediaPlayerWrapper.pause();
    }

    public void destroy() {
        mMediaPlayerWrapper.destroy();
    }

    // --------------------------------

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surface = new Surface(surfaceTexture);
        mMediaPlayerWrapper.setSurface(surface);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mMediaPlayerWrapper.setSurface(null);
        this.surface = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
