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
import android.nfc.Tag;
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
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class ScheduleActivity extends AppCompatActivity {
    // 마지막으로 뒤로 가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로 가기 버튼을 누를 때 표시
    private Toast toast;
    private static final String TAG = "ScheduleActivity";
    Button save_btn, add_btn, delete_btn, dayOK_btn, stopBtn, cancelBtn, deleteMessage;
    EditText message, title, dateEnd, timeStart, dateStart, timeEnd, cycle, userPhone, userId, scenarioNum;
    TextView sPhone;
    TableLayout tableLayout1;
    String sch_key = "",  sUserId, sUserPhone;
    JSONObject joSendData;
    Toolbar myToolbar;



    ArrayList<String> standardMsg = new ArrayList<String>();

    // 팝업
    String pTitle, msg, button, pMsg, pButton, pButton2;

    // 시작날짜
    Calendar sCalendar = Calendar.getInstance();
    Calendar cal = Calendar.getInstance();
    Calendar now = Calendar.getInstance();
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

    // 언어
    public static String getLocale() {
        String LaunguageOfLocale = Locale.getDefault().getLanguage();
        return LaunguageOfLocale;
    }

    // 팝업
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

    // 팝업2 확인 후 화면 refresh
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

    // 화면 클릭하면 키보드 창 사라짐
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
        setContentView(R.layout.activity_schedule);

        // 키보드 사용시 UI
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        save_btn = (Button) findViewById(R.id.save_btn);
        message = (EditText) findViewById(R.id.message);
        dateStart = (EditText) findViewById(R.id.DateStart);
        timeStart = (EditText) findViewById(R.id.TimeStart);
        tableLayout1 = (TableLayout) findViewById(R.id.tableLayout1);
        sPhone = (TextView) findViewById((R.id.sPhone));
        deleteMessage = (Button) findViewById(R.id.delete_message);

        sPhone.setEnabled(false);

        // 받는사람리스트
        StringBuilder sbItemList = new StringBuilder();
        final ArrayList<String> itemList = new ArrayList<String>();

        // 툴바(메뉴)
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        Log.d("디바이스 : ", getLocale());

        sUserId = SharedPreference.getAttribute(getApplicationContext(), "userID"); // 세션 아이디
        sUserPhone = SharedPreference.getAttribute(getApplicationContext(), "userPhone"); // 세션 전화번호
        Log.d("SharePre", "sUserid : " + sUserId);
        Log.d("SharePre", "sUserPhone : " + sUserPhone);

        class ScheduleTask extends AsyncTask<String, Void, String> {
            String sendMsg, receiveMsg;

            @Override
            protected String doInBackground(String... strings) {
                Log.d("ScheduleActivity : ", "doInBackground 시작");
                try {
                    String str;
                    URL url = new URL("http://uitool.org:18080/coalarm/biz/android/AndroidSchedule.jsp");
                    //URL url = new URL("http://223.223.4.31:8088/coalarm/biz/android/AndroidSchedule.jsp");
                    Log.d("ScheduleActivity", "conn시작");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                    if (strings[0].equals("recentSelect")) { // 최신내역 SELECT
                        sendMsg = "userID=" + strings[1] + "&type=" + strings[0];
                        Log.d("RecentlyScheduleActivity", "=================recentSelect=============>>>>> " + sendMsg);
                    } else if (strings[0].equals("ongoingSelect")) { // 진행중 내역
                        sendMsg = "userID=" + strings[1] + "&type=" + strings[0];
                        Log.d("CheckScheduleActivity", "=================ongoingSelect=============>>>>> " + sendMsg);
                    } else if (strings[0].equals("stopUpdate")) { // 중지
                        sendMsg = "schedule_key=" + strings[1] + "&type=" + strings[0];
                        Log.d("CheckScheduleActivity", "=================stopUpdate=============>>>>> " + sendMsg);
                    } else if (strings[0].equals("saveSchedule")) {
                        if (joSendData == null) {
                            // 조회된 데이터가 없을 때
                            joSendData = new JSONObject();
                        }
                        joSendData.put("message", strings[1]);
                        joSendData.put("start_date", strings[2]);
                        joSendData.put("start_time", strings[3]);
                        joSendData.put("end_date", strings[2]);
                        joSendData.put("end_time", strings[3]);
                        joSendData.put("user_id", strings[4]);
                        sendMsg = "DateStart=" + strings[2] + "&message=" + strings[1] + "&TimeStart=" + strings[3] + "&sUserId=" + strings[4] + "&sUserPhone=" + strings[5] + "&type=" + strings[0] + "&joSendData=" + joSendData.toString();
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
                        Log.d("ScheduleActivity", "통신 결과 : " + conn.getResponseCode());
                    } else {
                        Log.d("통신 결과", conn.getResponseCode() + "에러");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return receiveMsg;
            }
        }

        // 스케줄 내역 string에 저장
        try {
            String recentResult = new ScheduleTask().execute("recentSelect", sUserId).get(); // 최근 스케줄
            String ongoingResult = new ScheduleTask().execute("ongoingSelect", sUserId).get(); // 진행중 스케줄

            Log.d("Normal", recentResult);
            Log.d("Normal", ongoingResult);

            JSONObject recentSelect = new JSONObject(recentResult);
            JSONObject ongoingSelect = new JSONObject(ongoingResult);

            //최근내역
            if (recentSelect != null) {
                JSONArray jaRecent = (JSONArray) recentSelect.get("selectResult");

                Log.d(TAG, jaRecent.toString());
                JSONObject joRTemp;

                for (int i = 0; i < jaRecent.length(); i++) {
                    joRTemp = (JSONObject) jaRecent.get(i);

                    Log.d("recentSelect2 조회2", String.valueOf(joRTemp));

                    TableRow tableRow = new TableRow(getApplicationContext()); //tablerow 생성

                    tableRow.setLayoutParams(new TableRow.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    ));

                    int rowHeight = 80;

                    TextView textView5 = new TextView(getApplicationContext());
                    String dateStr = joRTemp.getString("send_date") + joRTemp.getString("send_time");
                    if (!"".equals(dateStr)) {
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
                    textView2.setPadding(10, 0, 0, 0);
                    textView2.setTextSize(18);

                    textView2.setBackgroundResource(R.drawable.schedule_border);
                    tableRow.addView(textView2, ViewGroup.LayoutParams.WRAP_CONTENT, rowHeight);
//                    tableRow.addView(textView2);

                    tableLayout1.addView(tableRow);
                }
            }

            //진행중내역
            if (ongoingSelect != null) {
                sch_key = "";
                joSendData = null;
                JSONArray jaIng = (JSONArray) ongoingSelect.get("selectResult");

                Log.d(TAG, jaIng.toString());
                JSONObject joATemp;

                if (jaIng.length() == 0) {
                    sPhone.setText(sUserPhone);
                }

                for (int i = 0; i < jaIng.length(); i++) {
                    joATemp = (JSONObject) jaIng.get(i);
                    joSendData = joATemp;

                    // 화면 값
                    message.setText(joATemp.getString("message"));

                    if (!"".equals(joATemp.getString("start_date")) && joATemp.getString("start_date").length() > 10) {
                        dateStart.setText(joATemp.getString("start_date").substring(0, 10).replaceAll("-", "/"));
                    }

                    if (!"".equals(joATemp.getString("start_date")) && joATemp.getString("start_date").length() > 18) {
                        Log.d(TAG, "시간====11111===== : " + joATemp.getString("start_date").substring(11, 19));
                        timeStart.setText(joATemp.getString("start_date").substring(11, joATemp.getString("start_date").length()));
                    }

                    sPhone.setText(joATemp.getString("phone_number"));
                    sch_key = joATemp.getString("schedule_key");

                    Log.d(TAG, "전화번호============= : " + sPhone);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 메세지 내용 삭제 버튼
        deleteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.setText(null);
            }
        });

        // 취소버튼
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        if ("".equals(sch_key)) { // 등록된 스케줄 없는 경우
            cancelBtn.setVisibility(View.GONE);

        } else { // 있는 경우
            cancelBtn.setVisibility(View.VISIBLE);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject stopResult = null;

                        if (getLocale().equals("ko")) {
                            pMsg = "스케줄을 취소하시겠습니까?";
                            pButton = "예";
                            pButton2 = "아니오";
                        } else {
                            pMsg = "Are you sure you want to cancel the schedule?";
                            pButton = "예";
                            pButton2 = "아니오";
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                        builder.setTitle(pTitle);
                        builder.setMessage(pMsg);

                        //YES
                        builder.setNegativeButton(pButton,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            String result = new ScheduleTask().execute("stopUpdate", sch_key).get();
                                            JSONObject stopResult = new JSONObject(result);

                                            if ("SUCCESS".equals(stopResult.getString("RESULT"))) {
                                                if (getLocale().equals("ko")) {
                                                    pTitle = "스케줄";
                                                    pMsg = "스케줄을 취소하였습니다.";
                                                    pButton = "확인";
                                                } else {
                                                    pTitle = "Schedule";
                                                    pMsg = "The schedule has been canceled.";
                                                    pButton = "OK";
                                                }
                                            }
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                                            builder.setTitle(pTitle);
                                            builder.setMessage(pMsg);
                                            builder.setPositiveButton(pButton,
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent intent = getIntent();
                                                            finish();
                                                            startActivity(intent);
                                                        }
                                                    });
                                            builder.show();
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

                       /*if ("FAIL".equals(stopResult.getString("RESULT"))) {
                            if (getLocale().equals("ko")) {
                                msg = "진행중인 스케줄이 없습니다.";
                                button = "확인";
                            } else {
                                msg = "There is no schedule in progress.";
                                button = "OK";
                            }
                            popup(pTitle, msg, button);
                        }*/

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getLocale().equals("ko")) {
                            pTitle = "스케줄";
                            msg = "스케줄 중지에 문제가 발생했습니다.";
                            button = "확인";
                        } else {
                            pTitle = "Schedule";
                            msg = "A schedule stop failed.";
                            button = "OK";
                        }
                        popup2(pTitle, msg, button);
                    }
                }

            });
        }


        // 시작날짜
        dateStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ScheduleActivity.this, myDatePicker1, sCalendar.get(Calendar.YEAR), sCalendar.get(Calendar.MONTH), sCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        // 시작 시간
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        timeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long now = System.currentTimeMillis();
                Date mDate = new Date(now);
                SimpleDateFormat simpleDate = new SimpleDateFormat("HH"); // 24시간
                SimpleDateFormat simpleDate2 = new SimpleDateFormat("mm");
                SimpleDateFormat simpleDate3 = new SimpleDateFormat("ss");
                String formatHour = simpleDate.format(mDate);
                String formatMinute = simpleDate2.format(mDate);
                String formatSecond = simpleDate3.format(mDate);

                view = View.inflate(ScheduleActivity.this, R.layout.time_dialog, null);
                final NumberPicker numberPickerHour = view.findViewById(R.id.numpicker_hours);
                numberPickerHour.setMaxValue(23);
                numberPickerHour.setValue(Integer.parseInt(formatHour));
                final NumberPicker numberPickerMinutes = view.findViewById(R.id.numpicker_minutes);
                numberPickerMinutes.setMaxValue(59);
                numberPickerMinutes.setValue(Integer.parseInt(formatMinute));
                final NumberPicker numberPickerSeconds = view.findViewById(R.id.numpicker_seconds);
                numberPickerSeconds.setMaxValue(59);
                numberPickerSeconds.setValue(Integer.parseInt(formatSecond));
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
                        Log.d(TAG, "시간===== : " + numberPickerHour.getValue());
                        Log.d(TAG, "분===== : " + numberPickerMinutes.getValue());
                        Log.d(TAG, "초===== : " + numberPickerSeconds.getValue());

                        timeStart.setText(leftPad(numberPickerHour.getValue(), 2, "0") + ":" + leftPad(numberPickerMinutes.getValue(), 2, "0") + ":" + leftPad(numberPickerSeconds.getValue(), 2, "0"));

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


        // 저장버튼
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 시간
                long now = System.currentTimeMillis();
                Date mDate = new Date(now);
                SimpleDateFormat simpleDate4 = new SimpleDateFormat("HHmmss");
                String formatTime = simpleDate4.format(mDate);
                Log.d(TAG, "현재 시간 : " + formatTime);
                String timeStarttxt = timeStart.getText().toString(); // 시작날짜
                timeStarttxt = timeStarttxt.replace(":", "");
                Log.d(TAG, "텍스트 시간 : " + timeStarttxt);

                int compareTime = formatTime.compareTo(timeStarttxt); // 양수면 formatTime이 전 시간  음수면 formatTime이 후
                Log.d(TAG, "비교 시간 : " + compareTime);
                // 날짜
                int compareDate = 0;
                long mNow = System.currentTimeMillis();
                Date mReDate = new Date(mNow); // 현재 날짜

                SimpleDateFormat mFormat1 = new SimpleDateFormat("yyyy-MM-dd"); // 텍스트 날짜
                String formatDate2 = mFormat1.format(mReDate); // 날짜
                String formatDateStart2 = String.valueOf(dateStart.getText()).replace("/", "-");

                if(!"날짜".equals(formatDateStart2) || "date".equals(formatDateStart2)) {
                    try {
                        Date formatDate = mFormat1.parse(formatDate2);
                        Date formatDateStart = mFormat1.parse(formatDateStart2);
                        Log.d(TAG, "formatDate : " + formatDate);
                        Log.d(TAG, "formatDateStrart : " + formatDateStart);
                        compareDate = formatDate.compareTo(formatDateStart); // 양수면 formatDate(현재)가 전 날짜
                        Log.d(TAG, "현재 날짜 비교 : " + compareDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                String DateStart = dateStart.getText().toString(); // 시작날짜
                DateStart = DateStart.replace("/", "");


                String Message = message.getText().toString(); // 메세지
                String sReceivePhone = sPhone.getText().toString(); // 받는사람


                String TimeStart = timeStart.getText().toString(); // 시작시간
                    /*TimeStart = TimeStart.replace("시 ", "");
                    TimeStart = TimeStart.replace("분", ""); //0416*/
                TimeStart = TimeStart.replace(":", "");


                if ("".equals(sch_key)) {
                    if ("".equals(sReceivePhone)) {
                        Log.d(TAG, "sbItemList");
                        if (getLocale().equals("ko")) {
                            pTitle = "스케줄";
                            msg = "받는 사람 연락처를 입력해주세요.";
                            button = "확인";
                        } else {
                            pTitle = "Schedule";
                            msg = "Please enter the recipient's contact information.";
                            button = "OK";
                        }
                        popup(pTitle, msg, button);
                    } else if ("".equals(Message)) {
                        if (getLocale().equals("ko")) {
                            pTitle = "스케줄";
                            msg = "메세지를 입력하세요.";
                            button = "확인";
                        } else {
                            pTitle = "Schedule";
                            msg = "Please enter a message.";
                            button = "OK";
                        }
                        popup(pTitle, msg, button);
                    } else if ("날짜".equals(DateStart) || "date".equals(DateStart)) {
                        if (getLocale().equals("ko")) {
                            pTitle = "스케줄";
                            msg = "예약 날짜를 지정해주세요.";
                            button = "확인";
                        } else {
                            pTitle = "Schedule";
                            msg = "Please specify a start date.";
                            button = "OK";
                        }
                        popup(pTitle, msg, button);
                    } else if ("시각".equals(TimeStart) || "time".equals(TimeStart)) {
                        if (getLocale().equals("ko")) {
                            pTitle = "스케줄";
                            msg = "예약 시간을 지정해주세요.";
                            button = "확인";
                        } else {
                            pTitle = "Schedule";
                            msg = "Please specify a start time.";
                            button = "OK";
                        }
                        popup(pTitle, msg, button);
                    } else if (compareDate>0) { // 현재 날짜가 전
                        if (getLocale().equals("ko")) {
                            pTitle = "스케줄";
                            msg = "현재날짜 이후로 설정해주세요.";
                            button = "확인";
                        } else {
                            pTitle = "Schedule";
                            msg = "Please set it after the current date.";
                            button = "OK";
                        }
                        popup(pTitle, msg, button);
                    } else {
                        if(compareDate == 0) {
                            if (compareTime>0) { // 현재 날짜가 전
                                if (getLocale().equals("ko")) {
                                    pTitle = "스케줄";
                                    msg = "현재시간 이후로 설정해주세요.";
                                    button = "확인";
                                } else {
                                    pTitle = "Schedule";
                                    msg = "Please set it after the current time.";
                                    button = "OK";
                                }
                                popup(pTitle, msg, button);
                                return;
                            }
                        }
                        try {
                            String result = new ScheduleTask().execute("saveSchedule", Message, DateStart, TimeStart, sUserId, sUserPhone, sReceivePhone).get();

                            if (getLocale().equals("ko")) {
                                pTitle = "스케줄";
                                msg = "스케줄 등록 되었습니다.";
                                button = "확인";
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
                } else{
                    if (getLocale().equals("ko")) {
                        pTitle = "스케줄";
                        msg = "이미 등록된 스케줄이 있습니다.";
                        button = "확인";
                    } else {
                        pTitle = "Schedule";
                        msg = "There is already a registered schedule.";
                        button = "OK";
                    }
                    popup(pTitle, msg, button);
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
            // iLen길이 만큼 strChar문자로 채운다.
            sbAddChar.append( strChar );
        } strResult = sbAddChar + strContext; // LPAD이므로, 채울문자열 + 원래문자열로 Concate한다.
        return strResult;

    }

    //추가된 소스, ToolBar에 menu.xml을 인플레이트함
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.account_cancel:
                if (getLocale().equals("ko")) {
                    pTitle = "회원탈퇴";
                    pMsg = "정말 탈퇴하시겠습니까?";
                    pButton = "예";
                    pButton2 = "아니오";
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
                                    Log.d("탈퇴되냐? : ",userInfo.getString("result"));
                                    //탈퇴후
                                    if ("FAIL".equals(userInfo.getString("result"))) {
                                        if (getLocale().equals("ko")) {
                                            pTitle = "회원탈퇴";
                                            pMsg = "회원탈퇴를 실패하였습니다.";
                                            pButton = "확인";
                                        } else {
                                            pTitle = "Account Cancellation";
                                            pMsg = "Unable to cancel your account.";
                                            pButton = "OK";
                                        }
                                        popup(pTitle, pMsg, pButton);

                                    } else {
                                        if (getLocale().equals("ko")) {
                                            pTitle = "회원탈퇴";
                                            pMsg = "회원탈퇴가 완료되었습니다.";
                                            pButton = "확인";
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
                                                        moveTaskToBack(true); // 태스크를 백그라운드로 이동
                                                        finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                                                        android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
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
                    pTitle = "앱 종료";
                    pMsg = "정말 종료하시겠습니까?";
                    pButton = "예";
                    pButton2 = "아니오";
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
                                moveTaskToBack(true); // 태스크를 백그라운드로 이동
                                finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                                android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
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