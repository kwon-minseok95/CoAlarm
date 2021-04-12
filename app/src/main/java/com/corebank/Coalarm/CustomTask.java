package com.corebank.Coalarm;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CustomTask extends AsyncTask<String, Void, String> {
    String sendMsg, receiveMsg;

    @Override
    protected String doInBackground(String... strings) {
        Log.d("CustomTask : " ,"doInBackground 시작");
        try{
            String str;
//            URL url = new URL("http://223.223.4.154:8088/coalarm/biz/android/AndroidConnect.jsp");
            URL url = new URL("http://uitool.org:18080/coalarm/biz/android/AndroidConnect.jsp");
            Log.d("CustomTask", "conn시작");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.d("CustomTask", "conn : " + conn);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

            if ("kakaoIDselect".equals(strings[0])) {
                sendMsg = "kakaoID=" + strings[1] + "&type=" + strings[0];
            } else if ("idCheck".equals(strings[0])) {
                sendMsg = "userID=" + strings[1] + "&type=" + strings[0];
            } else if("join".equals(strings[0])){
                sendMsg = "userID=" + strings[1] + "&userPassword=" + strings[2] + "&userPhone=" + strings[3] + "&type=" + strings[0];
            } else if("loginSuccess".equals(strings[0])){
                sendMsg = "userID=" + strings[1] + "&userPassword=" + strings[2] + "&type=" + strings[0];
            } else if("kakaojoin".equals(strings[0])) {
                sendMsg = "kakaoID=" + strings[1] + "&userPhone=" + strings[2] + "&authCd=" + strings[3]+ "&type=" + strings[0];
            } else if("modifyInfo".equals(strings[0])){
                sendMsg = "userID=" + strings[1] + "&userPassword=" + strings[2] + "&userPhone=" + strings[3]+ "&type=" + strings[0];
            } else if("deleteAccount".equals(strings[0])){
                sendMsg = "userID=" + strings[1] + "&type=" + strings[0];
            } else if("smsCertification".equals(strings[0])) {
                sendMsg = "userPhone=" + strings[1] + "&type=" + strings[0];
            } else if("selectTerms".equals(strings[0])){
                sendMsg =  "type=" + strings[0] +"&gubun=" + strings[1] + "&title=" + strings[2];
            }

            Log.d("CustomTask", "sendMsg : " + sendMsg);

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
                Log.d("CustomTask", "통신 결과 : " + conn.getResponseCode());

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