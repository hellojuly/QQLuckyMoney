package me.hellojuly.qq.luckymoney.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by July on 2016/7/31.
 */
public class FromServiceMsg {

    public int fromVersion;
    public int appId;
    public int appSeq;
    public int flag;
    public int ssoSeq;
    public int resultCode;
    public String uin;
    public String errorMsg;
    public String serviceCmd;
    public String msfCommand;
    public byte[] msgCookie;
    public byte[] wupBuffer;
    public HashMap attributes;
    public Bundle extraData;

    public Long time;

    @Override
    public String toString() {
        return "FromServiceMsg{" +
                "fromVersion=" + fromVersion +
                ", appId=" + appId +
                ", appSeq=" + appSeq +
                ", flag=" + flag +
                ", ssoSeq=" + ssoSeq +
                ", resultCode=" + resultCode +
                ", uin='" + uin + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", serviceCmd='" + serviceCmd + '\'' +
                ", msfCommand='" + msfCommand + '\'' +
                ", msgCookie=" + Arrays.toString(msgCookie) +
                ", wupBuffer=" + Arrays.toString(wupBuffer) +
                ", attributes=" + attributes +
                ", extraData=" + extraData +
                ", time=" + time +
                '}';
    }

    public static final class Impl implements BaseColumns {
        private Impl() {
        }

        public static final String COLUMN_FROM_VERSION = "fromVersion";
        public static final String COLUMN_APP_ID = "appId";
        public static final String COLUMN_APP_SEQ = "appSeq";
        public static final String COLUMN_FLAG = "flag";
        public static final String COLUMN_SSO_SEQ = "ssoSeq";
        public static final String COLUMN_RESULT_CODE = "resultCode";
        public static final String COLUMN_UIN = "uin";
        public static final String COLUMN_ERROR_MESSAGE = "errorMsg";
        public static final String COLUMN_SERVICE_CMD = "serviceCmd";
        public static final String COLUMN_MSF_COMMAND = "msfCommand";
        public static final String COLUMN_MSG_COOKIE = "msgCookie";
        public static final String COLUMN_WUP_BUFFER = "wupBuffer";
        public static final String COLUMN_ATTRIBUTES = "attributes";
        public static final String COLUMN_EXTRA_DATA = "extraData";

        public static final String COLUMN_TIME = "time";

        public static FromServiceMsg toBeanValues(Cursor cursor) {
            FromServiceMsg fromServiceMsg = new FromServiceMsg();
            fromServiceMsg.fromVersion = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_FROM_VERSION));
            fromServiceMsg.appId = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_APP_ID));
            fromServiceMsg.appSeq = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_APP_SEQ));
            fromServiceMsg.flag = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_FLAG));
            fromServiceMsg.ssoSeq = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_SSO_SEQ));
            fromServiceMsg.resultCode = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_RESULT_CODE));
            fromServiceMsg.uin = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_UIN));
            fromServiceMsg.errorMsg = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_ERROR_MESSAGE));
            fromServiceMsg.serviceCmd = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_SERVICE_CMD));
            fromServiceMsg.msfCommand = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_MSF_COMMAND));

            fromServiceMsg.msgCookie = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_MSG_COOKIE)).getBytes();
            fromServiceMsg.wupBuffer = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_WUP_BUFFER)).getBytes();
            fromServiceMsg.attributes = JSON.parseObject(cursor.getString(cursor.getColumnIndex(Impl.COLUMN_ATTRIBUTES)), HashMap.class);
            fromServiceMsg.extraData = JSON.parseObject(cursor.getString(cursor.getColumnIndex(Impl.COLUMN_EXTRA_DATA)), Bundle.class);

            fromServiceMsg.time = cursor.getLong(cursor.getColumnIndex(Impl.COLUMN_TIME));
            return fromServiceMsg;
        }

        // 对象转字段,放入表中
        public static ContentValues toContentValues(FromServiceMsg msg) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_FROM_VERSION, msg.fromVersion);
            values.put(COLUMN_APP_ID, msg.appId);
            values.put(COLUMN_APP_SEQ, msg.appSeq);
            values.put(COLUMN_FLAG, msg.flag);
            values.put(COLUMN_SSO_SEQ, msg.ssoSeq);
            values.put(COLUMN_RESULT_CODE, msg.resultCode);
            values.put(COLUMN_UIN, msg.uin);
            values.put(COLUMN_ERROR_MESSAGE, msg.errorMsg);
            values.put(COLUMN_SERVICE_CMD, msg.serviceCmd);
            values.put(COLUMN_MSF_COMMAND, msg.msfCommand);

            try {
                values.put(COLUMN_MSG_COOKIE, new String(msg.msgCookie, "UTF8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                values.put(COLUMN_WUP_BUFFER, new String(msg.wupBuffer, "UTF8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            values.put(COLUMN_ATTRIBUTES, JSON.toJSONString(msg.attributes));
            values.put(COLUMN_EXTRA_DATA, JSON.toJSONString(msg.extraData));

            values.put(COLUMN_TIME, System.currentTimeMillis());
            return values;
        }
    }
}
