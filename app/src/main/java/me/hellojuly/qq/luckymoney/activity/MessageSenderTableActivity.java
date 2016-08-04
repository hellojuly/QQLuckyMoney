package me.hellojuly.qq.luckymoney.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.TextView;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import me.hellojuly.qq.luckymoney.MyApplication;
import me.hellojuly.qq.luckymoney.R;
import me.hellojuly.qq.luckymoney.bean.ToServiceMsg;
import me.hellojuly.qq.luckymoney.db.DatabaseContext;
import me.hellojuly.qq.luckymoney.db.ServiceMsgSQLiteHelper;

/**
 * Created by July on 2016/7/31.
 */
public class MessageSenderTableActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private GridView gv_msg;
    private ServiceCursorAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_sender);

        gv_msg = (GridView) findViewById(R.id.gv_msg);
        mAdapter = new ServiceCursorAdapter(this, null, 0);
        gv_msg.setAdapter(mAdapter);
        gv_msg.setOnItemClickListener(this);
        gv_msg.setOnItemLongClickListener(this);

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final ToServiceMsg msg = mAdapter.getItem(position);
        new AlertDialog.Builder(MessageSenderTableActivity.this).setItems(new String[]{"过滤"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        new SQLiteHelperAsync().execute(msg.serviceCmd);
                        break;
                }
            }
        }).show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ToServiceMsg msg = mAdapter.getItem(position);
        MessageToAndFromActivity.startAction(this, msg.appSeq);
    }

    public class SQLiteHelperAsync extends AsyncTask<String, Void, Void> {

        private ServiceMsgSQLiteHelper mServiceMsgSQLiteHelper;

        public SQLiteHelperAsync() {
            if (mServiceMsgSQLiteHelper == null) {
                mServiceMsgSQLiteHelper = new ServiceMsgSQLiteHelper(new DatabaseContext(MyApplication.applicationContext));
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            mServiceMsgSQLiteHelper.updateServiceCmdFilter(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mServiceMsgSQLiteHelper.close();
            mAdapter.notifyDataSetChanged();
        }
    }

    public static class MyCursorLoader extends CursorLoader {

        private ServiceMsgSQLiteHelper mServiceMsgSQLiteHelper;

        public MyCursorLoader(Context context) {
            super(context);
            mServiceMsgSQLiteHelper = new ServiceMsgSQLiteHelper(new DatabaseContext(context));
        }

        /**
         * 查询数据等操作放在这里执行
         */
        @Override
        protected Cursor onLoadInBackground() {
            return mServiceMsgSQLiteHelper.querySendMsg();
        }
    }

    public class ServiceCursorAdapter extends CursorAdapter {

        public ServiceCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.grid_item_msg, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ToServiceMsg msg = ToServiceMsg.Impl.toBeanValues(cursor);
            TextView tv_id = (TextView) view.findViewById(R.id.tv_id);
            TextView tv_toVersion = (TextView) view.findViewById(R.id.tv_toVersion);
            TextView tv_appId = (TextView) view.findViewById(R.id.tv_appId);
            TextView tv_appSeq = (TextView) view.findViewById(R.id.tv_appSeq);
            TextView tv_ssoSeq = (TextView) view.findViewById(R.id.tv_ssoSeq);
            TextView tv_uin = (TextView) view.findViewById(R.id.tv_uin);
            TextView tv_uinType = (TextView) view.findViewById(R.id.tv_uinType);
            TextView tv_timeout = (TextView) view.findViewById(R.id.tv_timeout);
            TextView tv_serviceName = (TextView) view.findViewById(R.id.tv_serviceName);
            TextView tv_serviceCmd = (TextView) view.findViewById(R.id.tv_serviceCmd);
            TextView tv_msfCmd = (TextView) view.findViewById(R.id.tv_msfCmd);
            TextView tv_attributes = (TextView) view.findViewById(R.id.tv_attributes);
            TextView tv_extraData = (TextView) view.findViewById(R.id.tv_extraData);
            TextView tv_time = (TextView) view.findViewById(R.id.tv_time);


            tv_id.setText(String.valueOf(msg.id));
            tv_toVersion.setText(String.valueOf(msg.toVersion));
            tv_appId.setText(String.valueOf(msg.appId));
            tv_appSeq.setText(String.valueOf(msg.appSeq));
            tv_ssoSeq.setText(String.valueOf(msg.ssoSeq));
            tv_uin.setText(String.valueOf(msg.uin));
            tv_uinType.setText(String.valueOf(msg.uinType));
            tv_timeout.setText(String.valueOf(msg.timeout));
            tv_serviceName.setText(String.valueOf(msg.serviceName));
            tv_serviceCmd.setText(String.valueOf(msg.serviceCmd));
            tv_msfCmd.setText(String.valueOf(msg.msfCommand));
            tv_attributes.setText(String.valueOf(msg.attributes));
            tv_extraData.setText(String.valueOf(msg.extraData));
            tv_time.setText(String.valueOf(msg.time));

        }

        @Override
        public ToServiceMsg getItem(int position) {
            Cursor cursor = (Cursor) super.getItem(position);
            ToServiceMsg msg = ToServiceMsg.Impl.toBeanValues(cursor);
            return msg;
        }
    }

}
