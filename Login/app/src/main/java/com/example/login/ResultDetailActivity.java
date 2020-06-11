package com.example.login;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;

public class ResultDetailActivity extends AppCompatActivity {

    public String privateKey = "MIICXQIBAAKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQABAoGAfY9LpnuWK5Bs50UVep5c93SJdUi82u7yMx4iHFMc/Z2hfenfYEzu+57fI4fvxTQ//5DbzRR/XKb8ulNv6+CHyPF31xk7YOBfkGI8qjLoq06V+FyBfDSwL8KbLyeHm7KUZnLNQbk8yGLzB3iYKkRHlmUanQGaNMIJziWOkN+N9dECQQD0ONYRNZeuM8zd8XJTSdcIX4a3gy3GGCJxOzv16XHxD03GW6UNLmfPwenKu+cdrQeaqEixrCejXdAFz/7+BSMpAkEA8EaSOeP5Xr3ZrbiKzi6TGMwHMvC7HdJxaBJbVRfApFrE0/mPwmP5rN7QwjrMY+0+AbXcm8mRQyQ1+IGEembsdwJBAN6az8Rv7QnD/YBvi52POIlRSSIMV7SwWvSK4WSMnGb1ZBbhgdg57DXaspcwHsFV7hByQ5BvMtIduHcT14ECfcECQATeaTgjFnqE/lQ22Rk0eGaYO80cc643BXVGafNfd9fcvwBMnk0iGX0XRsOozVt5AzilpsLBYuApa66NcVHJpCECQQDTjI2AQhFc1yRnCU/YgDnSpJVm1nASoRUnU8Jfm3Ozuku7JUXcVpt08DFSceCEX9unCuMcT72rAQlLpdZir876";
    private TextView tvResult; // 结果
    private TextView tvResult1;
    private TextView resultName;  //显示名字的文本框
    private TextView resultState; //显示状态的文本框
    private TextView resultTime; //显示二维码生成时间的文本框 之后会改成有效时间
    private Button btn_detail; //查看详情
    private String detailApi = "";
    private String detail_url = "https://credit.cjbdi.com/app/getUserInfo?cardid=";//详情请求接口
    private URL login_url = null;
    private ImageButton iBtn_rback;  //扫描结果返回的按钮
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);
        iBtn_rback = findViewById(R.id.ibtn_resultBack);
        iBtn_rback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //接收传递过来的参数
        Intent intent = getIntent();
        String scanResult = intent.getStringExtra("scanResult");
        String stateColor = "";
        //将扫描出的信息显示出来
        try {
            //张三;110222199504212210;green;2020-06-05-14-30-07
            String scan_result =  decrypt(scanResult,privateKey);
            final String[] splitResult = scan_result.split(";");
            switch (splitResult[2]){
                case "green":
                    stateColor = "绿色";
                    break;
                case "yellow":
                    stateColor = "黄色";
                    break;
                case "red":
                    stateColor = "红色";
                    break;
                default:
                    break;
            }
            String result = "姓名: " + splitResult[0] + System.getProperty ("line.separator") + "身份证号: " + splitResult[1] + System.getProperty ("line.separator") + "状态: " + stateColor + System.getProperty ("line.separator") + "时间: " + splitResult[3].substring(0,10);
            //为了使用高版本的方法需要这个装饰器
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat scanDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

            //获取二维码生成时间
            Date date1 = scanDate.parse(splitResult[3]); //固定格式的二维码生成时间
            assert date1 != null;
            long t1 = date1.getTime();

            //获取过期时间
            long tcExpire = t1 + 600000;
            @SuppressLint("SimpleDateFormat")
            String tExpire = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tcExpire);

            //获取当前时间
            Date date2 = new Date();
            String timestamp = String.valueOf(date2.getTime());
            long t2 = Long.parseLong(timestamp);
//            Date date2 = new Date();
//            String date3 = scanDate.format(date2);
//            Date date4 = scanDate.parse(date3); //固定格式的当前时间
//            assert date4 != null;
//            long t2 = date4.getTime();   //当前时间的时间戳
//                System.out.println("当前时间"+ t2);
//                System.out.println("二维码生成时间"+ t1);
            //设置最大时间间隔600000毫秒
            if(t2 - t1 > 600000000){
                Toast toast = Toast.makeText(ResultDetailActivity.this, "二维码已过期，请重新扫描！", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 10);
                toast.show();
            }else{
                resultName = findViewById(R.id.txt_resultName);
                String nameBox = "姓名: " + splitResult[0];
                resultName.setText(nameBox);
                resultState = findViewById(R.id.txt_resultState);
                String stateBox = "状态: " + stateColor;
                resultState.setText(stateBox);
                resultTime = findViewById(R.id.txt_resultTime);
                String timeBox = "二维码有效时间: " + System.getProperty ("line.separator") + tExpire;
                resultTime.setText(timeBox);

//                tvResult = findViewById(R.id.txt_result);
//                tvResult.setText(result);
                btn_detail = findViewById(R.id.btn_detail);
                btn_detail.setVisibility(View.VISIBLE);
                btn_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("accountInfo",MainActivity.MODE_PRIVATE);
                String token = sharedPreferences.getString("LOGINTOKEN","");
                String expireTime = sharedPreferences.getString("EXPIRE","1800"); //token过期时间30分钟
                long exTime = Long.parseLong(expireTime);

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat scanDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                Date date = new Date();
                long t5 = date.getTime();   //当前时间的时间戳
                long t3 = sharedPreferences.getLong("CURRENTTIME",0); //这是生成token的时间戳
                if(t5 - t3 > exTime*1000){
                    Toast toast = Toast.makeText(ResultDetailActivity.this, "身份认证到期，请重新登陆！", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 10);
                    toast.show();
                } else{
                    //首先创建URL对象以及HttpURLConnection对象
//                                String detailApi = detail_url + splitResult[1] + "&token=" + token;
//                                detailApi = detail_url + "320924196908200013" + "&token=" + token;
//                                detailApi = detail_url + "342425198807270833" + "&token=" + token;
//                    detailApi = detail_url + "ivXPH3FeTTZK8xNO36xWhI0GxTUahjmr" + "&token=" + token;
                    detailApi = detail_url + "Z1093jJLxDM8k6BuTttYo4rbr8yPew9B" + "&token=" + token;
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            getDetail();
                        }
                    });

                }
            }
        });
    }

        } catch (Exception e) {
            System.out.println(e);
            Toast toast = Toast.makeText(ResultDetailActivity.this, R.string.scan_fail, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 10);
            toast.show();
        }

}
    //另开子线程来处理detail网络请求线程
    private void getDetail(){
        try {
            String allDetail = "";
            login_url = new URL(detailApi);
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
            int code = conn.getResponseCode();
            if (code == 200){
                //发送成功，读取json数据
                InputStream inputStream = conn.getInputStream();
                //将数据输入流转化成String类型
                try{
                    String result = NetUtils.readString(inputStream);
                    JSONObject jsonObject1 = new JSONObject(result);
                    System.out.println(jsonObject1.toString());
                    String resultMsg = jsonObject1.getString("msg");
                    if(resultMsg.equals("success")){
                        JSONObject data = jsonObject1.getJSONObject("data");
                        //注意：data中的内容带有中括号[]，所以要转化为JSONArray类型的对象
                        JSONArray xsayfb;
                        JSONArray xzayfb;
                        try{
                            xsayfb = data.getJSONArray("xsayfb");
                            for (int i = 0; i < xsayfb.length(); i++) {
                                allDetail += "刑事案由分布: " + System.getProperty("line.separator") + "   " + xsayfb.getJSONObject(i).getString("xsay") + System.getProperty("line.separator");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try{
                            xzayfb = data.getJSONArray("xzayfb");
                            for (int j = 0; j < xzayfb.length(); j++) {
                                allDetail = allDetail  + "行政案由分布: " + System.getProperty ("line.separator") + "   " + xzayfb.getJSONObject(j).getString("xzay") + System.getProperty ("line.separator");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(!allDetail.equals("")){
                            try{
                                tvResult1 = findViewById(R.id.txt_detail);
                                tvResult1.setText(allDetail);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else{
                            Looper.prepare();
                            Toast toast = Toast.makeText(ResultDetailActivity.this, "未查到详细信息！", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 10);
                            toast.show();
                            Looper.loop();
                        }

                    }else{
                        Toast toast = Toast.makeText(ResultDetailActivity.this, "未能查到详细信息！", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 10);
                        toast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // #################################################################################################
    //RSA解密
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String decrypt(String str, String privateKey) throws Exception{
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );
        byte[] decodeKey = Base64.decode(privateKey);;
        RSAPrivateKey privateKey2 = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodeKey));
        byte[] inputByte = Base64.decode(str.getBytes("UTF-8"));
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey2);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }


}
