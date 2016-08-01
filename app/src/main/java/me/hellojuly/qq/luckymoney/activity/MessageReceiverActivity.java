package me.hellojuly.qq.luckymoney.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import me.hellojuly.qq.luckymoney.MyApplication;
import me.hellojuly.qq.luckymoney.R;
import me.hellojuly.qq.luckymoney.bean.FromServiceMsg;
import me.hellojuly.qq.luckymoney.db.DatabaseContext;
import me.hellojuly.qq.luckymoney.db.ServiceMsgSQLiteHelper;

/**
 * Created by July on 2016/7/31.
 */
public class MessageReceiverActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private ListView lvMessage;
    private ServiceCursorAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_msg);

        lvMessage = (ListView) findViewById(R.id.listView);
        mAdapter = new ServiceCursorAdapter(this, null, 0);
        lvMessage.setAdapter(mAdapter);
        lvMessage.setOnItemLongClickListener(this);
        lvMessage.setOnItemClickListener(this);

        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int appSeq = (int) mAdapter.getItem(position);
        new SQLiteHelperAsync().execute(appSeq);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int appSeq = (int) mAdapter.getItem(position);
        MessageToAndFromActivity.startAction(this, appSeq);
    }

    public class SQLiteHelperAsync extends AsyncTask<Integer, Void, Void> {

        private ServiceMsgSQLiteHelper mServiceMsgSQLiteHelper;

        public SQLiteHelperAsync() {
            if (mServiceMsgSQLiteHelper == null) {
                mServiceMsgSQLiteHelper = new ServiceMsgSQLiteHelper(new DatabaseContext(MyApplication.applicationContext));
            }
        }

        @Override
        protected Void doInBackground(Integer... params) {
            mServiceMsgSQLiteHelper.deleteReceiver(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mServiceMsgSQLiteHelper.close();
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
            return mServiceMsgSQLiteHelper.queryReceiverMsg();
        }
    }

    public class ServiceCursorAdapter extends CursorAdapter {

        public ServiceCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_service_msg_from, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
            FromServiceMsg msg = FromServiceMsg.Impl.toBeanValues(cursor);
            tv_content.setText(msg.toString());
        }

        @Override
        public Object getItem(int position) {
            Cursor cursor = (Cursor) super.getItem(position);
            FromServiceMsg msg = FromServiceMsg.Impl.toBeanValues(cursor);
            return msg.appSeq;
        }
    }

}
