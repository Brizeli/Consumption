package com.next.consumption;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewFuelingDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dnfView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_fueling_layout, null);
        return new AlertDialog.Builder(getContext())
                .setView(dnfView)
                .setTitle(R.string.new_fuelling)
                .setPositiveButton(R.string.save, null)
//        (dialog, which) -> {
//                    if (saveToDb()) {
//                        Toast.makeText(this, getString(R.string.data_saved), Toast.LENGTH_SHORT).show();
//                        displayConsumption();
//                        displayListView();
//                    }
//                    mSharedPref.edit()
//                            .putString(PRICE, getaText(mDialogNewFueling, R.id.edit_price))
//                            .apply();
//                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}
