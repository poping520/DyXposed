package com.poping520.dyxposed.framework;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.poping520.dyxposed.model.Library;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.performance.Releasenable;
import com.poping520.dyxposed.util.JSON;
import com.poping520.dyxposed.util.Objects;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dyxposed 数据库管理类
 * <p>
 * Created by WangKZ on 18/12/05.
 *
 * @author poping520
 * @version 1.0.0
 */
public class DyXDBHelper extends SQLiteOpenHelper implements Releasenable {

    private static final int VERSION = 1;

    private static final DyXDBHelper INSTANCE = new DyXDBHelper();

    public static DyXDBHelper getInstance() {
        return INSTANCE;
    }

    private DyXDBHelper() {
        super(DyXContext.getApplicationContext(), DyXContext.APP_DB_NAME, null, VERSION);
    }

    // 创建数据库
    @Override
    public void onCreate(SQLiteDatabase db) {
        // module 表
        String sql =
                "create table if not exists module(id VARCHAR primary key, author VARCHAR, name TEXT, `desc` TEXT, version VARCHAR, " +
                        "target TEXT, entryClass VARCHAR, entryMethod VARCHAR, enable INT2, dex BLOB)";
        db.execSQL(sql);

        // library 表
        // | id | scope | path | enable | assets |
        sql = "create table if not exists library(id INTEGER primary key autoincrement, scope VARCHAR, path TEXT, enable INT2, assets VARCHAR)";
        db.execSQL(sql);
        // 向表增加默认的 lib
        final LibraryAssets[] assets = LibraryAssets.values();
        for (LibraryAssets asset : assets) {
            insertLib(db, asset.generateLibrary());
        }
    }

    /**
     * 向 module 表中增加一个 {@link Module}
     *
     * @param module  模块对象
     * @param dexData 模块 dex(.jar) 字节数组
     */
    public void insertModule(Module module, byte[] dexData) {
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

    /**
     * 从 module 表中删除一个模块
     *
     * @param moduleId 模块唯一 id
     */
    public void deleteModule(String moduleId) {
        final SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from module where id = ?", new String[]{moduleId});
    }

    /**
     * 更新数据库中一个模块
     *
     * @param module  模块对象
     * @param dexData 模块 dex(.jar) 字节数组
     */
    public void updateModule(Module module, byte[] dexData) {
        deleteModule(module.id);
        insertModule(module, dexData);
    }

    /**
     * 更新启用状态
     *
     * @param moduleId 模块唯一 id
     * @param enable   是否启用
     */
    public void updateModule(String moduleId, boolean enable) {
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
    public Module queryModule(String moduleId) {
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
    public List<Module> queryAllModule() {
        final SQLiteDatabase db = getReadableDatabase();
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
    public byte[] queryModuleDexBytes(String moduleId) {
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

    public void insertLib(Library lib) {
        insertLib(getWritableDatabase(), lib);
    }

    private void insertLib(SQLiteDatabase db, Library lib) {
        String sql = "insert into library(scope, path, enable, assets) values(?, ?, ?, ?)";
        String assets = "";
        if (lib.assets != null) {
            assets = lib.assets.name();
        }
        db.execSQL(sql, new Object[]{lib.scope.name(), lib.path, lib.enable, assets});
    }

    @NonNull
    public List<Library> queryAllLib() {
        return queryLibs(Library.Scope.values());
    }

    public List<File> queryEnableLibFiles(Library.Scope... scopes) {
        final List<Library> libs = queryLibs(scopes);
        List<File> list = new ArrayList<>();
        for (Library lib : libs) {
            if (!lib.enable)
                continue;

            if (lib.isDyXLibrary()) {
                lib.assets.release();
            }
            list.add(new File(lib.path));
        }
        return list;
    }

    /**
     * @param scopes
     * @return
     */
    public List<Library> queryLibs(Library.Scope... scopes) {
        final SQLiteDatabase db = getReadableDatabase();
        List<Library> list = new ArrayList<>();

        if (Objects.isEmptyArray(scopes)) {
            return list;
        }

        // 查询全部
        String sql = "select * from library";

        // 查询部分
        if (scopes.length < Library.Scope.values().length) {
            StringBuilder sb = new StringBuilder();
            sb.append(sql).append(" where");
            for (int i = 0; i < scopes.length; i++) {
                sb.append(" scope like '").append(scopes[i].name()).append("'");
                if (i < scopes.length - 1) {
                    sb.append(" or");
                }
            }
            sql = sb.toString();
        }

        final Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // |   id   |  scope |  path  | enable | assets |
                final Library library = new Library(
                        Library.Scope.valueOf(cursor.getString(1)),
                        cursor.getString(2),
                        cursor.getInt(3) == 1
                );
                final String assets = cursor.getString(4);
                if (!TextUtils.isEmpty(assets)) {
                    library.assets = LibraryAssets.valueOf(assets);
                }
                list.add(library);
            }
            cursor.close();
        }
        return list;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void release() {
        if (getReadableDatabase().isOpen()) {
            close();
        }
    }
}
