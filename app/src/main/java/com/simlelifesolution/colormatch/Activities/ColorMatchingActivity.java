package com.simlelifesolution.colormatch.Activities;

import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.simlelifesolution.colormatch.Fragment.FragmentComplementaryColor;
import com.simlelifesolution.colormatch.Fragment.FragmentSimilarColor;
import com.simlelifesolution.colormatch.Helpers.ViewPagerAdapter;
import com.simlelifesolution.colormatch.R;

public class ColorMatchingActivity extends AppCompatActivity
{
//region...... variables declaration
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar mToolbar;

    String strIntentrecvdColor;
    ImageView mImgVwCover ;
//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_matching);

        mImgVwCover = (ImageView)findViewById(R.id.colormatching_cover);

        initialize_tabs();
    }


    private void initialize_tabs()
    {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle("Similar Colors");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //strIntentrecvdColor = "#F46B50";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {  strIntentrecvdColor = extras.getString("intnt_colorCode");     }

        mImgVwCover.setBackgroundColor(Color.parseColor(strIntentrecvdColor));
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter vwPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        vwPagerAdapter.addFragment(new FragmentSimilarColor(), "Similar");
        vwPagerAdapter.addFragment(new FragmentComplementaryColor(), "Complementary");
        // adapter.addFragment(new TvshowFragment(), "TV Shows");
        viewPager.setAdapter(vwPagerAdapter);
    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }



}
