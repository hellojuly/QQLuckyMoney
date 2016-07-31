package me.hellojuly.qq.luckymoney.db;

/**
 * Created by July on 2016/7/31.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import me.hellojuly.qq.luckymoney.bean.FromServiceMsg;

/**
 * Desc:
 * Created by 庞承晖
 * Date: 2016/7/4.
 * Time: 11:32
 */
public class ServiceMsgSQLiteHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "ServiceMsg.db";
    public static final String DB_TABLE_FROM = "FromService";//服务器下发的数据
    public static final String DB_TABLE_TO = "ToService";//发送到服务器的数据
    private static final int DB_VERSION = 1;

    public ServiceMsgSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            upgradeTo(db, version);
        }
    }

    /**
     * Upgrade database from (version - 1) to version.
     */
    private void upgradeTo(SQLiteDatabase db, int version) {
        switch (version) {
            case 1:
                createFromServiceTable(db);
                break;
            default:
                throw new IllegalStateException("Don't know how to upgrade to " + version);
        }
    }

    /**
     * 创建表(从服务器)
     *
     * @param db
     */
    private void createFromServiceTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_FROM + "(" +
                    FromServiceMsg.Impl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FromServiceMsg.Impl.COLUMN_FROM_VERSION + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_APP_ID + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_APP_SEQ + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_FLAG + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_SSO_SEQ + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_RESULT_CODE + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_UIN + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_ERROR_MESSAGE + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_SERVICE_CMD + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_MSF_COMMAND + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_MSG_COOKIE + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_WUP_BUFFER + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_ATTRIBUTES + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_EXTRA_DATA + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_TIME + " LONG"
                    + " );");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 查询消息列表
     *
     * @return
     */
    public static List<FromServiceMsg> convertToFromServiceMsg(Cursor cursor) {
        List<FromServiceMsg> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            FromServiceMsg fromServiceMsg = FromServiceMsg.Impl.toBeanValues(cursor);
            list.add(fromServiceMsg);
        }
        return list;
    }

    /**
     * 插入数据
     *
     * @param msg
     */
    public void insert(FromServiceMsg msg) {
        getWritableDatabase().insert(DB_TABLE_FROM, null, FromServiceMsg.Impl.toContentValues(msg));

    }

    /**
     * 查询列表
     *
     * @return
     */
    public Cursor query() {
        return getReadableDatabase().rawQuery("select * from " + DB_TABLE_FROM + " order by time", null);
    }

    /**
     * 删除数据
     *
     * @param appSeq
     */
    public void delete(int appSeq) {
        getReadableDatabase().delete(DB_TABLE_FROM, FromServiceMsg.Impl.COLUMN_APP_SEQ + "=? ", new String[]{appSeq + ""});
    }


}
