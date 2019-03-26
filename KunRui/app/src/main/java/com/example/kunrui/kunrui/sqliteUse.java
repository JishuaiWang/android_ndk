package com.example.kunrui.kunrui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class sqliteUse extends SQLiteOpenHelper{
    private static String tableName = "imsiMsg";
    private static String tableBlackList = "blackList";
    private static String tableWhiteList = "whiteList";
    private static String tableHistoryList = "historyList";
    private static String modelCode = "modelCode";
    private static String bandTable = "bandTable";
    private static String freqMsg = "freqMsg";
    private Context t_context;
    public sqliteUse(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        t_context = context;
    }


    private static final String CREATE_IMSIMSG = "create table if not exists "+
            tableName+
            "(id integer primary key autoincrement, "+
//            "imsi text unique," + unique代表唯一
            "imsi text," +
            "Attribution text," +
            "cptTime text," +
            "rssi text," +
            "ulfreqTdd integer," +
            "dlfreqTdd integer," +
            "bandTdd integer," +
            "pciTdd integer," +
            "ulfreqFdd integer," +
            "dlfreqFdd integer," +
            "bandFdd integer," +
            "pciFdd integer," +
            "modelType integer" +
            ");";

    private static final String CREATE_BLACK_LIST= "create table if not exists "+
            tableBlackList+
            "(imsi text primary key," +
            "band integer," +
            "Remarks text);";

    private static final String CREATE_WHITE_LIST= "create table if not exists "+
            tableWhiteList+
            "(imsi text primary key," +
            "band integer," +
            "carrier text," +
            "name text," +
            "unit integer," +   //单位
            "phoneNumber integer," +

            "Remarks text);";

    private static final String CREATE_HISTORY_LIST = "create table if not exists "+
            tableHistoryList+
            "(imsi text," +
            "Attribution text," +
            "cptTime text);";

    private static final String CREATE_MODEL_CODE = "create table if not exists "+
            modelCode+
            "(type text," +
            "model text);";

    private static final String CREATE_BAND = "create table if not exists "+
            bandTable+
            "(typeCode integer," +
            "Msg text);";

  private static final String CREATE_FREQ_TYPE = "create table if not exists "+
              freqMsg+
          "(modelCode text," +          //产品型号
          "typeCode integer," +       //模块编号(7,8,12,13)
          "modelType text," +        //TDD/FDD
          "band integer," +             //band 38,39,40
          "ulfreq integer," +           //上行频点
          "dlfreq integer," +           //下行频点
          "pci integer," +              //物理小区标识号
          "cellStatu integer," +            //小区状态
          "Type integer," +            //抓捕设备编号
          "imsi text);";            //抓捕号码

    @Override
//    执行数据库操作
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_IMSIMSG);
//        Toast.makeText(t_context, "Create successed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists "+ tableName);
        onCreate(db);
    }
    public void resCreate(SQLiteDatabase db) {
        db.execSQL("drop table if exists "+ tableName);
        onCreate(db);
        db.execSQL(CREATE_IMSIMSG);
        onCreate(db);

        db.execSQL("drop table if exists "+ freqMsg);
        onCreate(db);
        db.execSQL(CREATE_FREQ_TYPE);
        onCreate(db);
    }

    public void BlacktableCreate(SQLiteDatabase db){
        db.execSQL(CREATE_BLACK_LIST);
        onCreate(db);
    }

    public void WhitetableCreate(SQLiteDatabase db){
        db.execSQL(CREATE_WHITE_LIST);
        onCreate(db);
    }

    public void HistorytableCreate(SQLiteDatabase db){
        db.execSQL(CREATE_HISTORY_LIST);
        onCreate(db);
    }

//    public void FlagSet(SQLiteDatabase db) {
//        db.execSQL(CREATE_FLAGSET);
//        onCreate(db);
//    }

    public void ModelCodeCreate(SQLiteDatabase db){
//        db.execSQL("drop table if exists "+ flagSet);
//        onCreate(db);
//        db.execSQL(CREATE_FLAGSET);
//        onCreate(db);

        db.execSQL("drop table if exists "+ modelCode);
        onCreate(db);
        db.execSQL(CREATE_MODEL_CODE);
        onCreate(db);

        db.execSQL("drop table if exists "+ bandTable);
        onCreate(db);
        db.execSQL(CREATE_BAND);
        onCreate(db);

        db.execSQL("drop table if exists "+ freqMsg);
        onCreate(db);
        db.execSQL(CREATE_FREQ_TYPE);
        onCreate(db);
    }

    public void ClearListWhite(SQLiteDatabase db) {
        db.execSQL("drop table if exists "+ tableWhiteList);
        onCreate(db);
    }

    public void copyFileFromAssets(String filepath, String fileName) {
        boolean result = false;
        try {
            System.out.println("check up------------------");
            // 检查 SQLite 数据库文件是否存在
            if (!(new File(filepath + fileName)).exists()) {
                // 如 SQLite 数据库文件不存在，再检查一下 database 目录是否存在
                File f = new File(filepath);
                // 如 database 目录不存在，新建该目录
                if (!f.exists()) {
                    f.mkdir();
                }
                try {
                    System.out.println("copy------------------");
                    // 得到 assets 目录下我们实现准备好的 SQLite 数据库作为输入流
                    InputStream is = t_context.getAssets().open(fileName);
                    // 输出流
                    OutputStream os = new FileOutputStream(filepath + fileName);
                    // 文件写入
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    System.out.println("copy sucess");
                    // 关闭文件流
                    os.flush();
                    os.close();
                    is.close();
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
