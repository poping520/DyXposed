package com.poping520.dyxposed.framework;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.util.JSON;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/15 13:53
 */
public class ModuleDBHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    private static final ModuleDBHelper INSTANCE = new ModuleDBHelper();

    public static ModuleDBHelper getInstance() {
        return INSTANCE;
    }

    private ModuleDBHelper() {
        super(DyXContext.getApplicationContext(), DyXContext.APP_DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "create table module(id VARCHAR primary key, author VARCHAR, name TEXT, `desc` TEXT, version VARCHAR, " +
                        "target TEXT, entryClass VARCHAR, entryMethod VARCHAR, enable INT2, dex BLOB)";
        db.execSQL(sql);
    }

    /**
     * 向数据库插入一个模块
     *
     * @param module  模块对象
     * @param dexData 模块 dex(.jar) 字节数组
     */
    public void insert(Module module, byte[] dexData) {
        final SQLiteDatabase db = getWritableDatabase();
        String sql = "insert into module values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] objs = new Object[]{
                module.id, module.author,
                JSON.stringMap2json(module.name), JSON.stringMap2json(module.desc),
                module.version, JSON.stringArray2json(module.target),
                module.entryClass, module.entryMethod,
                module.enable, dexData
        };
        db.execSQL(sql, objs);
    }

    public void delete(String moduleId) {
        final SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from module where id = ?", new String[]{moduleId});
    }

    /**
     * 更新数据库中一个模块
     *
     * @param module  模块对象
     * @param dexData 模块 dex(.jar) 字节数组
     */
    public void update(Module module, byte[] dexData) {
        delete(module.id);
        insert(module, dexData);
    }

    /**
     * 更新启用状态
     *
     * @param moduleId 模块唯一 id
     * @param enable   是否启用
     */
    public void update(String moduleId, boolean enable) {
        final SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update module set enable = ? where id = ?",
                new Object[]{enable, moduleId});
    }

    /**
     * 从数据库查询模块对象
     *
     * @param moduleId 模块唯一 id
     */
    @Nullable
    public Module query(String moduleId) {
        final SQLiteDatabase db = getReadableDatabase();
        Module module = null;
        final Cursor cursor = db.rawQuery("select * from module where id = ?", new String[]{moduleId});
        if (cursor != null) {
            // id 唯一性
            if (cursor.moveToFirst()) {
                module = getModuleByCursor(cursor);
            }
            cursor.close();
        }
        return module;
    }

    /**
     * 查询所有模块
     */
    @NonNull
    public List<Module> queryAll() {
        final SQLiteDatabase db = getWritableDatabase();
        List<Module> list = new ArrayList<>();
        final Cursor cursor = db.rawQuery("select * from module", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(getModuleByCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    private Module getModuleByCursor(Cursor cursor) {
        Module module = new Module();
        final Class<Module> clz = Module.class;

        final String[] columnNames = cursor.getColumnNames();
        for (String columnName : columnNames) {
            //排除 dex 列
            if ("dex".equals(columnName)) continue;

            final int index = cursor.getColumnIndex(columnName);
            try {
                final Field field = clz.getField(columnName);
                final Class<?> fieldType = field.getType();
                if (fieldType.equals(String.class))
                    field.set(module, cursor.getString(index));
                else if (fieldType.equals(Map.class))
                    field.set(module, JSON.json2StringMap(cursor.getString(index)));
                else if (fieldType.equals(String[].class))
                    field.set(module, JSON.json2StringArray(cursor.getString(index)));
                else if (fieldType.equals(boolean.class))
                    field.set(module, cursor.getInt(index) != 0);
                else
                    throw new IllegalStateException("Module class has been changed");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return module;
    }

    /**
     * 从数据库查询模块 dex(.jar)
     *
     * @param moduleId 模块唯一 id
     * @return 模块 dex(.jar)字节数组
     */
    @Nullable
    public byte[] queryDexBytes(String moduleId) {
        final SQLiteDatabase db = getReadableDatabase();

        byte[] bytes = null;
        final Cursor cursor = db.rawQuery(
                "select dex from module where id = ?", new String[]{moduleId});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                bytes = cursor.getBlob(0);
            }
            cursor.close();
        }
        return bytes;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
