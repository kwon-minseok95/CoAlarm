package com.corebank.Coalarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.corebank.Coalarm.R;
import com.corebank.Coalarm.SharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ScheduleActivity extends AppCompatActivity {
    // ??????????????? ?????? ?????? ????????? ????????? ?????? ??????
    private long backKeyPressedTime = 0;
    // ??? ?????? ?????? ?????? ????????? ?????? ??? ??????
    private Toast toast;
    private static final String TAG = "ScheduleActivity";
    Button save_btn, add_btn, delete_btn, dayOK_btn, stopBtn, cancelBtn;
    EditText message, title, dateEnd, timeStart, dateStart, timeEnd, cycle, userPhone, userId, scenarioNum;
    TextView sPhone;
    TableLayout tableLayout1;
    String sch_key = "",  sUserId, sUserPhone;
    JSONObject joSendData;
    Toolbar myToolbar;

    ArrayList<String> standardMsg = new ArrayList<String>();

    // ??????
    String pTitle, msg, button, pMsg, pButton, pButton2;

    // ????????????
    Calendar sCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener myDatePicker1 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            sCalendar.set(Calendar.YEAR, year);
            sCalendar.set(Calendar.MONTH, month);
            sCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateStart();
        }
    };

    private int simple_spinner_dropdown_itemsimple_spinner_dropdown_item;

    // ??????
    public static String getLocale() {
        String LaunguageOfLocale = Locale.getDefault().getLanguage();
        return LaunguageOfLocale;
    }

    // ??????
    public void popup(String pTitle, String msg, String button){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ScheduleActivity.this);
        builder.setTitle(pTitle);
        builder.setMessage(msg);
        builder.setPositiveButton(button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    // ??????2 ?????? ??? ?????? refresh
    public void popup2(String pTitle, String msg, String button){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ScheduleActivity.this);
        builder.setTitle(pTitle);
        builder.setMessage(msg);
        builder.setPositiveButton(button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
        builder.show();
    }

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

    // ????????? ??? ?????? ????????? ?????????
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // ????????? ????????? UI
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        save_btn = (Button)findViewById(R.id.save_btn);
        message = (EditText)findViewById(R.id.message);
        dateStart = (EditText) findViewById(R.id.DateStart);
        timeStart = (EditText) findViewById(R.id.TimeStart);
        tableLayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        sPhone = (TextView) findViewById((R.id.sPhone));

        sPhone.setEnabled(false);

        // ?????????????????????
        StringBuilder  sbItemList = new StringBuilder();
        final ArrayList<String> itemList  = new ArrayList<String>();

        // ??????(??????)
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Log.d("???????????? : ", getLocale());

        sUserId = SharedPreference.getAttribute(getApplicationContext(), "userID"); // ?????? ?????????
        sUserPhone = SharedPreference.getAttribute(getApplicationContext(), "userPhone"); // ?????? ????????????
        Log.d("SharePre","sUserid : " + sUserId);
        Log.d("SharePre","sUserPhone : " + sUserPhone);

        class ScheduleTask extends AsyncTask<String, Void, String> {
            String sendMsg, receiveMsg;

            @Override
            protected String doInBackground(String... strings) {
                Log.d("ScheduleActivity : ", "doInBackground ??????");
                try {
                    String str;
                    URL url = new URL("http://uitool.org:18080/coalarm/biz/android/AndroidSchedule.jsp");
//                    URL url = new URL("http://..4.154:8088/coalarm/biz/android/AndroidSchedule.jsp");
                    Log.d("ScheduleActivity", "conn??????");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                    if (strings[0].equals("recentSelect")) { // ???????????? SELECT
                        sendMsg = "userID=" + strings[1] + "&type=" + strings[0];
                        Log.d("RecentlyScheduleActivity", "=================recentSelect=============>>>>> " + sendMsg);
                    } else if (strings[0].equals("ongoingSelect")) { // ????????? ??????
                        sendMsg = "userID=" + strings[1] +  "&type=" + strings[0];
                        Log.d("CheckScheduleActivity", "=================ongoingSelect=============>>>>> " + sendMsg);
                    } else if (strings[0].equals("stopUpdate")) { // ??????
                        sendMsg = "schedule_key=" + strings[1] + "&type=" + strings[0];
                        Log.d("CheckScheduleActivity", "=================stopUpdate=============>>>>> " + sendMsg);
                    } else if(strings[0].equals("saveSchedule")){
                        if(joSendData == null) {
                            // ????????? ???????????? ?????? ???
                            joSendData = new JSONObject();
                        }
                        joSendData.put("message", strings[1]);
                        joSendData.put("start_date", strings[2]);
                        joSendData.put("start_time", strings[3]);
                        joSendData.put("end_date", strings[2]);
                        joSendData.put("end_time", strings[3]);
                        joSendData.put("user_id", strings[4]);
                        sendMsg = "DateStart="+strings[2]+"&message="+strings[1]+"&TimeStart="+strings[3]+"&sUserId="+strings[4]+"&sUserPhone="+strings[5]+"&type="+strings[0]+"&joSendData="+joSendData.toString() ;
                        Log.d("saveSchedule", "=================sendMsg=============>>>>> " + sendMsg);
                    }

                    osw.write(sendMsg);
                    osw.flush();
                    if (conn.getResponseCode() == conn.HTTP_OK) {
                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                        BufferedReader reader = new BufferedReader(tmp);
                        StringBuffer buffer = new StringBuffer();
                        while ((str = reader.readLine()) != null) {
                            buffer.append(str);
                        }
                        receiveMsg = buffer.toString();
                        Log.d("ScheduleActivity", "?????? ?????? : " + conn.getResponseCode());
                    } else {
                        Log.d("?????? ??????", conn.getResponseCode() + "??????");
                    }

                    }catch(MalformedURLException e){
                        e.printStackTrace();
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    return receiveMsg;
                }
            }

        // ????????? ?????? string??? ??????
        try {
            String recentResult = new ScheduleTask().execute("recentSelect", sUserId).get(); // ?????? ?????????
            String ongoingResult = new ScheduleTask().execute("ongoingSelect", sUserId).get(); // ????????? ?????????

            Log.d("Normal", recentResult);
            Log.d("Normal", ongoingResult);

            JSONObject recentSelect = new JSONObject(recentResult);
            JSONObject ongoingSelect = new JSONObject(ongoingResult);

            //????????????
            if (recentSelect != null) {
                JSONArray jaRecent = (JSONArray) recentSelect.get("selectResult");

                Log.d(TAG, jaRecent.toString());
                JSONObject joRTemp;

                for (int i = 0; i < jaRecent.length(); i++) {
                    joRTemp = (JSONObject) jaRecent.get(i);

                    Log.d("recentSelect2 ??????2", String.valueOf(joRTemp));

                    TableRow tableRow = new TableRow(getApplicationContext()); //tablerow ??????

                    tableRow.setLayoutParams(new TableRow.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));

                    int rowHeight = 80;

                    TextView textView5 = new TextView(getApplicationContext());
                    String dateStr = joRTemp.getString("send_date") + joRTemp.getString("send_time");
                    if(!"".equals(dateStr)) {
                        SimpleDateFormat parseSdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        Date date = parseSdf.parse(dateStr);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        textView5.setText(sdf.format(date));
                    } else {
                        textView5.setText("");
                    }

                    textView5.setGravity(Gravity.CENTER);
                    textView5.setTextSize(18);
                    textView5.setMinHeight(100);


                    textView5.setBackgroundResource(R.drawable.schedule_border);
                    tableRow.addView(textView5, 520, rowHeight);
//                    tableRow.addView(textView5);

                    TextView textView7 = new TextView(getApplicationContext());
                    textView7.setText(joRTemp.getString("status_flg"));
                    textView7.setGravity(Gravity.CENTER);
                    textView7.setTextSize(18);

                    textView7.setBackgroundResource(R.drawable.schedule_border);
                    tableRow.addView(textView7, 300, rowHeight);
//                    tableRow.addView(textView7);

                    TextView textView2 = new TextView(getApplicationContext());
                    textView2.setText(joRTemp.getString("message"));
                    textView2.setGravity(Gravity.CENTER);
                    textView2.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    textView2.setPadding(10,0,0,0);
                    textView2.setTextSize(18);


                    textView2.setBackgroundResource(R.drawable.schedule_border);
                    tableRow.addView(textView2, ViewGroup.LayoutParams.WRAP_CONTENT, rowHeight);
//                    tableRow.addView(textView2);

                    tableLayout1.addView(tableRow);
                }

            }

            //???????????????
            if (ongoingSelect != null) {
                sch_key = "";
                joSendData = null;
                JSONArray jaIng = (JSONArray) ongoingSelect.get("selectResult");

                Log.d(TAG, jaIng.toString());
                JSONObject joATemp;

                if(jaIng.length() == 0) {
                    sPhone.setText(sUserPhone);
                }

                for (int i = 0; i < jaIng.length(); i++) {
                    joATemp = (JSONObject) jaIng.get(i);
                    joSendData = joATemp;

                    message.setText(joATemp.getString("message"));

                    if(!"".equals(joATemp.getString("start_date")) && joATemp.getString("start_date").length() > 10) {
                        dateStart.setText(joATemp.getString("start_date").substring(0,10).replaceAll("-","/"));
                    }

                    if(!"".equals(joATemp.getString("start_date")) && joATemp.getString("start_date").length() > 18) {
                        Log.d(TAG,"??????====11111===== : " + joATemp.getString("start_date").substring(11,19));
                        timeStart.setText(joATemp.getString("start_date").substring(11, joATemp.getString("start_date").length()));
                    }

                    sPhone.setText(joATemp.getString("phone_number"));
                    sch_key = joATemp.getString("schedule_key");

                    Log.d(TAG,"????????????============= : " + sPhone);

                }



            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ????????????
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if("".equals(sch_key)) {
                        if (getLocale().equals("ko")) {
                            msg = "???????????? ???????????? ????????????.";
                            button = "??????";
                        } else {
                            msg = "There is no schedule in progress.";
                            button = "OK";
                        }
                        popup(pTitle, msg, button);
                    }
                    String result = new ScheduleTask().execute("stopUpdate", sch_key).get();
                    JSONObject stopResult = new JSONObject(result);

                        if ("SUCCESS".equals(stopResult.getString("RESULT"))) {
                            if (getLocale().equals("ko")) {
                                msg = "???????????? ?????????????????????.";
                                button = "??????";
                            } else {
                                msg = "The schedule has been canceled.";
                                button = "OK";
                            }
                            popup2(pTitle, msg, button);

                        } else if ("FAIL".equals(stopResult.getString("RESULT"))) {
                            if (getLocale().equals("ko")) {
                                msg = "???????????? ???????????? ????????????.";
                                button = "??????";
                            } else {
                                msg = "There is no schedule in progress.";
                                button = "OK";
                            }
                            popup(pTitle, msg, button);
                        }


                    } catch (Exception e) {
                    e.printStackTrace();
                    if (getLocale().equals("ko")) {
                        msg = "????????? ????????? ????????? ??????????????????.";
                        button = "??????";
                    } else {
                        msg = "A schedule stop failed.";
                        button = "OK";
                    }
                }
            }
        });



        // ????????????
        dateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ScheduleActivity.this, myDatePicker1, sCalendar.get(Calendar.YEAR), sCalendar.get(Calendar.MONTH), sCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // ?????? ??????
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        timeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view = View.inflate(ScheduleActivity.this, R.layout.time_dialog, null);
                final NumberPicker numberPickerHour = view.findViewById(R.id.numpicker_hours);
                numberPickerHour.setMaxValue(23);
                numberPickerHour.setValue(sharedPreferences.getInt("Hours", 0));
                final NumberPicker numberPickerMinutes = view.findViewById(R.id.numpicker_minutes);
                numberPickerMinutes.setMaxValue(59);
                numberPickerMinutes.setValue(sharedPreferences.getInt("Minutes", 0));
                final NumberPicker numberPickerSeconds = view.findViewById(R.id.numpicker_seconds);
                numberPickerSeconds.setMaxValue(59);
                numberPickerSeconds.setValue(sharedPreferences.getInt("Seconds", 0));
                Button cancel = view.findViewById(R.id.cancel);
                Button ok = view.findViewById(R.id.ok);
                AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                builder.setView(view);
                final AlertDialog alertDialog = builder.create();
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG,"??????===== : "+ numberPickerHour.getValue());
                        Log.d(TAG,"???===== : "+ numberPickerMinutes.getValue());
                        Log.d(TAG,"???===== : "+ numberPickerSeconds.getValue());


//                        if(numberPickerHour.getValue() < 10) {
//                            timeStart.setText("0" + numberPickerHour.getValue());
//                        } else {
//                            timeStart.setText(numberPickerHour.getValue());
//                        }
//
//                        if(numberPickerHour.getValue() < 10) {
//
//                            if (numberPickerMinutes.getValue() < 10) {
//                                if (numberPickerSeconds.getValue() < 10) {
//                                    timeStart.setText("0" + numberPickerHour.getValue() + ":" + "0" + numberPickerMinutes.getValue() + ":" + "0" + numberPickerSeconds.getValue());
//                                } else{
//                                    timeStart.setText("0" + numberPickerHour.getValue() + ":" + "0" + numberPickerMinutes.getValue() + ":" + numberPickerSeconds.getValue());
//                                }
//                            } else{
//                                timeStart.setText("0" + numberPickerHour.getValue() + ":" + numberPickerMinutes.getValue() + ":" + numberPickerSeconds.getValue());
//                            }
//                        } else if(numberPickerHour.getValue() >= 10){
//                            if(numberPickerMinutes.getValue() < 10) {
//                                if(numberPickerSeconds.getValue() < 10){
//                                    timeStart.setText(numberPickerHour.getValue() + ":" + "0" + numberPickerMinutes.getValue() + ":" + "0" + numberPickerSeconds.getValue());
//                                } else{
//                                    timeStart.setText(numberPickerHour.getValue() + ":" + numberPickerMinutes.getValue() + ":" + "0" + numberPickerSeconds.getValue());
//                                }
//                            }
//                        }

                        timeStart.setText(leftPad(numberPickerHour.getValue(),2,"0") + ":" + leftPad(numberPickerMinutes.getValue(),2,"0") + ":" + leftPad(numberPickerSeconds.getValue(),2,"0"));

//                        timeTV.setText(String.format("%1$d:%2$02d:%3$02d", numberPickerHour.getValue(), numberPickerMinutes.getValue(), numberPickerSeconds.getValue()));
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("Hours", numberPickerHour.getValue());
                        editor.putInt("Minutes", numberPickerMinutes.getValue());
                        editor.putInt("Seconds", numberPickerSeconds.getValue());
                        editor.apply();
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });





        // ????????????
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.d("sbItemList : " , sbItemList);
                long mNow = System.currentTimeMillis();
                Date mReDate = new Date(mNow);
                SimpleDateFormat mFormat1 = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat mFormat2 = new SimpleDateFormat("HHmm");
                String formatDate = mFormat1.format(mReDate); // ??????
                String formatTime = mFormat2.format(mReDate); // ??????

                String Message = message.getText().toString(); // ?????????

                String DateStart = dateStart.getText().toString(); // ????????????
                DateStart = DateStart.replace("/", "");

                String sReceivePhone = sPhone.getText().toString();


                String TimeStart = timeStart.getText().toString(); // ????????????
                /*TimeStart = TimeStart.replace("??? ", "");
                TimeStart = TimeStart.replace("???", ""); //0416*/
                TimeStart = TimeStart.replace(":", "");



                if ("".equals(sReceivePhone)) {
                    Log.d(TAG, "sbItemList");
                    if (getLocale().equals("ko")) {
                        pTitle = "?????????";
                        msg = "?????? ?????? ???????????? ??????????????????.";
                        button = "??????";
                    } else {
                        pTitle = "Schedule";
                        msg = "Please enter the recipient's contact information.";
                        button = "OK";
                    }
                    popup(pTitle, msg, button);
                } else if ("??????".equals(DateStart) || "date".equals(DateStart)) {
                    if (getLocale().equals("ko")) {
                        pTitle = "?????????";
                        msg = "?????? ????????? ??????????????????.";
                        button = "??????";
                    } else {
                        pTitle = "Schedule";
                        msg = "Please specify a start date.";
                        button = "OK";
                    }
                    popup(pTitle, msg, button);
                } else if ("??????".equals(TimeStart) || "time".equals(TimeStart)) {
                    if (getLocale().equals("ko")) {
                        pTitle = "?????????";
                        msg = "?????? ????????? ??????????????????.";
                        button = "??????";
                    } else {
                        pTitle = "Schedule";
                        msg = "Please specify a start time.";
                        button = "OK";
                    }
                    popup(pTitle, msg, button);
                }

                try {
                    String result = new ScheduleTask().execute("saveSchedule", Message, DateStart, TimeStart, sUserId, sUserPhone, sReceivePhone).get();

                    if (getLocale().equals("ko")) {
                        pTitle = "?????????";
                        msg = "????????? ?????? ???????????????.";
                        button = "??????";
                    } else {
                        pTitle = "Schedule";
                        msg = "The schedule has been registered.";
                        button = "OK";
                    }
                    popup2(pTitle, msg, button);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void updateDateStart() {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);

        EditText dateStart = (EditText) findViewById(R.id.DateStart);
        dateStart.setText(sdf.format(sCalendar.getTime()));
    }

    private String leftPad(int strContext, int iLen, String strChar) {
        return leftPad(String.valueOf(strContext), iLen, strChar);
    }

    private String leftPad(String strContext, int iLen, String strChar) {
        String strResult = "";
        StringBuilder sbAddChar = new StringBuilder();
        for( int i = strContext.length(); i < iLen; i++ ) {
            // iLen?????? ?????? strChar????????? ?????????.
            sbAddChar.append( strChar );
        } strResult = sbAddChar + strContext; // LPAD?????????, ??????????????? + ?????????????????? Concate??????.
        return strResult;

    }

    //????????? ??????, ToolBar??? menu.xml??? ??????????????????
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    //????????? ??????, ToolBar??? ????????? ????????? select ???????????? ???????????? ??????
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.account_cancel:
                if (getLocale().equals("ko")) {
                    pTitle = "????????????";
                    pMsg = "?????? ?????????????????????????";
                    pButton = "???";
                    pButton2 = "?????????";
                } else {
                    pTitle = "Account Cancellation";
                    pMsg = "Are you sure you want to cancel your account?";
                    pButton = "YES";
                    pButton2 = "NO";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                builder.setTitle(pTitle);
                builder.setMessage(pMsg);

                //YES
                builder.setNegativeButton(pButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String result = new CustomTask().execute("deleteAccount", sUserId).get();
                                    JSONObject userInfo = new JSONObject(result);
                                    Log.d("????????????? : ",userInfo.getString("result"));
                                    //?????????
                                    if ("FAIL".equals(userInfo.getString("result"))) {
                                        if (getLocale().equals("ko")) {
                                            pTitle = "????????????";
                                            pMsg = "??????????????? ?????????????????????.";
                                            pButton = "??????";
                                        } else {
                                            pTitle = "Account Cancellation";
                                            pMsg = "Unable to cancel your account.";
                                            pButton = "OK";
                                        }
                                        popup(pTitle, pMsg, pButton);

                                    } else {
                                        if (getLocale().equals("ko")) {
                                            pTitle = "????????????";
                                            pMsg = "??????????????? ?????????????????????.";
                                            pButton = "??????";
                                        } else {
                                            pTitle = "Account Cancellation";
                                            pMsg = "Your account cancellation has been verified.";
                                            pButton = "OK";
                                        }

                                        AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                                        builder.setTitle(pTitle);
                                        builder.setMessage(pMsg);
                                        builder.setPositiveButton(pButton,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                                moveTaskToBack(true); // ???????????? ?????????????????? ??????
                                                                finishAndRemoveTask(); // ???????????? ?????? + ????????? ??????????????? ?????????
                                                                android.os.Process.killProcess(android.os.Process.myPid()); // ??? ???????????? ??????
                                                    }
                                                });
                                        builder.show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                //NO
                builder.setPositiveButton(pButton2,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();
                break;

            case R.id.allLogout:
                if (getLocale().equals("ko")) {
                    pTitle = "??? ??????";
                    pMsg = "?????? ?????????????????????????";
                    pButton = "???";
                    pButton2 = "?????????";
                } else {
                    pTitle = "Account Cancellation";
                    pMsg = "Are you sure you want to quit the app?";
                    pButton = "YES";
                    pButton2 = "NO";
                }

                AlertDialog.Builder builder2 = new AlertDialog.Builder(ScheduleActivity.this);
                builder2.setTitle(pTitle);
                builder2.setMessage(pMsg);

                //YES
                builder2.setNegativeButton(pButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                moveTaskToBack(true); // ???????????? ?????????????????? ??????
                                finishAndRemoveTask(); // ???????????? ?????? + ????????? ??????????????? ?????????
                                android.os.Process.killProcess(android.os.Process.myPid()); // ??? ???????????? ??????
                            }
                        });
                //NO
                builder2.setPositiveButton(pButton2,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder2.show();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // ?????? ?????? ?????? ????????? ????????? ?????? ?????? ?????? ?????? ?????? ??????

        // ??????????????? ?????? ?????? ????????? ????????? ????????? 2.5?????? ?????? ?????? ????????? ?????? ???
        // ??????????????? ?????? ?????? ????????? ????????? ????????? 2.5?????? ???????????? Toast ??????
        // 2500 milliseconds = 2.5 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "?????? ?????? ????????? ??? ??? ??? ???????????? ???????????????.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        // ??????????????? ?????? ?????? ????????? ????????? ????????? 2.5?????? ?????? ?????? ????????? ?????? ???
        // ??????????????? ?????? ?????? ????????? ????????? ????????? 2.5?????? ????????? ???????????? ??????
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            finishAffinity();
            System.runFinalization();
            System.exit(0);
            toast.cancel();
            toast = Toast.makeText(this,"????????? ????????? ???????????????.",Toast.LENGTH_LONG);
            toast.show();
        }
    }
}