package site.hanschen.glwallpaperservice.demo;

import android.app.*;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Map<CharSequence, ResolveInfo> mLiveWallpapers = new HashMap<>();
    private CharSequence[] mLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_play_live_wallpaper).setOnClickListener(mOnClickListener);
        findViewById(R.id.main_stop_live_wallpaper_system).setOnClickListener(mOnClickListener);
        findViewById(R.id.main_stop_live_wallpaper_lock).setOnClickListener(mOnClickListener);
        findViewById(R.id.main_send_notification).setOnClickListener(mOnClickListener);

        PackageManager packageManager = getPackageManager();
        Intent queryIntent = new Intent("android.service.wallpaper.WallpaperService");
        List<ResolveInfo> list = packageManager.queryIntentServices(queryIntent, PackageManager.GET_META_DATA);
        if (list != null && list.size() > 0) {
            mLabels = new CharSequence[list.size()];
            for (int i = 0; i < list.size(); i++) {
                ResolveInfo resolveInfo = list.get(i);
                CharSequence label = resolveInfo.loadLabel(packageManager);
                mLabels[i] = label;
                mLiveWallpapers.put(label, resolveInfo);
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.main_play_live_wallpaper) {
                chooseLiveWallpaper();
            } else if (id == R.id.main_stop_live_wallpaper_system) {
                clearLiveWallpaperSystem();
            } else if (id == R.id.main_stop_live_wallpaper_lock) {
                clearLiveWallpaperLock();
            } else if (id == R.id.main_send_notification) {
                sendNotification();
            }
        }
    };

    private void chooseLiveWallpaper() {
        if (mLiveWallpapers.size() > 0) {
            new AlertDialog.Builder(this).setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mLabels),
                                                     new DialogInterface.OnClickListener() {
                                                         @Override
                                                         public void onClick(DialogInterface dialog, int which) {
                                                             ResolveInfo resolveInfo = mLiveWallpapers.get(mLabels[which]);
                                                             startLiveWallpaper(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
                                                         }
                                                     }).show();
        } else {
            Toast.makeText(getApplicationContext(), "没有可用的动态壁纸", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLiveWallpaper(String pkg, String name) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(pkg, name));
        startActivity(intent);
    }

    private void clearLiveWallpaperSystem() {
        try {
            WallpaperManager.getInstance(this).clear(WallpaperManager.FLAG_SYSTEM);
        } catch (IOException ignored) {
        }
    }

    private void clearLiveWallpaperLock() {
        try {
            WallpaperManager.getInstance(this).clear(WallpaperManager.FLAG_LOCK);
        } catch (IOException ignored) {
        }
    }

    public boolean isLiveWallpaperRunning(String targetServiceName) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
        if (wallpaperInfo != null) {
            String currentLiveWallpaperServiceName = wallpaperInfo.getServiceName();
            return currentLiveWallpaperServiceName.equals(targetServiceName);
        }
        return false;
    }

    private void sendNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        NotificationCompat.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String id = "channel_01";
            NotificationChannel channel = new NotificationChannel(id, "name", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("description");
            channel.enableLights(false);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(MainActivity.this, id);
        } else {
            builder = new NotificationCompat.Builder(MainActivity.this);
        }
        builder.setContentTitle("测试").setContentText("测试").setAutoCancel(true).setShowWhen(true).setSmallIcon(R.mipmap.ic_launcher);
        manager.notify(1, builder.build());
    }
}
