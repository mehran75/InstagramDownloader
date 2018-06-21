package ir.mehran.app.mega.instagramdownloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(getApplicationContext(),"با کلیک کردن روی copy share url پست ها را دانلود کنید",Toast.LENGTH_LONG).show();

        return super.onStartCommand(intent, flags, startId);
    }

    ClipboardManager.OnPrimaryClipChangedListener listener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            showText();
        }
    };


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopSelf();
        }
    };

    NotificationManager manager;
    static Notification notification;


    @Override
    public void onCreate() {
        super.onCreate();
        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).addPrimaryClipChangedListener(listener);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);


        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);


        notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("اینستادانلود")
                .setContentText("آماده به کار")
                .setSmallIcon(R.drawable.ic)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // don't let user remove it
//                .addAction(android.R.drawable.ic_menu_close_clear_cancel,"بستن",stopService)
                .build();


        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0, notification);

    }

    @Override
    public void onDestroy() {

        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).removePrimaryClipChangedListener(listener);
        manager.cancel(0);
        super.onDestroy();


    }


    void showText() {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        DownloadPostContent pageContent = new DownloadPostContent(getApplicationContext());


        String str = manager.getPrimaryClip().getItemAt(0).getText().toString();
        Log.i("url", str);


        if (str.contains("https://www.instagram.com/p/"))
            if (isNetworkAvailable()) {
                Bundle bundle = new Bundle();
                bundle.putString("requestType","service");
                bundle.putString("url",str);
                pageContent.execute(bundle);
            }
            else
                Toast.makeText(getApplicationContext(),"لطفا از اتصال خود به اینترنت اطمینان حاصل فرمایید",Toast.LENGTH_LONG).show();


    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
