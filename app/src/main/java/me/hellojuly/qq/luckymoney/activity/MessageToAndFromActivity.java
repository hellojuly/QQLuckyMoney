package me.hellojuly.qq.luckymoney.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;

import me.hellojuly.qq.luckymoney.MyApplication;
import me.hellojuly.qq.luckymoney.R;
import me.hellojuly.qq.luckymoney.bean.FromServiceMsg;
import me.hellojuly.qq.luckymoney.bean.ToServiceMsg;
import me.hellojuly.qq.luckymoney.db.DatabaseContext;
import me.hellojuly.qq.luckymoney.db.ServiceMsgSQLiteHelper;

/**
 * Desc:
 * Created by 庞承晖
 * Date: 2016/8/1.
 * Time: 19:18
 */
public class MessageToAndFromActivity extends AppCompatActivity {

    private int mAppSeq;

    private TextView tv_from, tv_to;

    public static void startAction(Context context, int appSeq) {
        Intent intent = new Intent(context, MessageToAndFromActivity.class);
        intent.putExtra("data", appSeq);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_to_and_from);
        tv_from = (TextView) findViewById(R.id.tv_from);
        tv_to = (TextView) findViewById(R.id.tv_to);
        mAppSeq = getIntent().getIntExtra("data", 0);
        new SenderSQLiteHelperAsync().execute(mAppSeq);
        new ReceiverSQLiteHelperAsync().execute(mAppSeq);
    }

    public class SenderSQLiteHelperAsync extends AsyncTask<Integer, Void, List<ToServiceMsg>> {

        private ServiceMsgSQLiteHelper mServiceMsgSQLiteHelper;

        public SenderSQLiteHelperAsync() {
            if (mServiceMsgSQLiteHelper == null) {
                mServiceMsgSQLiteHelper = new ServiceMsgSQLiteHelper(new DatabaseContext(MyApplication.applicationContext));
            }
        }

        @Override
        protected List<ToServiceMsg> doInBackground(Integer... params) {
            List<ToServiceMsg> list = mServiceMsgSQLiteHelper.queryMessageSender(params[0]);
            return list;
        }

        @Override
        protected void onPostExecute(List<ToServiceMsg> toServiceMsg) {
            super.onPostExecute(toServiceMsg);
            StringBuilder builder = new StringBuilder();
            for (ToServiceMsg msg : toServiceMsg) {
                builder.append(msg.toString());
                builder.append("\n\n=============================================");
            }
            tv_to.setText(builder.toString());
            mServiceMsgSQLiteHelper.close();
        }
    }

    public class ReceiverSQLiteHelperAsync extends AsyncTask<Integer, Void, List<FromServiceMsg>> {

        private ServiceMsgSQLiteHelper mServiceMsgSQLiteHelper;

        public ReceiverSQLiteHelperAsync() {
            if (mServiceMsgSQLiteHelper == null) {
                mServiceMsgSQLiteHelper = new ServiceMsgSQLiteHelper(new DatabaseContext(MyApplication.applicationContext));
            }
        }

        @Override
        protected List<FromServiceMsg> doInBackground(Integer... params) {
            List<FromServiceMsg> list = mServiceMsgSQLiteHelper.queryMessageReceiver(params[0]);
            return list;
        }

        @Override
        protected void onPostExecute(List<FromServiceMsg> toServiceMsg) {
            super.onPostExecute(toServiceMsg);
            StringBuilder builder = new StringBuilder();
            for (FromServiceMsg msg : toServiceMsg) {
                builder.append(msg.toString());
                builder.append("\n\n=============================================");
            }
            tv_from.setText(builder.toString());
            mServiceMsgSQLiteHelper.close();
        }
    }
}
