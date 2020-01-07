package net.androidwing.hotxposed.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * 数据库操作类
 *
 * @author z.houbin
 */
public class SqliteHelper {

    public static String getCursorValue(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        String v = "";
        switch (cursor.getType(columnIndex)) {
            case Cursor.FIELD_TYPE_BLOB:
                v = new String(cursor.getBlob(columnIndex));
                break;
            case Cursor.FIELD_TYPE_FLOAT:
                v = String.valueOf(cursor.getFloat(columnIndex));
                break;
            case Cursor.FIELD_TYPE_INTEGER:
                v = String.valueOf(cursor.getLong(columnIndex));
                break;
            case Cursor.FIELD_TYPE_STRING:
                v = cursor.getString(columnIndex);
                break;
            default:
                v = "";
        }
        return v;
    }

    public static String dumpCursor(Cursor cursor) {
        StringBuilder builder = new StringBuilder();
        String[] columnNames = cursor.getColumnNames();
        while (cursor.moveToNext()) {
            for (String columnName : columnNames) {
                int columnIndex = cursor.getColumnIndex(columnName);
                builder.append(columnName);
                builder.append(":");
                Object v = new Object();
                switch (cursor.getType(columnIndex)) {
                    case Cursor.FIELD_TYPE_BLOB:
                        v = cursor.getBlob(columnIndex);
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        v = cursor.getFloat(columnIndex);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        v = cursor.getLong(columnIndex);
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        v = cursor.getString(columnIndex);
                        break;
                    default:
                        v = "";
                }

                builder.append(v);
                builder.append(", ");
            }
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public static ContentValues cursorToContents(Cursor cursor) {
        ContentValues values = new ContentValues();
        if (cursor != null) {
            String[] columnNames = cursor.getColumnNames();
            for (String columnName : columnNames) {
                int columnIndex = cursor.getColumnIndex(columnName);
                switch (cursor.getType(columnIndex)) {
                    case Cursor.FIELD_TYPE_BLOB:
                        values.put(columnName, cursor.getBlob(columnIndex));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        values.put(columnName, cursor.getFloat(columnIndex));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        values.put(columnName, cursor.getLong(columnIndex));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        values.put(columnName, cursor.getString(columnIndex));
                        break;
                    default:
                        values.put(columnName, "");
                }
            }
        }
        return values;
    }
}
