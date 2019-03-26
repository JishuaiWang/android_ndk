package com.example.kunrui.kunrui;

import com.example.kunrui.kunrui.Resource.excelRead;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WhiteList extends TitleActivity{
    private fileCreate fileCreate = new fileCreate();
    private sqliteUse sqliteUse;
    private EditText editText;
    private SQLiteDatabase db;
    private excelRead excelread = new excelRead();
    private int ID = 0, No = -1;
    private String imsiMsg = "";
    private String RemarkMsg = "";
    private String Name = "";
    private String Unit = "";
    private String Phone = "";
    private String Carrier = "";
    private int [][]delete_list;
    private ArrayList<String> data_list = new ArrayList<>();

//    private void initWhite () {
//        SQLiteDatabase db;
//        db = sqliteUse.getReadableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("imsi", "123456789112345");
//        contentValues.put("Remarks","黑龙江");
//        db.insert("whiteList", null, contentValues);
//        System.out.println("initWhite");
//    }

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

    //获取名单
    private void getNList() {
        System.out.println("getNList");
        ID = 0;
        data_list.clear();
        LinearLayout linearLayout = findViewById(R.id.whiteList);
        linearLayout.removeAllViews();
        try {
            db = sqliteUse.getReadableDatabase();
            Cursor cursor = db.query("whiteList", null, null, null, null, null, null);
            //调用moveToFirst()将数据指针移动到第一行的位置。
            if (cursor.moveToFirst()) {
                do {
                    ID++;
                    String IMSI = cursor.getString(cursor.getColumnIndex("imsi"));
                    String REMARKS = cursor.getString(cursor.getColumnIndex("Remarks"));
                    String NAME = cursor.getString(cursor.getColumnIndex("name"));
                    String PHONENUM = cursor.getString(cursor.getColumnIndex("phoneNumber"));
                    String UNIT = cursor.getString(cursor.getColumnIndex("unit"));
                    String CARRIER = cursor.getString(cursor.getColumnIndex("carrier"));
                    ArrayList<String> Msg = new ArrayList<>();
                    Msg.add(String.valueOf(ID));
                    Msg.add(NAME);
                    Msg.add(UNIT);
                    Msg.add(IMSI);
                    Msg.add(PHONENUM);
                    Msg.add(CARRIER);
                    Msg.add(REMARKS);
                    //创建一行
                    TableRow tableRow=new TableRow(WhiteList.this);

                    //这里一定要用android.widget.TableRow.LayoutParams，不然TableRow不显示
                    tableRow.setLayoutParams(new LinearLayout.LayoutParams(android.widget.TableRow.LayoutParams.MATCH_PARENT, android.widget.TableRow.LayoutParams.MATCH_PARENT));

                    setEditAdd(5, Msg, 0);
                    tableRow.addView(editText);
                    //然后通过Cursor的getColumnIndex()获取某一列中所对应的位置的索引

                    setEditAdd(18,  Msg, 1);
                    tableRow.addView(editText);
                    data_list.add(IMSI);

                    setEditAdd(35,  Msg, 2);
                    tableRow.addView(editText);


                    setEditAdd(45,  Msg, 3);
                    tableRow.addView(editText);


                    setEditAdd(30,  Msg, 4);
                    tableRow.addView(editText);


                    setEditAdd(18,  Msg, 5);
                    tableRow.addView(editText);

                    setEditAdd(18,  Msg, 6);
                    tableRow.addView(editText);

                    //将一行数据添加到表格中
                    linearLayout.addView(tableRow);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            System.out.println("getNList:" + e);
        }

        delete_list = new int[ID][2];
        for(int i = 0; i<ID; i++) {
            delete_list[i][0] = i;
            delete_list[i][1] = 0;
        }
    }

    //dp单位转换成px单位
    public static int dp2px(Context context, float dpValue){
        return (int)(dpValue * (context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void setEditAdd(int width_t, final ArrayList Msg, final int status)
    {
        String value = String.valueOf(Msg.get(status));
        int height=dp2px(WhiteList.this, 40);
        int margin=dp2px(WhiteList.this, 0.5f);
        int padding=dp2px(WhiteList.this, 2);
        int width = dp2px(WhiteList.this, width_t);

        editText=new EditText(WhiteList.this);
        editText.setSingleLine();
        editText.setBackgroundColor(Color.parseColor("#0d122e"));
        android.widget.TableRow.LayoutParams params=new android.widget.TableRow.LayoutParams(width, height);
        params.weight=1;
        editText.setGravity(Gravity.CENTER);
        params.setMargins(margin,margin,margin,margin);
        editText.setLayoutParams(params);
        editText.setPadding(padding,padding,padding,padding);
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setTextSize(13);
        editText.setText(value);
        editText.setTextColor(Color.parseColor("#FFFFFF"));

        //设置不可编辑但有点击
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout linearLayout = findViewById(R.id.whiteList);

                if(status == 0) {
                    TableRow tableRow = (TableRow) linearLayout.getChildAt(Integer.parseInt(String.valueOf(Msg.get(0))) - 1);
                    int flag_del = delete_list[Integer.parseInt(String.valueOf(Msg.get(0))) - 1][1];
                    if(flag_del == 0) {
                        delete_list[Integer.parseInt(String.valueOf(Msg.get(0))) - 1][1] = 1;
                        for(int i = 0; i<6; i++) {
                            EditText editTextDel = (EditText) tableRow.getChildAt(i);
                            editTextDel.setTextColor(Color.rgb(255, 0, 0));
                        }
                    } else if(flag_del == 1) {
                        delete_list[Integer.parseInt(String.valueOf(Msg.get(0))) - 1][1] = 0;
                        for(int i = 0; i<6; i++) {
                            EditText editTextDel = (EditText) tableRow.getChildAt(i);
                            editTextDel.setTextColor(Color.parseColor("#FFFFFF"));
                        }
                    }
                }

                if(status != 0) {
                    Name = String.valueOf(Msg.get(1));
                    Unit = String.valueOf(Msg.get(2));
                    imsiMsg = String.valueOf(Msg.get(3));
                    Phone = String.valueOf(Msg.get(4));
                    Carrier= String.valueOf(Msg.get(5));
                    RemarkMsg = String.valueOf(Msg.get(6));
                    if (No == -1) {
                        No = Integer.parseInt(String.valueOf(Msg.get(0))) - 1;
                        TableRow tableRow = (TableRow) linearLayout.getChildAt(No);
                        for (int i = 0; i < 7; i++) {
                            tableRow.getChildAt(i).setBackgroundColor(Color.parseColor("#00008B"));
                        }
                    } else if (No != Integer.parseInt(String.valueOf(Msg.get(0))) - 1) {
                        TableRow tableRow_old = (TableRow) linearLayout.getChildAt(No);
                        for (int i = 0; i < 7; i++) {
                            tableRow_old.getChildAt(i).setBackgroundColor(Color.parseColor("#0d122e"));
                        }
                        No = Integer.parseInt(String.valueOf(Msg.get(0))) - 1;
                        TableRow tableRow = (TableRow) linearLayout.getChildAt(No);
                        for (int i = 0; i < 7; i++) {
                            tableRow.getChildAt(i).setBackgroundColor(Color.parseColor("#00008B"));
                        }
                    }
                }
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.white_list);
        initView(0);//初始化标题

        TextView title = findViewById(R.id.title);
        title.setText("名单列表");

        sqliteUse = new sqliteUse(this, "smtp.db", null, 1);
        Button insertList = findViewById(R.id.insertList);

        db = sqliteUse.getReadableDatabase();
        sqliteUse.WhitetableCreate(db);
        db.close();
        getNList();
        insertList.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            @Override
            public void onClick(View v) {
                waitSql();
                Map<String, ArrayList> modelCode = new HashMap<>();
                String result = excelread.readExcel(data_list, modelCode);
                if(result.equals("FALSE")) {
                    Toast.makeText(getApplicationContext(), "/sdcard/xml/data.xls 文件不存在", Toast.LENGTH_SHORT).show();
                }

                SQLiteDatabase db;
                db = sqliteUse.getReadableDatabase();
                Set<String> model = modelCode.keySet();
                for(String IMSI: model) {
                    ArrayList MSG_LIST = modelCode.get(IMSI);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("name", String.valueOf(MSG_LIST.get(0)));
                    contentValues.put("unit", String.valueOf(MSG_LIST.get(1)));
                    contentValues.put("imsi", String.valueOf(MSG_LIST.get(2)));
                    contentValues.put("phoneNumber", String.valueOf(MSG_LIST.get(3)));
                    contentValues.put("carrier", String.valueOf(MSG_LIST.get(4)));
                    contentValues.put("Remarks", String.valueOf(MSG_LIST.get(5)));
                    db.insert("whiteList", null, contentValues);
                }
                db.close();

                getNList();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Button revert = findViewById(R.id.revert);
        revert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WhiteList.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //编辑名单
        Button editList = findViewById(R.id.editList);
        editList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imsiMsg.equals("")) {
                    Toast.makeText(getApplicationContext(), "请选中需要编辑的对象", Toast.LENGTH_SHORT).show();
                } else {
                    LayoutInflater factory = LayoutInflater.from(WhiteList.this);
                    final View textEntryView = factory.inflate(R.layout.listalert, null);
                    final EditText editImsi = textEntryView.findViewById(R.id.editTextImsi);
                    final EditText editReverts = textEntryView.findViewById(R.id.editTextReverts);
                    final EditText editUnit = textEntryView.findViewById(R.id.unit);
                    final EditText editName = textEntryView.findViewById(R.id.uniName);
                    final EditText editPhone = textEntryView.findViewById(R.id.alert_phoneNum);
                    final EditText editCarrier = textEntryView.findViewById(R.id.carrier);
                    editImsi.setText(imsiMsg);
                    editReverts.setText(RemarkMsg);
                    editName.setText(Name);
                    editUnit.setText(Unit);
                    editPhone.setText(Phone);
                    editCarrier.setText(Carrier);
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(WhiteList.this);
                    builder.setTitle("编辑:");
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setView(textEntryView);
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            waitSql();
                            try {
                                final String IMSI = String.valueOf(editImsi.getText());
                                final String REMARKS = String.valueOf(editReverts.getText());
                                final String NAME = String.valueOf(editName.getText());
                                final String UNIT = String.valueOf(editUnit.getText());
                                final String PHONENUM = String.valueOf(editPhone.getText());
                                final String CARRIER  = String.valueOf(editCarrier.getText());
                                if (String.valueOf(editImsi.getText()).length() != 15) {
                                    //中文输入没做判断
                                    Toast.makeText(getApplicationContext(), "IMSI号位数输入不正确", Toast.LENGTH_SHORT).show();
                                } else if (IMSI.equals(imsiMsg) && REMARKS.equals(RemarkMsg) && NAME.equals(Name) && UNIT.equals(Unit) && PHONENUM.equals(Phone) && CARRIER.equals(Carrier)) {
                                    Toast.makeText(getApplicationContext(), "名单没有修改", Toast.LENGTH_SHORT).show();
                                } else {
                                    int flag_edit = 1;
                                    for (int i = 0; i < ID; i++) {
                                        if (data_list.get(i).equals(IMSI) && !IMSI.equals(imsiMsg)) {
                                            Toast.makeText(getApplicationContext(), "IMSI已存在", Toast.LENGTH_SHORT).show();
                                            flag_edit = 0;
                                        }
                                    }

                                    if (flag_edit == 1) {
                                        SQLiteDatabase db;
                                        db = sqliteUse.getWritableDatabase();
                                        ContentValues contentValues = new ContentValues();

                                        contentValues.put("name", NAME);
                                        contentValues.put("unit", UNIT);
                                        contentValues.put("imsi", IMSI);
                                        contentValues.put("phoneNumber", PHONENUM);
                                        contentValues.put("carrier", CARRIER);
                                        contentValues.put("Remarks", REMARKS);
                                        //数据库插入失败返回-1
                                        if (db.update("whiteList", contentValues, "imsi=?", new String[]{imsiMsg}) == -1) {
                                            Toast.makeText(getApplicationContext(), "系统错误", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Name = NAME;
                                            Unit = UNIT;
                                            imsiMsg = IMSI;
                                            Phone = PHONENUM;
                                            RemarkMsg = REMARKS;
                                            Carrier = CARRIER;
                                            LinearLayout linearLayout = findViewById(R.id.whiteList);
                                            TableRow tableRow = (TableRow) linearLayout.getChildAt(No);
                                            EditText editName = (EditText) tableRow.getChildAt(1);
                                            EditText editUnit = (EditText) tableRow.getChildAt(2);
                                            EditText editImsi = (EditText) tableRow.getChildAt(3);
                                            EditText editPhone = (EditText) tableRow.getChildAt(4);
                                            EditText editCarrier = (EditText) tableRow.getChildAt(5);
                                            EditText editRemarks = (EditText) tableRow.getChildAt(6);

                                            editName.setText(NAME);
                                            editUnit.setText(UNIT);
                                            editImsi.setText(IMSI);
                                            editPhone.setText(PHONENUM);
                                            editCarrier.setText(CARRIER);
                                            editRemarks.setText(REMARKS);
                                        }
                                    }
                                }
                                db.close();
                            }catch (Exception e){
                                System.out.println("edit:"+e);
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
            }
        });

        //添加数据
        Button listAdd = findViewById(R.id.listAdd);
        listAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(WhiteList.this);
                final View textEntryView = factory.inflate(R.layout.listalert, null);
                final EditText editTextImsi = textEntryView.findViewById(R.id.editTextImsi);
                final EditText editTextReverts = textEntryView.findViewById(R.id.editTextReverts);
                final EditText editUnit = textEntryView.findViewById(R.id.unit);
                final EditText editName = textEntryView.findViewById(R.id.uniName);
                final EditText editPhone = textEntryView.findViewById(R.id.alert_phoneNum);
                final EditText editCarrier = textEntryView.findViewById(R.id.carrier);
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(WhiteList.this);
                builder.setTitle("添加名单:");
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setView(textEntryView);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @SuppressLint({"SetTextI18n", "DefaultLocale"})
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        waitSql();
                        try {
                            SQLiteDatabase db;
                            db = sqliteUse.getWritableDatabase();
                            final LinearLayout linearLayout = findViewById(R.id.whiteList);
                            ContentValues contentValues = new ContentValues();
                            final String IMSI = String.valueOf(editTextImsi.getText());
                            final String REMARKS = String.valueOf(editTextReverts.getText());
                            final String NAME = String.valueOf(editName.getText());
                            final String UNIT = String.valueOf(editUnit.getText());
                            final String PHONENUM = String.valueOf(editPhone.getText());
                            final String CARRIER = String.valueOf(editCarrier.getText());
                            if (String.valueOf(editTextImsi.getText()).length() != 15) {
                                //中文输入没做判断
                                Toast.makeText(getApplicationContext(), "IMSI号位数输入不正确", Toast.LENGTH_SHORT).show();
                            } else {
                                contentValues.put("name", NAME);
                                contentValues.put("unit", UNIT);
                                contentValues.put("imsi", IMSI);
                                contentValues.put("phoneNumber", PHONENUM);
                                contentValues.put("carrier", CARRIER);
                                contentValues.put("Remarks", REMARKS);
                                //数据库插入失败返回-1
                                if (db.insert("whiteList", null, contentValues) == -1) {
                                    Toast.makeText(getApplicationContext(), "添加内容重复", Toast.LENGTH_SHORT).show();
                                } else {
                                    ID++;
                                    System.out.println("添加一条数据: ID:" + ID);

                                    //用于判断添加IMSI名单后,保存原有选中删除的目标下表
                                    int[] old_ID = new int[ID];
                                    System.out.println("????1");
                                    for(int i = 0; i<ID - 1; i++) {
                                        System.out.println("????2");
                                        if(delete_list[i][1] == 1) {
                                            old_ID[i] = 1;
                                        } else {
                                            old_ID[i] = 0;
                                        }
                                    }
                                    System.out.println("????3");
                                    delete_list = new int[ID][2];
                                    System.out.println("????4");
                                    for(int i = 0; i<ID; i++) {
                                        System.out.println("????5");
                                        delete_list[i][0] = i;
                                        delete_list[i][1] = 0;
                                        if(old_ID[i] == 1) {
                                            delete_list[i][1] = 1;
                                        }
                                    }

                                    ArrayList<String> Msg = new ArrayList<>();
                                    Msg.add(String.valueOf(ID));
                                    Msg.add(NAME);
                                    Msg.add(UNIT);
                                    Msg.add(IMSI);
                                    Msg.add(PHONENUM);
                                    Msg.add(CARRIER);
                                    Msg.add(REMARKS);

                                    //创建一行
                                    TableRow tableRow=new TableRow(WhiteList.this);

                                    //这里一定要用android.widget.TableRow.LayoutParams，不然TableRow不显示
                                    tableRow.setLayoutParams(new LinearLayout.LayoutParams(android.widget.TableRow.LayoutParams.MATCH_PARENT, android.widget.TableRow.LayoutParams.MATCH_PARENT));

                                    setEditAdd(5, Msg, 0);
                                    tableRow.addView(editText);
                                    //然后通过Cursor的getColumnIndex()获取某一列中所对应的位置的索引

                                    setEditAdd(18,  Msg, 1);
                                    tableRow.addView(editText);
                                    data_list.add(IMSI);

                                    setEditAdd(35,  Msg, 2);
                                    tableRow.addView(editText);


                                    setEditAdd(45,  Msg, 3);
                                    tableRow.addView(editText);


                                    setEditAdd(30,  Msg, 4);
                                    tableRow.addView(editText);


                                    setEditAdd(18,  Msg, 5);
                                    tableRow.addView(editText);

                                    setEditAdd(18,  Msg, 6);
                                    tableRow.addView(editText);

                                    //将一行数据添加到表格中
                                    linearLayout.addView(tableRow);

                                    db.close();

                                    System.out.println("添加数据Sucess");
                                }
                            }
                        }catch (Exception e)
                        {
                            System.out.println("add blackList:"+e);
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

        Button delList = findViewById(R.id.delList);
        delList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitSql();
                SQLiteDatabase db;
                db = sqliteUse.getWritableDatabase();
                LinearLayout linearLayout = findViewById(R.id.whiteList);
                int flag_del = 0;
                for(int i = 0; i<ID; i++) {
                    System.out.println("i:" + i + "ID:" + ID);
                    System.out.println("delete_list[" + i + "][1]:" + delete_list[i][1]);
                    if(delete_list[i][1] == 1) {
                        System.out.println("删除目标");
                        flag_del++;
                        TableRow tableRow = (TableRow) linearLayout.getChildAt(i);
                        EditText editIMSIDel = (EditText) tableRow.getChildAt(3);
                        String IMSI_DEL = editIMSIDel.getText().toString();
                        System.out.println("IMSI_DEL" + IMSI_DEL);
                        db.delete("whiteList", "imsi=?", new String[]{IMSI_DEL});
                    }
                }
                db.close();
                if(flag_del == 0) {
                    Toast.makeText(getApplicationContext(), "没有选中删除目标", Toast.LENGTH_SHORT).show();
                }

                getNList();
            }
        });

        Button garbage = findViewById(R.id.garbage);
        garbage.setVisibility(View.VISIBLE);
        garbage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WhiteList.this, AlertDialog.THEME_HOLO_DARK);
                builder.setTitle("确认清空列表");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db = sqliteUse.getReadableDatabase();
                        sqliteUse.ClearListWhite(db);
                        sqliteUse.WhitetableCreate(db);
                        db.close();
                        getNList();
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return (R.layout.white_list);
    }

    //回退功能
    public void onBackPressed() {
        super.onBackPressed();//注释掉这行,back键不退出activity
    }
}
