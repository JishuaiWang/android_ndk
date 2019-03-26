//
// Created by HYX on 2018/8/3.
//
#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <string.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <android/log.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/time.h>
#include <time.h>
#include "sqlite3.h"
#include "tcpServer.h"

#ifndef KUNRUI_TCPFUNCTION_H
#define KUNRUI_TCPFUNCTION_H

//包头
typedef struct SmtpsHeader{
    unsigned short Flag;
    unsigned short  Code;
    unsigned short Version;
    unsigned short  Len;
    unsigned int  Seq;
} SmtpsHeader;

//报文加密解密状态
typedef struct BlowFishStatus{
    int encrypt = 0;             //加密：1表示解析成功，0表示失败
    int decode = 0;              //解密：1表示解析成功，0表示失败
    int status = 0;
}BlowFishStatus;

//报文消息体
typedef struct smtpSendBody{
    SmtpsHeader *header;
    char *body;
    int bodylen;
    int fd;
}smtpSendBody;

int smtpServerJudge();//程序执行主体
int tcpServerInit();//套接字初始化
int tcpServerConnect();//建立套接字连接
int socketServerClose();//关闭套接字
int getSmtpHeader(char *frame, int len, SmtpsHeader *header);//解析包头
int smtpsServerIndication(char *frame, int len, int fd);//加密认证
int mkframeValueUnit8(char *frame,short Sequence,short FrameId, uint8_t value,char * mac);//组包函数
int SmtpsServerIndFrames(SmtpsHeader *header,char *body,int bodylen, int fd);//报文内容收集与回复
static int Ind_AuthReq(SmtpsHeader *header,char *body,int bodylen, int fd);//注册相应
static int Ind_EchoReq(SmtpsHeader *header,char *body,int bodylen, int fd);//心条请求
int mkframe(char *frame,int32_t Sequence,short FrameId,char * mac);        //回复请求响应内容
static int Ind_ReportReq(SmtpsHeader *header,char *body,int bodylen,int fd);//接受上报消息响应信息
int ParseReportEid(short elementid, short bodyLen, char *pbody, unsigned char *cliMac);//解析上报内容
static int Ind_WarningReq(SmtpsHeader *header,char *body,int bodylen, int fd);//警告
static int Ind_CfgQryRsp(SmtpsHeader *header,char *body,int bodylen,int fd);//配置查询响应
int ParseCfgQryRspModelCode(SmtpsHeader *header,short elementid, short bodyLen, char *pbody, unsigned char *cliMac, int CodeType);
int ParseModelCfg(short elementid, short len, char *pvalue, QueryRspInfo_t *cfgInfo, int CodeType);  //解析每个模块配置
static int Ind_ConfigRsp(SmtpsHeader *header,char *body,int bodylen,int fd);//配置响应
int CfgMsgSend(SmtpsHeader *header,char *body,int bodylen,int fd);  //配置消息线程发送

int freqBandInsert(QueryRspInfo_t *cfgInfo, sqlite3 *db, int CodeType, flagSet flagSet_t);//频段信息插入
void *Imsi_Position(void *arg); //获取默认信息,开启定时消息发送线程
void *bulkDumpSql_pthread(void *arg); //数据插入线程
static int mkelement(char *basic,char **dest,short elementid,short len,char *value, int Code);

static int mkshort(char *frame,char **dest,short value);
static int mkchar(char *frame,char **dest,char value);
static unsigned char getbyte(char *p);
static int cpychar(char *frame,char *dest,char value);
static int cpyshort(char *frame,char *dest,short value);
static int mkelement2(char *basic,char **dest,short elementid,short len,char *value);
int ParseWarningEid(short elementid, short bodyLen, char *pbody,char *macStr);//警告消息解析
int ParseWarningReq(short elementid, short len, char *pvalue,char * reason);//警告消息回复
int GetTimeStr(char *timeStr);//时间格式化
int fileWriteType(char *target, int status);//config.txt write
int fileReadType(char *target);//config.txt write
int fileWriteStatus(char *result, char *fileName);//写入状态
int fileWriteLogs(char *result, char *fileName);//写日志
int freqBandSelect(configMsg_t *configMsg, int statu, sqlite3 *db); //数据库插入操作
char *sqliteQuery(char *phoneNum, sqlite3 *db);
int freqBandMemory(char *type, sqlite3 *db);    //根据收到类型存储band
int callback(void *ptr, int column_num, char **column_val, char **column_name);//回调函数
int callback2(void *ptr, int column_num, char **column_val, char **column_name);
long getCurrentTime();//获取当前时间
static int Ind_CtrlRsp(SmtpsHeader *header,char *body,int bodylen,int fd);//控制响应
int ParseCtrlRspParam(short elementid, short bodyLen, char *pbody, CtrlRspInfo_t *ctrlRsp);
int bulkDumpSql(ScrollMsg scrollMsg, flagSet flagSet_t, BandInfo_t BandTdd, BandInfo_t BandFdd);
int Mac2Str(char *szMacAddress, unsigned char * MacAddr);
int standardToStamp(char *str_time); //时间戳转换函数
int mkframeSendThread(ScrollMsg *scrollMsg, smtpSendBody *SendBody, short FrameId, char *mac);//用于报文消息发送和校验
int threadSleepExit(sqlite3 *db, int i);//用于检测线程退出的函数
void wirelessInsert(BandInfo_t band); //无线参数变化函数
int cptTimeGet(char *nowtime); //获取当前时间
#endif //KUNRUI_TCPFUNCTION_H
