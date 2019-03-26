package com.example.kunrui.kunrui.Resource;


import android.annotation.SuppressLint;
import android.util.Log;

import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;

public class excelRead {
    public String readExcel(ArrayList<String> data_list, Map<String, ArrayList> modelCode){
        int status = 0;
        try {
            @SuppressLint("SdCardPath") FileInputStream inputStream = new FileInputStream("/sdcard/xml/data.xls");
            Workbook workbook=Workbook.getWorkbook(inputStream);
            //获取第一张excel数据表。
            Sheet sheet=workbook.getSheet(0);
            int rows=sheet.getRows();//获取该表中有多少行数据。
            Log.e("readExcelToDB",rows+"-------rows-------");
            for(int i=1;i<rows;i++){
                //sheet.getCell(0,i))，在这里i表示第几行数据，012346表示第几列，从0开始算。
                String NAME = (sheet.getCell(0,i)).getContents();
                String UNIT = (sheet.getCell(1,i)).getContents();
                String IMSI=(sheet.getCell(2,i)).getContents();
                String PHONENUM = (sheet.getCell(3,i)).getContents();
                String CARRIER=(sheet.getCell(4,i)).getContents();
                String REMARKS=(sheet.getCell(5,i)).getContents();
                for(int j = 0; j < data_list.size(); j++) {
                    if(IMSI.length() != 15)
                    {
                        System.out.println("IMSI.length() != 15");
                        continue;
                    }
                    if(data_list.get(j).equals(IMSI)){
                        System.out.println("!data_list.get(j).equals(IMSI)");
                        status = 1;
                        break;
                    }
                }
                if(status == 0) {
                    System.out.println("INSERT");
                    ArrayList<String> MSG_LIST = new ArrayList<>();
                    MSG_LIST.add(NAME);
                    MSG_LIST.add(UNIT);
                    MSG_LIST.add(IMSI);
                    MSG_LIST.add(PHONENUM);
                    MSG_LIST.add(CARRIER);
                    MSG_LIST.add(REMARKS);
                    modelCode.put(IMSI, MSG_LIST);
                }
                status = 0;
            }
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "FALSE";
    }
}
