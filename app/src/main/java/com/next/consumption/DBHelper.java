package com.next.consumption;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Nextaty on 25.03.2018.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "consumption.db";

    public static class HistoryEntry implements BaseColumns {
        public static final String T_NAME = "history";
        public static final String COL_COST = "cost";
        public static final String COL_DATE = "date";
        public static final String COL_MILEAGE = "mileage";
        public static final String COL_AMOUNT = "amount";
        public static final String COL_CONSUMPTION = "consumption";
        public static final String COL_LOCATION = "location";
        public static final int IDX_ID = 0;
        public static final int IDX_DATE = 1;
        public static final int IDX_MILEAGE = 2;
        public static final int IDX_AMOUNT = 3;
        public static final int IDX_CONSUMPTION = 4;
        public static final int IDX_LOCATION = 5;
        public static final int IDX_COST = 6;
    }

    private static final File EXTERNAL_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "ConsumptionMeter");
    private static final File EXPORT_FILE = new File(EXTERNAL_DIRECTORY, DBHelper.DB_NAME);
    private static final String TAG = DBHelper.class.getSimpleName();

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + HistoryEntry.T_NAME + "(" +
                HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                HistoryEntry.COL_DATE + " INTEGER NOT NULL," +
                HistoryEntry.COL_MILEAGE + " INTEGER NOT NULL," +
                HistoryEntry.COL_AMOUNT + " REAL NOT NULL," +
                HistoryEntry.COL_CONSUMPTION + " REAL NOT NULL," +
                HistoryEntry.COL_LOCATION + " TEXT DEFAULT '', " +
                HistoryEntry.COL_COST + " REAL DEFAULT 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
//        db.execSQL("DROP TABLE IF EXISTS " + T_NAME);
//        onCreate(db);
//        db.execSQL("ALTER TABLE " + T_NAME + " ADD COLUMN " + COL_COST + " REAL DEFAULT 0;");
    }

    public boolean save(FuelRecord record) {
        SQLiteDatabase db = getWritableDatabase();
        int count = db.query(HistoryEntry.T_NAME, null, null, null, null, null, null).getCount();
        long res = db.insert(HistoryEntry.T_NAME, null, getContentValueFromRecord(count, record));
        return res != -1;
    }

    private void saveAll(List<FuelRecord> records) {
        SQLiteDatabase db = getWritableDatabase();
        int count = db.query(HistoryEntry.T_NAME, null, null, null, null, null, null).getCount();
        db.beginTransaction();
        try {
            for (FuelRecord r : records)
                db.insert(HistoryEntry.T_NAME, null, getContentValueFromRecord(count++, r));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<FuelRecord> getAllRecords() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<FuelRecord> records = new ArrayList<>();
        try (Cursor c = db.query(HistoryEntry.T_NAME, null, null, null, null, null, null)) {
            while (c.moveToNext()) {
                records.add(getFuelRecordAtCursor(c));
            }
        } catch (Exception e) {
            Log.d(this.toString(), e.getMessage());
        }
        return records;
    }

    public void clear() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + HistoryEntry.T_NAME);
        onCreate(db);
    }

    public int delete(Long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(HistoryEntry.T_NAME, HistoryEntry._ID + "=" + id, null);
    }

    public FuelRecord getById(Long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(HistoryEntry.T_NAME, null, HistoryEntry._ID + " = " + id, null, null, null, null)) {
            if (c.moveToFirst())
                return getFuelRecordAtCursor(c);
        } catch (Exception e) {
            Log.d(this.toString(), e.getMessage());
        }
        return null;
    }

    public double getAvgConsumption() {
        Cursor cursor = getReadableDatabase().query(HistoryEntry.T_NAME, null, null, null, null, null, null);
        if (cursor.getCount() < 2) {
            cursor.close();
        } else {
            try (Cursor c = getReadableDatabase().rawQuery(
                    "SELECT avg(" + HistoryEntry.COL_CONSUMPTION + ") FROM " + HistoryEntry.T_NAME +
                            " WHERE " + HistoryEntry.COL_CONSUMPTION + " > 0", null)) {
                c.moveToFirst();
                return c.getDouble(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public FuelRecord getLastRecord() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(HistoryEntry.T_NAME, null, null, null, null, null, null)) {
            if (c.moveToLast())
                return getFuelRecordAtCursor(c);
        } catch (Exception e) {
            Log.d(this.toString(), e.getMessage());
        }
        return null;
    }

    @NonNull
    private FuelRecord getFuelRecordAtCursor(Cursor c) {
        return new FuelRecord(
                c.getLong(HistoryEntry.IDX_ID),
                c.getLong(HistoryEntry.IDX_DATE),
                c.getLong(HistoryEntry.IDX_MILEAGE),
                c.getFloat(HistoryEntry.IDX_AMOUNT),
                c.getFloat(HistoryEntry.IDX_CONSUMPTION),
                c.getString(HistoryEntry.IDX_LOCATION),
                c.getFloat(HistoryEntry.IDX_COST));
    }

    private ContentValues getContentValueFromRecord(int count, FuelRecord record) {
        ContentValues values = new ContentValues();
        values.put(HistoryEntry.COL_DATE, record.date);
        values.put(HistoryEntry.COL_MILEAGE, record.mileage);
        values.put(HistoryEntry.COL_AMOUNT, record.amount);
        values.put(HistoryEntry.COL_CONSUMPTION, count == 0 ? 0 : record.consumption);
        values.put(HistoryEntry.COL_LOCATION, record.location);
        values.put(HistoryEntry.COL_COST, record.cost);
        return values;
    }

    public boolean backup() {
        if (!isSdPresent()) return false;
        if (!EXTERNAL_DIRECTORY.exists()) EXTERNAL_DIRECTORY.mkdirs();
        try (PrintWriter out = new PrintWriter(EXPORT_FILE)) {
            String json = new Gson().toJson(getAllRecords(), new TypeToken<List<FuelRecord>>() {
            }.getType());
            out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean restore() {
        if (!isSdPresent()) return false;
        if (!EXPORT_FILE.exists()) {
            Log.d(TAG, "File does not exist");
            return false;
        }
        try (BufferedReader in = new BufferedReader(new FileReader(EXPORT_FILE))) {
            List<FuelRecord> records = new Gson().fromJson(in, new TypeToken<List<FuelRecord>>() {
            }.getType());
            saveAll(records);
        } catch (IOException | ClassCastException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean isSdPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

}
