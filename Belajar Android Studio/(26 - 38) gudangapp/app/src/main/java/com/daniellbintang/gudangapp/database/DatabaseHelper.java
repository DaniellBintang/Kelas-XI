package com.daniellbintang.gudangapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.daniellbintang.gudangapp.model.Barang;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gudang.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_BARANG = "barang";
    private static final String COL_ID = "id";
    private static final String COL_NAMA = "nama";
    private static final String COL_STOK = "stok";
    private static final String COL_HARGA = "harga";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_BARANG + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NAMA + " TEXT,"
                + COL_STOK + " INTEGER,"
                + COL_HARGA + " REAL)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BARANG);
        onCreate(db);
    }

    // CRUD Methods
    public long tambahBarang(Barang barang) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAMA, barang.getNama());
        values.put(COL_STOK, barang.getStok());
        values.put(COL_HARGA, barang.getHarga());
        long id = db.insert(TABLE_BARANG, null, values);
        db.close();
        return id;
    }

    public List<Barang> getSemuaBarang() {
        List<Barang> listBarang = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BARANG, null);
        if (cursor.moveToFirst()) {
            do {
                Barang barang = new Barang(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_STOK)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_HARGA))
                );
                listBarang.add(barang);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listBarang;
    }

    public int updateBarang(Barang barang) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAMA, barang.getNama());
        values.put(COL_STOK, barang.getStok());
        values.put(COL_HARGA, barang.getHarga());
        int rows = db.update(TABLE_BARANG, values, COL_ID + "=?",
                new String[]{String.valueOf(barang.getId())});
        db.close();
        return rows;
    }

    public void hapusBarang(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BARANG, COL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }
}