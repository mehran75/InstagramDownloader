package ir.mehran.app.mega.instagramdownloader;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment {

    RecyclerView recyclerView;


    public RecentFragment() {
        // Required empty public constructor
    }

    List<Bitmap> data;

    SwipeRefreshLayout swipeLayout;
    RecyclerAdapter adapter;
    public static Bitmap image;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_recent, container, false);

        data = new ArrayList<>();



        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setData();
            }
        });

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RecyclerAdapter();

        setData();
        recyclerView.setAdapter(adapter);


        return v;
    }



    private void setData() {

        String root = Environment.getExternalStorageDirectory().toString() + "instaDownload/profiles";
        File file = new File(root);
        if (file.exists()) {
            File[] files = file.listFiles();

            data.clear();

            try {
                for (File x : files) {
//                    data.add(BitmapFactory.decodeFile(x.getAbsolutePath()));
                    FileInputStream in = new FileInputStream(x);
                    data.add(BitmapFactory.decodeStream(in));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        adapter.add(data);

        adapter.notifyDataSetChanged();

        swipeLayout.setRefreshing(false);
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.holder> {

        List<Bitmap> dataList;
        LayoutInflater inflater;

        class holder extends RecyclerView.ViewHolder {
            ImageView imageView;


            public holder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.profile_image_recent);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(),ShowImage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                        startActivity(intent);
                    }
                });
            }
        }

        public RecyclerAdapter() {
            dataList = new ArrayList<>();
            inflater = LayoutInflater.from(getActivity().getApplicationContext());

        }

        public void add(List<Bitmap> dataList){
            this.dataList.clear();
            this.dataList.addAll(dataList);
        }

        @Override
        public holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.recycler_item, parent, false);

            return new holder(v);
        }

        @Override
        public void onBindViewHolder(holder holder, int position) {
//            Log.i("bitmap", "bit " + String.valueOf(position));
            holder.imageView.setImageBitmap(dataList.get(position));
        }


        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

}
