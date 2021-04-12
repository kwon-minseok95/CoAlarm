package com.corebank.Coalarm;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class RegisterActivityFree extends AppCompatActivity {
    // 마지막으로 뒤로 가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로 가기 버튼을 누를 때 표시
    private Toast toast;
    EditText userId, userPwd, userPwdChk, userPn, authText, terms, nickText;
    Button registerBtn, check_button, authBtn, authConfirm;
    boolean duplOk =  false;
    boolean authOk =  false;
    boolean confirmOk =  false;
    String title, msg, button;
    Spinner memberSpinner;
    CheckBox agreeCheck;
    TextView seeMore;
    JSONObject termsResult3;


    class AuthTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            Log.d("AuthTask : " ,"doInBackground 시작");
            try{
                String str;
                URL url = new URL("http://uitool.org:18080/coalarm/biz/android/AndroidPhoneAuth.jsp");
                Log.d("AuthTask", "conn시작");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Log.d("AuthTask", "conn : " + conn);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                if("authPhone".equals(strings[0])){
                    sendMsg = "userPhone=" + strings[1] + "&type=" + strings[0];
                } else if("authConfirm".equals(strings[0])){
                    sendMsg = "userPhone=" + strings[1] + "&authKey=" + strings[2] + "&type=" + strings[0] ;
                }
                Log.d("AuthTask", "sendMsg : " + sendMsg);

                osw.write(sendMsg);
                osw.flush();
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    Log.d("AuthTask", "통신 결과 : " + conn.getResponseCode());

                } else {
                    Log.d("통신 결과", conn.getResponseCode()+"에러");
                }
            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }

    public static String getLocale() {
        String LaunguageOfLocale = Locale.getDefault().getLanguage();
        return LaunguageOfLocale;
    }

    public void popup(String title, String msg, String button){

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivityFree.this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
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
        setContentView(R.layout.activity_register_free);
        Log.d("RegisterActivityFree : ", "onCreate시작");

//        userId = (EditText) findViewById(R.id.userID);
//        userPwd = (EditText) findViewById(R.id.userPassword);
//        userPwdChk = (EditText) findViewById(R.id.userPasswordChk);
        userPn = (EditText) findViewById(R.id.userPhone);
        // userEmail2 = (EditText) findViewById(R.id.userEmail);
        registerBtn = (Button) findViewById(R.id.register_button);
        //check_button = (Button) findViewById((R.id.check_button2));
        //memberSpinner = (Spinner) findViewById(R.id.memberSort);
        authBtn = (Button) findViewById(R.id.auth_button);
        authConfirm = (Button) findViewById(R.id.authConfirm);
        authText = (EditText) findViewById(R.id.authCode);
        agreeCheck = (CheckBox) findViewById(R.id.agreeCheck);
        seeMore = (TextView) findViewById(R.id.seeMore);
        terms = (EditText) findViewById(R.id.userID2);

        terms.setEnabled(false);

        try {
                String result = new CustomTask().execute("selectTerms", "2", "약관동의").get();

            JSONObject termsResult = new JSONObject(result);
            Log.d("이용약관 :", termsResult.toString());
            JSONArray termsResult2 = new JSONArray(termsResult.getString("selectResult"));
            Log.d("TL", termsResult2.get(0).toString());
            termsResult3 = new JSONObject(termsResult2.get(0).toString());
            terms.setText(termsResult3.getString("NOTE"));
            Log.d("hh", String.valueOf(terms.getText()));
        } catch (Exception e) {
            e.printStackTrace();
        }



        if(getLocale().equals("ko")){
            registerBtn.setBackgroundResource(R.drawable.kakao_signup_ko);
        }else{
            registerBtn.setBackgroundResource(R.drawable.kakao_signup_en);
        }

        //로그인 종류에 따라 화면 전환
        Intent intent2 = getIntent();
        String joinType = intent2.getStringExtra("joinType"); // 회원가입 타입(일반, 카카오)
        String kakaoID = intent2.getStringExtra("kakaoID");

        Log.v("회원가입 타입 : ", joinType);


//        Intent intent = getIntent();
//        String joinType = intent.getStringExtra("joinType");
//        String kakaoID = intent.getStringExtra("kakaoID");

        Log.d("알아보자", "joinType : " + joinType);

        if(getLocale().equals("ko")){
            registerBtn.setBackgroundResource(R.drawable.kakao_signup_ko);
        }else{
            registerBtn.setBackgroundResource(R.drawable.kakao_signup_en);
        }

        //핸드폰 인증
        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userPhone = userPn.getText().toString();
                if (userPhone.equals("")) {
                    if (getLocale().equals("ko")) {
                        title = "핸드폰 번호 인증";
                        msg = "핸드폰번호를 입력해주세요.";
                        button = "확인";
                    } else {
                        title = "Phone number authentication";
                        msg = "Please enter the phone number to use.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                }
                try {
                    String result = new AuthTask().execute("authPhone", userPhone).get();
                    Log.d("RegisterActivityFree(authPhone)", "authPhone 실행 완료 : " + result);
                    JSONObject authResult = new JSONObject(result);

                    // result = SUCCESS or FAIL
                    if ("SUCCESS".equals(authResult.getString("RESULT"))) {
                        if (getLocale().equals("ko")) {
                            title = "핸드폰 번호 인증";
                            msg = "입력하신 번호로 인증번호를 전송했습니다.";
                            button = "확인";
                        } else {
                            title = "Phone number authentication";
                            msg = "Please check your authentication code on your phone";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        authOk = true;
                    } else {
                        if (getLocale().equals("ko")) {
                            title = "핸드폰 번호 인증";
                            msg = "인증번호 전송 실패";
                            button = "확인";
                        } else {
                            title = "Phone number authentication";
                            msg = "Sending authentication code failed.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getLocale().equals("ko")) {
                        title = "핸드폰 번호 인증";
                        msg = "인증번호 전송 실패";
                        button = "확인";
                    } else {
                        title = "Phone number authentication";
                        msg = "Sending authentication code failed.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                }
            }
        });

        //인증번호 확인
        authConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userPhone = userPn.getText().toString();
                String authKey = authText.getText().toString();
                if ("".equals(authKey)) {
                    if (getLocale().equals("ko")) {
                        title = "핸드폰 번호 인증";
                        msg = "인증번호를 입력해주세요.";
                        button = "확인";
                    } else {
                        title = "Phone number authentication";
                        msg = "Please enter the Authentication key.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                }
                try {
                    String result = new AuthTask().execute("authConfirm", userPhone, authKey).get();
                    Log.d("RegisterActivity(authPhone)", "authPhone 실행 완료 : " + result);
                    JSONObject authResult = new JSONObject(result);

                    // result = SUCCESS or FAIL
                    if ("SUCCESS".equals(authResult.getString("RESULT"))) {
                        if (getLocale().equals("ko")) {
                            title = "핸드폰 번호 인증";
                            msg = "핸드폰 인증이 완료되었습니다.";
                            button = "확인";
                        } else {
                            title = "Phone number authentication";
                            msg = "Successed.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        confirmOk = true;

                    } else {
                        if (getLocale().equals("ko")) {
                            title = "핸드폰 번호 인증";
                            msg = "핸드폰 인증번호를 확인하세요.";
                            button = "확인";
                        } else {
                            title = "Phone number authentication";
                            msg = "Please check a authentication code.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getLocale().equals("ko")) {
                        title = "핸드폰 번호 인증";
                        msg = "핸드폰 인증번호를 확인하세요.";
                        button = "확인";
                    } else {
                        title = "Phone number authentication";
                        msg = "Please check a authentication code.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                }
            }
        });

        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLocale().equals("ko")) {
                    title = "이용약관";
                    try {
                        msg = termsResult3.getString("NOTE");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    button = "확인";
                } else {
                    title = "Terms and conditions";
                    try {
                        msg = termsResult3.getString("NOTE");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    button = "OK";
                }
                popup(title, msg, button);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!agreeCheck.isChecked()) {
                    if (getLocale().equals("ko")) {
                        title = "회원가입";
                        msg = "회원가입을 실패했습니다. 약관 확인해주세요.";
                        button = "확인";
                    } else {
                        title = "Register";
                        msg = "Registration failed. Please check the ID.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                } else if("".equals(userPn.getText().toString())){
                    if (getLocale().equals("ko")) {
                        title = "회원가입";
                        msg = "회원가입을 실패했습니다. 번호 확인해주세요.";
                        button = "확인";
                    } else {
                        title = "Register";
                        msg = "Registration failed. Please check the ID.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                } else if("".equals(authText.getText().toString())) {
                    if (getLocale().equals("ko")) {
                        title = "회원가입";
                        msg = "회원가입을 실패했습니다. 인증번호 입력해주세요.";
                        button = "확인";
                    } else {
                        title = "Register";
                        msg = "Registration failed. Please check the ID.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                } else if(!confirmOk) {
                    if (getLocale().equals("ko")) {
                        title = "회원가입";
                        msg = "회원가입을 실패했습니다. 인증번호 확인해주세요.";
                        button = "확인";
                    } else {
                        title = "Register";
                        msg = "Registration failed. Please check the ID.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                } else {
                    try {
                        String userPhone = userPn.getText().toString();
                        String result = new CustomTask().execute("kakaojoin", kakaoID, userPhone, "1").get();

                        Log.d("RegisterActivityFree(Kakao Sign up)", "Sign up 실행 완료 : " + result);
                        JSONObject kakaoSignUpResult = new JSONObject(result);
                        if ("FAIL".equals(kakaoSignUpResult.getString("result"))) {
                            if (getLocale().equals("ko")) {
                                title = "회원가입";
                                msg = "회원가입을 실패했습니다. 아이디를 확인해주세요.";
                                button = "확인";
                            } else {
                                title = "Register";
                                msg = "Registration failed. Please check the ID.";
                                button = "OK";
                            }
                            popup(title, msg, button);
                        } else {
                            if (getLocale().equals("ko")) {
                                msg = "회원가입을 환영합니다.";
                                button = "확인";
                            } else {
                                msg = "Thank you for your registration.";
                                button = "OK";
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivityFree.this);
                            builder.setMessage(msg);
                            builder.setPositiveButton(button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(RegisterActivityFree.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    }).setCancelable(false);
                            builder.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getLocale().equals("ko")) {
                            title = "회원가입";
                            msg = "회원가입을 실패했습니다. 아이디를 확인해주세요.";
                            button = "확인";
                        } else {
                            title = "Register";
                            msg = "Registration failed. Please check the ID.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // 기존 뒤로 가기 버튼의 기능을 막기 위해 주석 처리 또는 삭제

        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지났으면 Toast 출력
        // 2500 milliseconds = 2.5 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지나지 않았으면 종료
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            finishAffinity();
            System.runFinalization();
            System.exit(0);
            toast.cancel();
            toast = Toast.makeText(this,"이용해 주셔서 감사합니다.",Toast.LENGTH_LONG);
            toast.show();
        }
    }
}