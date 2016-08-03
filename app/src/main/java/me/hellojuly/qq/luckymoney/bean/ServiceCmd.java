package me.hellojuly.qq.luckymoney.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Created by July on 2016/8/2.
 */
public class ServiceCmd {
    public String name;
    public boolean needResp;
    public boolean filter;
    public String memo;

    public static ServiceCmd getInstance(String name, boolean needResp) {
        ServiceCmd cmd = new ServiceCmd();
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

        public static ServiceCmd toBeanValues(Cursor cursor) {
            ServiceCmd serviceCmd = new ServiceCmd();
            if (cursor == null) return serviceCmd;
            serviceCmd.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            serviceCmd.needResp = cursor.getInt(cursor.getColumnIndex(COLUMN_NEED_RESP)) == 1;
            serviceCmd.filter = cursor.getInt(cursor.getColumnIndex(COLUMN_FILTER)) == 1;
            serviceCmd.memo = cursor.getString(cursor.getColumnIndex(COLUMN_MEMO));
            return serviceCmd;
        }

        // 对象转字段,放入表中
        public static ContentValues toContentValues(ServiceCmd serviceCmd) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, serviceCmd.name);
            values.put(COLUMN_NEED_RESP, serviceCmd.needResp ? 1 : 0);
            values.put(COLUMN_FILTER, serviceCmd.filter ? 1 : 0);
            values.put(COLUMN_MEMO, serviceCmd.memo);
            return values;
        }
    }

}
