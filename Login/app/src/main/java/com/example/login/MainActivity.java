package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText name_put_in; //用户名输入框
    private EditText password_put_in;  //用户密码输入框
    private String userpwd = "";
    private String username = "";
    private String mysql_url = "";
    private Button btn_name;   //用户名清空
    private Button btn_password; //用户密码清空
    private Button btn_login;    //登陆点击按钮
    private String auto = "0";
    //用于自动登录
    private SharedPreferences sp;

    //记住账号的标志常数
    private final String MAK = "innoview";

    public Map<String,String> map = new HashMap<>();  //为了写死几个账号密码

    @SuppressLint("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //更改状态栏样式
        super.onCreate(savedInstanceState);
//        Toast toast = Toast.makeText(MainActivity.this,"首次登陆", Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.CENTER, 0, 10);
//        toast.show();

        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("accountInfo", Context.MODE_PRIVATE);
        sp.edit().putBoolean("ISCHECK", true).apply();
//        Intent intent = getIntent();
//        //此时注销用户时传过来的零阻止了它进行自动登录
//        auto = intent.getStringExtra("auto");

        //写死两个用户
        map.put("111","222");
        map.put("123","222");
        //定位到用户和密码的输入框
        name_put_in = (EditText) findViewById(R.id.name_put_in);
        password_put_in = (EditText) findViewById(R.id.password_put_in);

        //记住用户名和密码
        if(sp.getBoolean("ISCHECK", false)){

            try{
                username = sp.getString("ACCOUNTVALUE","");
            }catch (Exception e) {
                // TODO: handle exception
            }
            name_put_in.setText(username);
            try{
                userpwd = sp.getString("PASSWORDVALUE","");
            }catch (Exception e) {
                // TODO: handle exception
            }
            password_put_in.setText(userpwd);
            //自动登录
        }


        //登陆按钮的点击事件
        btn_login = findViewById(R.id.btn_login);
        //##############登录按钮的点击事件开始
        btn_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //重新设置为自动登录
                auto = "1";
                //获取输入框内容
                username = name_put_in.getText().toString();
                userpwd = password_put_in.getText().toString();
                //登陆验证
                if(map.containsKey(username) && map.get(username).equals(userpwd)){
                    //切换页面
                    //相当于是第一次点击登录状态,验证密码都通过了以后,这里把内容放进Share里
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("ACCOUNTVALUE", username);
                    editor.putString("PASSWORDVALUE", userpwd);
                    editor.apply();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,PopWindowActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if(map.containsKey(username) && !map.get(username).equals(userpwd)){

                    //密码不对则弹框提示
                    Toast toast = Toast.makeText(MainActivity.this, R.string.check_pwd, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 10);
                    toast.show();
                }
                else {
                    Toast toast = Toast.makeText(MainActivity.this, R.string.user_check, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 10);
                    toast.show();
                }
            }
        });
        //##############登录按钮的点击事件结束
    }
    //登陆验证
    public void loginCheck(String username, String userpwd){
        if(map.containsKey(username) && map.get(username).equals(userpwd)){
            //切换页面
            Intent intent = new Intent();
            intent.setClass(MainActivity.this,PopWindowActivity.class);
            startActivity(intent);
            finish();
        }

    }

}
