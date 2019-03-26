//
// Created by HYX on 2018/8/3.
//
#include "com_example_kunrui_kunrui_serverStart.h"

JNIEXPORT jstring JNICALL Java_com_example_kunrui_kunrui_serverStart_serverStart
  (JNIEnv *env, jclass){
    smtpServerJudge();
//    return env->NewStringUTF("tcpServerStart");
    return 0;
  }
