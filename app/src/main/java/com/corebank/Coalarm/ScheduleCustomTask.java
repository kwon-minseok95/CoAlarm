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

public class ScheduleCustomTask extends AsyncTask<String, Void, String> {
    String sendMsg, receiveMsg;

    @Override
    protected String doInBackground(String... strings) {
        Log.d("ScheduleCustomTask : " ,"doInBackground 시작");
        try{
            String str;
            URL url = new URL("http://uitool.org:18080/coalarm/biz/android/AndroidSchedule.jsp");

            Log.d("ScheduleCustomTask", "conn시작");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.d("ScheduleCustomTask", "conn : " + conn);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

            if ("stdMsgSelect".equals(strings[0])) {
                sendMsg = "sUserId=" + strings[1] + "&type=" + strings[0];
            } else if ("stdMsgInsert".equals(strings[0])) {
                sendMsg = "sUserId=" + strings[1] + "&msgCodeTxtStr=" + strings[2] + "&msgFormatTxtStr=" + strings[3] + "&use_YnStr=" + strings[4] + "&type=" + strings[0];
            } else if ("stdMsgDelete".equals(strings[0])) {
                sendMsg = "sUserId=" + strings[1] + "&msgCodeTxtStr=" + strings[2] + "&type=" + strings[0];
            } else if ("stdMsgUpdate".equals(strings[0])) {
                sendMsg = "sUserId=" + strings[1] + "&msgCodeTxtStr=" + strings[2] + "&msgFormatTxtStr=" + strings[3] + "&use_YnStr=" + strings[4] + "&type=" + strings[0];
            }

            Log.d("ScheduleCustomTask", "sendMsg : " + sendMsg);

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
                Log.d("ScheduleCustomTask", "통신 결과 : " + conn.getResponseCode());

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
