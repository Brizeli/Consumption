package com.next.consumption;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nextaty on 26.03.2018.
 */

class FuelRecordsListAdapter extends ArrayAdapter<FuelRecord> {
    private final DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
    private Context mContext;
    private final int mResource;

    public FuelRecordsListAdapter(@NonNull Context context, int resource, @NonNull List<FuelRecord> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);
        FuelRecord record = getItem(position);
        DecimalFormat decimalFormat = new DecimalFormat("###.##");
        if (!record.blank) {
            displayText(convertView, R.id.txtVw_id, String.valueOf(record.id));
            displayText(convertView, R.id.txtVw_location, record.location);
            int[] style = {};
            if (record.date != null)
                displayText(convertView, R.id.txtVw_date, df.format(new Date(record.date)));
            else {
                style = new int[]{1};
                displayText(convertView, R.id.txtVw_date, mContext.getString(R.string.total) + record.month, style);
            }
            displayText(convertView, R.id.txtVw_mileage, record.mileage.toString(), style);
            displayText(convertView, R.id.txtVw_amount, decimalFormat.format(record.amount), style);
            String consumption = record.consumption == null ? "" :
                    record.consumption == 0. ? "---" :
                            decimalFormat.format(record.consumption);
            displayText(convertView, R.id.txtVw_consumption, consumption);
            displayText(convertView, R.id.txtVw_cost, decimalFormat.format(record.cost), style);
        } else {
            displayText(convertView, R.id.txtVw_date, "", 2);
            ArrayList<View> outViews = new ArrayList<>();
            convertView.findViewsWithText(outViews, "divider", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
            for (int i = 0; i < outViews.size(); i++) outViews.get(i).setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    private void displayText(View view, int id, String text, int... style) {
        TextView tv = view.findViewById(id);
        if (style.length > 0) {
            if (style[0] == 1)
                tv.setTypeface(null, Typeface.BOLD);
            else if (style[0] == 2)
                tv.setTextSize(50f);
        }
        tv.setText(text);
    }
}
