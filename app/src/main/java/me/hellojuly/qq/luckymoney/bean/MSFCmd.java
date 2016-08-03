package me.hellojuly.qq.luckymoney.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by July on 2016/8/2.
 */
public class MSFCmd {

    public String name;
    public boolean needResp;
    public boolean filter;
    public String memo;

    public static MSFCmd getInstance(String name, boolean needResp) {
        MSFCmd cmd = new MSFCmd();
        cmd.name = name;
        cmd.needResp = needResp;
        cmd.memo = "";
        return cmd;
    }

    public static final class Impl implements BaseColumns {
        private Impl() {
        }

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NEED_RESP = "needResp";
        public static final String COLUMN_FILTER = "filter";
        public static final String COLUMN_MEMO = "memo";

        public static MSFCmd toBeanValues(Cursor cursor) {
            MSFCmd msfCmd = new MSFCmd();
            if (cursor == null) return msfCmd;
            msfCmd.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            msfCmd.needResp = cursor.getInt(cursor.getColumnIndex(COLUMN_NEED_RESP)) == 1;
            msfCmd.filter = cursor.getInt(cursor.getColumnIndex(COLUMN_FILTER)) == 1;
            msfCmd.memo = cursor.getString(cursor.getColumnIndex(COLUMN_MEMO));
            return msfCmd;
        }

        // 对象转字段,放入表中
        public static ContentValues toContentValues(MSFCmd msfCmd) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, msfCmd.name);
            values.put(COLUMN_NEED_RESP, msfCmd.needResp ? 1 : 0);
            values.put(COLUMN_FILTER, msfCmd.filter ? 1 : 0);
            values.put(COLUMN_MEMO, msfCmd.memo);
            return values;
        }
    }
}
