package edu.wmich.android.popularmovies1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by vineeth on 7/3/16.
 */
public class DetailActivity extends ActionBarActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlacementFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public static class PlacementFragment extends Fragment {

        private ArrayList<String> imageArray;

        public PlacementFragment(){
            setHasOptionsMenu(true);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.grid_detail,container,false);
            Intent intent = getActivity().getIntent();
            if(intent!=null && intent.hasExtra(Intent.EXTRA_TEXT)){
                String imagePath;
                String baseurl = "http://image.tmdb.org/t/p/w500";
                ImageView imageView = null;
                imagePath = null
                        ;

                imageArray = intent.getStringArrayListExtra(Intent.EXTRA_TEXT);
                for(int i=0;i<imageArray.size();i++){

                    ((TextView) rootView.findViewById(R.id.movietitle)).setText(imageArray.get(0));
                    ((TextView) rootView.findViewById(R.id.overview)).setText(imageArray.get(1));
                    ((TextView) rootView.findViewById(R.id.releasedate)).setText(imageArray.get(2));
                    ((TextView) rootView.findViewById(R.id.vote)).setText(imageArray.get(3));
                    imagePath = imageArray.get(4);
                 }

                ImageView imageView1 = (ImageView) rootView.findViewById(R.id.imageview);
                Picasso.with(getContext()).load(baseurl.concat(imagePath)).into(imageView1);

            }
            return rootView;
        }
    }
}
