package ir.mehran.app.mega.instagramdownloader;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ir.adad.client.Adad;
import ir.adad.client.InterstitialAdListener;

public class MainActivity extends AppCompatActivity {


    static SharedPreferences sharedPreferences;
    MyViewPager viewPager;
    TabLayout tabLayout;
    Fragment[] fragments = {
            new DownloadFragment(),
            new PostFragment(),
//            new RecentFragment(),
            new HelpFragment(),
            new AboutMeFragment()
    };

    String[] titles = {
            "دانلود پروفایل",
            "دانلود پست",
//            "دانلود شده ها",
            "راهنمای استفاده",
            "تماس باما"
    };

    static Toolbar toolbar;

    static boolean isRunning;

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Adad.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getApplicationContext().getResources().getConfiguration().setLocale(new Locale("fa"));
        }

        sharedPreferences = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);


        if (isRunning) {
            finish();
        }

        FontsOverride.setDefaultFont(getApplicationContext(), "SERIF", "iran_sans_mobile_light.ttf");

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);


        Switch serviceSwitch;
        serviceSwitch = (Switch) findViewById(R.id.service_switch);

        serviceSwitch.setChecked(sharedPreferences.getBoolean("switch", false));

        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(getBaseContext(), MyService.class);
                if (isChecked) {
                    startService(intent);
                    sharedPreferences.edit().putBoolean("switch", true).apply();
                } else {
                    stopService(intent);
                    sharedPreferences.edit().putBoolean("switch", false).apply();
                }
            }
        });


        viewPager = (MyViewPager) findViewById(R.id.viewPager);
        MyAdapter adapter = new MyAdapter(getSupportFragmentManager(), fragments, titles);

        viewPager.setAdapter(adapter);
        viewPager.setPagingEnabled(true);


        tabLayout = (TabLayout) findViewById(R.id.tabLayout);


        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSmoothScrollingEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        Adad.prepareInterstitialAd();


        Adad.prepareInterstitialAd(new InterstitialAdListener() {
            @Override
            public void onInterstitialAdDisplayed() {

            }

            @Override
            public void onInterstitialClosed() {

            }

            @Override
            public void onAdLoaded() {
                Adad.showInterstitialAd(getApplicationContext());

            }

            @Override
            public void onAdFailedToLoad() {

            }

            @Override
            public void onMessageReceive(JSONObject message) {

            }

            @Override
            public void onRemoveAdsRequested() {

            }
        });

    }

//    public static void startInstalledAppDetailsActivity(final Activity context) {
//        if (context == null) {
//            return;
//        }
//        final Intent i = new Intent();
//        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        i.addCategory(Intent.CATEGORY_DEFAULT);
//        i.setData(Uri.parse("package:" + context.getPackageName()));
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//        context.startActivity(i);
//    }


    class MyAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentsList;
        String[] titles;

        public MyAdapter(FragmentManager fm, Fragment[] fragments, String[] titles) {
            super(fm);
            fragmentsList = new ArrayList<>();
            Collections.addAll(fragmentsList, fragments);
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentsList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }
    }


}
