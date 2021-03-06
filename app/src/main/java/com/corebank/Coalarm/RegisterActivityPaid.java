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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class RegisterActivityPaid extends AppCompatActivity {
    EditText userId, userPwd, userPwdChk, userPn, authText, birthText, nickText;
    Button registerBtn, check_button, authBtn, authConfirm;
    boolean duplOk =  false;
    boolean authOk =  false;
    boolean confirmOk =  false;
    String title, msg, button;
    Spinner memberSpinner;


    class AuthTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            Log.d("AuthTask : " ,"doInBackground ์์");
            try{
                String str;
                URL url = new URL("http://uitool.org:18080/coalarm/biz/android/AndroidPhoneAuth.jsp");
                Log.d("AuthTask", "conn์์");

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
                    Log.d("AuthTask", "ํต์? ๊ฒฐ๊ณผ : " + conn.getResponseCode());

                } else {
                    Log.d("ํต์? ๊ฒฐ๊ณผ", conn.getResponseCode()+"์๋ฌ");
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

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivityPaid.this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    // ํค๋ณด๋ ์ฐฝ ํ๋ฉด ํด๋ฆญ์ ์ฌ๋ผ์ง
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
        setContentView(R.layout.activity_register);
        Log.d("RegisterActivity2 : ", "onCreate์์");

        userId = (EditText) findViewById(R.id.userID);
        userPwd = (EditText) findViewById(R.id.userPassword);
        userPwdChk = (EditText) findViewById(R.id.userPasswordChk);
        userPn = (EditText) findViewById(R.id.userPhone);
        // userEmail2 = (EditText) findViewById(R.id.userEmail);
        registerBtn = (Button) findViewById(R.id.register_button);
        check_button = (Button) findViewById((R.id.check_button2));
        memberSpinner = (Spinner) findViewById(R.id.memberSort);
        authBtn = (Button) findViewById(R.id.auth_button);
        authConfirm = (Button) findViewById(R.id.authConfirm);
        authText = (EditText) findViewById(R.id.authCode);




        // ๊ตฌ๋ถ(์คํผ๋)
        ArrayAdapter arrAdapter = ArrayAdapter.createFromResource(this, R.array.membersort_array, android.R.layout.simple_spinner_dropdown_item);

        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memberSpinner.setEnabled(false);
        memberSpinner.setClickable(false);
        memberSpinner.setAdapter(arrAdapter);

        memberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)parent.getChildAt(0)).setTextColor(Color.GRAY);
            };
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //๋ก๊ทธ์ธ ์ข๋ฅ์ ๋ฐ๋ผ ํ๋ฉด ์?ํ
        Intent intent2 = getIntent();
        String joinType = intent2.getStringExtra("joinType"); // ํ์๊ฐ์ ํ์(์ผ๋ฐ, ์นด์นด์ค)
        String kakaoID = intent2.getStringExtra("kakaoID");

        Log.v("ํ์๊ฐ์ ํ์ : ",joinType);

        if(joinType.equals("origin")){
            //userEmail2.setVisibility(View.GONE);
            memberSpinner.setVisibility(View.GONE);
        }else if(joinType.equals("kakao")){
            userId.setVisibility(View.GONE);
            userPwd.setVisibility(View.GONE);
            userPwdChk.setVisibility(View.GONE);
            check_button.setVisibility(View.GONE);
        };

//        Intent intent = getIntent();
//        String joinType = intent.getStringExtra("joinType");
//        String kakaoID = intent.getStringExtra("kakaoID");

        Log.d("์์๋ณด์", "joinType : " + joinType);

        //ํธ๋ํฐ ์ธ์ฆ
        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userPhone = userPn.getText().toString();
                if (userPhone.equals("")) {
                    if (getLocale().equals("ko")) {
                        title = "ํธ๋ํฐ ๋ฒํธ ์ธ์ฆ";
                        msg = "ํธ๋ํฐ๋ฒํธ๋ฅผ ์๋?ฅํด์ฃผ์ธ์.";
                        button = "ํ์ธ";
                    } else {
                        title = "Phone number authentication";
                        msg = "Please enter the phone number to use.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                }
                try {
                    String result = new AuthTask().execute("authPhone", userPhone).get();
                    Log.d("RegisterActivity(authPhone)", "authPhone ์คํ ์๋ฃ : " + result);
                    JSONObject authResult = new JSONObject(result);

                    // result = SUCCESS or FAIL
                    if ("SUCCESS".equals(authResult.getString("RESULT"))) {
                        if(getLocale().equals("ko")){
                            title = "ํธ๋ํฐ ๋ฒํธ ์ธ์ฆ";
                            msg = "์๋?ฅํ์? ๋ฒํธ๋ก ์ธ์ฆ๋ฒํธ๋ฅผ ์?์กํ์ต๋๋ค.";
                            button = "ํ์ธ";
                        }else{
                            title = "Phone number authentication";
                            msg = "Please check your authentication code on your phone";
                            button = "OK";
                        }
                        popup(title, msg, button);
                    } else{
                        if(getLocale().equals("ko")){
                            title = "ํธ๋ํฐ ๋ฒํธ ์ธ์ฆ";
                            msg = "์ธ์ฆ๋ฒํธ ์?์ก ์คํจ";
                            button = "ํ์ธ";
                        }else{
                            title = "Phone number authentication";
                            msg = "Sending authentication code failed.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(getLocale().equals("ko")){
                        title = "ํธ๋ํฐ ๋ฒํธ ์ธ์ฆ";
                        msg = "์ธ์ฆ๋ฒํธ ์?์ก ์คํจ";
                        button = "ํ์ธ";
                    }else{
                        title = "Phone number authentication";
                        msg = "Sending authentication code failed.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                }
            }
        });

        //์ธ์ฆ๋ฒํธ ํ์ธ
        authConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userPhone = userPn.getText().toString();
                String authKey = authText.getText().toString();
                if ("".equals(authKey)) {
                    if (getLocale().equals("ko")) {
                        title = "ํธ๋ํฐ ๋ฒํธ ์ธ์ฆ";
                        msg = "์ธ์ฆ๋ฒํธ๋ฅผ ์๋?ฅํด์ฃผ์ธ์.";
                        button = "ํ์ธ";
                    } else {
                        title = "Phone number authentication";
                        msg = "Please enter the Authentication key.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                }
                try {
                    String result = new AuthTask().execute("authConfirm", userPhone, authKey).get();
                    Log.d("RegisterActivity(authPhone)", "authPhone ์คํ ์๋ฃ : " + result);
                    JSONObject authResult = new JSONObject(result);

                    // result = SUCCESS or FAIL
                    if ("SUCCESS".equals(authResult.getString("RESULT"))) {
                        if(getLocale().equals("ko")){
                            title = "ํธ๋ํฐ ๋ฒํธ ์ธ์ฆ";
                            msg = "ํธ๋ํฐ ์ธ์ฆ์ด ์๋ฃ๋์์ต๋๋ค.";
                            button = "ํ์ธ";
                        }else{
                            title = "Phone number authentication";
                            msg = "Successed.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        authOk = true;
                    } else{
                        if(getLocale().equals("ko")){
                            title = "ํธ๋ํฐ ๋ฒํธ ์ธ์ฆ";
                            msg = "ํธ๋ํฐ ์ธ์ฆ๋ฒํธ๋ฅผ ํ์ธํ์ธ์.";
                            button = "ํ์ธ";
                        }else{
                            title = "Phone number authentication";
                            msg = "Please check a authentication code.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if(getLocale().equals("ko")){
                        title = "ํธ๋ํฐ ๋ฒํธ ์ธ์ฆ";
                        msg = "ํธ๋ํฐ ์ธ์ฆ๋ฒํธ๋ฅผ ํ์ธํ์ธ์.";
                        button = "ํ์ธ";
                    }else{
                        title = "Phone number authentication";
                        msg = "Please check a authentication code.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                }
            }
        });


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = userId.getText().toString();
                String userPassword = userPwd.getText().toString();
                String userPasswordChk = userPwdChk.getText().toString();
                String userPhone = userPn.getText().toString();
                //  String userEmail = userEmail2.getText().toString();

                String memberType = memberSpinner.getSelectedItem().toString(); // ๊ตฌ๋ถ

                if (memberType.equals("๊ฐ์ธ")||memberType.equals("Individuals")) {
                    memberType = "1";
                } else if (memberType.equals("์?๋ฃ๊ฐ์ธ")||memberType.equals("Paid individuals")) {
                    memberType = "2";
                } else if (memberType.equals("๊ธฐ์")||memberType.equals("Enterprises")) {
                    memberType = "3";
                } else if (memberType.equals("์ง์")||memberType.equals("Employees")) {
                    memberType = "4";
                } else if (memberType.equals("๊ด๋ฆฌ์")||memberType.equals("Admin")) {
                    memberType = "9";
                }


                Log.d("registerBtn : ", "userID : " + userID + "userPassword : " + userPassword + "userPhone : " + userPhone);
                Log.d("๋๋ฐ์ด์ค ์ธ์ด : ", getLocale());
                Log.d("memberType : ", memberType);

                if(joinType.equals("origin")) { // ์ผ๋ฐ ํ์๊ฐ์

                    if (userID.equals("")) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "์์ด๋๋ฅผ ์๋?ฅํด์ฃผ์ธ์.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Please enter the ID to use.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    } else if (duplOk == false) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "์์ด๋ ์ค๋ณต๊ฒ์ฌ๋ฅผ ํด์ฃผ์ธ์.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Please check for duplicate IDs.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    } else if (authOk == false) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "ํธ๋ํฐ ์ธ์ฆ์ ํด์ฃผ์ธ์.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Please check phone number verification.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    } else if (userPassword.equals("")) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "๋น๋ฐ๋ฒํธ๋ฅผ ์๋?ฅํด์ฃผ์ธ์.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Please enter the password.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    } else if (userPasswordChk.equals("")) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "๋น๋ฐ๋ฒํธํ์ธ์ ์๋?ฅํ์ธ์.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Please confirm the password.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    } else if (userPhone.equals("")) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "ํธ๋ํฐ ๋ฒํธ๋ฅผ ์๋?ฅํ์ธ์.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Please enter the phone number.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    } else if (!userPassword.equals(userPasswordChk)) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "๋น๋ฐ๋ฒํธ๊ฐ ์ผ์นํ์ง ์์ต๋๋ค.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Passwords do not match. Please check the password.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    }
                } else {

                    if (authOk == false) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "ํธ๋ํฐ ์ธ์ฆ์ ํด์ฃผ์ธ์.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Please check phone number verification.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    } else if (userPhone.equals("")) {
                        if (getLocale().equals("ko")) {
                            title = "ํ์๊ฐ์";
                            msg = "ํธ๋ํฐ ๋ฒํธ๋ฅผ ์๋?ฅํ์ธ์.";
                            button = "ํ์ธ";
                        } else {
                            title = "Register";
                            msg = "Please enter the phone number.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                        return;
                    }
                }

                if(joinType.equals("origin")) { // ์ผ๋ฐ ํ์๊ฐ์
                    try {
                        String result = new CustomTask().execute("join", userID, userPassword, userPhone).get();
                        Log.d("RegisterActivity(Sign up)", "Sign up ์คํ ์๋ฃ : " + result);
                        JSONObject signUpResult = new JSONObject(result);

                        // result = success or fail
                        if ("SUCCESS".equals(signUpResult.getString("result"))) {

                            if(getLocale().equals("ko")){
                                msg = "ํ์๊ฐ์์ ํ์ํฉ๋๋ค.";
                                button = "ํ์ธ";
                            }else{
                                msg = "Thank you for your registration.";
                                button = "OK";
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivityPaid.this);
                            builder.setMessage(msg);
                            builder.setPositiveButton(button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(RegisterActivityPaid.this, ScheduleActivity.class);
                                            startActivity(intent);
                                        }
                                    }).setCancelable(false);
                            builder.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Log.d("RegisterActivity", "CustomTask ์คํ ์๋ฃ : " + result);
                        if(getLocale().equals("ko")){
                            title = "ํ์๊ฐ์" ;
                            msg = "ํ์๊ฐ์์ ์คํจํ์ต๋๋ค. ์์ด๋๋ฅผ ํ์ธํด์ฃผ์ธ์.";
                            button = "ํ์ธ";
                        }else{
                            title = "Register" ;
                            msg = "Registration failed. Please check the ID.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                    }
                } else if(joinType.equals("kakao")){ // ์นด์นด์ค ํ์๊ฐ์

                    try {
                        String result = new CustomTask().execute("kakaojoin", kakaoID, userPhone, memberType).get();

                        Log.d("RegisterActivity(Kakao Sign up)", "Sign up ์คํ ์๋ฃ : " + result);
                        JSONObject kakaoSignUpResult = new JSONObject(result);
                        if ("FAIL".equals(kakaoSignUpResult.getString("result"))) {
                            if(getLocale().equals("ko")){
                                title = "ํ์๊ฐ์" ;
                                msg = "ํ์๊ฐ์์ ์คํจํ์ต๋๋ค. ์์ด๋๋ฅผ ํ์ธํด์ฃผ์ธ์.";
                                button = "ํ์ธ";
                            }else{
                                title = "Register" ;
                                msg = "Registration failed. Please check the ID.";
                                button = "OK";
                            }
                            popup(title, msg, button);
                        } else {
                            if(getLocale().equals("ko")){
                                msg = "ํ์๊ฐ์์ ํ์ํฉ๋๋ค.";
                                button = "ํ์ธ";
                            }else{
                                msg = "Thank you for your registration.";
                                button = "OK";
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivityPaid.this);
                            builder.setMessage(msg);
                            builder.setPositiveButton(button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(RegisterActivityPaid.this, ScheduleActivity.class);
                                            startActivity(intent);
                                        }
                                    }).setCancelable(false);
                            builder.show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if(getLocale().equals("ko")){
                            title = "ํ์๊ฐ์" ;
                            msg = "ํ์๊ฐ์์ ์คํจํ์ต๋๋ค. ์์ด๋๋ฅผ ํ์ธํด์ฃผ์ธ์.";
                            button = "ํ์ธ";
                        }else{
                            title = "Register" ;
                            msg = "Registration failed. Please check the ID.";
                            button = "OK";
                        }
                        popup(title, msg, button);
                    }
                }
            }
        });

        //ID์ค๋ณต์ฒดํฌ
        check_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idId = userId.getText().toString();
                duplOk = true;
                //String idReg = "/^[a-z]+[a-z0-9]{5,19}$/g";
                if (idId.equals("")) {
                    Log.d("์ค๋ณต id", "์์ด๋์๋?ฅ์์");
                    if(getLocale().equals("ko")){
                        title = "ํ์๊ฐ์" ;
                        msg = "์์ด๋๋ฅผ ์๋?ฅํด์ฃผ์ธ์.";
                        button = "ํ์ธ";
                    }else{
                        title = "Register" ;
                        msg = "Please enter the ID to use.";
                        button = "OK";
                    }
                    popup(title, msg, button);
                } else {
                    try {
                        Log.d("์๋?ฅ์์ด๋", idId);
                        String result = new CustomTask().execute("idCheck", idId).get();
                        JSONObject checkid = new JSONObject(result);

                        Log.d("RegisterActivity", "CustomTask(Select) ์คํ ์๋ฃ : " + result);
                        //์ค๋ณต id ์์
                        if ("N".equals(checkid.getString("DUPLI_CHECK"))) {
                            if(getLocale().equals("ko")){
                                title = "ํ์๊ฐ์" ;
                                msg = "์ฌ์ฉํ? ์ ์๋ ์์ด๋์๋๋ค.";
                                button = "ํ์ธ";
                            }else{
                                title = "Register" ;
                                msg = "This ID can be used.";
                                button = "OK";
                            }
                            popup(title, msg, button);

                            //์ค๋ณต id ์์
                        } else if("Y".equals(checkid.getString("DUPLI_CHECK"))){
                            Log.d("์ค๋ณต id", "์์");
                            if(getLocale().equals("ko")){
                                title = "ํ์๊ฐ์" ;
                                msg = "์ด๋ฏธ ๋ฑ๋ก๋์ด ์๋ ์์ด๋์๋๋ค.";
                                button = "ํ์ธ";
                            }else{
                                title = "Register" ;
                                msg = "This ID is already registered.";
                                button = "OK";
                            }
                            popup(title, msg, button);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}