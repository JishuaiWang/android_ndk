package com.example.kunrui.kunrui.Resource;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.kunrui.kunrui.fileCreate;
import com.example.kunrui.kunrui.sqliteUse;

public class poll_wireless_exchange {
    public void insert(int ret, sqliteUse sqliteUse, fileCreate fileCreate) {
        System.out.println("poll_wireless_exchange, ret:" + ret);
        while(fileCreate.read_status("SQLITE_LOCK") == 1){
            System.out.println("被bulkDumpSql占用");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        SQLiteDatabase db;
        db = sqliteUse.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        switch (ret){
            case 1:
                contentValues.put("ulfreq", 38950);
                contentValues.put("dlfreq", 38950);
                contentValues.put("pci", 333);
                contentValues.put("band", 40);
                db.update("freqMsg", contentValues, "typeCode=?", new String[]{"8"});

                contentValues = new ContentValues();
                contentValues.put("ulfreq", 19825);
                contentValues.put("dlfreq", 1825);
                contentValues.put("pci", 333);
                contentValues.put("band", 3);
                db.update("freqMsg", contentValues, "typeCode=?", new String[]{"12"});
                break;
            case 2:
                contentValues.put("ulfreq", 38400);
                contentValues.put("dlfreq", 38400);
                contentValues.put("pci", 333);
                contentValues.put("band", 39);
                db.update("freqMsg", contentValues, "typeCode=?", new String[]{"8"});

                contentValues = new ContentValues();
                contentValues.put("ulfreq", 19650);
                contentValues.put("dlfreq", 1650);
                contentValues.put("pci", 333);
                contentValues.put("band", 3);
                db.update("freqMsg", contentValues, "typeCode=?", new String[]{"12"});
                break;
            case 3:
                contentValues.put("ulfreq", 37900);
                contentValues.put("dlfreq", 37900);
                contentValues.put("pci", 333);
                contentValues.put("band", 38);
                db.update("freqMsg", contentValues, "typeCode=?", new String[]{"8"});

                contentValues = new ContentValues();
                contentValues.put("ulfreq", 18100);
                contentValues.put("dlfreq", 100);
                contentValues.put("pci", 333);
                contentValues.put("band", 1);
                db.update("freqMsg", contentValues, "typeCode=?", new String[]{"12"});
                break;
        }
        db.close();
    }
}
