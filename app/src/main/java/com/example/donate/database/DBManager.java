package com.example.donate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.donate.fragments.Base;
import com.example.donate.models.Donation;

import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private SQLiteDatabase database;
    private DBDesigner dbHelper;

    public DBManager(Context context) {
        dbHelper = new DBDesigner(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        database.close();
    }

    public void add(Donation d) {
        ContentValues values = new ContentValues();
        values.put("amount", d.amount);
        values.put("method", d.paymenttype);

        database.insert("donations", null, values);
    }

    public List<Donation> getAll() {
        List<Donation> donations = new ArrayList<Donation>();
        Cursor cursor = database.rawQuery("SELECT * FROM donations", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Donation d = toDonation(cursor);
            donations.add(d);
            cursor.moveToNext();
        }
        cursor.close();
        return donations;
    }

    private Donation toDonation(Cursor cursor) {
        Donation pojo = new Donation();
        pojo._id = cursor.getString(0);
        pojo.amount = cursor.getInt(1);
        pojo.paymenttype = cursor.getString(2);
        return pojo;
    }

    public void setTotalDonated(Base base) {
        Cursor c = database.rawQuery("SELECT SUM(amount) FROM donations", null);
        c.moveToFirst();
        if (!c.isAfterLast())
            base.app.totalDonated = c.getInt(0);
    }

    public void reset() {
        database.delete("donations", null, null);
    }
}