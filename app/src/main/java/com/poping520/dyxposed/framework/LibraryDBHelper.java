package com.poping520.dyxposed.framework;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/5 17:32
 */
public class LibraryDBHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    private static final LibraryDBHelper INSTANCE = new LibraryDBHelper();

    public static LibraryDBHelper getInstance() {
        return INSTANCE;
    }

    private LibraryDBHelper() {
        super(DyXContext.getApplicationContext(), DyXContext.APP_DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "create table library(id INTEGER primary key autoincrement, name TEXT, path TEXT, enable INT2)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
