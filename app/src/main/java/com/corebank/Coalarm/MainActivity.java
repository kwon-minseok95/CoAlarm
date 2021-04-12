package com.corebank.Coalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    String title, msg, button;
//하이루
    public static String getLocale() {
        String LaunguageOfLocale = Locale.getDefault().getLanguage();
        return LaunguageOfLocale;
    }

    // 키보드 창 화면 클릭시 사라짐
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                if (oAuthToken != null){
                    // TBD
                    updateKakaoLoginUi();
                }
                if (throwable != null){
                    // TBD
                }
                return null;
            }
        };

        if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)){
            UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this, callback);
        } else {
            UserApiClient.getInstance().loginWithKakaoAccount(MainActivity.this, callback);
        }
    }


    private void updateKakaoLoginUi(){
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if(user != null){
                    String kakaoID = Long.toString(user.getId());
                    // 카카오 아이디 있으면 바로 로그인 아니면 회원가입으로 넘어감
                    try {
                        String result = new CustomTask().execute("kakaoIDselect", kakaoID).get();
                        Log.d(TAG,"result : " + result);
                        JSONObject kakaoInfo = new JSONObject(result);

                        // 카카오 아이디로 로그인, 회원가입
                        if("N".equals(kakaoInfo.getString("SUCCESS"))){ // 카카오 ID 없음 -> 회원가입
                            if(getLocale().equals("ko")){
                                title = "로그인 확인" ;
                                msg = "가입되지 않은 회원입니다.\n" + "회원가입 페이지로 이동합니다.";
                                button = "확인";
                            }else{
                                title = "Login" ;
                                msg = "This is not a registered ID.\n" + "Please create a new account.";
                                button = "OK";
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(title);
                            builder.setMessage(msg);
                            builder.setCancelable(false);
                            builder.setPositiveButton(button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(MainActivity.this, RegisterActivityFree.class);
                                            String joinType = "kakao";
                                            String resultKakao = Long.toString(user.getId());
                                            intent.putExtra("joinType",joinType);
                                            intent.putExtra("kakaoID",resultKakao);
                                            startActivity(intent);
                                        }
                                    });
                            builder.show();
                        }else{ // 카카오 아이디 있음 -> 로그인
                            if(getLocale().equals("ko")){
                                title = "로그인 확인" ;
                                msg = "로그인되었습니다.";
                                button = "확인";
                            }else{
                                title = "Login" ;
                                msg = "Login Successed.";
                                button = "OK";
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(title);
                            builder.setMessage(msg);
                            builder.setCancelable(false);
                            builder.setPositiveButton(button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
                                                SharedPreference.setAttribute(getApplicationContext(), "userID", kakaoInfo.getString("USER_ID"));
                                                SharedPreference.setAttribute(getApplicationContext(), "password", kakaoInfo.getString("NICKNAME"));
                                                SharedPreference.setAttribute(getApplicationContext(), "userPhone", kakaoInfo.getString("PHONE"));
                                                startActivity(intent);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            builder.setCancelable(false);
                            builder.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }
                return null;
            }
        });
    }
}