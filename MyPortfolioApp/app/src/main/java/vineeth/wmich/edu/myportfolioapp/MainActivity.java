package vineeth.wmich.edu.myportfolioapp;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button spotify = (Button) findViewById(R.id.button1);
        Button scoresapp = (Button) findViewById(R.id.button2);
        Button libraryapp = (Button) findViewById(R.id.button3);
        Button builditbigger = (Button) findViewById(R.id.button4);
        Button baconreader = (Button) findViewById(R.id.button5);
        Button capstone = (Button) findViewById(R.id.button6);

            spotify.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "This button is going to launch my Spotify App!", Toast.LENGTH_LONG);
                    toast1.show();
                }
            });
            scoresapp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "This button is going to launch my Scores App!",Toast.LENGTH_LONG);
                    toast1.show();
                }
            });
            libraryapp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "This button is going to launch my Library App!",Toast.LENGTH_LONG);
                    toast1.show();
                }
            });
            builditbigger.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "This button is going to launch my Build it bigger App!",Toast.LENGTH_LONG);
                    toast1.show();
                }
            });
            baconreader.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "This button is going to launch my Bacon! ;-)",Toast.LENGTH_LONG);
                    toast1.show();
                }
            });
            capstone.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "This button is going to launch my Capstone App!",Toast.LENGTH_LONG);
                    toast1.show();
                }
            });

    }

}
