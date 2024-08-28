package com.drawingapps.tracedrawing.drawingsketch.drawingapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.ardrawing.tracedrawing.drawingsketch.drawingapps.R;

import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new Fragment1()).commit();
    }

    public void showFragment(Fragment fragment, String tag) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment1 != null && fragment1.isAdded()) {
                ft.show(fragment1);
            } else { // fragment needs to be added to frame container
                ft.add(R.id.frameLayout, fragment, tag);
            }
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.size() > 0) {
                for (Fragment frag : fragments) {
                    if (frag != fragment1) {
                        if (frag.isAdded())
                            ft.hide(frag);
                    }
                }
            }
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}