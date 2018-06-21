package ir.mehran.app.mega.instagramdownloader;


import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment {


    private CardView cardView;
    private EditText postUrl_text;
    private ProgressBar progressBar;
    private String caption_text = "";

    public PostFragment() {
        // Required empty public constructor
    }

    ImageView post_image;
    VideoView videoView;
    Button copyCaption;
    Button search_btn;
    ImageButton left_btn, right_btn;
    ViewFlipper viewFlipper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_post, container, false);

        copyCaption = v.findViewById(R.id.copy_caption);
        search_btn = v.findViewById(R.id.download_btn);
        postUrl_text = v.findViewById(R.id.post_url_post_fragment);
        viewFlipper = v.findViewById(R.id.view_flipper);
        cardView = v.findViewById(R.id.post_search_cardView);

        left_btn = v.findViewById(R.id.left_btn);
        right_btn = v.findViewById(R.id.right_btn);

        copyCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (caption_text != null || !caption_text.equals("")) {
                    ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("",caption_text));
                    Toast.makeText(getContext(), "کپشن کپی شد.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "کپشن موجود نیست.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        progressBar = v.findViewById(R.id.progressBar);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = postUrl_text.getText().toString();

                if (text.contains("https://www.instagram.com/p/")) {

                    try {
                        if (isNetworkAvailable()) {
                            Bundle bundle = new Bundle();
                            bundle.putString("requestType", "fragment");
                            bundle.putString("url", text);
                            cardView.setAlpha(.5f);
                            showProgress();
                            new DownloadPostContent(PostFragment.this).execute(bundle);

                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), "لینک وارد شده صحیح نیست", Toast.LENGTH_SHORT).show();
                }
            }
        });

        postUrl_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardView.setAlpha(1);
            }
        });


        return v;
    }


    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        search_btn.setVisibility(View.GONE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        search_btn.setVisibility(View.VISIBLE);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void preDownload() {
        if (viewFlipper != null) {
            viewFlipper.removeAllViews();
        }
        left_btn.setVisibility(View.GONE);
        right_btn.setVisibility(View.GONE);

        showProgress();

    }

    public void postDownload(final Bundle bundle) {
        caption_text = bundle.getString("caption");

        if (!caption_text.equals(""))
            copyCaption.setVisibility(View.VISIBLE);
        else
            copyCaption.setVisibility(View.GONE);

        if (bundle.getString("type").equals("GraphImage")) {

            final ImageView imageView = new ImageView(getContext());

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(getContext()).load(bundle.getString("url")).into(imageView);

            viewFlipper.addView(imageView);

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


                    saveImageToSDCard(imageView, bundle.getString("id"));
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                    Toast.makeText(getContext(), "عکس مورد نظر ذخیره شد!", Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ShowImage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    RecentFragment.image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    getContext().startActivity(intent);
                    Toast.makeText(getContext(), "برای ذخیره روی عکس چند ثانیه نگهدارید", Toast.LENGTH_LONG).show();

                }
            });

        } else if (bundle.getString("type").equals("GraphVideo")) {
            try {
                String path = "";

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
                        , RelativeLayout.LayoutParams.MATCH_PARENT);

                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);


                RelativeLayout container = new RelativeLayout(getContext());
                container.setLayoutParams(params);


                final VideoView videoView = new VideoView(getContext());
                videoView.setVideoURI(Uri.parse(bundle.getString("video_url")));
                videoView.setLayoutParams(params);
                MediaController mediaController = new MediaController(getContext());
                mediaController.setMediaPlayer(videoView);
                videoView.setMediaController(mediaController);
                videoView.requestFocus();

                videoView.start();

                container.addView(videoView);
                viewFlipper.addView(container);

                saveVideoToSDCard(new URL(bundle.getString("video_url")), bundle.getString("id"));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (bundle.getString("type").equals("GraphSidecar")) {

            String[] jsonArray = bundle.getStringArray("array");

            for (int i = 0; i < jsonArray.length; i++) {
                try {
                    final JSONObject jsonObject = new JSONObject(jsonArray[i]);
                    System.out.println("jsonObject = " + jsonObject);


                    if (jsonObject.getBoolean("is_video")) {


                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
                                , RelativeLayout.LayoutParams.MATCH_PARENT);

                        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);


                        RelativeLayout container = new RelativeLayout(getContext());
                        container.setLayoutParams(params);

                        final VideoView videoView = new VideoView(getContext());
                        videoView.setLayoutParams(params);
                        videoView.setVideoURI(Uri.parse(jsonObject.getString("video_url")));
                        MediaController mediaController = new MediaController(getContext());
                        mediaController.setMediaPlayer(videoView);
                        videoView.setMediaController(mediaController);
                        videoView.requestFocus();

                        container.addView(videoView);
                        viewFlipper.addView(container);

                        saveVideoToSDCard(new URL(jsonObject.getString("video_url")), jsonObject.getString("id"));
                    } else {

                        final ImageView imageView = new ImageView(getContext());
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Picasso.with(getContext()).load(jsonObject.getString("display_url"))
                                .into(imageView);

                        imageView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                try {
                                    saveImageToSDCard(imageView, jsonObject.getString("id"));
                                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrator.vibrate(100);
                                    Toast.makeText(getContext(), "عکس مورد نظر ذخیره شد!", Toast.LENGTH_LONG).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                        });

                        viewFlipper.addView(imageView);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            left_btn.setVisibility(View.VISIBLE);
            right_btn.setVisibility(View.VISIBLE);

            left_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewFlipper.showPrevious();
                }
            });

            right_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewFlipper.showNext();
                }
            });
        }

        hideProgress();

        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);

    }


    private String saveVideoToSDCard(URL url, String id) throws IOException {

        int TIMEOUT_CONNECTION = 5000;//5sec
        int TIMEOUT_SOCKET = 30000;//30sec

        //create dir if not exist
        String root = Environment.getExternalStorageDirectory().toString();
        File folder = new File(root, "instaDownload");
        if (!folder.exists())
            folder.mkdir();


        File folder2 = new File(root + "/instaDownload", "videos");
        if (!folder2.exists())
            folder2.mkdir();


        new DownloadPostContent.DownloadVideo(getContext()).execute(url.toString(), id, folder2.getPath());
        return folder2 + File.separator + id;

    }

    private void saveImageToSDCard(ImageView imageView, String id) {

        //create dir if not exist
        String root = Environment.getExternalStorageDirectory().toString();
        File folder = new File(root, "instaDownload");
        if (!folder.exists())
            folder.mkdir();

        File photo_folder = new File(root + "/instaDownload", "photos");
        if (!photo_folder.exists())
            photo_folder.mkdir();

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        id += "_img.jpg";

        File img = new File(photo_folder, id);
        if (img.exists())
            img.delete();
        try {
            OutputStream out = new FileOutputStream(img);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


/*

    class DownloadPost extends AsyncTask<URL, Boolean, Bundle> {


        private Context context;
        private Exception error;
        private ProgressDialog progressDialog;

        public DownloadPost(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(context);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("درحال دانلود");
            progressDialog.setMessage("لطفا صبر کنید...");

            progressDialog.show();


        }

        @Override
        protected Bundle doInBackground(URL... urls) {

            try {

                Bundle bundle = new Bundle();

                String str = "";
                HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int r = reader.read(), i = 0;

                while (r != -1) {
                    if (++i > 8000 && i < 17000) {
                        str += (char) r;

                    }
                    r = reader.read();
                    if (i > 16000)
                        break;
                }

                String root = Environment.getExternalStorageDirectory().toString();
                File folder = new File(root, "instaDownload");
                if (!folder.exists()) {
                    folder.mkdir();
                }


                if (str.contains("og:video\" content=")) {
                    String res = "";
                    Pattern pattern = Pattern.compile("<meta property=\"og:video\" content=\"(.*?)\" />");
                    Matcher m = pattern.matcher(str);

                    while (m.find()) {
                        res += m.group(1);
                    }

//           -----------------download----------------------------
                    File downloadsFolder = new File(root + "/instaDownload", "videos");
                    if (!downloadsFolder.exists())
                        downloadsFolder.mkdir();

                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(res));

                    request.setTitle("video_" + downloadsFolder.listFiles().length + 1);
                    int num = downloadsFolder.listFiles().length + 1;
                    request.setDestinationInExternalPublicDir("/instaDownload/videos",
                            "video_" + num + ".mp4");

                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

//                    downloadManager.enqueue(request);
//           -----------------download----------------------------
                    progressDialog.cancel();

                    String caption = "";


                    System.out.println(str.indexOf("<meta name=\"description\" content=\""));

                    Pattern pattern2 = Pattern.compile("<meta name=\"description\" content=\"(.*?)\" />");
                    Matcher m2 = pattern2.matcher(str);

                    while (m2.find()) {
                        Log.i("find", m2.group(1));
                        caption += m2.group(1);
                    }

                    bundle.putBoolean("video", true);
                    bundle.putString("location", root + "/instaDownload/videos" + "video_" + num + ".mp4");
                    bundle.putString("caption", caption);

                    return bundle;


                } else if (str.contains("og:image\" content=\"")) {
//            System.out.println("photo");
                    String res = "";
                    Pattern pattern = Pattern.compile("<meta property=\"og:image\" content=\"(.*?)\" />");
                    Matcher m = pattern.matcher(str);
                    while (m.find())
                        res += m.group(1);

                }

            } catch (Exception e) {
                error = e;
            }


            return null;
        }

        @Override
        protected void onPostExecute(Bundle bundle) {

            if (bundle.getBoolean("video")){
                caption_et.setText(bundle.getString("caption"));
            } else {

            }

        }
    }

*/
}

