package site.hanschen.glwallpaperservice.demo;

import android.opengl.GLSurfaceView;

import site.hanschen.glwallpaperservice.EglConfigChooser;
import site.hanschen.glwallpaperservice.GLWallpaperService;

/**
 * @author chenhang
 */
public class MyGLWallpaperService extends GLWallpaperService {

    @Override
    protected GLEngine createGLEngine() {
        return new GLEngine() {
            @Override
            protected void setupGLSurfaceView(boolean isPreview) {
                setEGLContextClientVersion(2);
                setEGLConfigChooser(new EglConfigChooser(8, 8, 8, 0, 0, 0, 0));
                setRenderer(new MyRenderer());
                setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            }
        };
    }
}
