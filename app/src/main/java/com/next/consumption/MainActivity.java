package com.next.consumption;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.next.consumption.Util.displayText;
import static com.next.consumption.Util.getaDouble;
import static com.next.consumption.Util.getaLong;
import static com.next.consumption.Util.getaText;
import static java.lang.Long.parseLong;

public class MainActivity extends AppCompatActivity
        implements MainFragment.MainFragmentListener, HistoryFragment.HistoryFragmentListener {

    private static final String TANK_VOLUME = "tnkVol";
    private static final String PRICE = "prc";
    private static final String LOCATION = "loc";
    public static final float FAB_OFFSET = 1.2f;
    private FragmentPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private DBHelper myDb;
    private DecimalFormat mDecimalFormat = new DecimalFormat("###.##");
    private boolean mLitersOn100km = true;
    private boolean isFabOpen;
    private View fabMain;
    private View fabPredict;
    private View bgFab;
    private FuelRecord mLastRecord;
    private Double mAvgConsumption;
    private SharedPreferences mSharedPref;
    private View mDialogNewFueling;
    private String mLocationString;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSharedPref = getPreferences(MODE_PRIVATE);
        mSectionsPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new MainFragment();
                    case 1:
                        return new HistoryFragment();
                }
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        if (!checkAndRequestPermissions(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}))
            updateLocation();
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    toolbar.setTitle(R.string.history);
                    toolbar.setNavigationIcon(R.drawable.ic_back_button);
                    toolbar.setNavigationOnClickListener(v -> goToFragment(0));
                } else {
                    toolbar.setTitle(R.string.consumption);
                    toolbar.setNavigationIcon(null);
                }
            }
        });
        fabMain = findViewById(R.id.fab_menu_newfuel);
        fabPredict = findViewById(R.id.fab_predict);
        bgFab = findViewById(R.id.bg_fab);
        fabMain.setOnClickListener(v -> {
            if (isFabOpen) {
                closeFabMenu();
                newFueling(v);
//                nf(v);
            } else openFabMenu();
        });
        fabPredict.setOnClickListener(v -> {
            closeFabMenu();
            predictMileage(v);
        });
        bgFab.setOnClickListener(v -> closeFabMenu());
        myDb = new DBHelper(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocationString = location.getLatitude() + "," + location.getLongitude();
                mSharedPref.edit().putString(LOCATION, mLocationString).apply();
//                Fragment dnf = getSupportFragmentManager().findFragmentByTag("dnf");
//                if (dnf != null)
//                    displayText(dnf.getView(), R.id.txtVw_location, mLocationString);
                if (mDialogNewFueling != null)
                    displayText(mDialogNewFueling, R.id.txtVw_location, mLocationString);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    private boolean checkAndRequestPermissions(String[] perms) {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean granted = true;
            for (int i = 0; i < perms.length && granted; i++) {
                if (checkSelfPermission(perms[i]) != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    requestPermissions(perms, 100);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                updateLocation();
    }

    @SuppressLint("MissingPermission")
    private void updateLocation() {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, mLocationListener);
    }

    private void openFabMenu() {
        isFabOpen = true;
        fabPredict.animate().translationYBy(-getResources().getDimension(R.dimen.fab_size) * FAB_OFFSET).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                fabPredict.setVisibility(View.VISIBLE);
                bgFab.setVisibility(View.VISIBLE);
            }
        });
        bgFab.animate().alpha(1f);
        bgFab.setClickable(true);
    }

    private void closeFabMenu() {
        isFabOpen = false;
        fabPredict.animate().translationYBy(getResources().getDimension(R.dimen.fab_size) * FAB_OFFSET).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabPredict.setVisibility(View.GONE);
                bgFab.setVisibility(View.GONE);
            }
        });
        bgFab.animate().alpha(0);
        bgFab.setClickable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(mLocationListener);
    }

    private void nf(View view) {
        NewFuelingDialog dnf = new NewFuelingDialog();
        getSupportFragmentManager().beginTransaction().add(dnf, "dnf").commit();
//        dnf.show(getSupportFragmentManager(),null);
//        fabMain.setVisibility(View.GONE);
    }

    private void newFueling(View view) {
        LayoutInflater inflater = getLayoutInflater();
        mDialogNewFueling = inflater.inflate(R.layout.dialog_new_fueling_layout, null);
        Long lastMileage = mLastRecord == null ? 0 : mLastRecord.mileage;
        if (mLocationString != null)
            displayText(mDialogNewFueling, R.id.txtVw_location, mLocationString);
        displayText(mDialogNewFueling, R.id.edit_prevMileage, lastMileage.toString());
        displayText(mDialogNewFueling, R.id.edit_currMileage, lastMileage.toString());
        displayText(mDialogNewFueling, R.id.edit_price, mSharedPref.getString(PRICE, ""));
        mDialogNewFueling.findViewById(R.id.edit_currMileage).requestFocus();
        Button btnCount = mDialogNewFueling.findViewById(R.id.btn_count);
        btnCount.setOnClickListener(v -> {
            //
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            //
            displayNewFuelingConsumption();
        });
        View resultCons = mDialogNewFueling.findViewById(R.id.txtVw_result);
        resultCons.setOnClickListener(v -> changeUnits(null));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(mDialogNewFueling)
                .setTitle(R.string.new_fuelling)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    if (saveToDb()) {
                        Toast.makeText(this, getString(R.string.data_saved), Toast.LENGTH_SHORT).show();
                        displayConsumption();
                        displayListView();
                    }
                    mSharedPref.edit()
                            .putString(PRICE, getaText(mDialogNewFueling, R.id.edit_price))
                            .apply();
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    private void displayNewFuelingConsumption() {
        Long currMileage = getaLong(mDialogNewFueling, R.id.edit_currMileage);
        Long prevMileage = getaLong(mDialogNewFueling, R.id.edit_prevMileage);
        Double amount = getaDouble(mDialogNewFueling, R.id.edit_amount);
        Double result = 0.;
        if ((prevMileage != 0 || currMileage != 0) && amount != 0) {
            result = mLitersOn100km ?
                    amount / (currMileage - prevMileage) * 100 :
                    (currMileage - prevMileage) / amount;
        }
        displayText(mDialogNewFueling, R.id.txtVw_result, mDecimalFormat.format(result));
        displayText(mDialogNewFueling, R.id.txtVw_units, getString(mLitersOn100km ? R.string.l_100km : R.string.km_1l));
    }

    private boolean saveToDb() {
        long date = new Date().getTime();
        float amount = getaDouble(mDialogNewFueling, R.id.edit_amount).floatValue();
        Long mileage = getaLong(mDialogNewFueling, R.id.edit_currMileage);
        float consumption = getaDouble(mDialogNewFueling, R.id.txtVw_result).floatValue();
        float price = getaDouble(mDialogNewFueling, R.id.edit_price).floatValue();
        return myDb.save(new FuelRecord(date, mileage, amount, consumption, mLocationString, price * amount));
    }

    private void predictMileage(View view) {
        if (mAvgConsumption == -1) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.msg_not_enough_records)
                    .setPositiveButton(R.string.close, null)
                    .create().show();
            return;
        }
        View predDialog = getLayoutInflater().inflate(R.layout.dialog_predict_layout, null);
        TextView editTankVolume = predDialog.findViewById(R.id.edit_tank_volume);
        Button btnCount = predDialog.findViewById(R.id.btn_count_predict);
        TextView editCurrMileage = predDialog.findViewById(R.id.edit_currMileage_predict);
        TextView txtVwResult = predDialog.findViewById(R.id.txtVw_result_predict);
        editTankVolume.setText(mSharedPref.getString(TANK_VOLUME, ""));
        Long lastMileage = mLastRecord == null ? 0 : mLastRecord.mileage;
        editCurrMileage.setText(lastMileage.toString());
        double avgCons = mAvgConsumption;
        btnCount.setOnClickListener(v -> {
            //
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            //
            String currMilString = editCurrMileage.getText().toString();
            long currMileage = currMilString.isEmpty() ? 0 : parseLong(currMilString);
            if (currMileage < lastMileage) {
                txtVwResult.setText("∞");
                return;
            }
            String tankVolString = editTankVolume.getText().toString();
            int tankVol = tankVolString.isEmpty() ? 0 : Integer.parseInt(tankVolString);
            double litersLeft = (currMileage - lastMileage) / 100.D * avgCons;
            double leftKm = (tankVol - litersLeft) / avgCons * 100;
            txtVwResult.setText(String.valueOf(Math.round(leftKm)));
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(predDialog)
                .setTitle(R.string.btn_mileage_predict)
                .setPositiveButton(R.string.close, (dialog, which) -> {
                    String tankVol = editTankVolume.getText().toString();
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.putString(TANK_VOLUME, tankVol).apply();
                })
                .create()
                .show();
        btnCount.callOnClick();
        editCurrMileage.requestFocus();
    }

    public void displayConsumption() {
        mLastRecord = myDb.getLastRecord();
        mAvgConsumption = myDb.getAvgConsumption();
        if (mLastRecord != null) {
            Float consumption = mLastRecord.consumption;
            Double avgConsumption = mAvgConsumption;
            if (!mLitersOn100km) {
                if (consumption > 0)
                    consumption = 100 / consumption;
                if (avgConsumption > 0)
                    avgConsumption = 100 / avgConsumption;
            }
            displayText(this, R.id.txtVw_current_consumption, consumption == 0 ? "---" : mDecimalFormat.format(consumption));
            displayText(this, R.id.txtVw_avg_consumption, avgConsumption == -1 ? "---" : mDecimalFormat.format(avgConsumption));
        } else {
            displayText(this, R.id.txtVw_current_consumption, "---");
            displayText(this, R.id.txtVw_avg_consumption, "---");
        }
        String units = getString(mLitersOn100km ? R.string.l_100km : R.string.km_1l);
        displayText(this, R.id.txtVw_units, units);
        displayText(this, R.id.txtVw_units_avg, units);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.ctx_menu_history, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        return super.onContextItemSelected(item);
    }

    public void displayListView() {
        TextView txtVw_units = findViewById(R.id.txtVw_units_history);
        txtVw_units.setText(mLitersOn100km ? R.string.l_100km : R.string.km_1l);
        final ListView listView = findViewById(R.id.vw_history);
        registerForContextMenu(listView);
        List<FuelRecord> fuelRecords = getRecordsWithTotals(myDb.getAllRecords());
        if (!mLitersOn100km)
            for (int i = 1; i < fuelRecords.size(); i++) {
                if (fuelRecords.get(i).consumption != null)
                    fuelRecords.get(i).consumption = 100 / fuelRecords.get(i).consumption;
            }
        FuelRecordsListAdapter adapter = new FuelRecordsListAdapter(this, R.layout.record_adapter_layout, fuelRecords);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String date = getaText(view, R.id.txtVw_date);
            String mileage = getaText(view, R.id.txtVw_mileage);
            String amount = getaText(view, R.id.txtVw_amount);
            String text = getString(R.string.date) + ": " + date + '\n' +
                    getString(R.string.mileage) + ": " + mileage + '\n' +
                    getString(R.string.amount) + ": " + amount;
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            final long rowId = getaLong(view, R.id.txtVw_id);
            String date = getaText(view, R.id.txtVw_date);
            String mileage = getaText(view, R.id.txtVw_mileage);
            String amount = getaText(view, R.id.txtVw_amount);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_remove_dialog)
                    .setMessage(getString(R.string.date) + ": " + date + '\n' +
                            getString(R.string.mileage) + ": " + mileage + '\n' +
                            getString(R.string.amount) + ": " + amount)
                    .setPositiveButton(R.string.delete_confirm, (dialog, which) -> {
                        if (myDb.delete(rowId) > 0) {
                            Toast.makeText(this, getString(R.string.record_deleted), Toast.LENGTH_SHORT).show();
                            fuelRecords.remove(position);
                            adapter.notifyDataSetChanged();
                            listView.invalidateViews();
                            displayConsumption();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            builder.create().show();
            return true;
        });
    }

    private List<FuelRecord> getRecordsWithTotals(List<FuelRecord> recs) {
        ArrayList<FuelRecord> result = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        FuelRecord record = recs.get(0);
        result.add(record);
        c.setTimeInMillis(record.date);
        int month = c.get(Calendar.MONTH);
        long prevMileage = record.mileage;
        long mileage = 0;
        float amount = record.amount;
        float cost = record.cost;
        for (int i = 1; i < recs.size(); i++) {
            record = recs.get(i);
            c.setTimeInMillis(record.date);
            int month1 = c.get(Calendar.MONTH);
            if (month == month1) {
                mileage += record.mileage - prevMileage;
                amount += record.amount;
                cost += record.cost;
                prevMileage = record.mileage;
                result.add(record);
            } else {
                result.add(new FuelRecord(new DateFormatSymbols(Locale.getDefault()).getShortMonths()[month], mileage, amount, cost));
                month = month1;
                mileage = 0;
                amount = 0f;
                cost = 0f;
                i--;
            }
        }
        result.add(new FuelRecord());
        return result;
    }

    public void changeUnits(View view) {
        mLitersOn100km = !mLitersOn100km;
        displayConsumption();
        displayListView();
        if (mDialogNewFueling != null) displayNewFuelingConsumption();
    }

    @Override
    public void goToFragment(int pos) {
        mViewPager.setCurrentItem(pos);
    }

    public boolean clearAllData() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.clear_data_confirm)
                    .setPositiveButton(R.string.delete_confirm, (dialog, which) -> {
                        myDb.clear();
                        displayListView();
                        displayConsumption();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean backUpData() {
        if (myDb.backup()) {
            Toast.makeText(this, getString(R.string.backup_done), Toast.LENGTH_SHORT).show();
            return true;
        }
        Toast.makeText(this, "ERROR!!!", Toast.LENGTH_SHORT).show();
        return false;
    }

    public boolean restoreData() {
        if (myDb.restore()) {
            displayListView();
            displayConsumption();
            return true;
        }
        return false;
    }

    public void clearText(View view) {
        TextView editText = (TextView) ((FrameLayout) view.getParent()).getChildAt(0);
        editText.setText("");
    }

}
