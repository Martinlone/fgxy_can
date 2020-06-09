package com.example.login;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.dommy.qrcode.util.Constant;
import com.example.google.zxing.activity.CaptureActivity;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bouncycastle.util.encoders.Base64;
import javax.crypto.Cipher;

public class PopWindowActivity extends AppCompatActivity {
    private Button btn_pop;
    private PopupWindow mPop;
    public static final int REQUEST_CODE = 111;
    TextView tvResult; // 结果
    TextView tvscan;
    private Button btn_detail;
    //私玥
    public String privateKey = "MIICXQIBAAKBgQDlOJu6TyygqxfWT7eLtGDwajtNFOb9I5XRb6khyfD1Yt3YiCgQWMNW649887VGJiGr/L5i2osbl8C9+WJTeucF+S76xFxdU6jE0NQ+Z+zEdhUTooNRaY5nZiu5PgDB0ED/ZKBUSLKL7eibMxZtMlUDHjm4gwQco1KRMDSmXSMkDwIDAQABAoGAfY9LpnuWK5Bs50UVep5c93SJdUi82u7yMx4iHFMc/Z2hfenfYEzu+57fI4fvxTQ//5DbzRR/XKb8ulNv6+CHyPF31xk7YOBfkGI8qjLoq06V+FyBfDSwL8KbLyeHm7KUZnLNQbk8yGLzB3iYKkRHlmUanQGaNMIJziWOkN+N9dECQQD0ONYRNZeuM8zd8XJTSdcIX4a3gy3GGCJxOzv16XHxD03GW6UNLmfPwenKu+cdrQeaqEixrCejXdAFz/7+BSMpAkEA8EaSOeP5Xr3ZrbiKzi6TGMwHMvC7HdJxaBJbVRfApFrE0/mPwmP5rN7QwjrMY+0+AbXcm8mRQyQ1+IGEembsdwJBAN6az8Rv7QnD/YBvi52POIlRSSIMV7SwWvSK4WSMnGb1ZBbhgdg57DXaspcwHsFV7hByQ5BvMtIduHcT14ECfcECQATeaTgjFnqE/lQ22Rk0eGaYO80cc643BXVGafNfd9fcvwBMnk0iGX0XRsOozVt5AzilpsLBYuApa66NcVHJpCECQQDTjI2AQhFc1yRnCU/YgDnSpJVm1nASoRUnU8Jfm3Ozuku7JUXcVpt08DFSceCEX9unCuMcT72rAQlLpdZir876";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                TextView tvPlus1 = view.findViewById(R.id.tv_plus1);
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
                TextView tvPlus2 = view.findViewById(R.id.tv_plus2);
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
        btn_detail = findViewById(R.id.btn_detail);
        String stateColor = "";
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来
            try {
                //张三;110222199504212210;green;2020-06-05-14-30-07
                String scan_result =  decrypt(scanResult,privateKey);
                String[] splitResult = scan_result.split(";");
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
                //获取二维码生成时间、时间戳
                Date date1 = scanDate.parse(splitResult[3]); //固定格式的二维码生成时间
                assert date1 != null;
                long t1 = date1.getTime();
                System.out.println(t1);
                //获取当前时间
                Date date2 = new Date();
                String date3 = scanDate.format(date2);
                Date date4 = scanDate.parse(date3); //固定格式的当前时间
                assert date4 != null;
                long t2 = date4.getTime();
                System.out.println(t2);
                //设置最大时间间隔600000毫秒
                if(t2 - t1 > 600000){
                    Toast toast = Toast.makeText(PopWindowActivity.this, "二维码已过期，请重新扫描！", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 10);
                    toast.show();
                }else{
                    tvResult.setText(result);
                    btn_detail.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                Toast toast = Toast.makeText(PopWindowActivity.this, R.string.scan_fail, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 10);
                toast.show();
            }

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
        System.out.println(outStr);
        return outStr;
    }
}
