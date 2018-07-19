package com.next.consumption;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

public class Util {
    public static void displayText(Activity a, int id, String text) {
        TextView tv = a.findViewById(id);
        if (tv != null)
            tv.setText(text);
    }

    public static void displayText(View v, int id, String text) {
        TextView tv = v.findViewById(id);
        if (tv != null)
            tv.setText(text);
    }

    public static Long getaLong(View view, int id) {
        String s = getaText(view, id);
        return s.isEmpty() ? 0 : Long.valueOf(s);
    }

    public static Double getaDouble(View view, int id) {
        String s = getaText(view, id);
        return s.isEmpty() ? 0 : Double.valueOf(s);
    }

    public static String getaText(View view, int txtVw_date) {
        if (view == null) return "";
        return view.<TextView>findViewById(txtVw_date).getText().toString();
    }
}
