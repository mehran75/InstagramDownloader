package ir.mehran.app.mega.instagramdownloader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadFragment extends Fragment {

    EditText username;
    ImageView profile_image;
    String link = "";
    Bitmap profile_img;
    String user = "";
    FloatingActionButton save;
    CardView searchCardView;
    ProgressBar progressBar;


    Button download;

    public DownloadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_downloader, container, false);


        username = (EditText) v.findViewById(R.id.profile_username);
        profile_image = (ImageView) v.findViewById(R.id.profile_image);
        profile_image.setImageResource(R.drawable.bg2);

        progressBar = v.findViewById(R.id.progressBar);


        searchCardView = (CardView) v.findViewById(R.id.search_cardView);

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchCardView.setAlpha(1);
            }
        });


        save = (FloatingActionButton) v.findViewById(R.id.save_btn);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImg();
            }
        });
        save.hide();

        download = (Button) v.findViewById(R.id.download_btn);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                download();
            }
        });


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShowImage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                RecentFragment.image = ((BitmapDrawable) profile_image.getDrawable()).getBitmap();

                startActivity(intent);
            }
        });


        return v;
    }


    public void download() {

        if ((!"".equals(String.valueOf(username.getText())))) {

            if (isNetworkAvailable()) {


                final Download_image download_image = new Download_image();

                user = String.valueOf(username.getText());
                link = "https://www.instagram.com/" + user;

                try {
                    showProgress();
                    Ion.with(getContext()).load(link).asString()
                            .setCallback(new FutureCallback<String>() {
                                @Override
                                public void onCompleted(Exception e, String result) {
                                    if (e == null) {
                                        Pattern pattern = Pattern.compile("<script type=\"text/javascript\">window._sharedData = (.*?);</script>\n" +
                                                "    <script type=\"text/javascript\">");
                                        Matcher matcher = pattern.matcher(result);
                                        if (matcher.find()) {
                                            try {
                                                JSONObject json = new JSONObject(matcher.group(1));
                                                String id = json.getJSONObject("entry_data")
                                                        .getJSONArray("ProfilePage")
                                                        .getJSONObject(0)
                                                        .getJSONObject("graphql")
                                                        .getJSONObject("user")
                                                        .getString("id");

                                                System.out.println(id);

                                                String grabImageLink = "https://i.instagram.com/api/v1/users/" + id + "/info/";
                                                System.out.println(grabImageLink);
                                                Ion.with(getContext()).load(grabImageLink).asString().setCallback(new FutureCallback<String>() {
                                                    @Override
                                                    public void onCompleted(Exception e, String result) {
                                                        try {
                                                            JSONObject json = new JSONObject(result);
                                                            String url = json.getJSONObject("user")
                                                                    .getJSONObject("hd_profile_pic_url_info")
                                                                    .getString("url");
                                                            System.out.println(url);
                                                            download_image.execute(url);

                                                        } catch (JSONException e1) {
                                                            e1.printStackTrace();
                                                            hideProgress();
                                                            Toast.makeText(getContext(), "دانلود ناموفق بود!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            } catch (JSONException e1) {

                                                e1.printStackTrace();
                                                hideProgress();
                                                Toast.makeText(getContext(), "دانلود ناموفق بود!", Toast.LENGTH_SHORT).show();

                                            }

                                        }

                                    } else {
                                        Toast.makeText(getContext(), "دانلود ناموفق بود!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
//                    progressDialog.show();
//                    download_image.execute(link);
                    save.hide();

                } catch (Exception e) {

                    e.printStackTrace();

                }
            } else {
                Toast.makeText(getActivity(), "دستگاه به اینترنت متصل نیست", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        download.setVisibility(View.GONE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        download.setVisibility(View.VISIBLE);
    }

    public void saveImg() {

        //MediaStore.Images.Media.insertImage(getContentResolver(), profile_img, user, "Profile image");
        createDirectoryAndSaveFile(profile_img, user);
        Toast.makeText(getActivity(), "ذخیره شد", Toast.LENGTH_SHORT).show();

    }


    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {


        //create dir if not exist
        String root = Environment.getExternalStorageDirectory().toString();
        File folder = new File(root, "instaDownload");
        if (!folder.exists()) {
            folder.mkdir();
        }

        File profilesFolder = new File(root + "/instaDownload", "profiles");

        if (!profilesFolder.exists())
            profilesFolder.mkdir();

        //create new file
        fileName += "_Image.jpg";

        File img = new File(profilesFolder, fileName);
        if (img.exists())
            img.delete();
        try {
            OutputStream out = new FileOutputStream(img);
            imageToSave.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class Download_image extends AsyncTask<String, Integer, Bitmap> {

        Exception error;


        @Override
        protected Bitmap doInBackground(String... url) {


            try {


                URL img_url = new URL(url[0]);
                HttpURLConnection connection = (HttpURLConnection) img_url.openConnection();
                InputStream inputStream = connection.getInputStream();

                return BitmapFactory.decodeStream(inputStream);

//                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            hideProgress();
            if (error == null) {
                profile_image.setImageBitmap(bitmap);
                profile_img = bitmap;

                //                progressDialog.setProgress(0);
                //                index = 0;

                save.show();
            } else {
                Toast.makeText(getActivity(), "خطا", Toast.LENGTH_LONG).show();
            }

        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

}