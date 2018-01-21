package cn.andrewlu.resmanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrewlu on 2018/1/21.
 */

public class ThemeDao {
    private ThemeDbHelper dbHelper;

    private final static String DB_NAME = "theme_info.db";
    private final static String TABLE_NAME = "THEME_INFO";
    private final static int DB_VER = 1;

    public ThemeDao(Context context) {
        dbHelper = new ThemeDbHelper(context);
        dbHelper.getWritableDatabase().enableWriteAheadLogging();
    }

    public List<ThemeInfo> getAllThemeInfo() {
        List<ThemeInfo> infos = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(TABLE_NAME, null, "1=1",
                    null, null, null, "id ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ThemeInfo info = dbHelper.parseCursor(cursor);
                    if (info != null) infos.add(info);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return infos;
    }

    public ThemeInfo getThemeByName(String themeName) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(TABLE_NAME, null, "name=?",
                    new String[]{themeName}, null, null, "id ASC", "LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {
                ThemeInfo info = dbHelper.parseCursor(cursor);
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public ThemeInfo getThemeByAssetName(String assetName) {
        if (assetName == null || assetName.length() <= 0) return null;
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(TABLE_NAME, null, "assetsPath=?",
                    new String[]{assetName}, null, null, "id ASC", "LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {
                ThemeInfo info = dbHelper.parseCursor(cursor);
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public void setThemeSelected(String themeName) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            String updateSql = "UPDATE " + TABLE_NAME + " SET selected = 0 WHERE selected = 1";
            db.execSQL(updateSql);

            updateSql = "UPDATE " + TABLE_NAME + " SET selected = 1 WHERE name= ?";
            db.execSQL(updateSql, new String[]{themeName});

            db.endTransaction();
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public ThemeInfo getCurrentTheme() {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getReadableDatabase().query(TABLE_NAME, null, "selected=?",
                    new String[]{"1"}, null, null, "id ASC", "LIMIT 1");
            if (cursor != null && cursor.moveToFirst()) {
                ThemeInfo info = dbHelper.parseCursor(cursor);
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public boolean saveTheme(ThemeInfo info) {
        try {
            ContentValues values = dbHelper.parseThem(info);
            if (info.id > 0) {
                dbHelper.getWritableDatabase().update(TABLE_NAME, values, "name=?", new String[]{info.name});
            } else {
                dbHelper.getWritableDatabase().insertOrThrow(TABLE_NAME, null, values);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private final class ThemeDbHelper extends SQLiteOpenHelper {

        public ThemeDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VER);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME)
                    .append("(id integer PRIMARY KEY AUTO_INCREMENT,")
                    .append("name varchar(100) NOT NULL,")
                    .append("author varchar(100),")
                    .append("description varchar(512),")
                    .append("tags varchar(255),")
                    .append("previewDrawables varchar(255),")
                    .append("minCompileVersion int,")
                    .append("maxCompileVersion int,")
                    .append("userId varchar(100),")
                    .append("apiKey varchar(200),")
                    .append("resToolVersion varchar(50),")
                    .append("fullPath varchar(255),")
                    .append("assetsPath varchar(255),")
                    .append("verName varchar,")
                    .append("verCode int,")
                    .append("md5 varchar(50),")
                    .append("size integer,")
                    .append("createAt integer,")
                    .append("selected int)");
            db.execSQL(buffer.toString());
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        public ThemeInfo parseCursor(Cursor cursor) {
            ThemeInfo info = new ThemeInfo();
            info.id = cursor.getInt(cursor.getColumnIndex("id"));
            info.name = cursor.getString(cursor.getColumnIndex("name"));
            info.author = cursor.getString(cursor.getColumnIndex("author"));
            info.description = cursor.getString(cursor.getColumnIndex("description"));
            info.minCompileVersion = cursor.getInt(cursor.getColumnIndex("minCompileVersion"));
            info.maxCompileVersion = cursor.getInt(cursor.getColumnIndex("maxCompileVersion"));
            info.userId = cursor.getString(cursor.getColumnIndex("userId"));
            info.apiKey = cursor.getString(cursor.getColumnIndex("apiKey"));
            info.resToolVersion = cursor.getString(cursor.getColumnIndex("resToolVersion"));
            info.fullPath = cursor.getString(cursor.getColumnIndex("fullPath"));
            info.assetsPath = cursor.getString(cursor.getColumnIndex("assetsPath"));
            info.verName = cursor.getString(cursor.getColumnIndex("verName"));
            info.verCode = cursor.getInt(cursor.getColumnIndex("verCode"));
            info.md5 = cursor.getString(cursor.getColumnIndex("md5"));
            info.size = cursor.getLong(cursor.getColumnIndex("size"));
            info.createAt = cursor.getLong(cursor.getColumnIndex("createAt"));
            info.selected = cursor.getInt(cursor.getColumnIndex("selected")) == 1;

            String tags = cursor.getString(cursor.getColumnIndex("tags"));
            String drawables = cursor.getString(cursor.getColumnIndex("previewDrawables"));
            info.tags = strToArray(tags);
            info.previewDrawables = strToArray(drawables);
            return info;
        }

        public ContentValues parseThem(ThemeInfo themeInfo) {
            ContentValues values = new ContentValues(18);
            values.put("name", themeInfo.name);
            values.put("author", themeInfo.author);
            values.put("description", themeInfo.description);
            values.put("tags", arrayToString(themeInfo.tags));
            values.put("previewDrawables", arrayToString(themeInfo.previewDrawables));
            values.put("minCompileVersion", themeInfo.minCompileVersion);
            values.put("maxCompileVersion", themeInfo.maxCompileVersion);
            values.put("userId", themeInfo.userId);
            values.put("apiKey", themeInfo.apiKey);
            values.put("resToolVersion", themeInfo.resToolVersion);
            values.put("fullPath", themeInfo.fullPath);
            values.put("assetsPath", themeInfo.assetsPath);
            values.put("verName", themeInfo.verName);
            values.put("verCode", themeInfo.verCode);
            values.put("md5", themeInfo.md5);
            values.put("size", themeInfo.size);
            values.put("createAt", themeInfo.createAt);
            values.put("selected", themeInfo.selected ? 1 : 0);
            return values;
        }

        public String arrayToString(String[] strs) {
            if (strs == null) return "";
            int iMax = strs.length - 1;
            StringBuilder b = new StringBuilder();
            for (int i = 0; ; i++) {
                b.append(String.valueOf(strs[i]));
                if (i == iMax)
                    return b.toString();
                b.append(", ");
            }
        }

        public String[] strToArray(String text) {
            if (text == null || text.length() == 0) return new String[0];
            return text.split(",");
        }
    }
}
