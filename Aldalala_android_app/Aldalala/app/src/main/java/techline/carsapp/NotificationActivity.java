package techline.carsapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Config.BaseURL;
import adapter.Notification_adapter;
import io.paperdb.Paper;
import model.Notification_model;
import util.CommonAsyTask;
import util.ConnectivityReceiver;
import util.LocaleHelper;
import util.NameValuePair;
import util.RecyclerTouchListener;
import util.Session_management;

public class NotificationActivity extends CommonAppCompatActivity {

    private static String TAG = NotificationActivity.class.getSimpleName();

    private RecyclerView rv_notification;

    private List<Notification_model> notification_modelList = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase, "ar"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Paper.init(this);

        String language = Paper.book().read("language");
        if (language == null) {
            Paper.book().write("language", "ar");
        }
        rv_notification = (RecyclerView) findViewById(R.id.rv_notification);
        rv_notification.setLayoutManager(new LinearLayoutManager(this));

        Session_management sessionManagement = new Session_management(this);
        String getuser_id = sessionManagement.getUserDetails().get(BaseURL.KEY_ID);

        if (ConnectivityReceiver.isConnected()){
            makeGetNotification(getuser_id);
        }else{
            ConnectivityReceiver.showSnackbar(this);
        }

        rv_notification.addOnItemTouchListener(new RecyclerTouchListener(this, rv_notification, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent detailIntent = new Intent(NotificationActivity.this, Notification_detailActivity.class);
                detailIntent.putExtra("title", notification_modelList.get(position).getTitle());
                detailIntent.putExtra("date", notification_modelList.get(position).getCreated_at());
                detailIntent.putExtra("desc", notification_modelList.get(position).getMessage());
                detailIntent.putExtra("image_path", notification_modelList.get(position).getImage_path());

                startActivity(detailIntent);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

    }

    private void makeGetNotification(String user_id) {

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair("user_id", user_id));

        // CommonAsyTask class for load data from api and manage response and api
        CommonAsyTask task = new CommonAsyTask(params,
                BaseURL.GET_NOTIFICATION_URL, new CommonAsyTask.VJsonResponce() {
            @Override
            public void VResponce(String response) {
                Log.e(TAG, response);

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Notification_model>>() {
                }.getType();

                notification_modelList = gson.fromJson(response, listType);

                Notification_adapter adapter = new Notification_adapter(notification_modelList);
                rv_notification.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                if (notification_modelList.isEmpty()) {
                    Toast.makeText(NotificationActivity.this, getResources().getString(R.string.record_not_found), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void VError(String responce) {
                Log.e(TAG, responce);
                Toast.makeText(NotificationActivity.this, "" + responce, Toast.LENGTH_SHORT).show();
            }
        }, true, this);
        task.execute();

    }

}
