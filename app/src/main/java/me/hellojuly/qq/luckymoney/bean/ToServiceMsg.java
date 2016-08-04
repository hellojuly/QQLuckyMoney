package me.hellojuly.qq.luckymoney.bean;

import android.content.ContentValues;
import android.database.Cursor;
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
public class ToServiceMsg {

    public int id;
    public int toVersion;
    public int appId;
    public int appSeq;
    public int ssoSeq;
    public String uin;

    public int uinType;
    public boolean needResp;//需要响应
    public boolean quickSendEnable;//快速发送
    public int quickSendStrategy;//快速发送策略
    public long sendTimeout;
    public long timeout;
    public String serviceName;

    public String serviceCmd;
    public String msfCommand;
    public byte[] wupBuffer;
    public HashMap attributes;
    public String extraData;

    public Long time;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ToServiceMsg{" +
                "\n    toVersion=" + toVersion +
                ",\n    appId=" + appId +
                ",\n    appSeq=" + appSeq +
                ",\n    ssoSeq=" + ssoSeq +
                ",\n    uin='" + uin + '\'' +
                ",\n    uinType=" + uinType +
                ",\n    needResp=" + needResp +
                ",\n    quickSendEnable=" + quickSendEnable +
                ",\n    quickSendStrategy=" + quickSendStrategy +
                ",\n    sendTimeout=" + sendTimeout +
                ",\n    timeout=" + timeout +
                ",\n    serviceName='" + serviceName + '\'' +
                ",\n    serviceCmd='" + serviceCmd + '\'' +
                ",\n    msfCommand='" + msfCommand + '\'' +
                ",\n    attributes=" + attributesToString() +
                ",\n    extraData='" + extraData + '\'' +
                ",\n    time=" + time);
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

        public static final String COLUMN_TO_VERSION = "toVersion";
        public static final String COLUMN_APP_ID = "appId";
        public static final String COLUMN_APP_SEQ = "appSeq";
        public static final String COLUMN_SSO_SEQ = "ssoSeq";
        public static final String COLUMN_UIN = "uin";

        public static final String COLUMN_UIN_TYPE = "uinType";
        public static final String COLUMN_NEED_RESP = "needResp";
        public static final String COLUMN_QUICK_SEND_ENABLE = "quickSendEnable";
        public static final String COLUMN_QUICK_SEND_STRATEGY = "quickSendStrategy";
        public static final String COLUMN_SEND_TIMEOUT = "sendTimeout";
        public static final String COLUMN_TIMEOUT = "timeout";
        public static final String COLUMN_SERVICE_NAME = "serviceName";

        public static final String COLUMN_SERVICE_CMD = "serviceCmd";
        public static final String COLUMN_MSF_COMMAND = "msfCommand";
        public static final String COLUMN_WUP_BUFFER = "wupBuffer";
        public static final String COLUMN_ATTRIBUTES = "attributes";
        public static final String COLUMN_EXTRA_DATA = "extraData";

        public static final String COLUMN_TIME = "time";

        public static ToServiceMsg toBeanValues(Cursor cursor) {
            ToServiceMsg fromServiceMsg = new ToServiceMsg();
            if (cursor == null) return fromServiceMsg;
            fromServiceMsg.id = cursor.getInt(cursor.getColumnIndex(Impl._ID));
            fromServiceMsg.toVersion = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_TO_VERSION));
            fromServiceMsg.appId = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_APP_ID));
            fromServiceMsg.appSeq = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_APP_SEQ));
            fromServiceMsg.ssoSeq = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_SSO_SEQ));
            fromServiceMsg.uin = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_UIN));

            fromServiceMsg.uinType = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_UIN_TYPE));
            fromServiceMsg.needResp = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_NEED_RESP)) == 1 ? true : false;
            fromServiceMsg.quickSendEnable = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_QUICK_SEND_ENABLE)) == 1 ? true : false;
            fromServiceMsg.quickSendStrategy = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_QUICK_SEND_STRATEGY));
            fromServiceMsg.sendTimeout = cursor.getLong(cursor.getColumnIndex(Impl.COLUMN_SEND_TIMEOUT));
            fromServiceMsg.timeout = cursor.getLong(cursor.getColumnIndex(Impl.COLUMN_TIMEOUT));
            fromServiceMsg.serviceName = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_SERVICE_NAME));

            fromServiceMsg.serviceCmd = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_SERVICE_CMD));
            fromServiceMsg.msfCommand = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_MSF_COMMAND));
            fromServiceMsg.wupBuffer = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_WUP_BUFFER)).getBytes();
            fromServiceMsg.attributes = JSON.parseObject(cursor.getString(cursor.getColumnIndex(Impl.COLUMN_ATTRIBUTES)), HashMap.class);
            fromServiceMsg.extraData = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_EXTRA_DATA));

            fromServiceMsg.time = cursor.getLong(cursor.getColumnIndex(Impl.COLUMN_TIME));
            return fromServiceMsg;
        }

        // 对象转字段,放入表中
        public static ContentValues toContentValues(ToServiceMsg msg) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TO_VERSION, msg.toVersion);
            values.put(COLUMN_APP_ID, msg.appId);
            values.put(COLUMN_APP_SEQ, msg.appSeq);
            values.put(COLUMN_SSO_SEQ, msg.ssoSeq);
            values.put(COLUMN_UIN, msg.uin);

            values.put(COLUMN_UIN_TYPE, msg.uinType);
            values.put(COLUMN_NEED_RESP, msg.needResp ? 1 : 0);
            values.put(COLUMN_QUICK_SEND_ENABLE, msg.quickSendEnable ? 1 : 0);
            values.put(COLUMN_QUICK_SEND_STRATEGY, msg.quickSendStrategy);
            values.put(COLUMN_SEND_TIMEOUT, msg.sendTimeout);
            values.put(COLUMN_TIMEOUT, msg.timeout);
            values.put(COLUMN_SERVICE_NAME, msg.serviceName);

            values.put(COLUMN_SERVICE_CMD, msg.serviceCmd);
            values.put(COLUMN_MSF_COMMAND, msg.msfCommand);

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
