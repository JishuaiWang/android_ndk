package com.example.kunrui.kunrui;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class fileCreate {
//    public void pollType(String status){
//        String filePath = "data/data/com.example.kunrui.kunrui/";
//        String fileName = "pollType.txt";
//        writeStatusToFile(status, filePath, fileName);
//    }

//    public void initData(String status){
//        String filePath = "data/data/com.example.kunrui.kunrui/";
//        String fileName = "status.txt";
//        writeStatusToFile(status, filePath, fileName);
//    }

//    public void flagSet(String status){
//        String filePath = "data/data/com.example.kunrui.kunrui/";
//        String fileName = "flagSet.txt";
//        writeStatusToFile(status, filePath, fileName);
//    }

//    public void sqlStatus(String status){
//        String filePath = "data/data/com.example.kunrui.kunrui/";
//        String fileName = "sqlStatus.txt";
//        writeStatusToFile(status, filePath, fileName);
//    }

//    public void powerStatus(String status){
//        String filePath = "data/data/com.example.kunrui.kunrui/";
//        String fileName = "powerStatus.txt";
//        writeStatusToFile(status, filePath, fileName);
//    }

    public void setRssi(String status){
        String filePath = "data/data/com.example.kunrui.kunrui/";
        String fileName = "getRssi.txt";
        clearInfoForFile(filePath+fileName);
        writeStatusToFile(status, filePath, fileName);
    }

    public void export(String Msg){
        @SuppressLint("SdCardPath") String filePath = "/sdcard/kunrui/";
        String fileName = "blackList.txt";
        clearInfoForFile(filePath+fileName);
        writeStatusToFile(Msg, filePath, fileName);
    }
    public String readFile(String status, String fileName){
        String filePath = "data/data/com.example.kunrui.kunrui/";
        status = readStatusToFile(status, filePath, fileName);
        return status;
    }

    private void writeStatusToFile(String status, String filePath, String filename)
    {
        String strFilePath = filePath + filename;
        try{
            File file = new File(strFilePath);
            if(!file.exists())
            {
                Log.d("Status", "Create the file:"+strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.write(status.getBytes());
            raf.close();
        }catch(Exception e)
        {
            Log.e("file", "Error On Write:"+e);
        }
    }

    private void makeFilePath(String filePath, String fileName)
    {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if(!file.exists())
            {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makeRootDirectory(String filepath)
    {
        File file = null;
        try {
            file = new File(filepath);
            if(!file.exists())
            {
                file.mkdir();
            }
        }catch (Exception e)
        {
            Log.i("error:", e+"");
        }
    }

    private String readStatusToFile(String status, String filePath, String filename)
    {
        makeFilePath(filePath, filename);
        String strFilePath = filePath + filename;
        String statu = "";
        try{
            File file = new File(strFilePath);
            FileInputStream raf = new FileInputStream(file);
            int length = (int)file.length();
            byte[] buf = new byte[length];
            raf.read(buf);
            statu = new String(buf);
            raf.close();
        }catch(Exception e)
        {
            Log.e("file", "Error On Write:"+e);
        }
        return statu;
    }

    //清空文件
    public static void clearInfoForFile(String fileName) {
        File file =new File(fileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init_config_file() {
        File file = new File("data/data/com.example.kunrui.kunrui/config.txt");
        try {
            file.delete();
            file.createNewFile();
            RandomAccessFile fw = new RandomAccessFile(file, "rw"); // 创建文件输出流

            fw.writeBytes("SINGER_APP_CONNECTED    0\n");//APP连接状态
            fw.writeBytes("SMTPS_CONNECTED    0\n");//北向网关协议连接
            fw.writeBytes("WIRELESS_SET    0\n");//无线参数设置
            fw.writeBytes("GATHER_MODEL    0\n");  //采集模式变化， 1周期， 3抓捕, 无线参数变化100
            fw.writeBytes("SQLITE_LOCK    0\n");//C++数据库占用
            fw.writeBytes("POLL_TYPE    0\n");  //轮询
            fw.writeBytes("POWER_ENTERED    0");//判断进入能量显示界面

            fw.close();
        } catch (Exception e) {
            System.out.println("init_config_file failed");
        }
    }

    public int write_status(String target, int status) {
        File file = new File("data/data/com.example.kunrui.kunrui/config.txt");
        try {
            RandomAccessFile fw = new RandomAccessFile(file, "rw"); // 创建文件输出流
            String str;
            int length = 0;
            while((str = fw.readLine()) != null) {
            	length += str.length() + 1;
            	if(str.split(" ", 2)[0].equals(target)) {
                    fw.seek(length - str.length() -1);
                    fw.writeBytes(target + "    " + status + "\n");
                    break;
                }
            }
            fw.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public int read_status(String target) {
        int status = 0;
        File file = new File("data/data/com.example.kunrui.kunrui/config.txt");
        try {
            RandomAccessFile fw = new RandomAccessFile(file, "rw"); // 创建文件输出流
            String str;
            while((str = fw.readLine()) != null) {
                if(str.split(" ", 2)[0].equals(target)) {
                    System.out.println("fw.readLine:" +str);
                    status = Integer.parseInt(str.split(" ", 2)[1].trim());
//                    System.out.println(str.split(" ", 2)[0] + "---------" + str.split(" ", 2)[1]);
                    break;
                }
            }
            fw.close();
            return status;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }
}
