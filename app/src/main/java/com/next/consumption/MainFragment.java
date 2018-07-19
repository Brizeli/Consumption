package com.next.consumption;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Nextaty on 17.04.2018.
 */

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();
    MainFragmentListener mainFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainFragmentListener = (MainFragmentListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        Log.d(TAG, "onCreate()");
        View frag = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            mainFragmentListener.displayConsumption();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mainFragmentListener.goToFragment(1);
        return super.onOptionsItemSelected(item);
    }

    public interface MainFragmentListener {

        void displayConsumption();

        void goToFragment(int pos);
    }
}
