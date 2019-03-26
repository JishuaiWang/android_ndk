package com.example.kunrui.kunrui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

/*
 * 自定义TitleActivity
 * */
public abstract class TitleActivity extends AppCompatActivity implements OnClickListener {
    private Button mMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView(0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);     //设置屏幕不旋转
    }

    public abstract int getLayoutId();
    //初始化组件
    public void initView(int i) {
        if(i == 1) {
            mMenu = findViewById(R.id.menu);
            mMenu.setOnClickListener(this);
        }
    }


    //返回按钮和提交按钮的点击判断监听事件
    @Override
    public void onClick(View v) {
        System.out.println("onclick");
        switch (v.getId()) {
            case R.id.menu:
                PopupMenu  menu = new PopupMenu(this, v);
                menu.inflate(R.menu.menu);
                //按钮点击事件
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                        switch (item.getItemId()){
                            case R.id.whitelist:
                                System.out.println("白名单编辑");
                                try {
                                    Context blackAction = createPackageContext("com.example.kunrui.kunrui", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                                    Class whiteClass = blackAction.getClassLoader().loadClass("com.example.kunrui.kunrui.WhiteList");
                                    Intent intent;
                                    intent = new Intent(blackAction, whiteClass);
                                    startActivity(intent);
                                } catch (PackageManager.NameNotFoundException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case R.id.blackAdd:
//                                tcpServerStatus = "Close";
//                                fileCreate.initData(tcpServerStatus);
                                System.out.println("黑名单添加");
                                try {
                                    Context blackAction = createPackageContext("com.example.kunrui.kunrui", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                                    Class blackClass = blackAction.getClassLoader().loadClass("com.example.kunrui.kunrui.Blacklist");
                                    Intent intent;
                                    intent = new Intent(blackAction, blackClass);
                                    startActivity(intent);
                                } catch (PackageManager.NameNotFoundException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case R.id.historyQuery:
//                                tcpServerStatus = "Close";
//                                fileCreate.initData(tcpServerStatus);
                                System.out.println("历史纪录查询");
                                try {
                                    Context historyAction = createPackageContext("com.example.kunrui.kunrui", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                                    Class historyClass = historyAction.getClassLoader().loadClass("com.example.kunrui.kunrui.Historylist");
                                    Intent intent;
                                    intent = new Intent(historyAction, historyClass);
                                    startActivity(intent);
                                } catch (PackageManager.NameNotFoundException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

                //按钮消失事件
                menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                    }
                });
                menu.show();
                break;
            default:
                break;
        }
    }

//    屏幕监听事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
