package com.example.kunrui.kunrui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kunrui.kunrui.Resource.RadarView;

import java.util.ArrayList;
import java.util.List;

public class PowerChart extends TitleActivity {
    public class CustomCurveChart extends View {
        // 坐标单位
        private String[] xLabel;
        private String[] yLabel;
        // 曲线数据
        private List<int[]> dataList;
        private List<Integer> colorList;
        private boolean showValue;
        // 默认边距
        private int margin = 20;
        // 距离左边偏移量
        private int marginX = 30;
        // 原点坐标
        private int xPoint;
        private int yPoint;
        // X,Y轴的单位长度
        private int xScale;
        private int yScale;
        // 画笔
        private Paint paintAxes;
        private Paint paintCoordinate;
        private Paint paintTable;
        private Paint paintCurve;
        private Paint paintRectF;
        private Paint paintValue;

        public CustomCurveChart(Context context, String[] xLabel, String[] yLabel,
                                List<int[]> dataList, List<Integer> colorList, boolean showValue) {
            super(context);
            this.xLabel = xLabel;
            this.yLabel = yLabel;
            this.dataList = dataList;
            this.colorList = colorList;
            this.showValue = showValue;
        }

        public CustomCurveChart(Context context) {
            super(context);
        }

        /**
         * 初始化数据值和画笔
         */
        public void init() {
            xPoint = margin + marginX;
            yPoint = this.getHeight() - margin;
            xScale = (this.getWidth() - 2 * margin - marginX) / (xLabel.length - 1);
            yScale = (this.getHeight() - 2 * margin) / (yLabel.length - 1);

            paintAxes = new Paint();
            paintAxes.setStyle(Paint.Style.STROKE);
            paintAxes.setAntiAlias(true);
            paintAxes.setDither(true);
            paintAxes.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            paintAxes.setStrokeWidth(4);

            paintCoordinate = new Paint();
            paintCoordinate.setStyle(Paint.Style.STROKE);
            paintCoordinate.setDither(true);
            paintCoordinate.setAntiAlias(true);
            paintCoordinate.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            paintCoordinate.setTextSize(15);

            paintTable = new Paint();
            paintTable.setStyle(Paint.Style.STROKE);
            paintTable.setAntiAlias(true);
            paintTable.setDither(true);
            paintTable.setColor(ContextCompat.getColor(getContext(), R.color.Gray));
            paintTable.setStrokeWidth(2);

            paintCurve = new Paint();
            paintCurve.setStyle(Paint.Style.STROKE);
            paintCurve.setDither(true);
            paintCurve.setAntiAlias(true);
            paintCurve.setStrokeWidth(3);
            PathEffect pathEffect = new CornerPathEffect(25);
            paintCurve.setPathEffect(pathEffect);

            paintRectF = new Paint();
            paintRectF.setStyle(Paint.Style.FILL);
            paintRectF.setDither(true);
            paintRectF.setAntiAlias(true);
            paintRectF.setStrokeWidth(3);

            paintValue = new Paint();
            paintValue.setStyle(Paint.Style.STROKE);
            paintValue.setAntiAlias(true);
            paintValue.setDither(true);
            paintValue.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            paintValue.setTextAlign(Paint.Align.CENTER);
            paintValue.setTextSize(15);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(ContextCompat.getColor(getContext(), R.color.colorGray));
            init();
            drawTable(canvas, paintTable);
            drawAxesLine(canvas, paintAxes);
            drawCoordinate(canvas, paintCoordinate);
            for (int i = 0; i < dataList.size(); i++) {
                drawCurve(canvas, paintCurve, dataList.get(i), colorList.get(i));
                if (showValue) {
                    drawValue(canvas, paintRectF, dataList.get(i), colorList.get(i));
                }
            }
        }

        /**
         * 绘制坐标轴
         */
        private void drawAxesLine(Canvas canvas, Paint paint) {
            // X
            canvas.drawLine(xPoint, yPoint, this.getWidth() - margin / 6, yPoint, paint);
            canvas.drawLine(this.getWidth() - margin / 6, yPoint, this.getWidth() - margin / 2, yPoint - margin / 3, paint);
            canvas.drawLine(this.getWidth() - margin / 6, yPoint, this.getWidth() - margin / 2, yPoint + margin / 3, paint);

            // Y
            canvas.drawLine(xPoint, yPoint, xPoint, margin / 6, paint);
            canvas.drawLine(xPoint, margin / 6, xPoint - margin / 3, margin / 2, paint);
            canvas.drawLine(xPoint, margin / 6, xPoint + margin / 3, margin / 2, paint);
        }

        /**
         * 绘制表格
         */
        private void drawTable(Canvas canvas, Paint paint) {
            Path path = new Path();
            // 横向线
            for (int i = 1; (yPoint - i * yScale) >= margin; i++) {
                int startX = xPoint;
                int startY = yPoint - i * yScale;
                int stopX = xPoint + (xLabel.length - 1) * xScale;
                path.moveTo(startX, startY);
                path.lineTo(stopX, startY);
                canvas.drawPath(path, paint);
            }

            // 纵向线
            for (int i = 1; i * xScale <= (this.getWidth() - margin); i++) {
                int startX = xPoint + i * xScale;
                int startY = yPoint;
                int stopY = yPoint - (yLabel.length - 1) * yScale;
                path.moveTo(startX, startY);
                path.lineTo(startX, stopY);
                canvas.drawPath(path, paint);
            }
        }

        /**
         * 绘制刻度
         */
        private void drawCoordinate(Canvas canvas, Paint paint) {
            // X轴坐标
            for (int i = 0; i <= (xLabel.length - 1); i++) {
                paint.setTextAlign(Paint.Align.CENTER);
                int startX = xPoint + i * xScale;
                canvas.drawText(xLabel[i], startX, this.getHeight() - margin / 6, paint);
            }

            // Y轴坐标
            for (int i = 0; i <= (yLabel.length - 1); i++) {
                paint.setTextAlign(Paint.Align.LEFT);
                int startY = yPoint - i * yScale;
                int offsetX;
                switch (yLabel[i].length()) {
                    case 1:
                        offsetX = 28;
                        break;

                    case 2:
                        offsetX = 20;
                        break;

                    case 3:
                        offsetX = 12;
                        break;

                    case 4:
                        offsetX = 5;
                        break;

                    default:
                        offsetX = 0;
                        break;
                }
                int offsetY;
                if (i == 0) {
                    offsetY = 0;
                } else {
                    offsetY = margin / 5;
                }
                // x默认是字符串的左边在屏幕的位置，y默认是字符串是字符串的baseline在屏幕上的位置
                canvas.drawText(yLabel[i], margin / 4 + offsetX, startY + offsetY, paint);
            }
        }

        /**
         * 绘制曲线
         */
        private void drawCurve(Canvas canvas, Paint paint, int[] data, int color) {
            paint.setColor(ContextCompat.getColor(getContext(), color));
            Path path = new Path();
            for (int i = 0; i <= (number - 1); i++) {
                if (i == 0) {
                    path.moveTo(xPoint, toY(data[0]));
                } else {
                    path.lineTo(xPoint + i * xScale, toY(data[i]));
                }

                if (i == xLabel.length - 1) {
                    path.lineTo(xPoint + i * xScale, toY(data[i]));
                }
            }
            canvas.drawPath(path, paint);
        }

        /**
         * 绘制数值
         */
        private void drawValue(Canvas canvas, Paint paint, int data[], int color) {
            paint.setColor(ContextCompat.getColor(getContext(), color));
            for (int i = 1; i <= (xLabel.length - 1); i++) {
                RectF rect;
                if (toY(data[i - 1]) < toY(data[i])) {
                    rect = new RectF(xPoint + i * xScale - 20, toY(data[i]) - 15,
                            xPoint + i * xScale + 20, toY(data[i]) + 5);
                    canvas.drawRoundRect(rect, 5, 5, paint);
                    canvas.drawText(data[i] + "w", xPoint + i * xScale, toY(data[i]), paintValue);
                } else if (toY(data[i - 1]) > toY(data[i])) {
                    rect = new RectF(xPoint + i * xScale - 20, toY(data[i]) - 5,
                            xPoint + i * xScale + 20, toY(data[i]) + 15);
                    canvas.drawRoundRect(rect, 5, 5, paint);
                    canvas.drawText(data[i] + "w", xPoint + i * xScale, toY(data[i]) + 10, paintValue);
                } else {
                    rect = new RectF(xPoint + i * xScale - 20, toY(data[i]) - 10,
                            xPoint + i * xScale + 20, toY(data[i]) + 10);
                    canvas.drawRoundRect(rect, 5, 5, paint);
                    canvas.drawText(data[i] + "w", xPoint + i * xScale, toY(data[i]) + 5, paintValue);
                }
            }
        }

        /**
         * 数据按比例转坐标
         */
        private float toY(int num) {
            float y;
            try {
                float a = (float) num / 100.0f;
                y = yPoint - a * yScale;
            } catch (Exception e) {
                return 0;
            }
            return y;
        }

    }

    private RadarView radarView;

    private void getNList() {
        Intent intent = getIntent();
        data_list = intent.getStringArrayListExtra("dataList");
    }

    private void waitSql() {
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

    private void insertImsi(String IMSI){
        waitSql();
        int ulfreqTdd = 0, dlfreqTdd = 0, pciTdd = 0, bandTdd = 0, ulfreqFdd = 0, dlfreqFdd = 0, pciFdd = 0, bandFdd = 0;
        int ulfreqTdd_t, dlfreqTdd_t, pciTdd_t, bandTdd_t, ulfreqFdd_t, dlfreqFdd_t, pciFdd_t, bandFdd_t;
        int modelType = 0, No = 0;
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = sqliteUse.getWritableDatabase();

            cursor = db.query("freqMsg", null, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex("typeCode"))) == 8) {
                        ulfreqTdd = cursor.getInt(cursor.getColumnIndex("ulfreq"));
                        dlfreqTdd = cursor.getInt(cursor.getColumnIndex("dlfreq"));
                        pciTdd = cursor.getInt(cursor.getColumnIndex("pci"));
                        bandTdd = cursor.getInt(cursor.getColumnIndex("band"));
                    } else if (Integer.parseInt(cursor.getString(cursor.getColumnIndex("typeCode"))) == 12) {
                        ulfreqFdd = cursor.getInt(cursor.getColumnIndex("ulfreq"));
                        dlfreqFdd = cursor.getInt(cursor.getColumnIndex("dlfreq"));
                        pciFdd = cursor.getInt(cursor.getColumnIndex("pci"));
                        bandFdd = cursor.getInt(cursor.getColumnIndex("band"));
                    }
                } while (cursor.moveToNext());
            }

            cursor = db.query("imsiMsg", null, "imsi = ?", new String[]{IMSI}, null, null, null);
            if(cursor.moveToFirst()){
                ContentValues contentValues;

                do{
                    ulfreqTdd_t = cursor.getInt(cursor.getColumnIndex("ulfreqTdd"));
                    dlfreqTdd_t = cursor.getInt(cursor.getColumnIndex("dlfreqTdd"));
                    pciTdd_t = cursor.getInt(cursor.getColumnIndex("pciTdd"));
                    bandTdd_t = cursor.getInt(cursor.getColumnIndex("bandTdd"));

                    ulfreqFdd_t = cursor.getInt(cursor.getColumnIndex("ulfreqFdd"));
                    dlfreqFdd_t = cursor.getInt(cursor.getColumnIndex("dlfreqFdd"));
                    pciFdd_t = cursor.getInt(cursor.getColumnIndex("pciFdd"));
                    bandFdd_t = cursor.getInt(cursor.getColumnIndex("bandFdd"));

                    modelType = cursor.getInt(cursor.getColumnIndex("modelType"));
                    System.out.println("modelType：" + modelType);

                    if(ulfreqTdd != ulfreqTdd_t || ulfreqFdd != ulfreqFdd_t ||
                            dlfreqTdd != dlfreqTdd_t || dlfreqFdd != dlfreqFdd_t || 
                            pciTdd != pciTdd_t || pciFdd != pciFdd_t ||
                            bandTdd != bandTdd_t || bandFdd != bandFdd_t
                            ) {
                        if(ulfreqTdd_t != 0 && dlfreqTdd_t !=0 && bandTdd_t != 0 && pciTdd_t !=0 &&
                                ulfreqFdd_t != 0 && dlfreqFdd_t != 0 && bandFdd_t != 0 && pciFdd_t !=0 &&
                                ulfreqTdd != 0 && dlfreqTdd !=0 && bandTdd != 0 && pciTdd !=0 &&
                                ulfreqFdd != 0 && dlfreqFdd != 0 && bandFdd != 0 && pciFdd !=0) {
                            fileCreate.write_status("WIRELESS_SET", 1);
                            System.out.println("修改无线参数");
                            contentValues = new ContentValues();
                            contentValues.put("ulfreq", cursor.getString(cursor.getColumnIndex("ulfreqTdd")));
                            contentValues.put("dlfreq", cursor.getString(cursor.getColumnIndex("dlfreqTdd")));
                            contentValues.put("pci", cursor.getString(cursor.getColumnIndex("pciTdd")));
                            contentValues.put("band", cursor.getString(cursor.getColumnIndex("bandTdd")));
                            db.update("freqMsg", contentValues, "typeCode=?", new String[]{"8"});

                            contentValues = new ContentValues();
                            contentValues.put("ulfreq", cursor.getString(cursor.getColumnIndex("ulfreqFdd")));
                            contentValues.put("dlfreq", cursor.getString(cursor.getColumnIndex("dlfreqFdd")));
                            contentValues.put("pci", cursor.getString(cursor.getColumnIndex("pciFdd")));
                            contentValues.put("band", cursor.getString(cursor.getColumnIndex("bandFdd")));
                            db.update("freqMsg", contentValues, "typeCode=?", new String[]{"12"});

                            break;
                        }
                    }
                    No++;
                } while (cursor.moveToNext() && No < 3);
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put("imsi", IMSI);
            contentValues.put("modelType", modelType);
            System.out.println("insertImsi, IMSI: " + IMSI + " modelType: " + modelType);
            db.update("freqMsg", contentValues, null, null);

            cursor.close();
            db.close();
            fileCreate.write_status("SQLITE_LOCK", 0);
        }catch (Exception e) {
            fileCreate.write_status("SQLITE_LOCK", 0);
            System.out.println("insertImsi" + e);
        }
    }

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
    private Button loading;
    private TextView time_position;
    private Button setUp;
    private int time_out = 0;
    private int oldValue = 0;
    private double angle = 0;
    private double powerSize = 0;
    private LinearLayout customCurveChart;
    private String RSSI = "";
//    private int No = 1;
//    private int powerStatus = 1;
    private sqliteUse sqliteUse = new sqliteUse(this, "smtp.db", null, 1);
    private ArrayList<String> data_list = new ArrayList<String>();
    private ArrayList<String> dataBand = new ArrayList<>();
    private String IMSI = null;
    private fileCreate fileCreate = new fileCreate();
    private TextToSpeech TTsSpeak;  //创建TTS对象
    private Spinner spinner;
    TextView valueLeiDa;
    private int[] valueList = new int[11];
    private int[] valueList_t;
    private int powerNum = 0, number = 0;
    private EditText editText;
    private int loadingStatus = 1;

    //dp单位转换成px单位
    public static int dp2px(Context context, float dpValue){
        return (int)(dpValue * (context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void setEditAdd(int width_t, ArrayList Msg, final int i, int status)
    {
        String values = String.valueOf(Msg.get(i));
        int height=dp2px(PowerChart.this, 40);
        int margin=dp2px(PowerChart.this, 0.5f);
        int padding=dp2px(PowerChart.this, 2);
        int width = dp2px(PowerChart.this, width_t);

        editText=new EditText(PowerChart.this);
        editText.setSingleLine();
        editText.setTextColor(Color.parseColor("#FFFFFF"));
//        editText.setBackgroundColor(Color.parseColor("#6495ED"));
        android.widget.TableRow.LayoutParams params=new android.widget.TableRow.LayoutParams(width, height);
        params.weight=1;
        editText.setGravity(Gravity.CENTER);
        params.setMargins(margin,margin,margin,margin);
        editText.setLayoutParams(params);
        editText.setPadding(padding,padding,padding,padding);
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setTextSize(14);

        //设置不可编辑但有点击
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);

        if(status == 0) {
            editText.setBackgroundResource(R.drawable.delete);
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = (int) spinner.getSelectedItemId();
                    if(id != i) {
                        LinearLayout linearLayout = findViewById(R.id.imsiListRepShow);
                        linearLayout.removeAllViews();
                        data_list.remove(i);
                        addImsiRep(data_list);
                        if(id > i) {
                            id = id - 1;
                        }
                        ArrayAdapter<String> arr_adapter= new ArrayAdapter<>(PowerChart.this, R.layout.simple_spinner_item, data_list);
                        //加载适配器
                        spinner.setAdapter(arr_adapter);
                        spinner.setSelection(id);
                    }else {
                        Toast.makeText(getApplicationContext(), "当前选中目标不可删除", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
            editText.setText(values);
    }

    public void addImsiRep(ArrayList data_list){
        for(int i = 0; i< data_list.size(); i++) {
            LinearLayout linearLayout = findViewById(R.id.imsiListRepShow);
            TableRow tableRow = new TableRow(PowerChart.this);
            tableRow.setBackgroundColor(Color.parseColor("#0d122e"));

            setEditAdd(0, data_list, i, 0);
            tableRow.addView(editText);

            setEditAdd(330, data_list, i, 1);
            tableRow.addView(editText);

            linearLayout.addView(tableRow);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_chart);
        initView(0);//初始化标题

        loading = findViewById(R.id.loading);
        time_position = findViewById(R.id.time_position);
        setUp = findViewById(R.id.setUp);
        setUp.setVisibility(View.VISIBLE);

        valueLeiDa = findViewById(R.id.valueLeiDa);
        //雷达页面初始化
        radarView = findViewById(R.id.radar);
        radarView.start();
        radarView.Init(0, 0, 0);

        TTsSpeak = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        TTsSpeak.setPitch(1.5f);
        getNList();
        addImsiRep(data_list);
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> arr_adapter= new ArrayAdapter<String>(this, R.layout.simple_spinner_item, data_list);
        //加载适配器
        spinner.setAdapter(arr_adapter);
        spinner.setSelection(0);
        final TextView value = findViewById(R.id.value);
        IMSI = (String) spinner.getSelectedItem();
        fileCreate.setRssi("0");
        //线程使用界面资源
        @SuppressLint("HandlerLeak") final Handler mHandler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        try {
                            IMSI = (String) spinner.getSelectedItem();
                            insertImsi(IMSI);
                            if (!RSSI.isEmpty()) {
                                System.out.println(RSSI + "---");
                                if (Float.parseFloat(RSSI) < 0.01 && Float.parseFloat(RSSI) >= 0) {
                                    System.out.println("没有记录rssi");
                                    oldValue = 0;
                                    value.setText("0");
                                    valueLeiDa.setText("");
                                    angle = Math.random() * Math.PI * 2;//随机角度
                                    radarView.Init(powerSize, angle, 0);
                                    time_position.setVisibility(View.VISIBLE);
                                    loading.setVisibility(View.VISIBLE);
                                } else {
                                    time_position.setVisibility(View.INVISIBLE);
                                    loading.setVisibility(View.INVISIBLE);
                                    int powerValue = (int) (((Float.parseFloat(RSSI) + 144) * 100) / 110);
                                    if (powerValue > 100) powerValue = 100;
//                                    if (oldValue != 0) {
//                                        powerValue = (int) (oldValue * 0.7 + powerValue * 0.3);
//                                    }


                                    if (oldValue != powerValue && powerValue > 30) {
                                        time_out = 0;
                                        oldValue = powerValue;

                                        String powerNum_t = String.valueOf(powerValue);
                                        //System.out.println((int) (((Float.parseFloat(RSSI) + 140) * 100) / 110));
                                        value.setText(powerNum_t);
                                        //写信号强度与距离转换公式
                                        TTsSpeak.speak(powerNum_t, TextToSpeech.QUEUE_ADD, null);
                                        powerSize = 1 - Double.parseDouble(powerNum_t) / 100;
                                        System.out.println("oldValue:" + oldValue + "powerValue:" + powerValue);
                                        System.out.println("powerSize:" + powerSize + "powerNum_t" + powerNum_t + "Double.parseDouble(powerNum_t)" + Double.parseDouble(powerNum_t) + "Double.parseDouble(powerNum_t)/100" + Double.parseDouble(powerNum_t) / 100);
                                        radarView.Init(powerSize, angle, 1);
                                        valueLeiDa.setText(powerNum_t);
                                        //折线图显示
                                        powerNum = powerValue;
                                        if (number < 11) {
                                            valueList_t = new int[number + 1];
                                            valueList[number] = powerNum * 10;
                                            System.arraycopy(valueList, 0, valueList_t, 0, number + 1);
                                            initCurveChart(valueList_t);
                                            number++;
                                        } else {
                                            System.arraycopy(valueList, 1, valueList, 0, 10);
                                            valueList[10] = powerNum * 10;
                                            initCurveChart(valueList);
                                        }
                                    } else {
                                        //半分钟内如果数据没有更新， 代表IMSI不在范围内
                                        time_out++;
                                        if(time_out > 20) {
                                            time_out = 0;//时间
                                            oldValue = 0;//次数
                                            fileCreate.setRssi("0.0000");
                                        }
                                    }
                                }
                            }
                        }catch (Exception e) {
                            System.out.println("mHandle 崩溃1");
                        }
                        break;
                    case 2:
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
                }
            }
        };

        final Thread getRssi = new Thread(){
//            private String strPower = "";
//            private boolean powerStatus = true;
            public void run() {
                while (fileCreate.read_status("POWER_ENTERED") == 1) {
                    try {
                        RSSI = fileCreate.readFile(RSSI, "getRssi.txt");
                        mHandler.sendEmptyMessage(0);
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("读取状态信息， powerStatus");
//                    powerStatus = fileCreate.readFile(strPower, "powerStatus.txt").equals("Open ");
                }
                System.out.println("getRssi线程退出");
            }
        };
        getRssi.start();

        new Thread() {
            private String strPower = "";
            public void run() {
                while (fileCreate.read_status("POWER_ENTERED") == 1) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(2);
                }
            }
        }.start();

        TextView title = findViewById(R.id.title);
        title.setText("详细能量图");

        value.setText("88");
        customCurveChart = findViewById(R.id.customCurveChart);
        initCurveChart(valueList);

        Button revert = findViewById(R.id.revert);
        revert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TTsSpeak.shutdown();
//                powerStatus = 1314;
                System.out.println("回退事件------------>");
//                fileCreate.powerStatus("Close");
                fileCreate.write_status("POWER_ENTERED", 0);
                Intent intent = new Intent(PowerChart.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        Button chose = findViewById(R.id.chose);
        chose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            }
        });

        Button exChange = findViewById(R.id.exChange);
        exChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout curveChart = findViewById(R.id.curveChart);
                View rader = findViewById(R.id.radar);
                if(curveChart.getVisibility() == View.INVISIBLE)
                {
                    curveChart.setVisibility(View.VISIBLE);
                    rader.setVisibility(View.INVISIBLE);
                    valueLeiDa.setVisibility(View.INVISIBLE);
                }else if(curveChart.getVisibility() == View.VISIBLE){
                    curveChart.setVisibility(View.INVISIBLE);
                    rader.setVisibility(View.VISIBLE);
                    valueLeiDa.setVisibility(View.VISIBLE);
                }
            }
        });

        setUp.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                int typeNum = 0;
                AlertDialog.Builder builder = new AlertDialog.Builder(PowerChart.this, AlertDialog.THEME_HOLO_LIGHT);
                LayoutInflater factory = LayoutInflater.from(PowerChart.this);
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

                dataBand = new ArrayList<>();
                dataBand.add("38-YD");
                dataBand.add("39-YD");
                dataBand.add("40-YD");
                ArrayAdapter<String> arrayAdapterTdd = new ArrayAdapter<String>(PowerChart.this, R.layout.spinner_item, dataBand);
                bandMsgTdd.setAdapter(arrayAdapterTdd);

                dataBand = new ArrayList<>();
                dataBand.add("1-DX");
                dataBand.add("3-DX");
                dataBand.add("3-LT");
                ArrayAdapter<String> arrayAdapterFdd = new ArrayAdapter<String>(PowerChart.this, R.layout.spinner_item, dataBand);
                bandMsgFdd.setAdapter(arrayAdapterFdd);

                try {
                    SQLiteDatabase db;
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
                                    ulfreqTdd.setText("39148");
                                    dlfreqTdd.setText("39148");
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
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        String sqlStatus = "";
                        System.out.println("开始保存配置" + fileCreate.read_status("SQLITE_LOCK"));
                        while(fileCreate.read_status("SQLITE_LOCK") == 1){
                            System.out.println("被bulkDumpSql占用");
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
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

                            System.out.println("String.valueOf(bandMsgTdd.getSelectedItem()).split(\"-\", 2)[0]:" + String.valueOf(bandMsgTdd.getSelectedItem()).split("-", 2)[0]);
                            System.out.println("String.valueOf(bandMsgFdd.getSelectedItem()).split(\"-\", 2)[0]:" + String.valueOf(bandMsgFdd.getSelectedItem()).split("-", 2)[0]);
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
    }

    @Override
    public int getLayoutId() {
        return (R.layout.power_chart);
    }

    private void initCurveChart(int[] value) {
        customCurveChart.removeAllViews();
        String[] xLabel = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
        String[] yLabel = {"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
        List<int[]> data = new ArrayList<>();
        List<Integer> color = new ArrayList<>();
        data.add(value);
        color.add(R.color.colorPrimary);
        customCurveChart.addView(new CustomCurveChart(this, xLabel, yLabel, data, color, false));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();//注释掉这行,back键不退出activity
        TTsSpeak.shutdown();
//        powerStatus = 1314;
        System.out.println("回退事件------------");
        fileCreate.write_status("POWER_ENTERED", 0);
    }
}
