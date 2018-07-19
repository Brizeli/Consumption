package com.next.consumption;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class HistoryFragment extends Fragment {
    private static final String TAG = HistoryFragment.class.getSimpleName();
    private HistoryFragmentListener historyFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        historyFragmentListener = (HistoryFragmentListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View frag = inflater.inflate(R.layout.fragment_history, container, false);
        setHasOptionsMenu(true);
        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            historyFragmentListener.displayListView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_history, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int itemId = item.getItemId();
            switch (itemId) {
                case R.id.action_backup:
                    return historyFragmentListener.backUpData();
                case R.id.action_restore:
                    return historyFragmentListener.restoreData();
                case R.id.action_clear:
                    return historyFragmentListener.clearAllData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    public interface HistoryFragmentListener {
        void displayListView();

        boolean backUpData();

        boolean restoreData();

        boolean clearAllData();
    }
}
