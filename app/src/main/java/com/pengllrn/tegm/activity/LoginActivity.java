package com.pengllrn.tegm.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.pengllrn.tegm.R;
import com.pengllrn.tegm.bean.User;
import com.pengllrn.tegm.gson.ParseJson;
import com.pengllrn.tegm.internet.OkHttp;
import com.pengllrn.tegm.utils.ActivityCollector;
import com.pengllrn.tegm.utils.Encryp;
import com.pengllrn.tegm.utils.SharedHelper;
import com.pengllrn.tegm.constant.Constant;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class LoginActivity extends AppCompatActivity {

    private EditText user_id;
    private EditText passward;
    private Button btn_login;
    private TextView forget;
    private String applyUrl = Constant.URL_LOGIN;
    private ParseJson mParseJson = new ParseJson();
    private SharedHelper sharedHelper;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO 完成信息的存储
            //TODO 完成登录成功后的操作
            switch (msg.what) {
                case 0x2017:
                    String responseData = (msg.obj).toString();
                    if (!responseData.equals("error")) {
                        User user = mParseJson.Json2User(responseData);
                        sharedHelper = new SharedHelper(getApplicationContext());
                        sharedHelper.save(user);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "欢迎您，" + sharedHelper.readbykey("username"), Toast.LENGTH_SHORT).show();
                        user_id.setText("");
                        passward.setText("");
                    } else {
                        Toast.makeText(getApplicationContext(), "账号或密码错误", Toast.LENGTH_SHORT).show();
                    }
                default:
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedHelper sharedHelper = new SharedHelper(this);
        if (!sharedHelper.readbykey("status").equals("")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_login);
        ActivityCollector.addActivity(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permission = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(LoginActivity.this, permission, 1);
        }
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user_id.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入登录账号", Toast.LENGTH_SHORT).show();
                    user_id.setFocusable(true);
                } else if (passward.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入登录密码", Toast.LENGTH_SHORT).show();
                    passward.setFocusable(true);
                } else {
                    //TODO 密码加密
                    OkHttp okHttp = new OkHttp(getApplicationContext(), mHandler);
                    RequestBody requestBody = new FormBody.Builder()
                            .add("userid", user_id.getText().toString())
                            .add("password", new Encryp().md5(passward.getText().toString()))
                            .build();
                    okHttp.postDataFromInternet(applyUrl, requestBody);
                }
            }
        });
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        user_id = (EditText) findViewById(R.id.user_id);
        passward = (EditText) findViewById(R.id.passward);
        btn_login = (Button) findViewById(R.id.btn_login);
        forget = (TextView) findViewById(R.id.forget);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
