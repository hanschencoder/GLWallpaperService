package site.hanschen.glwallpaperservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * @author chenhang
 */
public abstract class GLWallpaperService extends WallpaperService {

    private static final String TAG = "GLWallpaperService";

    protected abstract GLEngine createGLEngine();

    @Override
    public final Engine onCreateEngine() {
        return createGLEngine();
    }

    public abstract class GLEngine extends Engine {

        private final GLWallpaperSurfaceView mGLSurfaceView;

        public GLEngine() {
            mGLSurfaceView = new GLWallpaperSurfaceView(GLWallpaperService.this, this);
        }

        protected abstract void setupGLSurfaceView(boolean isPreview);

        @CallSuper
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            Log.d(TAG, "onCreate:" + this + ", surfaceHolder=" + surfaceHolder);
        }

        @CallSuper
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            Log.d(TAG, "onSurfaceCreated:" + this + ", surfaceHolder=" + holder);
            setupGLSurfaceView(isPreview());
            mGLSurfaceView.surfaceCreated(holder);
        }

        @CallSuper
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            Log.d(TAG, "onSurfaceChanged:" + this + ", width=" + width + ", height=" + height + ", surfaceHolder=" + holder);
            mGLSurfaceView.surfaceChanged(holder, format, width, height);
        }

        @CallSuper
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            Log.d(TAG, "onSurfaceDestroyed:" + this + ", surfaceHolder=" + holder);
            mGLSurfaceView.surfaceDestroyed(holder);
            mGLSurfaceView.onDestroy();
        }

        @CallSuper
        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d(TAG, "onDestroy:" + this);
        }

        //----------------- GLSurfaceView Delegate -----------------
        public void setGLWrapper(GLSurfaceView.GLWrapper glWrapper) {
            mGLSurfaceView.setGLWrapper(glWrapper);
        }

        public void setDebugFlags(int debugFlags) {
            mGLSurfaceView.setDebugFlags(debugFlags);
        }

        public int getDebugFlags() {
            return mGLSurfaceView.getDebugFlags();
        }

        public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
            mGLSurfaceView.setPreserveEGLContextOnPause(preserveOnPause);
        }

        public boolean getPreserveEGLContextOnPause() {
            return mGLSurfaceView.getPreserveEGLContextOnPause();
        }

        public void setRenderer(GLSurfaceView.Renderer renderer) {
            mGLSurfaceView.setRenderer(renderer);
        }

        public void setEGLContextFactory(GLSurfaceView.EGLContextFactory factory) {
            mGLSurfaceView.setEGLContextFactory(factory);
        }

        public void setEGLWindowSurfaceFactory(GLSurfaceView.EGLWindowSurfaceFactory factory) {
            mGLSurfaceView.setEGLWindowSurfaceFactory(factory);
        }

        public void setEGLConfigChooser(GLSurfaceView.EGLConfigChooser configChooser) {
            mGLSurfaceView.setEGLConfigChooser(configChooser);
        }

        public void setEGLConfigChooser(boolean needDepth) {
            mGLSurfaceView.setEGLConfigChooser(needDepth);
        }

        public void setEGLContextClientVersion(int version) {
            mGLSurfaceView.setEGLContextClientVersion(version);
        }

        public void setRenderMode(int renderMode) {
            mGLSurfaceView.setRenderMode(renderMode);
        }

        public int getRenderMode() {
            return mGLSurfaceView.getRenderMode();
        }

        public void requestRender() {
            try {
                mGLSurfaceView.requestRender();
            } catch (Throwable ignored) {
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mGLSurfaceView.surfaceCreated(holder);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mGLSurfaceView.surfaceDestroyed(holder);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            mGLSurfaceView.surfaceChanged(holder, format, w, h);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void surfaceRedrawNeededAsync(SurfaceHolder holder, Runnable finishDrawing) {
            mGLSurfaceView.surfaceRedrawNeededAsync(holder, finishDrawing);
        }

        public void surfaceRedrawNeeded(SurfaceHolder holder) {
            mGLSurfaceView.surfaceRedrawNeeded(holder);
        }

        public void onPause() {
            try {
                mGLSurfaceView.onPause();
            } catch (Throwable ignored) {
            }
        }

        public void onResume() {
            try {
                mGLSurfaceView.onResume();
            } catch (Throwable ignored) {
            }
        }

        public void queueEvent(Runnable r) {
            mGLSurfaceView.queueEvent(r);
        }
    }

    @SuppressLint("ViewConstructor")
    public static class GLWallpaperSurfaceView extends GLSurfaceView {

        private final Engine mEngine;

        public GLWallpaperSurfaceView(@NonNull Context context, @NonNull Engine engine) {
            super(context);
            mEngine = engine;
            getHolder().removeCallback(this);
        }

        @Override
        public SurfaceHolder getHolder() {
            if (mEngine != null && mEngine.getSurfaceHolder() != null) {
                return mEngine.getSurfaceHolder();
            } else {
                return super.getHolder();
            }
        }

        public void onDestroy() {
            super.onDetachedFromWindow();
        }
    }
}
