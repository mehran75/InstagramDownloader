package ir.mehran.app.mega.instagramdownloader;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DownloadPostContent extends AsyncTask<Bundle, Void, Bundle> {

//    private final ImageButton left_btn, right_btn;
    private Context context;
//    private TextView caption_et;
//    private ViewFlipper viewFlipper;
    private Exception error;
    private boolean is_from_service = false;
    private ProgressDialog progressDialog;

    private PostFragment postFragment;

    public DownloadPostContent(PostFragment postFragment) {
        this.postFragment =postFragment;
//
        this.context = postFragment.getContext();
//        this.caption_et = caption_et;
//        this.viewFlipper = viewFlipper;
//        this.left_btn = left_btn;
//        this.right_btn = right_btn;
    }

    public DownloadPostContent(Context applicationContext) {
        this.context = applicationContext;
//        left_btn = null;
//        right_btn = null;
        is_from_service = true;
    }


    @Override
    protected void onPreExecute() {


        if (is_from_service) {
            MyService.notification = new NotificationCompat.Builder(context,"0")
                    .setContentTitle("اینستادانلود")
                    .setContentText("درحال دریافت اطلاعات...")
                    .setSmallIcon(R.drawable.ic)
                    .setOngoing(true) // don't let user remove it
//                .addAction(android.R.drawable.stat_notify_sync_noanim,"Exit",pendingIntent)
                    .build();

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(0);
            manager.notify(0, MyService.notification);

        } else {
            if (postFragment != null)
            postFragment.preDownload();

        }

    }

    @Override
    protected Bundle doInBackground(Bundle... urls) {

        try {

            Document doc = Jsoup.connect(urls[0].getString("url")).get();

            Pattern pattern = Pattern.compile("<script type=\"text/javascript\">window._sharedData = (.*?)</script>");
            Matcher matcher = pattern.matcher(doc.toString());

            String result = "";
            while (matcher.find()) {
                result = matcher.group(1);
            }

            JSONObject jsonObject = new JSONObject(result).getJSONObject("entry_data")
                    .getJSONArray("PostPage")
                    .getJSONObject(0)
                    .getJSONObject("graphql")
                    .getJSONObject("shortcode_media");

            Bundle bundle = new Bundle();
            bundle.putString("requestType", urls[0].getString("requestType"));

            bundle.putString("type", jsonObject.getString("__typename"));

            bundle.putString("caption", jsonObject.getJSONObject("edge_media_to_caption")
                    .getJSONArray("edges")
                    .getJSONObject(0)
                    .getJSONObject("node").getString("text"));

            switch (bundle.getString("type")) {


//                single video item
                case "GraphVideo":
                    bundle.putString("video_url", jsonObject.getString("video_url"));
                    bundle.putString("display_url", jsonObject.getString("display_url"));
                    bundle.putString("id", jsonObject.getString("id"));
                    break;

//                single image item
                case "GraphImage":
                    bundle.putString("url", jsonObject.getString("display_url"));
                    bundle.putString("id", jsonObject.getString("id"));
                    break;

//                multiple item
                case "GraphSidecar":

                    JSONArray array = jsonObject.getJSONObject("edge_sidecar_to_children").getJSONArray("edges");

                    String[] jsons = new String[array.length()];

                    for (int i = 0; i < array.length(); i++) {
                        jsons[i] = array.getJSONObject(i).getJSONObject("node").toString();
                    }

                    bundle.putStringArray("array", jsons);

                    break;
            }

            return bundle;


        } catch (Exception e) {
            e.printStackTrace();
            error = e;
        }


        return null;
    }

    @Override
    protected void onPostExecute(final Bundle bundle) {
        if (error == null && bundle != null) {

            if (bundle.getString("requestType").equals("service")) {

                if (bundle.getString("type").equals("GraphImage")) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(bundle.getString("url")));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                } else if (bundle.getString("type").equals("GraphVideo")) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(bundle.getString("video_url")));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                } else if (bundle.getString("type").equals("GraphSidecar")) {
                    String[] jsonArray = bundle.getStringArray("array");

                    for (int i = 0; i < jsonArray.length; i++) {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonArray[i]);
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    (jsonObject.getBoolean("is_video") ?
                                            Uri.parse(jsonObject.getString("video_url")) :
                                            Uri.parse(jsonObject.getString("display_url"))));

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
//                set notification to standBy mode

                MyService.notification = new NotificationCompat.Builder(context)
                        .setContentTitle("اینستادانلود")
                        .setContentText("آماده به کار")
                        .setSmallIcon(R.drawable.ic)
                        .setOngoing(true) // don't let user remove it
//                .addAction(android.R.drawable.stat_notify_sync_noanim,"Exit",pendingIntent)
                        .build();

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(0);
                manager.notify(0, MyService.notification);

            } else if (bundle.getString("requestType").equals("fragment")) {

                if (postFragment != null)
                    postFragment.postDownload(bundle);

            }
        } else {
            Toast.makeText(context, "خطایی رخ داده است", Toast.LENGTH_LONG).show();
        }

    }



    static class DownloadVideo extends AsyncTask<String, Void, Void> {

        public DownloadVideo(Context context) {
            this.context = context;
        }

        Context context;

        @Override
        protected Void doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                String id = params[1];
                File folder2 = new File(params[2]);

                //        save video
                id += "_video.mp4";
//Open a connection to that URL.
                URLConnection ucon = url.openConnection();

                //this timeout affects how long it takes for the app to realize there's a connection problem
                ucon.setReadTimeout(5000);
                ucon.setConnectTimeout(20000);


                //Define InputStreams to read from the URLConnection.
                // uses 3KB download buffer
                InputStream is = ucon.getInputStream();
                BufferedInputStream inStream = new BufferedInputStream(is, is.available() + 100);
                File file = new File(folder2, id);
                FileOutputStream outStream = new FileOutputStream(file);
                byte[] buff = new byte[is.available() + 100];

                //Read bytes (and store them) until there is nothing more to read(-1)
                int len = 0;
                while ((len = inStream.read(buff)) != -1) {
                    outStream.write(buff, 0, len);
                }

                //clean up
                outStream.flush();
                outStream.close();
                inStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(context, "ویدیو در پوشه instaDownload ذخیره شد", Toast.LENGTH_SHORT).show();
        }
    }
}
