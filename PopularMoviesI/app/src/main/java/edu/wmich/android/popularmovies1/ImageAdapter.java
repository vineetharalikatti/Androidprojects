package edu.wmich.android.popularmovies1;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import java.util.ArrayList;
import com.squareup.picasso.*;
/**
 * Created by vineeth on 6/29/16.
 */
public class ImageAdapter extends ArrayAdapter<ImageObject> {

    private Context mContext;
    int layoutResourceId;
    ArrayList<ImageObject> popularMovies;
    int listOfMovies = 0;

    public ImageAdapter(Context context, int resource, ArrayList<ImageObject> popularMovies) {
        super(context, resource, popularMovies);
        this.mContext = context;
        this.layoutResourceId = layoutResourceId;
        this.popularMovies = popularMovies;
    }

    public void setGridData(ArrayList<ImageObject> mGridData) {
        this.popularMovies = mGridData;
        notifyDataSetChanged();
    }

    public int getCount() {


        if (popularMovies != null) {
            listOfMovies = popularMovies.size();
        }
        return listOfMovies;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        View view;
        ImageObject object = getItem(position);

        if (convertView == null) {
            // if it's not recycled, initialize some attributes

        } else {
            imageView = (ImageView)convertView;
        }
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(new GridView.LayoutParams(800, 800));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);
        imageView.setAdjustViewBounds(true);
        ImageObject image = popularMovies.get(position);
        String baseurl = "http://image.tmdb.org/t/p/w500";
        String passedurl = "/cGOPbv9wA5gEejkUN892JrveARt.jpg";

        Log.e("base url Image Adaptere",""+baseurl.concat(passedurl));
        Picasso.with(getContext()).load(baseurl.concat(image.getPoster_path())).into(imageView);

        return imageView;
    }

}
