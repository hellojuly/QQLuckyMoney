package me.hellojuly.qq.luckymoney.db;

/**
 * Created by July on 2016/7/31.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.hellojuly.qq.luckymoney.bean.FromServiceMsg;
import me.hellojuly.qq.luckymoney.bean.ToServiceMsg;

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
                createToServiceTable(db);
                break;
            default:
                throw new IllegalStateException("Don't know how to upgrade to " + version);
        }
    }

    /**
     * 创建表(接收)
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
                    FromServiceMsg.Impl.COLUMN_SSO_SEQ + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_UIN + " TEXT, " +

                    FromServiceMsg.Impl.COLUMN_FLAG + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_RESULT_CODE + " INTEGER, " +
                    FromServiceMsg.Impl.COLUMN_ERROR_MESSAGE + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_MSG_COOKIE + " TEXT, " +

                    FromServiceMsg.Impl.COLUMN_SERVICE_CMD + " TEXT, " +
                    FromServiceMsg.Impl.COLUMN_MSF_COMMAND + " TEXT, " +
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
     * 创建表(发送)
     *
     * @param db
     */
    private void createToServiceTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_TO + "(" +
                    ToServiceMsg.Impl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ToServiceMsg.Impl.COLUMN_TO_VERSION + " INTEGER, " +
                    ToServiceMsg.Impl.COLUMN_APP_ID + " INTEGER, " +
                    ToServiceMsg.Impl.COLUMN_APP_SEQ + " INTEGER, " +
                    ToServiceMsg.Impl.COLUMN_SSO_SEQ + " INTEGER, " +
                    ToServiceMsg.Impl.COLUMN_UIN + " TEXT, " +

                    ToServiceMsg.Impl.COLUMN_UIN_TYPE + " INTEGER, " +
                    ToServiceMsg.Impl.COLUMN_NEED_RESP + " INTEGER, " +
                    ToServiceMsg.Impl.COLUMN_QUICK_SEND_ENABLE + " INTEGER, " +
                    ToServiceMsg.Impl.COLUMN_QUICK_SEND_STRATEGY + " INTEGER, " +
                    ToServiceMsg.Impl.COLUMN_SEND_TIMEOUT + " LONG, " +
                    ToServiceMsg.Impl.COLUMN_TIMEOUT + " LONG, " +
                    ToServiceMsg.Impl.COLUMN_SERVICE_NAME + " TEXT, " +

                    ToServiceMsg.Impl.COLUMN_SERVICE_CMD + " TEXT, " +
                    ToServiceMsg.Impl.COLUMN_MSF_COMMAND + " TEXT, " +
                    ToServiceMsg.Impl.COLUMN_WUP_BUFFER + " TEXT, " +
                    ToServiceMsg.Impl.COLUMN_ATTRIBUTES + " TEXT, " +
                    ToServiceMsg.Impl.COLUMN_EXTRA_DATA + " TEXT, " +
                    ToServiceMsg.Impl.COLUMN_TIME + " LONG"
                    + " );");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 查询消息列表(接收消息)
     *
     * @return
     */
    public static List<FromServiceMsg> convertToReceiverMsg(Cursor cursor) {
        List<FromServiceMsg> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            FromServiceMsg fromServiceMsg = FromServiceMsg.Impl.toBeanValues(cursor);
            list.add(fromServiceMsg);
        }
        return list;
    }

    /**
     * 查询消息列表(发送消息)
     *
     * @return
     */
    public static List<ToServiceMsg> convertToSenderMsg(Cursor cursor) {
        List<ToServiceMsg> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            ToServiceMsg toServiceMsg = ToServiceMsg.Impl.toBeanValues(cursor);
            list.add(toServiceMsg);
        }
        return list;
    }

    /**
     * 插入数据(接收)
     *
     * @param msg
     */
    public void insert(FromServiceMsg msg) {
        getWritableDatabase().insert(DB_TABLE_FROM, null, FromServiceMsg.Impl.toContentValues(msg));
    }

    /**
     * 插入数据(发送)
     *
     * @param msg
     */
    public void insert(ToServiceMsg msg) {
        getWritableDatabase().insert(DB_TABLE_TO, null, ToServiceMsg.Impl.toContentValues(msg));
    }

    /**
     * 查询消息列表(接收)
     *
     * @return
     */
    public Cursor queryReceiverMsg() {
        return getReadableDatabase().rawQuery("select * from " + DB_TABLE_FROM + " order by time desc", null);
    }

    /**
     * 查询消息列表(发送)
     *
     * @return
     */
    public Cursor querySendMsg() {
        return getReadableDatabase().rawQuery("select * from " + DB_TABLE_TO + " where " + ToServiceMsg.Impl.COLUMN_NEED_RESP + "=1" + " order by time desc", null);
    }

    /**
     * 删除消息(接收)
     *
     * @param appSeq
     */
    public void deleteReceiver(int appSeq) {
        getReadableDatabase().delete(DB_TABLE_FROM, FromServiceMsg.Impl.COLUMN_APP_SEQ + "=? ", new String[]{appSeq + ""});
    }

    /**
     * 删除消息(发送)
     *
     * @param appSeq
     */
    public void deleteSender(int appSeq) {
        getReadableDatabase().delete(DB_TABLE_TO, FromServiceMsg.Impl.COLUMN_APP_SEQ + "=? ", new String[]{appSeq + ""});
    }

    /**
     * 查询消息
     *
     * @param appSeq
     * @return
     */
    public List<FromServiceMsg> queryMessageReceiver(int appSeq) {
        Cursor cursor = getReadableDatabase().rawQuery("select * from " + DB_TABLE_FROM + " where " +
                        FromServiceMsg.Impl.COLUMN_RESULT_CODE + "=1000 and " +
                        FromServiceMsg.Impl.COLUMN_APP_SEQ + "=" + appSeq,
                null);
        return convertToReceiverMsg(cursor);
    }

    /**
     * 查询消息
     *
     * @param appSeq
     * @return
     */
    public List<ToServiceMsg> queryMessageSender(int appSeq) {
        Cursor cursor = getReadableDatabase().rawQuery("select * from " + DB_TABLE_TO + " where " +
                        FromServiceMsg.Impl.COLUMN_APP_SEQ + "=" + appSeq,
                null);
        return convertToSenderMsg(cursor);
    }

}
