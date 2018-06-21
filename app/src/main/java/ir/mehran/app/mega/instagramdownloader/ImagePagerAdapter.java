package ir.mehran.app.mega.instagramdownloader;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class ImagePagerAdapter extends PagerAdapter {

    private List<View> viewList;
    private Context context;
    private LayoutInflater inflater;

    public ImagePagerAdapter(Context context) {
        this.context = context;
        viewList = new ArrayList<>();
    }


    @Override
    public int getCount() {
        return viewList.size();
    }

    public void addView(View view) {
        viewList.add(view);
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.adapter_layout,container,false);
    }

    @Override
    public int getItemPosition(Object object) {

        return viewList.indexOf(object);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        viewList.remove(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
