package com.example.login;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.dommy.qrcode.util.Constant;
import com.example.google.zxing.activity.CaptureActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Handler;

import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
//这里可能还没有解决


public class PopWindowActivity extends AppCompatActivity {
    private Button btn_pop;
    private PopupWindow mPop;
    public static final int REQUEST_CODE = 111;
    private TextView tvResult; // 结果
    private TextView tvResult1;
    private URL login_url = null;
    private String detailApi = "";
    private TextView tvscan;
    private Button btn_detail; //查看详情
    private String detail_url = "https://credit.cjbdi.com/app/getUserInfo?cardid=";//详情请求接口
    //私玥
    public String privateKey = "MIICXQIBAAKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQABAoGAfY9LpnuWK5Bs50UVep5c93SJdUi82u7yMx4iHFMc/Z2hfenfYEzu+57fI4fvxTQ//5DbzRR/XKb8ulNv6+CHyPF31xk7YOBfkGI8qjLoq06V+FyBfDSwL8KbLyeHm7KUZnLNQbk8yGLzB3iYKkRHlmUanQGaNMIJziWOkN+N9dECQQD0ONYRNZeuM8zd8XJTSdcIX4a3gy3GGCJxOzv16XHxD03GW6UNLmfPwenKu+cdrQeaqEixrCejXdAFz/7+BSMpAkEA8EaSOeP5Xr3ZrbiKzi6TGMwHMvC7HdJxaBJbVRfApFrE0/mPwmP5rN7QwjrMY+0+AbXcm8mRQyQ1+IGEembsdwJBAN6az8Rv7QnD/YBvi52POIlRSSIMV7SwWvSK4WSMnGb1ZBbhgdg57DXaspcwHsFV7hByQ5BvMtIduHcT14ECfcECQATeaTgjFnqE/lQ22Rk0eGaYO80cc643BXVGafNfd9fcvwBMnk0iGX0XRsOozVt5AzilpsLBYuApa66NcVHJpCECQQDTjI2AQhFc1yRnCU/YgDnSpJVm1nASoRUnU8Jfm3Ozuku7JUXcVpt08DFSceCEX9unCuMcT72rAQlLpdZir876";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //下次想新开新的线程处理主线程里面的ui
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //
        setContentView(R.layout.activity_pop_window);
        btn_pop = (Button) findViewById(R.id.btn_pop);
        btn_pop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //对于一个没有被载入或者想要动态载入的界面，都需要使用LayoutInflater.inflate()来载入；
                View view = getLayoutInflater().inflate(R.layout.activity_pop,null);
                //设置这个view得出现位置 大小等信息
                mPop = new PopupWindow(view,390, ViewGroup.LayoutParams.WRAP_CONTENT);
                mPop.setOutsideTouchable(true);
                mPop.setFocusable(true);
                mPop.showAsDropDown(btn_pop);

                //#######扫一扫的TextView点击事件
                TextView tvPlus1 = view.findViewById(R.id.tv_scan_new);
                //获取资源图片
                Drawable leftDrawable1 = getResources().getDrawable(R.drawable.scan);
                //设置图片的尺寸，奇数位置后减前得到宽度，偶数位置后减前得到高度。
                leftDrawable1.setBounds(10, 0, 80, 70);
                //设置图片在TextView中的位置
                tvPlus1.setCompoundDrawables(leftDrawable1, null, null, null);
                tvPlus1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //扫一扫的点击事件
                        tvResult = (TextView) findViewById(R.id.txt_result);
                        mPop.dismiss();
                        startQrCode();
                    }
                });
                //########退出登录的TextView点击事件
                TextView tvPlus2 = view.findViewById(R.id.tv_logout);
                //获取资源图片
                Drawable leftDrawable2 = getResources().getDrawable(R.drawable.logout);
                //设置图片的尺寸，奇数位置后减前得到宽度，偶数位置后减前得到高度。
                leftDrawable2.setBounds(10, 0, 80, 70);
                //设置图片在TextView中的位置
                tvPlus2.setCompoundDrawables(leftDrawable2, null, null, null);
                tvPlus2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPop.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(PopWindowActivity.this,MainActivity.class);
                        intent.putExtra("auto", "0");
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    private void startQrCode() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(PopWindowActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
            }
            // 申请权限
            ActivityCompat.requestPermissions(PopWindowActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(PopWindowActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(PopWindowActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // 扫描结果呈现
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //查看详情点击事件的触发

        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);

            //跳转到结果展示界面
            Intent intent = new Intent();
            intent.setClass(PopWindowActivity.this,ResultDetailActivity.class);
            intent.putExtra("scanResult", scanResult);
            startActivity(intent);
            finish();
        }
    }
}
