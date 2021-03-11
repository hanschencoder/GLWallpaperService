package site.hanschen.glwallpaperservice;

import android.opengl.EGLExt;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * @author chenhang
 */
public class EglConfigChooser implements GLSurfaceView.EGLConfigChooser {

    private static final String TAG = "GLWallpaperService";

    public static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
    public static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;

    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    protected int mNumSamples;
    protected final int[] mConfigAttribs;
    private final int[] mValue = new int[1];

    public EglConfigChooser(int r, int g, int b, int a, int depth, int stencil, int numSamples) {
        mRedSize = r;
        mGreenSize = g;
        mBlueSize = b;
        mAlphaSize = a;
        mDepthSize = depth;
        mStencilSize = stencil;
        mNumSamples = numSamples;

        mConfigAttribs = new int[]{EGL10.EGL_RED_SIZE,
                                   4,
                                   EGL10.EGL_GREEN_SIZE,
                                   4,
                                   EGL10.EGL_BLUE_SIZE,
                                   4,
                                   EGL10.EGL_RENDERABLE_TYPE,
                                   EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                                   EGL10.EGL_NONE};
    }

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        // get (almost) all configs available by using r=g=b=4 so we can chose with big confidence :)
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, mConfigAttribs, null, 0, num_config);
        int numConfigs = num_config[0];
        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        // now actually read the configurations.
        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, mConfigAttribs, configs, numConfigs, num_config);
        EGLConfig config = chooseConfig(egl, display, configs);
        printConfig(egl, display, config);
        return config;
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
        EGLConfig best = null;
        EGLConfig bestAA = null;
        EGLConfig safe = null; // default back to 565 when no exact match found

        for (EGLConfig config : configs) {
            int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);

            // We need at least mDepthSize and mStencilSize bits
            if (d < mDepthSize || s < mStencilSize) {
                continue;
            }

            // We want an *exact* match for red/green/blue/alpha
            int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
            int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
            int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
            int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

            // Match RGB565 as a fallback
            if (safe == null && r == 5 && g == 6 && b == 5 && a == 0) {
                safe = config;
            }
            // if we have a match, we chose this as our non AA fallback if that one
            // isn't set already.
            if (best == null && r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize) {
                best = config;

                // if no AA is requested we can bail out here.
                if (mNumSamples == 0) {
                    break;
                }
            }

            // now check for MSAA support
            int hasSampleBuffers = findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLE_BUFFERS, 0);
            int numSamples = findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0);

            // We take the first sort of matching config, thank you.
            if (bestAA == null &&
                hasSampleBuffers == 1 &&
                numSamples >= mNumSamples &&
                r == mRedSize &&
                g == mGreenSize &&
                b == mBlueSize &&
                a == mAlphaSize) {
                bestAA = config;
                continue;
            }

            // for this to work we need to call the extension glCoverageMaskNV which is not
            // exposed in the Android bindings. We'd have to link agains the NVidia SDK and
            // that is simply not going to happen.
            // still no luck, let's try CSAA support
            hasSampleBuffers = findConfigAttrib(egl, display, config, EGL_COVERAGE_BUFFERS_NV, 0);
            numSamples = findConfigAttrib(egl, display, config, EGL_COVERAGE_SAMPLES_NV, 0);

            // We take the first sort of matching config, thank you.
            if (bestAA == null &&
                hasSampleBuffers == 1 &&
                numSamples >= mNumSamples &&
                r == mRedSize &&
                g == mGreenSize &&
                b == mBlueSize &&
                a == mAlphaSize) {
                bestAA = config;
                continue;
            }
        }

        if (bestAA != null) {
            return bestAA;
        } else if (best != null) {
            return best;
        } else {
            return safe;
        }
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

    private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) {
        int[] attributes = {EGL10.EGL_BUFFER_SIZE,
                            EGL10.EGL_RED_SIZE,
                            EGL10.EGL_GREEN_SIZE,
                            EGL10.EGL_BLUE_SIZE,
                            EGL10.EGL_ALPHA_SIZE,
                            EGL10.EGL_DEPTH_SIZE,
                            EGL10.EGL_STENCIL_SIZE,
                            EGL10.EGL_SAMPLES,
                            EGL10.EGL_SAMPLE_BUFFERS,
                            EGL_COVERAGE_BUFFERS_NV,
                            EGL_COVERAGE_SAMPLES_NV};
        String[] names = {"EGL_BUFFER_SIZE",
                          "EGL_RED_SIZE",
                          "EGL_GREEN_SIZE",
                          "EGL_BLUE_SIZE",
                          "EGL_ALPHA_SIZE",
                          "EGL_DEPTH_SIZE",
                          "EGL_STENCIL_SIZE",
                          "EGL_SAMPLES",
                          "EGL_SAMPLE_BUFFERS",
                          "EGL_COVERAGE_BUFFERS_NV",
                          "EGL_COVERAGE_SAMPLES_NV"};
        Log.d(TAG, "chooseConfig:");
        int[] value = new int[1];
        for (int i = 0; i < attributes.length; i++) {
            int attribute = attributes[i];
            String name = names[i];
            if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                Log.d(TAG, String.format("%s: %d", name, value[0]));
            } else {
                egl.eglGetError();
            }
        }
    }
}
