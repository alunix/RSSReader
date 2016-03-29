package com.igordotsenko.dotsenkorssreader;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.igordotsenko.dotsenkorssreader.entities.Item;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ItemContentActivity extends AppCompatActivity {
    TextView title;
    ImageView thumbnail;
    TextView subtitle;
    TextView pubdate;
    TextView content;
    ImageButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_content);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //imageLoader initialization for thumbnail setting
        ImageLoader imageLoader = ImageLoader.getInstance();
        Intent intent = getIntent();

        //Views initialization
        title = (TextView) findViewById(R.id.item_content_title);
        thumbnail = (ImageView) findViewById(R.id.item_content_image);
        subtitle = (TextView) findViewById(R.id.item_content_subtitle);
        pubdate = (TextView) findViewById(R.id.item_content_pubdate);
        content = (TextView) findViewById(R.id.item_content_content);
        backButton = (ImageButton) findViewById(R.id.item_content_back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Setting views' content
        title.setText(intent.getStringExtra(Item.TITLE));
        subtitle.setText(intent.getStringExtra(Item.SUBTITLE));
        pubdate.setText(intent.getStringExtra(Item.PUBDATE));
        content.setText(intent.getStringExtra(Item.DESCRIPTION));

        //Setting thumbnail
        String thumbnailUrl = intent.getStringExtra(Item.THUMBNAIL);
        if ( thumbnailUrl != null) {
            imageLoader.displayImage(thumbnailUrl, thumbnail);
        }
    }
}
