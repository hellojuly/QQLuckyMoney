package me.hellojuly.qq.luckymoney.bean;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by July on 2016/7/31.
 */
public class FromServiceMsg {

    public int fromVersion;
    public int appId;
    public int appSeq;
    public int ssoSeq;
    public String uin;

    public String serviceCmd;
    public String msfCommand;
    public byte[] wupBuffer;
    public HashMap attributes;
    public String extraData;

    public int flag;
    public int resultCode;
    public String errorMsg;
    public byte[] msgCookie;

    public Long time;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FromServiceMsg{" +
                "\n    fromVersion=" + fromVersion +
                ",\n    appId=" + appId +
                ",\n    appSeq=" + appSeq +
                ",\n    flag=" + flag +
                ",\n    ssoSeq=" + ssoSeq +
                ",\n    resultCode=" + resultCode +
                ",\n    uin='" + uin + '\'' +
                ",\n    errorMsg='" + errorMsg + '\'' +
                ",\n    serviceCmd='" + serviceCmd + '\'' +
                ",\n    msfCommand='" + msfCommand + '\'' +
                ",\n    attributes=" + attributesToString() +
                ",\n    extraData=" + extraData +
                ",\n    time=" + time);
        try {
            builder.append(",\n    msgCookie=" + new String(msgCookie, "UTF8"));
        } catch (Exception e) {
        }
        try {
            builder.append(",\n    wupBuffer=" + new String(wupBuffer, "UTF8"));
        } catch (Exception e) {
        }
        builder.append("}");
        return builder.toString();
    }

    public String attributesToString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        if (attributes != null) {
            Set set = attributes.keySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = attributes.get(key);
                builder.append("\n        " + key.toString() + "=" + value.toString());
            }
        }
        builder.append("\n    }");
        return builder.toString();
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
            if (cursor == null) return fromServiceMsg;
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
            fromServiceMsg.extraData = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_EXTRA_DATA));

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
            values.put(COLUMN_EXTRA_DATA, msg.extraData);

            values.put(COLUMN_TIME, System.currentTimeMillis());
            return values;
        }
    }
}
