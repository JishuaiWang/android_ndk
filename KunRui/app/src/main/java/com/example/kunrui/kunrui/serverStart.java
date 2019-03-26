package com.example.kunrui.kunrui;

public class serverStart {
    tcpThread Thread;
    static {
        System.loadLibrary("native-lib");
    }
    public static native String serverStart();
    public void tcpCreate(){
        Thread = new serverStart.tcpThread();
    }
    public class tcpThread extends Thread{
        @Override
        public void run(){
            super.run();
            serverStart.serverStart();
        }
    }
}
