package edu.wmich.android.popularmovies1;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import java.util.ArrayList;

/**
 * Created by vineeth on 6/29/16.
 */
public class ImageAdapter extends ArrayAdapter<String> {

    private Context mContext;
    int layoutResourceId;
    ArrayList<String> popularMovies;

    public ImageAdapter(Context context, int resource, ArrayList<String> popularMovies) {
        super(context, resource, popularMovies);
        this.mContext = context;
        this.layoutResourceId = layoutResourceId;
        this.popularMovies = popularMovies;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(800, 800));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.got1,
            R.drawable.got2,
            R.drawable.got3,
            R.drawable.got4,
            R.drawable.hungergames
    };
}
