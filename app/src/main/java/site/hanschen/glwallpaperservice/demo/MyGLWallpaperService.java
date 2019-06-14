package site.hanschen.glwallpaperservice.demo;

import android.service.wallpaper.WallpaperService;
import site.hanschen.glwallpaperservice.GLWallpaperService;

/**
 * @author chenhang
 */
public class MyGLWallpaperService extends GLWallpaperService {

    @Override
    public WallpaperService.Engine onCreateEngine() {
        GLEngine engine = new GLEngine();
        engine.setEGLContextClientVersion(2);
        engine.setRenderer(new MyRenderer());
        engine.setRenderMode(RENDERMODE_CONTINUOUSLY);
        return engine;
    }
}
