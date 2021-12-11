package hk.edu.cuhk.ie.iems5722.a2_1155169171;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity
{
    ArrayList<ClassroomBean.ClassBean> classrooms=new ArrayList<ClassroomBean.ClassBean>();
    ListView listView;
    ClassroomAdapter classroomadapter;
    List<ClassroomBean.ClassBean> rowsBeanList;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.tb2);
        setSupportActionBar(toolbar);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.lv_classroom);

//        CharSequence name = getString(R.string.default_notification_channel_name);
//        String CHANNEL_ID = getString(R.string.default_notification_channel_id);
//        createNotificationChannel(name,CHANNEL_ID);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        WebClient.getInstance().getClassRoomList().enqueue(new Callback<ClassroomBean>()
        {
            @Override
            public void onResponse(Call<ClassroomBean> call, Response<ClassroomBean> response )
            {
                    if(response.isSuccessful())
                    {
                        Log.i("response",response.toString());
                        ClassroomBean cb = response.body();
                        rowsBeanList = cb.getData();
                        for(int i=0; i<rowsBeanList.size();i++){
                            classrooms.add(rowsBeanList.get(i));
                        }
                        classroomadapter = new ClassroomAdapter(MainActivity.this, classrooms);
                        listView.setAdapter(classroomadapter);
                    }else{

                    }
            }
                @Override
                public void onFailure(Call<ClassroomBean> call, Throwable t) { }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                TextView textView= (TextView) view.findViewById(R.id.classroom_name);
                TextView textView1= (TextView) view.findViewById(R.id.classroom_id);
                intent.putExtra("room_title",textView.getText());
                intent.putExtra("room_id",textView1.getText());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        System.out.println("isGoogleApiAvailability: "+isGooglePlayServicesAvailable(this));

    }
    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
//        googleApiAvailability.m
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status,9000).show();
            }
            return false;
        }
        return true;
    }
//    private void createNotificationChannel(CharSequence name,String CHANNEL_ID) {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            CharSequence name = getString(R.string.default_notification_channel_name);
////            String description = getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
////            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }


    @Override
    public void onResume(){
        super.onResume();
        System.out.println("isGoogleApiAvailability: "+isGooglePlayServicesAvailable(this));
//        isGooglePlayServicesAvailable(this);
        // put your code here...

    }

}