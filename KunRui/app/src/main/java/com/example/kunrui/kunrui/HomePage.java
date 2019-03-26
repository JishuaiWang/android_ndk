package com.example.kunrui.kunrui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.kunrui.kunrui.Resource.poll_wireless_exchange;

public class HomePage extends TitleActivity {
    private Thread sqlSelect;
    private Thread judge_poll;
    private Thread judgeConnect;
    private Thread icon_slide;
    private Handler mHandler;
    private ImageView innerView;
    private ImageView outerView;
    private ImageView middle_view;
    private ImageView icon_left;
    private ImageView icon_right;
    ViewGroup.MarginLayoutParams margin_left;
    ViewGroup.MarginLayoutParams margin_right;
    private Button aSwitch;
    private Button tdd;
    private Button fdd;
    private Spinner bandMsgTdd;
    private Spinner bandMsgFdd;
    private EditText ulfreqTdd;
    private EditText ulfreqFdd;
    private EditText dlfreqTdd;
    private EditText dlfreqFdd;
    private EditText pciTdd;
    private EditText pciFdd;
    private Button arrowScroll;
    private Button whiteList;
    private Button positionImsi;
    private ScrollView msgScroll;
    private TextView number_percent;
    private TextView Qcc;
    private TextView imsi_count;
    private TextView RSSI;
    private TextView remark_homepage;
    private TextView cptTime_homepage;
    private TextView imsi_homePage;
    private TextView id_homePage;
    private serverStart tcpServer = new serverStart();
    private View HomePage;
    private View startScan;
    private Button loading;
    private fileCreate fileCreate = new fileCreate();
    private Button connectStart;
    private EditText editText;
    private SQLiteDatabase db;
    private sqliteUse sqliteUse;
    private poll_wireless_exchange poll_wireless_exchange = new poll_wireless_exchange();
    private ArrayList<String> imsi_tmp = new ArrayList<>();    //存储抓不到的IMSI号
    private String[] NIMSI = new String[1024];
    private String[] NRemarks = new String[1024];
    private String[] NName = new String[1024];
    private String[] NPhoneNum = new String[1024];
    private String[] NUnit = new String[1024];
    private String IMSI = "";
    private int id_old = 0;
    private int click_id = 0;
    private int num = 0;
    private int icon_pos_no = 0;
    private int loadingStatus = 1;
    private int scrollStatus = 1;

    @RequiresApi(api = Build.VERSION_CODES.N)
    //时间转换成时间戳
    public static String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    private void waitsql() {
        int i = 0;
        while(fileCreate.read_status("SQLITE_LOCK") == 1){
            System.out.println("被bulkDumpSql占用");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
            if(i == 4) {
                fileCreate.write_status("SQLITE_LOCK", 0);
                break;
            }
        }
    }

    private void InserModelType() {
        SQLiteDatabase db;
        db = sqliteUse.getReadableDatabase();
        Map<String, String> modelCode = new HashMap<>();
        //范围7~23

        //legu型号
        modelCode.put("RD-A-2", "8");
        modelCode.put("RD-B-2", "8,10");
        modelCode.put("RD-B-5-DF", "7,9");
        modelCode.put("RD-B-5-DE", "7,8");
        modelCode.put("RD-B-5-EF", "8,9");
        modelCode.put("RD-D-5-DE", "7,8,12,13");
        modelCode.put("RD-D-5-EF", "8,9,12,13");

        //tzl型号
        //TZL-010有能是'TZL-010': ['8',],暂时没有好方法同时兼容
        modelCode.put("TZL-010", "8");
        modelCode.put("TZL-211", "8");
        modelCode.put("TZL-010-3", "12");
        modelCode.put("TZL-021-E3", "8,12");
        modelCode.put("TZL-031-E3", "18,22,23");
        modelCode.put("TZLW-031-E3", "18,22,23");
        modelCode.put("TZL-221-E", "8,10");
        modelCode.put("TZL-521-DF", "7,9");
        modelCode.put("TZL-521-DE", "7,8");
        modelCode.put("TZL-521-EF", "8,9");
        modelCode.put("TZL-541-DE", "7,8,12,13");
        modelCode.put("TZL-541-EF", "8,9,12,13");
        modelCode.put("TZL-561", "7,8,9,10,12,13");

        //pys型号
        modelCode.put("LTEF-211", "8");
        modelCode.put("LTEF-221-E", "8,10");
        modelCode.put("LTEF-521-DF", "7,9");
        modelCode.put("LTEF-521-DE", "7,8");
        modelCode.put("LTEF-521-EF", "8,9");
        modelCode.put("LTEF-541-DE", "7,8,12,13");
        modelCode.put("LTEF-541-EF", "8,9,12,13");

        //asd型号
        //ASD-010有能是'ASD-010': ['8',],暂时没有好方法同时兼容
        modelCode.put("ASD-010", "8");
        modelCode.put("ASD-010-3", "12");
        modelCode.put("ASD-211", "8");
        modelCode.put("ASD-021-E3", "8,12");
        modelCode.put("ASD-221-E", "8,10");
        modelCode.put("ASD-521-DF", "7,9");
        modelCode.put("ASD-521-DE", "7,8");
        modelCode.put("ASD-521-EF", "8,9");
        modelCode.put("ASD-031-E3", "18,22,23");
        modelCode.put("ASD-541-DE", "7,8,12,13");
        modelCode.put("ASD-541-EF", "8,9,12,13");

        //update
        modelCode.put("RD-B-1-SE", "8,12");
        modelCode.put("RD-B-0-SE", "8,12");
        modelCode.put("RD-C-1-E", "8,12,13");
        modelCode.put("RD-F-10", "7,8,9,10,12,13");
        modelCode.put("RD-B-10-GSM-10", "8,10,24");
        modelCode.put("RD-C-10-TDD", "7.8.9");
        modelCode.put("RD-C-10-FDD ", "10,12,13");


        Set<String> model = modelCode.keySet();
        for(String key : model){
            String Code = modelCode.get(key);
            System.out.println(key + " " + Code);

            ContentValues contentValues = new ContentValues();
            contentValues.put("type", key);
            contentValues.put("model",Code);
            db.insert("modelCode", null, contentValues);
        }

        Map<String, String> bandtable = new HashMap<String, String>();
        bandtable.put("7", "TDD D频,38,37750-38249");
        bandtable.put("8", "TDD E频,40,38650-39649");
        bandtable.put("9", "TDD F频,39,38250-38649");
        bandtable.put("10", "FDD Band1电信,1,0-599");
        bandtable.put("11", "FDD Band1联通,1,0-599");
        bandtable.put("12", "FDD Band3电信,3,1200-1949");
        bandtable.put("13", "FDD Band3联通,3,1200-1949");
        //bandtable.put("14", "对应所有型号");
        //bandtable.put("15", "路由器固件版本升级");
        bandtable.put("16", "TDD E频,40,38650-39649");//（室内普通)
        bandtable.put("17", "TDD D频,38,37750-38249");//（室内加功放)
        bandtable.put("18", "TDD E频,40,38650-39649");// (室内加功放)
        bandtable.put("19", "TDD F频,39,38250-38649");// (室内加功放)
        bandtable.put("20", "FDD Band1电信,1,0-599");
        bandtable.put("21", "FDD Band1联通,1,0-599");
        bandtable.put("22", "FDD Band3电信,3,1200-1949");
        bandtable.put("23", "FDD Band3联通,3,1200-1949");

        Set<String> type = bandtable.keySet();
        for(String key :type)
        {
            String GSM = bandtable.get(key);
            System.out.println(GSM);
            ContentValues contentValues = new ContentValues();
            contentValues.put("typeCode", key);
            contentValues.put("Msg", GSM);
            db.insert("bandTable", null, contentValues);
        }

        db.close();
    }

    //获取白名单
    private void getNList() {
        NIMSI = new String[1024];
        NRemarks = new String[1024];
        NName = new String[1024];
        NUnit = new String[1024];
        NPhoneNum = new String[1024];
        try {
            num = 0;
            SQLiteDatabase db;
            db = sqliteUse.getReadableDatabase();
            Cursor cursor = db.query("whiteList", null, null, null, null, null, null);
            //调用moveToFirst()将数据指针移动到第一行的位置。
            if (cursor.moveToFirst()) {
                do {
                    //然后通过Cursor的getColumnIndex()获取某一列中所对应的位置的索引
                    String IMSI = cursor.getString(cursor.getColumnIndex("imsi"));
                    String REMARKS = cursor.getString(cursor.getColumnIndex("Remarks"));
                    String NAME = cursor.getString(cursor.getColumnIndex("name"));
                    String PHONENUM = cursor.getString(cursor.getColumnIndex("phoneNumber"));
                    String UNIT = cursor.getString(cursor.getColumnIndex("unit"));
                    NIMSI[num] = IMSI;
                    NRemarks[num] = REMARKS;
                    NName[num] = NAME;
                    NUnit[num] = UNIT;
                    NPhoneNum[num] = PHONENUM;
                    num++;
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            System.out.println("getNList:" + e);
        }
    }

    public void sendPerNumber(int num) {
        ArrayList<String> Msg = new ArrayList<>();
        Msg.add(String.valueOf(num));
        Message message = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("Num", Msg);
        message.setData(bundle);
        message.what = 4;
        mHandler.sendMessage(message);
    }

    public void rotate(ImageView view, int Degrees) {
        //旋转GIF设置
        view.setVisibility(View.VISIBLE);
        aSwitch.setVisibility(View.GONE);
        Animation rotateAnimation = new RotateAnimation(0, Degrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(1000);  //旋转时间
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        view.startAnimation(rotateAnimation);
    }

    //dp单位转换成px单位
    public static int dp2px(Context context, float dpValue){
        return (int)(dpValue * (context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    //动态添加函数
    @TargetApi(Build.VERSION_CODES.N)
    public void setEditAdd(int width_t, final ArrayList Msg, final int status)
    {
        String value;
        switch (status) {
            case 3:
                value = String.valueOf((int) ((Float.parseFloat(String.valueOf(Msg.get(3))) + 144) * 100 / 110));
                break;
            case 4:
                value = "";
                break;
            default:
                value = String.valueOf(Msg.get(status));
        }
        int height=dp2px(HomePage.this, 40);
        int margin=dp2px(HomePage.this, 0.5f);
        int padding=dp2px(HomePage.this, 2);
        int width = dp2px(HomePage.this, width_t);

        editText=new EditText(HomePage.this);
        editText.setSingleLine();
        editText.setBackgroundColor(Color.parseColor("#0d122e"));
        android.widget.TableRow.LayoutParams params=new android.widget.TableRow.LayoutParams(width, height);
        params.weight=1;
        editText.setGravity(Gravity.CENTER);
        params.setMargins(margin,margin,margin,margin);
        editText.setLayoutParams(params);
        editText.setPadding(padding,padding,padding,padding);
        editText.setTextSize(12);

        //设置不可编辑但有点击
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setTextColor(Color.parseColor("#FFFFFFFF"));

        for (int i = 0; i< num; i++) {
            if (NIMSI[i].equals(String.valueOf(Msg.get(0)))){
                editText.setTextColor(Color.rgb(0, 255, 0));
                if(status == 4) {
                    value = NName[i] + " " +
                            NUnit[i] + " " +
                            //NPhoneNum[i] + " " +
                            NRemarks[i];
                }
            }
        }
        editText.setText(value);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollStatus = 0;
                LinearLayout linearLayout = findViewById(R.id.TableLayout);

                System.out.println("click_id:" + click_id);
                TableRow tableRow = (TableRow) linearLayout.getChildAt(click_id);
                for (int i = 0; i < 7; i++) {
                    tableRow.getChildAt(i).setBackgroundColor(Color.parseColor("#0d122e"));
                }

                click_id = Integer.parseInt(String.valueOf(Msg.get(5))) - 1;
                tableRow = (TableRow) linearLayout.getChildAt(click_id);
                for(int i = 0; i<7; i++) {
                    tableRow.getChildAt(i).setBackgroundColor(Color.parseColor("#00008B"));
                }
                System.out.println("click_id:" + click_id);

                IMSI = (String) Msg.get(0);
            }
        });
    }

    @SuppressLint("HandlerLeak")
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        initView(1);//初始化标题

        innerView = findViewById(R.id.innerView);
        outerView = findViewById(R.id.outerView);
        middle_view = findViewById(R.id.middle_view);
        icon_left = findViewById(R.id.icon_left);
        icon_right = findViewById(R.id.icon_right);
        margin_left =  new ViewGroup.MarginLayoutParams(icon_left.getLayoutParams());
        margin_right =  new ViewGroup.MarginLayoutParams(icon_right.getLayoutParams());

        whiteList = findViewById(R.id.whiteList);
        positionImsi = findViewById(R.id.positionImsi);
        msgScroll = findViewById(R.id.msgScroll);
        Button setUp = findViewById(R.id.setUp);
        arrowScroll = findViewById(R.id.arrowScroll);
        number_percent = findViewById(R.id.number_percent);

        Qcc = findViewById(R.id.Qcc);
        RSSI = findViewById(R.id.RSSI);
        remark_homepage = findViewById(R.id.remark_homepage);
        cptTime_homepage = findViewById(R.id.cptTime_homepage);
        imsi_homePage = findViewById(R.id.imsi_homePage);
        id_homePage = findViewById(R.id.id_homePage);
        imsi_count = findViewById(R.id.imsi_count);

        loading = findViewById(R.id.loading);
        HomePage = findViewById(R.id.homePage);
        startScan = findViewById(R.id.startScan);
        connectStart = findViewById(R.id.connectStart);
        sqliteUse = new sqliteUse(this, "smtp.db", null, 1);

        sqliteUse.copyFileFromAssets("data/data/com.example.kunrui.kunrui/databases/", "QCellCore.db");     //归属地表导入
        fileCreate.init_config_file();

        db = sqliteUse.getReadableDatabase();
        sqliteUse.BlacktableCreate(db);
        sqliteUse.WhitetableCreate(db);
        sqliteUse.HistorytableCreate(db);
        sqliteUse.ModelCodeCreate(db);
        db.close();

        aSwitch = findViewById(R.id.aSwitch);
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //控制开关字体颜色
                if (aSwitch.getText().toString().equals("常规模式")) {
                    aSwitch.setText("轮询模式");
                    //Toast.makeText(getApplicationContext(), "全抓模式下，周期内扫描时间过长，是否继续", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this, AlertDialog.THEME_HOLO_DARK);
                    builder.setTitle("轮询模式下，周期内扫描时间过长，是否继续");
                    builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.out.println("继续");
                            fileCreate.write_status("POLL_TYPE", 1);
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            aSwitch.setText("常规模式");
                            fileCreate.write_status("POLL_TYPE", 0);
                        }
                    });
                    builder.setCancelable(false);
                    builder.create().show();
                }else {
                    aSwitch.setText("常规模式");
                    fileCreate.write_status("POLL_TYPE", 0);
                }
            }
        });

        InserModelType();       //插入模块类型码
        getNList();     //获取最新白名单单信息

        mHandler = new Handler() {
            @SuppressLint("SetTextI18n")
            @TargetApi(Build.VERSION_CODES.N)
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void handleMessage (Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                final ArrayList<String> Msg;
                switch (msg.what) {
                    case 0:
                        Msg = bundle.getStringArrayList("Msg");
                        assert Msg != null;

                        imsi_tmp.add(Msg.get(0));
                        imsi_tmp = new ArrayList<>(new LinkedHashSet<>(imsi_tmp));
                        if(imsi_tmp.size() > id_old) {
                            Msg.add(String.valueOf(imsi_tmp.size()));
                            Msg.add("1");

                            System.out.println("数据添加:" + Msg);
                            LinearLayout linearLayout = findViewById(R.id.TableLayout);
                            //创建一行
                            TableRow tableRow = new TableRow(HomePage.this);
                            tableRow.setBackgroundColor(Color.parseColor("#6495ED"));
                            //这里一定要用android.widget.TableRow.LayoutParams，不然TableRow不显示
                            tableRow.setLayoutParams(new LinearLayout.LayoutParams(android.widget.TableRow.LayoutParams.MATCH_PARENT, android.widget.TableRow.LayoutParams.MATCH_PARENT));
                            setEditAdd(0, Msg, 5);//ID
                            tableRow.addView(editText);
                            setEditAdd(70, Msg, 0);//IMSI
                            tableRow.addView(editText);
                            setEditAdd(100, Msg, 1);//Time
                            tableRow.addView(editText);
                            setEditAdd(70, Msg, 2);//归属地
                            tableRow.addView(editText);
                            setEditAdd(20, Msg, 3);//RSSI
                            tableRow.addView(editText);
                            setEditAdd(20, Msg, 6);//次数
                            tableRow.addView(editText);
                            setEditAdd(110, Msg, 4);//详细信息
                            tableRow.addView(editText);
                            //将一行数据添加到表格中
                            linearLayout.addView(tableRow);
                        } else if(imsi_tmp.size() > 0) {
                            for(int i = 0; i< imsi_tmp.size(); i++) {
                                if(imsi_tmp.get(i).equals(Msg.get(0))) {
                                    LinearLayout linearLayout = findViewById(R.id.TableLayout);
                                    TableRow tableRow = (TableRow) linearLayout.getChildAt(i);
                                    EditText edit_imsi = (EditText) tableRow.getChildAt(1);
                                    EditText edit_time = (EditText) tableRow.getChildAt(2);
                                    EditText edit_rssi = (EditText) tableRow.getChildAt(4);
                                    EditText edit_count = (EditText) tableRow.getChildAt(5);
                                    edit_time.setText(Msg.get(1));
                                    edit_rssi.setText(String.valueOf((int) ((Float.parseFloat(String.valueOf(Msg.get(3))) + 144) * 100 / 110)));
                                    edit_count.setText(String.valueOf(1 + Integer.parseInt(String.valueOf(edit_count.getText()))));
                                    System.out.println(edit_imsi.getText() + "..." + imsi_tmp.get(i));
                                }
                            }
                        }

                        id_old = imsi_tmp.size();
                        if(scrollStatus == 1) {
                            if (msgScroll != null) {
                                msgScroll.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        msgScroll.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                });
                            }
                        }
                        break;
                    case 1:
                        //建立连接失败
                        connectStart.setTextColor(Color.parseColor("#FFFFFF"));
                        connectStart.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "网络原因连接失败,请稍后重试", Toast.LENGTH_SHORT).show();

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        HomePage.setBackgroundResource(R.drawable.bg);
                        whiteList.setVisibility(View.VISIBLE);
                        connectStart.setVisibility(View.VISIBLE);
                        aSwitch.setVisibility(View.VISIBLE);

                        msgScroll.setVisibility(View.GONE);
                        startScan.setVisibility(View.GONE);
                        positionImsi.setVisibility(View.GONE);
                        Qcc.setVisibility(View.GONE);
                        RSSI.setVisibility(View.GONE);
                        remark_homepage.setVisibility(View.GONE);
                        cptTime_homepage.setVisibility(View.GONE);
                        imsi_homePage.setVisibility(View.GONE);
                        id_homePage.setVisibility(View.GONE);
                        arrowScroll.setVisibility(View.GONE);
                        icon_left.setVisibility(View.GONE);
                        icon_right.setVisibility(View.GONE);
                        imsi_count.setVisibility(View.GONE);
                        LinearLayout linearRemove = findViewById(R.id.TableLayout);
                        linearRemove.removeAllViews();
                        break;
                    case 2:
                        //建立连接成功
//                        HomePage.setBackgroundColor(Color.argb(100, 255, 255, 255));
//                        HomePage.getBackground().setAlpha(200);   透明度
                        HomePage.setBackgroundResource(R.drawable.now_select_bg);
                        connectStart.setVisibility(View.GONE);
                        whiteList.setVisibility(View.GONE);
                        aSwitch.setVisibility(View.GONE);
                        icon_left.setVisibility(View.GONE);
                        icon_right.setVisibility(View.GONE);

                        Qcc.setVisibility(View.VISIBLE);
                        RSSI.setVisibility(View.VISIBLE);
                        msgScroll.setVisibility(View.VISIBLE);
                        startScan.setVisibility(View.VISIBLE);
                        positionImsi.setVisibility(View.VISIBLE);
                        remark_homepage.setVisibility(View.VISIBLE);
                        cptTime_homepage.setVisibility(View.VISIBLE);
                        imsi_homePage.setVisibility(View.VISIBLE);
                        id_homePage.setVisibility(View.VISIBLE);
                        imsi_count.setVisibility(View.VISIBLE);
                        arrowScroll.setVisibility(View.VISIBLE);
                        loadingStatus = 1;
                        break;
                    case 3:
                        //加载中...
                        switch (loadingStatus) {
                            case 1:
                                loadingStatus = 2;
                                loading.setText(".  ");
                                break;
                            case 2:
                                loadingStatus = 3;
                                loading.setText(".. ");
                                break;
                            case 3:
                                loadingStatus = 1;
                                loading.setText("...");
                                break;
                        }
                        break;
                    case 4:
                        Msg = bundle.getStringArrayList("Num");
                        assert Msg != null;
                        number_percent.setText(Msg.get(0) + "%");
                        break;
                    case 5:
                        innerView.clearAnimation();
                        outerView.clearAnimation();
                        middle_view.clearAnimation();
                        innerView.setVisibility(View.GONE);
                        outerView.setVisibility(View.GONE);
                        middle_view.setVisibility(View.GONE);
                        number_percent.setVisibility(View.GONE);
                        icon_left.setVisibility(View.GONE);
                        icon_right.setVisibility(View.GONE);
                        break;
                    case 6:
                        margin_left.setMargins(625 + icon_pos_no * 3, 275, 0, 0);
                        RelativeLayout.LayoutParams layoutParams_left = new RelativeLayout.LayoutParams(margin_left);
                        layoutParams_left.width = 180;
                        layoutParams_left.height = 80;
                        icon_left.setLayoutParams(layoutParams_left);   //设置图片位置及宽高

                        margin_right.setMargins(1150 - icon_pos_no * 3, 480, 0, 0);
                        RelativeLayout.LayoutParams layoutParams_right = new RelativeLayout.LayoutParams(margin_right);
                        layoutParams_right.width = 180;
                        layoutParams_right.height = 80;
                        icon_right.setLayoutParams(layoutParams_right);
                        break;
                    default:
                        break;
                }
            }
        };

        new Thread() {
            public void run() {
                while (loadingStatus != 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(3);
                }
            }
        }.start();

        judge_poll = new Thread(){
            private int i = 1, num = 0, statu = 0;
            public void run(){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fileCreate.write_status("WIRELESS_SET", 1);
                poll_wireless_exchange.insert(i, sqliteUse, fileCreate);
                if(fileCreate.read_status("POLL_TYPE") == 1) {
                    while (fileCreate.read_status("SINGER_APP_CONNECTED") == 1) {

                        if (fileCreate.read_status("POWER_ENTERED") == 0) {
                            num++;
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        while (fileCreate.read_status("POWER_ENTERED") == 1) {
                            statu = 1;
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if(statu == 1) {
                            poll_wireless_exchange.insert(i, sqliteUse, fileCreate);
                            statu = 0;
                        }

                        if(num == 120) {
                            System.out.println("*********TYPE CHANGED**********" + i);
                            num = 0;
                            i++;
                            poll_wireless_exchange.insert(i, sqliteUse, fileCreate);
                            if (i == 3) i = 0;
                        }
                    }
                }
            }
        };

        //定时采集数据功能
        sqlSelect = new Thread(){
            public void run(){
                int No = 0;
                SQLiteDatabase db;
                db = sqliteUse.getReadableDatabase();
                while (fileCreate.read_status("SINGER_APP_CONNECTED") == 1) {
                    getNList();     //获取最新白名单单信息

                    try {
                        waitsql();
                        db = sqliteUse.getReadableDatabase();
                        Cursor cursor = db.query("imsiMsg", null, null, null, null, null, null);
                        System.out.println("No:"+No+",cursor.getCount():"+cursor.getCount());
                        if(No < cursor.getCount())
                        {
                            cursor.moveToFirst();
                            while (No != 0 )
                            {
                                No--;
                                cursor.moveToNext();
                            }
                            do{
                                ArrayList<String> Msg = new ArrayList<>();
                                Msg.add(cursor.getString(cursor.getColumnIndex("imsi")));
                                Msg.add(cursor.getString(cursor.getColumnIndex("cptTime")));
                                byte[] Attribution = cursor.getBlob(cursor.getColumnIndex("Attribution"));
                                try {
                                    String attribution = new String(Attribution, "GB2312");
                                    Msg.add(attribution);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                Msg.add(cursor.getString(cursor.getColumnIndex("rssi")));
                                Msg.add(String.valueOf(cursor.getInt(cursor.getColumnIndex("id"))));
                                Message message = mHandler.obtainMessage();
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("Msg", Msg);
                                message.setData(bundle);
                                message.what = 0;
                                mHandler.sendMessage(message);
                            } while (cursor.moveToNext());
                        }
                        db.close();
                        No = cursor.getCount();
                        cursor.close();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }catch (Exception e)
                    {
                        System.out.println("db:"+e);
                    }
                }
                System.out.println("滚动结束");
                db.close();
                if (fileCreate.read_status("SINGER_APP_CONNECTED") == 0) {
                    System.out.println("无网络");
                    mHandler.sendEmptyMessage(1);
                }
            }
        };

        //检验连接状态
        judgeConnect = new Thread(){
            private int num = 0, modelId, typeCode, i;
            public void run() {
                Cursor cursor;
                SQLiteDatabase db;
                db = sqliteUse.getReadableDatabase();
                sqliteUse.resCreate(db);
                db.close();
                num = 0;
                try {
                    while (num < 10) {
                        num++;
                        System.out.println("Loading..." + num);
                        modelId = 0;
                        db = sqliteUse.getReadableDatabase();
                        cursor = db.query("freqMsg", null, null, null, null, null, null);
                        if (cursor.moveToFirst()) {
                            do {
                                typeCode = cursor.getInt(cursor.getColumnIndex("typeCode"));
                                if (typeCode == 8 || typeCode == 12) {
                                    modelId++;
                                }
                                System.out.println("typeCode:" + modelId + " " + cursor.getInt(cursor.getColumnIndex("typeCode")));
                            } while (cursor.moveToNext());
                        }
                        db.close();
                        cursor.close();
                        for (i = (num - 1) * 10; i < num * 10; i++) {
                            Thread.sleep(100);
                            sendPerNumber(i);
                        }
                        if (modelId == 2) {
                            break;
                        }
                    }
                    if (num == 10) {
                        System.out.println("10 连接失败");
                        fileCreate.write_status("SINGER_APP_CONNECTED", 0);
                        mHandler.sendEmptyMessage(1);
                    } else {
                        for (i = (num - 1) * 10; i < 101; i++) {
                            sendPerNumber(i);
                            Thread.sleep(20);
                        }
                        mHandler.sendEmptyMessage(2);
                        new Thread(sqlSelect).start();
                        new Thread(judge_poll).start();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
                mHandler.sendEmptyMessage(5);
            }
        };

        icon_slide = new Thread() {
            private int revert = 0;
            public void run() {
                icon_pos_no = 0;
                while (fileCreate.read_status("SINGER_APP_CONNECTED") == 1) {
                    mHandler.sendEmptyMessage(6);

                    if(icon_pos_no == 0)
                        revert = 0;
                    else if (icon_pos_no == 175)
                        revert = 1;

                    if(revert == 0)
                        icon_pos_no++;
                    if(revert == 1)
                        icon_pos_no--;

                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        connectStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number_percent.setVisibility(View.VISIBLE);
                connectStart.setEnabled(false);
                connectStart.setTextColor(Color.parseColor("#A9A9A9"));

                //旋转GIF
                rotate(innerView, 360);
                rotate(outerView, -360);
                rotate(middle_view, 360);

                fileCreate.write_status("SINGER_APP_CONNECTED", 1);
                new Thread(judgeConnect).start();

                tcpServer.tcpCreate();
                tcpServer.Thread.start();

//                new Thread(icon_slide).start();   左右光点转动效果

                icon_left.setVisibility(View.VISIBLE);
                icon_right.setVisibility(View.VISIBLE);
            }
        });

        //定位
        positionImsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!IMSI.equals("")) {
                        ArrayList<String> data_list = new ArrayList<>();
                        data_list.add(IMSI);
                        data_list.addAll(imsi_tmp);
                        Set<String> hashSet = new LinkedHashSet<String>(data_list);
                        List<String> sortHashList = new ArrayList<String>(hashSet);
                        System.out.println("定位IMSI：" + sortHashList);
                        data_list = (ArrayList<String>) sortHashList;
                        System.out.println("..." + data_list);
                        //跳转页面到能量显示
                        Intent intent = new Intent(HomePage.this, PowerChart.class);
                        intent.putExtra("dataList", data_list);
                        intent.putExtra("No", String.valueOf(data_list.size() - 1));
                        //给RSSI设初值
                        fileCreate.setRssi("0.0000");
                        fileCreate.write_status("POWER_ENTERED", 1);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "请先选中需要查询的IMSI号", Toast.LENGTH_SHORT).show();
                    }
                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
            }
        });

        whiteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, WhiteList.class);
                startActivity(intent);
            }
        });

        setUp.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                int typeNum = 0;
                AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this, AlertDialog.THEME_HOLO_LIGHT);
                LayoutInflater factory = LayoutInflater.from(HomePage.this);
                View textEntryView = factory.inflate(R.layout.set_up, null);
                textEntryView.setBackgroundColor(Color.argb(20, 255, 255, 240));

                tdd = textEntryView.findViewById(R.id.tdd);
                fdd = textEntryView.findViewById(R.id.fdd);
                bandMsgTdd = textEntryView.findViewById(R.id.bandMsgTdd);
                bandMsgFdd = textEntryView.findViewById(R.id.bandMsgFdd);
                ulfreqTdd = textEntryView.findViewById(R.id.ulfreqTdd);
                ulfreqFdd = textEntryView.findViewById(R.id.ulfreqFdd);
                dlfreqTdd = textEntryView.findViewById(R.id.dlfreqTdd);
                dlfreqFdd = textEntryView.findViewById(R.id.dlfreqFdd);
                pciTdd = textEntryView.findViewById(R.id.pciTdd);
                pciFdd = textEntryView.findViewById(R.id.pciFdd);

                ArrayList<String> dataBand = new ArrayList<>();
                dataBand.add("38-YD");
                dataBand.add("39-YD");
                dataBand.add("40-YD");
                ArrayAdapter<String> arrayAdapterTdd = new ArrayAdapter<String>(HomePage.this, R.layout.spinner_item, dataBand);
                bandMsgTdd.setAdapter(arrayAdapterTdd);

                dataBand = new ArrayList<>();
                dataBand.add("1-DX");
                dataBand.add("3-DX");
                dataBand.add("3-LT");
                ArrayAdapter<String> arrayAdapterFdd = new ArrayAdapter<String>(HomePage.this, R.layout.spinner_item, dataBand);
                bandMsgFdd.setAdapter(arrayAdapterFdd);

                try {
                    db = sqliteUse.getReadableDatabase();
                    Cursor cursor = db.query("freqMsg", null, null, null, null, null, null, null);
                    if (cursor.moveToFirst()) {
                        do {
                            System.out.println("+++++++++" + Integer.parseInt(cursor.getString(cursor.getColumnIndex("band"))));
                            System.out.println(cursor.getString(cursor.getColumnIndex("ulfreq")));
                            System.out.println(cursor.getString(cursor.getColumnIndex("dlfreq")));
                            System.out.println(cursor.getString(cursor.getColumnIndex("pci")));
                            System.out.println("cellStatu:" + Integer.parseInt(cursor.getString(cursor.getColumnIndex("cellStatu"))));
                            typeNum++;

                            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex("typeCode"))) == 8) {
                                ulfreqTdd.setText(cursor.getString(cursor.getColumnIndex("ulfreq")));
                                dlfreqTdd.setText(cursor.getString(cursor.getColumnIndex("dlfreq")));
                                pciTdd.setText(cursor.getString(cursor.getColumnIndex("pci")));

                                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex("cellStatu"))) == 1) {
                                    tdd.setTextColor(Color.parseColor("#00ce41"));
                                }

                                switch (Integer.parseInt(cursor.getString(cursor.getColumnIndex("band")))){
                                    case 38:
                                        System.out.println(38);
                                        bandMsgTdd.setSelection(0);
                                        break;
                                    case 39:
                                        System.out.println(39);
                                        bandMsgTdd.setSelection(1);
                                        break;
                                    case 40:
                                        System.out.println(40);
                                        bandMsgTdd.setSelection(2);
                                        break;
                                }
                            }
                            if(Integer.parseInt(cursor.getString(cursor.getColumnIndex("typeCode"))) == 12) {
                                ulfreqFdd.setText(cursor.getString(cursor.getColumnIndex("ulfreq")));
                                dlfreqFdd.setText(cursor.getString(cursor.getColumnIndex("dlfreq")));
                                pciFdd.setText(cursor.getString(cursor.getColumnIndex("pci")));

                                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex("cellStatu"))) == 1) {
                                    fdd.setTextColor(Color.parseColor("#00ce41"));
                                }

                                switch (Integer.parseInt(cursor.getString(cursor.getColumnIndex("band")))){
                                    case 1:
                                        System.out.println(1);
                                        bandMsgFdd.setSelection(0);
                                        break;
                                    case 3:
                                        System.out.println(3);
                                        bandMsgFdd.setSelection(1);
                                        if(Integer.parseInt(cursor.getString(cursor.getColumnIndex("ulfreq"))) == 19650) {
                                            bandMsgFdd.setSelection(2);
                                        }
                                        break;
                                }
                            }
                        } while (cursor.moveToNext());
                    }
                    db.close();
                    cursor.close();
                }catch (Exception e) {
                    System.out.println(e);
                }

                final int finalTypeNum = typeNum;
                final int[] flag = {0};

                bandMsgTdd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(flag[0] < 2)
                            flag[0]++;
                        else if(finalTypeNum == 2) {
                            System.out.println("bandMsgTdd spinner");
                            switch ((String) bandMsgTdd.getSelectedItem()) {
                                case "38-YD":
                                    ulfreqTdd.setText("37900");
                                    dlfreqTdd.setText("37900");
                                    pciTdd.setText("333");
                                    break;
                                case "39-YD":
                                    ulfreqTdd.setText("38400");
                                    dlfreqTdd.setText("38400");
                                    pciTdd.setText("333");
                                    break;
                                case "40-YD":
                                    ulfreqTdd.setText("38950");
                                    dlfreqTdd.setText("38950");
                                    pciTdd.setText("333");
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                bandMsgFdd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(flag[0] < 2)
                            flag[0]++;
                        else if(finalTypeNum == 2) {
                            System.out.println("bandMsgFdd spinner");
                            switch ((String) bandMsgFdd.getSelectedItem()) {
                                case "1-DX":
                                    ulfreqFdd.setText("18100");
                                    dlfreqFdd.setText("100");
                                    pciTdd.setText("333");
                                    break;
                                case "3-DX":
                                    ulfreqFdd.setText("19825");
                                    dlfreqFdd.setText("1825");
                                    pciTdd.setText("333");
                                    break;
                                case "3-LT":
                                    ulfreqFdd.setText("19650");
                                    dlfreqFdd.setText("1650");
                                    pciTdd.setText("333");
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                builder.setTitle("无线参数配置");
                builder.setView(textEntryView);
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("开始保存配置" + fileCreate.read_status("SQLITE_LOCK"));
                        waitsql();
                        try {
                            SQLiteDatabase db;
                            db = sqliteUse.getReadableDatabase();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("ulfreq", ulfreqTdd.getEditableText().toString());
                            contentValues.put("dlfreq", dlfreqTdd.getEditableText().toString());
                            contentValues.put("pci", pciTdd.getEditableText().toString());
                            contentValues.put("band", String.valueOf(bandMsgTdd.getSelectedItem()).split("-", 2)[0]);
                            db.update("freqMsg", contentValues, "typeCode=?", new String[]{"8"});

                            contentValues = new ContentValues();
                            contentValues.put("ulfreq", ulfreqFdd.getEditableText().toString());
                            contentValues.put("dlfreq", dlfreqFdd.getEditableText().toString());
                            contentValues.put("pci", pciFdd.getEditableText().toString());
                            contentValues.put("band", String.valueOf(bandMsgFdd.getSelectedItem()).split("-", 2)[0]);
                            db.update("freqMsg", contentValues, "typeCode=?", new String[]{"12"});

                            db.close();

//                            fileCreate.flagSet("CHANGE");
                            fileCreate.write_status("WIRELESS_SET", 1);

                            System.out.println("TDD TYPE:" + String.valueOf(bandMsgTdd.getSelectedItem()).split("-", 2)[0]
                                    + "ulfreq" + ulfreqTdd.getEditableText().toString()
                                    + "dlfreq" + dlfreqTdd.getEditableText().toString());
                            System.out.println("FDD TYPE:" + String.valueOf(bandMsgFdd.getSelectedItem()).split("-", 2)[0]
                                    + "ulfreq" + ulfreqFdd.getEditableText().toString()
                                    + "dlfreq" + dlfreqFdd.getEditableText().toString());
                        }
                        catch (Exception e) {
                            System.out.println("保存失败" + e);
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();// 显示对话框
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        arrowScroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollStatus = 1;
            }
        });
    }

    //回退功能
    public void onBackPressed() {
        //super.onBackPressed();//注释掉这行,back键不退出activity
        AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("确认退出");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

//    //屏幕监听事件
//    @Override
//    public boolean onTouchEvent(MotionEvent Event){
//        switch (Event.getAction()){
//            case MotionEvent.ACTION_OUTSIDE:
//                break;
//            case MotionEvent.ACTION_DOWN:
//                aSwitch.setChecked(false);
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        return super.onTouchEvent(Event);
//    }

    @Override
    public int getLayoutId() {
        return (R.layout.home_page);
    }
}

//        fileCreate.write_status("SINGER_APP_CONNECTED", 1);
//        fileCreate.write_status("SMTPS_CONNECTED", 1);
//        fileCreate.write_status("WIRELESS_SET", 1);
//        fileCreate.write_status("SQLITE_LOCK", 1);
//        fileCreate.write_status("POWER_ENTERED", 1);
//        fileCreate.write_status("POLL_TYPE", 1);
//
//        fileCreate.read_status("SINGER_APP_CONNECTED");
//        fileCreate.read_status("SMTPS_CONNECTED");
//        fileCreate.read_status("WIRELESS_SET");
//        fileCreate.read_status("SQLITE_LOCK");
//        fileCreate.read_status("POWER_ENTERED");
//        fileCreate.read_status("POLL_TYPE");