package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText name_put_in; //用户名输入框
    private EditText password_put_in;  //用户密码输入框
    private String userpwd = "0";
    private String username = "0";
    private String mysql_url = "https://credit.cjbdi.com/app/login";

    private Button btnName;   //用户名清空
    private Button btnPassword; //用户密码清空
    private Button btnLogin;    //登陆点击按钮
    private SharedPreferences sp;

    @SuppressLint("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //忽略证书问题
        handleSSLHandshake();

        sp = getSharedPreferences("accountInfo", Context.MODE_PRIVATE);
        sp.edit().putBoolean("ISCHECK", true).apply();
        //定位到用户和密码的输入框
        name_put_in = (EditText) findViewById(R.id.name_put_in);
        password_put_in = (EditText) findViewById(R.id.password_put_in);
        //记住用户名和密码
        if (sp.getBoolean("ISCHECK", false)) {

            try {
                username = sp.getString("ACCOUNTVALUE", "");
            } catch (Exception e) {

                // TODO: handle exception
            }
            name_put_in.setText(username);
            try {
                userpwd = sp.getString("PASSWORDVALUE", "");
            } catch (Exception e) {
                // TODO: handle exception
            }
            password_put_in.setText(userpwd);
        }
        //删除按钮点击事件
//        btnName = findViewById(R.id.btn_name);
//        btnName.setOnClickListener(this);
//        btnPassword = findViewById(R.id.btn_password);
//        btnPassword.setOnClickListener(this);
        //登陆按钮的点击事件
        btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                username = name_put_in.getText().toString();
                userpwd = password_put_in.getText().toString();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            onClickButtonLogin(username, userpwd);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                break;
            case R.id.btn_name:
                onClickButtonName(view);
            case R.id.btn_password:
                onClickButtonPassword(view);
            default:
                break;
        }
    }

    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

    //用户名文本框删除
    private void onClickButtonName(View view) {
//        Toast.makeText(MainActivity.this, "删除用户名!", Toast.LENGTH_SHORT).show();
        name_put_in.setText("");
    }

    //用户密码文本框删除
    private void onClickButtonPassword(View view) {
//        Toast.makeText(MainActivity.this, "删除密码!", Toast.LENGTH_SHORT).show();
        password_put_in.setText("");
    }

    //用户点击登录
    private void onClickButtonLogin(String username, String userpwd) throws IOException {

        String resultCode;
        String resultMsg;
        String resultExpire;
        String resultToken;

        JSONObject jsonObject = new JSONObject();
        if (username.length() == 0) {
            //这里加Looper的原因：程序在主线程中创建handler后会创建一个looper对象，而子线程却不会
            Looper.prepare();
            Toast toast = Toast.makeText(MainActivity.this, "用户名不能为空！", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 10);
            toast.show();
            Looper.loop();
        } else if (userpwd.length() == 0) {
            Looper.prepare();
            Toast toast = Toast.makeText(MainActivity.this, "用户密码不能为空", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 10);
            toast.show();
            Looper.loop();
        } else {
            try {
                jsonObject.put("userName", username);
                jsonObject.put("password", userpwd);
                String content = String.valueOf(jsonObject);
                //首先创建URL对象以及HttpURLConnection对象
                URL login_url = new URL(mysql_url);
                HttpURLConnection conn = (HttpURLConnection) login_url.openConnection();
                //设置请求方法
                conn.setRequestMethod("POST");
                //设置超时时间
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                //Post方式没法设置缓存，手动设置一下
                conn.setUseCaches(false);
                //设置请求类型
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");//使用的是json请求类型

                //写输出流，将要转的参数写入流
                conn.getOutputStream().write(content.getBytes());
                OutputStream os = conn.getOutputStream();
                os.write(content.getBytes());
                os.close();
                int code = conn.getResponseCode();
                if (code == 200) {
                    //发送成功，读取json数据
                    InputStream inputStream = conn.getInputStream();
                    //将数据输入流转化成String类型
                    String result = NetUtils.readString(inputStream);
                    JSONObject jsonObject1 = new JSONObject(result);
                    resultMsg = jsonObject1.getString("msg");
                    //登陆验证
                    if (resultMsg.equals("success")) {
                        //切换页面
                        //相当于是第一次点击登录状态,验证密码都通过了以后,这里把内容放进Share里
                        resultCode = jsonObject1.getString("code");
                        resultToken = jsonObject1.getString("token");
                        resultExpire = jsonObject1.getString("expire");

                        //获取登陆时间的时间戳，也就是token生成的时间，半小时后失效
                        Date date = new Date();
                        long t2 = date.getTime();
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("ACCOUNTVALUE", username);
                        editor.putString("PASSWORDVALUE", userpwd);
                        editor.putString("LOGINTOKEN", resultToken);
                        editor.putString("EXPIRE", resultExpire);
                        editor.putLong("CURRENTTIME",t2);
                        editor.apply();
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, PopWindowActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Looper.prepare();
                        Toast toast = Toast.makeText(MainActivity.this, R.string.user_check, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 10);
                        toast.show();
                        Looper.loop();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}