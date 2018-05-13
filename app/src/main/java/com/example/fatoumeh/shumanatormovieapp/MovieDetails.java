package com.example.fatoumeh.shumanatormovieapp;

import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetails extends AppCompatActivity {


    private TextView tvTitle;
    private TextView tvOverview;
    private TextView tvVoteAvg;
    private TextView tvReleaseDate;
    private ImageView imgPoster;
    private Button btShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Intent intent = getIntent();

        String titleStr = getString(R.string.original_title);
        String overviewStr = getString(R.string.overview);
        String voteStr = getString(R.string.vote_average);
        String dateStr = getString(R.string.release_date);
        String posterStr = getString(R.string.poster_path);

        String title, overview="";

        if (intent.hasExtra(titleStr)) {
            tvTitle=findViewById(R.id.tv_title);
            title=intent.getStringExtra(titleStr);
            tvTitle.setText(title);
        }

        if (intent.hasExtra(overviewStr)) {
            tvOverview=findViewById(R.id.tv_overview);
            overview=intent.getStringExtra(overviewStr);
            tvOverview.setText(overview);
        }

        if (intent.hasExtra(posterStr)) {
            imgPoster=findViewById(R.id.img_movie_poster);
            String path=intent.getStringExtra(posterStr);
            Picasso.with(this)
                    .load(path)
                    .error(R.drawable.ic_launcher_background)
                    .into(imgPoster);
        }

        if (intent.hasExtra(voteStr)) {
            tvVoteAvg=findViewById(R.id.tv_rating);
            tvVoteAvg.setText(String.valueOf(intent.getDoubleExtra(voteStr,0)));
        }

        if (intent.hasExtra(dateStr)) {
            tvReleaseDate=findViewById(R.id.tv_release_date);
            String date=intent.getStringExtra(dateStr);
            String newDate=getNewDate(date);
            tvReleaseDate.setText(newDate);
        }

        btShare=findViewById(R.id.bt_share);
        btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mimeType="text/plain";
                String shareText = ((TextView)findViewById(R.id.tv_title)).getText()+
                        "\n\n" + ((TextView)findViewById(R.id.tv_overview)).getText();
                ShareCompat.IntentBuilder.from(MovieDetails.this)
                        .setChooserTitle(getString(R.string.share_chooser))
                        .setType(mimeType)
                        .setText(shareText)
                        .startChooser();
            }
        });

    }

    private String getNewDate(String date) {
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat newDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date dateObj = null;
        try {
            dateObj = currentDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String newDateTime = newDateFormat.format(dateObj);
        return newDateTime;
    }
}
