# GLWallpaperService
支持使用 openGL 开发 Android 动态壁纸

 - 和 GLSurfaceView 接口使用保持一致
 - 支持 OpenGL 2.0

# 添加 GLWallpaperService 依赖

通过Gradle构建：

```groovy
compile 'io.github.hanschencoder-glwallpaperservice-0.1.1'
```

通过Maven构建：

```xml
<dependency>
  <groupId>io.github.hanschencoder</groupId>
  <artifactId>glwallpaperservice</artifactId>
  <version>0.1.1</version>
  <type>pom</type>
</dependency>
```

# 如何使用

1. 继承 `GLWallpaperService` 并设置 `GLEngine`

```java
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
```

2. 实现 `GLSurfaceView#Renderer`， `Renderer` 的使用和 `GLSurfaceView#Renderer` 完全一致，这里不再赘述

```java
public class MyRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
}
```