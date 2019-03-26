//
// Created by HYX on 2018/8/3.
//

#ifndef KUNRUI_TCPSERVER_H
#define KUNRUI_TCPSERVER_H

#include <stddef.h>
#include <stdarg.h>
#include <assert.h>
#include <sys/time.h>
#include <time.h>
#include "sqlite3.h"

typedef signed char int8_t;
typedef unsigned char uint8_t;
typedef signed short int16_t;
typedef unsigned short uint16_t;
typedef signed int int32_t;
typedef unsigned int	uint32_t;
typedef unsigned char MacAddr_t[6];

#define staticIp "192.168.1.137"
#define smtpServerPort 9876
#define listenNum 5
#define BLOWFISH_KEY "_@k#2o(!-r+$q]~"        //BlowFish密钥
//#define BLOWFISH_KEY "6t#$z@_321"        //BlowFish密钥
#define OPEN "Open"
#define CLOSE "Close"
#define USUAL "USUAL"
#define CHANGE "CHANGE"
#define SINGER_APP_CONNECTED "SINGER_APP_CONNECTED"
#define SMTPS_CONNECTED "SMTPS_CONNECTED"
#define WIRELESS_SET "WIRELESS_SET"
#define SQLITE_LOCK "SQLITE_LOCK"
#define POWER_ENTERED "POWER_ENTERED"
#define GATHER_MODEL "GATHER_MODEL"


#define ImsiMsg "imsiMsg"
#define Attribution "queryAttribution"
#define CONFIG_TXT "data/data/com.example.kunrui.kunrui/config.txt"
#define IMSI_RSSI "data/data/com.example.kunrui.kunrui/getRssi.txt"
#define SQLITE_DB "data/data/com.example.kunrui.kunrui/databases/QCellCore.db"
#define SQLITE_DB_CREATE "data/data/com.example.kunrui.kunrui/databases/smtp.db"
#define LOG_FILE "/sdcard/xml/debug.txt"
#define IMSI_DEFAULT "100000000000001"

#define MAX_TCP_BUF_SIZE_RECV   174080
#define WSTP_EID_COMM_MAC 		0x0001
#define WSTP_EID_COMM_RESULT 	0x0004

#define WSTP_PROTOCOLID			0X5001
#define WSTP_SERVER_PORT 		5000
#define WSTP_BUF_LEN			(8*1024)	//8k

#define WSTP_EID_CFG_LTE_REM_SCAN_ON_BOOT   0x5002
#define WSTP_EID_CFG_ENABLE_CELL            0x5019

#define SMTPS_PROTOCOL_TYPE_DECODE     0x5001
#define SMTPS_PROTOCOL_TYPE_NONE_DECODE     0x5000
/* mtps Frame's ID */
#define SMTPS_FID_AUTHREQ 		0X0001
#define SMTPS_FID_AUTHRSP 		0X0002
#define SMTPS_FID_REPREQ 		0X0003
#define SMTPS_FID_REPRSP 		0X0004
#define SMTPS_FID_CTRLREQ 		0X0005
#define SMTPS_FID_CTRLRSP 		0X0006
#define SMTPS_FID_CFGREQ 		0X0007
#define SMTPS_FID_CFGRSP 		0X0008
#define SMTPS_FID_ECHOREQ 		0X0009
#define SMTPS_FID_ECHORSP 		0X000A
#define SMTPS_FID_CFGQUERYREQ 	0X000B
#define SMTPS_FID_CFGQUERYRSP 	0X000C
#define SMTPS_FID_WARNINGREQ	0X000D
#define SMTPS_FID_WARNINGRSP	0X000E

/* smtps Common **********************************/

#define SMTPS_EID_COMM_MAC 						0x0001
#define SMTPS_EID_COMM_MODEL 					0x0002
#define SMTPS_EID_COMM_FW 						0x0003
#define SMTPS_EID_COMM_RESULT 					0x0004
#define SMTPS_EID_COMM_BOOT_TYPE				0x0005
#define SMTPS_EID_COMM_REM_STATUS				0x0006
#define SMTPS_EID_COMM_ROUTER_VERSION			0x000F


/*Auth Id******************/
#define SMTPS_EID_AUTH_CLEAR_TEXT 				0x2001
#define SMTPS_EID_AUTH_CIPHER_TEXT				0x2002


/*Report Id**************************************/
#define SMTPS_EID_RPT_IMSI 						0x3002
#define SMTPS_EID_RPT_IMEI 						0x3003
#define SMTPS_EID_RPT_BAND_INFO					0x3004
#define SMTPS_EID_RPT_TAC						0x3005
#define SMTPS_EID_RPT_FREQ_LIST					0x3006
#define SMTPS_EID_RPT_NEIGHBOR_LIST				0x3007
#define SMTPS_EID_RPT_PLMN						0x300A
#define SMTPS_EID_RPT_FREQ						0x300B
#define SMTPS_EID_RPT_PCI						0x300C
#define SMTPS_EID_RPT_RSSI						0x300D
#define SMTPS_EID_RPT_CUR_FREQ					0x300E
#define SMTPS_EID_RPT_CUR_TIME					0x300F
#define SMTPS_EID_RPT_DEVICE_STATE				0x3010
#define SMTPS_EID_RPT_CONIFG_REPORT				0x3011
#define SMTPS_EID_RPT_IMSI_CACHING_AND_TRANS_STATUS		 0x3012
#define SMTPS_EID_RPT_GPS_LNG_LAT				0x3013

//系统和小区启动时长，没有加入到协议文档中，请注意  171129
#define WSTP_EID_RPT_SYS_AND_CELL_UPTIME		0x3014

#define SMTPS_EID_RPT_ANTENNA_ANGLE             0x3015


/*Control Id***************/
#define SMTPS_EID_CTL_UPGRADE_REQ 				0x4001
#define SMTPS_EID_CTL_UPGRADE_RSP 				0x4002
#define SMTPS_EID_CTL_REBOOT_REQ 				0x4003
#define SMTPS_EID_CTL_REBOOT_RSP 				0x4004
#define SMTPS_EID_CTL_RESET_REQ 				0x4005
#define SMTPS_EID_CTL_RESET_RSP 				0x4006
#define SMTPS_EID_CTL_UPGRADE_ROUTER_REQ		0x4007
#define SMTPS_EID_CTL_UPGRADE_ROUTER_RSP		0x4008
#define SMTPS_EID_CTL_RESPOND                   0x4009


/*Config Id****************/
#define SMTPS_EID_CFG_PARA_SELECT_ALGORITHM 	0x5001
#define SMTPS_EID_CFG_PARA_SELF_CONFIG_ENABLE	0x5002
#define SMTPS_EID_CFG_LTE_REM_SCAN_BAND_LIST 	0x5003
#define SMTPS_EID_CFG_LTE_REM_SCAN_ARFCN_LIST 	0x5004
#define SMTPS_EID_CFG_CAPTURE_ENABLE 			0x5005
#define SMTPS_EID_CFG_REDIRECT_ENABLE 			0x5006
#define SMTPS_EID_CFG_IRAT_REDIRECT_ENABLE 		0x5007
#define SMTPS_EID_CFG_4G_FREQUENCY				0x5008
#define SMTPS_EID_CFG_2G_FREQUENCY				0x5009
#define SMTPS_EID_CFG_MODE_TYPE 				0x500A
#define SMTPS_EID_CFG_COLLECT_PERIOD 			0x500B
#define SMTPS_EID_CFG_TAC 						0x500C
#define SMTPS_EID_CFG_SYN_TYPE					0x500D
#define SMTPS_EID_CFG_BAND_INFO					0x500E
#define SMTPS_EID_CFG_FREQ_LIST					0x500F
#define SMTPS_EID_CFG_NEIGHBOR_LIST				0x5010

#define SMTPS_EID_CFG_BAND						0x5013
#define SMTPS_EID_CFG_LOCK_IMSI					0x5014
#define SMTPS_EID_CFG_WHITE_LIST				0x5015
#define SMTPS_EID_CFG_BLACK_LIST				0x5016
#define SMTPS_EID_CFG_POWER						0x5017
#define SMTPS_EID_CFG_SEARCH_IMSI				0x5018
#define SMTPS_EID_CFG_REBOOT_CELL				0x5019
#define SMTPS_EID_CFG_PLMN						0x5020
#define SMTPS_EID_CFG_RESELECT_PRIORITY			0x5021
#define SMTPS_EID_CFG_EXTERNAL_IP				0x5022
#define SMTPS_EID_CFG_SERVER_IP					0x5023
#define SMTPS_EID_CFG_QRX_LEVEL_MIN				0x5024
#define SMTPS_EID_CFG_GPS_OFFSET_TIME			0x5027
#define SMTPS_EID_CFG_GPS_OFFSET_TIME_AUTO_CONFIG			0x5028
#define SMTPS_EID_CFG_ROUTER_WAN_MODE			0x5029
#define SMTPS_EID_CFG_START_REVERSE_SHH			0x5030
#define SMTPS_EID_CFG_STOP_REVERSE_SHH			0x5031
#define SMTPS_EID_CFG_SAVE_CONFIG				0x5032
#define SMTPS_EID_CFG_SET_LOG_LEVEL				0x5033
#define SMTPS_EID_CFG_UE_TO_2G_IMIS_LIST		0x5034
#define SMTPS_EID_CFG_INITIAL_TAC				0x5035
#define SMTPS_EID_CFG_END_TAC					0x5036
#define SMTPS_EID_CFG_REM_ENABLE				0x5037
#define SMTPS_EID_CFG_NTP_CFG					0x5038
//同频干扰抑制级别
#define SMTPS_EID_CFG_INTRA_INTERFERENCE_RESTRAIN_LEVEL		0x5039


/*warning id************************************************************/
#define SMTPS_EID_WARNING_VERSION				0x7001
#define SMTPS_EID_WARNING_EXIT_ABNORMAL       	0x7002
#define SMTPS_EID_WARNING_REBOOT_ABNORMAL       0x7003
#define SMTPS_EID_WARNING_UNPACK_WARNING		0x7004

typedef struct flagSet {
    int ID = 0;
    int8_t cellRes = 1;
    int flag_position = 0;
    int pthread_statu = 0;  //用于线程退出
    int type_statu = 0;     //修改频段的
    uint32_t band = 0;
    int cellStaus[2][2] = {0};
    char IMSI[16] = IMSI_DEFAULT;
    char IMSI_SELECT_TDD[16] = IMSI_DEFAULT;
    char IMSI_SELECT_FDD[16] = IMSI_DEFAULT;
}flagSet;

//邻区表
typedef struct neighbor_t{
    uint32_t pci;
    uint32_t cgi;
    uint32_t type;
    uint32_t tac;
    uint32_t freq;
    unsigned int reselection_priority;
    int gpsOffsetTime;
    char RSRP[8];
    char plmn[6];
    char reserved[2];//保证字节对齐
}neighbor_t;

//上行下行
typedef struct freq_t{
    uint32_t upFreq;
    uint32_t downFreq;
}freq_t;

//band信息
typedef struct band_t{
    uint32_t band;
    uint32_t ulfreq;
    uint32_t dlfreq;
    uint32_t pci;
}band_t;

//配置参数信息
typedef struct configMsg_t{
    char modelCode[16];
    band_t band[8];
    char IMSI[16] = IMSI_DEFAULT;
    int Type = 0;
    char trsPower[5];
}configMsg_t;

//报文处理状态统计
typedef struct MessageRecvStatistic_t{
    int message_num;//消息个数
    int recv_num;//调用recv次数
    int error_num;//解析出错统计
    long last_time;//上次统计时间
}MessageRecvStatistic_t;

//状态信息上报
typedef struct uptime_sys_and_cell_t{
    char sys_uptime[16];//系统启动时长
    char cell_uptime[16];//小区启动时长
}uptime_sys_and_cell_t;

typedef struct NewTcpLientParam_t{
    int newfd;
//    char ip[INET_ADDRSTRLEN];
    int port;
}NewTcpLientParam_t;

typedef struct paramsForSimpleClient_t{
    int fd;
//    char ip[INET_ADDRSTRLEN];
//    MYSQL *mysql;  //mysql句柄
//    redisContext* redis; //redis 句柄
    char macStr[20];
    int imsi_recv_count;//每个tcp buf中包含的imsi总数
    int certification;//认证状态 1 已认证
    int password_error;//报文解密失败标志
}paramsForSimpleClient_t;

typedef struct ReportInfoExist_t{
    uint8_t macStr;
    uint8_t modelCode;
    uint8_t imsi;
    uint8_t rssi;
    uint8_t captureTime;
    uint8_t deviceState;
    uint8_t lngLat;
    uint8_t imsiTransInfo;
    uint8_t sys_cell_uptime;
}ReportInfoExist_t;

//状态信息上
typedef struct DeviceState_t
{
    uint8_t	cell_state;//小区状态0:down 1:up
    uint8_t	power_state;//电源状态始终为1
    uint8_t	cnm_sync_state;//空口同步状态 0:未同步 1：同步
    uint8_t	gps_sync_state;//GPS同步状态 0:未同步 1：同步
    uint8_t	ptp_sync_state;//1588同步状态 0:未同步 1：同步
    char		cpu_use_rate[8];//CPU使用率 ect.98%
    char		mem_use_rate[8];//内存使用率ect.98%
    char		cpu_temperature[8];//cpu温度28.5
}DeviceState_t;

typedef struct GpsLngLat_t{
    char Longitude[16];//经度
    char Latitude[16]; //纬度
}GpsLngLat_t;

typedef struct ImsiTransmitInfoRpt_t{
    int rptSuccessCount;//前30s上报成功计数
    int realTimeCount;//前30s实时数据发送计数
    int retransmitCount;//前30s重传发送计数
    int offlineCount;//前30s离线缓存数据发送计数
    int percentage;//前30s上报成功百分比
    int remainOfflineCount;//当前离线缓存数据余量
    char oldestTime[20];//当前剩余离线缓存数据中，最老的一条的捕获时间
}ImsiTransmitInfoRpt_t;

typedef struct ReportInfo_t{
    ReportInfoExist_t exist;
    char macStr[20];//设备mac地址
    short modelCode;//模块类型码，用于区分捕获的模块
    char imsi[16];
    char rssi[16];
    char captureTime[20];
    DeviceState_t deviceState;
    GpsLngLat_t lngLat;
    ImsiTransmitInfoRpt_t imsiTransInfo;
    uptime_sys_and_cell_t sys_cell_uptime;
}ReportInfo_t;

typedef struct AuthReqInfoExist_t{
    uint8_t productModel;
    uint8_t firwareVersion;
    uint8_t bootType;
    uint8_t remStatus;
    uint8_t routerFirwareVersion;
    uint8_t lngLat;
}AuthReqInfoExist_t;

typedef struct AuthReqInfo_t{
    AuthReqInfoExist_t exist;
    char macStr[20];
    //char clearTxt[16];
    //char cipherTxt[16];
    char productModel[32];
    char firwareVersion[32];
    uint8_t bootType;
    uint8_t remStatus;
    char routerFirwareVersion[32];
    GpsLngLat_t lngLat;
}AuthReqInfo_t;

typedef struct BandInfo_t{
    uint32_t band;
    uint32_t ulfreq;
    uint32_t dlfreq;
    uint32_t pci;
}BandInfo_t;

typedef struct NtpCfg_t{
    int enable;    //1使能，0关闭
    int period;		//同步周期，单位分钟,取值：1/5/10/15/25/30/50/60/180/360/720
    char serverIp[16];  //ntp服务器地址，如202.112.31.197
}NtpCfg_t;


typedef struct QueryRspInfoExist_t{
    uint8_t capturePeriod;
    uint8_t tac;
    uint8_t sync_mode;
    uint8_t bandInfo;
    uint8_t trsPower;
    uint8_t resPriority;
    uint8_t qrxLevelMin;
    uint8_t gpsOffsetTime;
    uint8_t gpsOffsetTimeSwitch;
    uint8_t logLevel;
    uint8_t initTac;
    uint8_t endTac;
    uint8_t ntpCfg;
    uint8_t irLevel;//同频干扰抑制级别
    uint8_t captureType;	//采集模式
    uint8_t lockImsi;	//定位IMSI
    uint8_t	whiteList;	//白名单
    uint8_t	blackList;	//黑名单
}QueryRspInfoExist_t;

typedef struct QueryRspInfo_t{
    QueryRspInfoExist_t exist;
    int modelCode;
    char macStr[20];
    int capturePeriod;
    int tac;
    uint8_t sync_mode;
    BandInfo_t bandInfo;
    char trsPower[5];
    uint8_t resPriority;
    int qrxLevelMin;
    int gpsOffsetTime;
    int gpsOffsetTimeSwitch;
    int logLevel;
    int initTac;
    int endTac;
    NtpCfg_t ntpCfg;
    int irLevel;//同频干扰抑制级别
    uint8_t captureType;	//采集模式(0:只抓一次模式, 1:周期模式, 2:全抓模式, 3:抓捕模式)
    char lockImsi[16];	//定位IMSI(字符串)
    char whiteList[256];	//白名单字符串(以','为分隔符)
    char blackList[256];	//黑名单字符串(以','为分隔符)
}QueryRspInfo_t;

typedef struct CtrlRspInfoExist_t{
    uint8_t lte_fw_upgrade_result;
    uint8_t reboot_result;
    uint8_t reboot_and_default_result;
    uint8_t router_fw_upgrade_result;
}CtrlRspInfoExist_t;

typedef struct CtrlRspInfo_t{
    CtrlRspInfoExist_t exist;
    uint8_t lte_fw_upgrade_result;
    uint8_t reboot_result;
    uint8_t reboot_and_default_result;
    uint8_t router_fw_upgrade_result;
}CtrlRspInfo_t;

typedef struct ScrollMsg{
    unsigned char mac[6] = {0};
    char SendBuf[1024] = {0};
    int Flag = 0;
    char number[256*8][8] = {0};
    char Imsi[256*8][32] = {0};
    char Attriution[256*8][32] = {0};
    char CptTime[256*8][32] = {0};
    char Rssi[256*8][16] = {0};
    int Type[256*8] = {0};
    int band[256*8] = {0};
    int modelType = 0;
    sqlite3 *dbQcell;
    sqlite3 *dbCreate;
    sqlite3 *dbSelect;
}ScrollMsg;


typedef struct msgList {
    ScrollMsg scrollMsg;
    flagSet flagSet_t;
    BandInfo_t BandTdd = {0};
    BandInfo_t BandFdd = {0};
}msgList;
int ParseAuthReqEid(short elementid, short len, char *pvalue,AuthReqInfo_t *authInfo);
int ParseReportReq(short elementid, short len, char *pvalue, ReportInfo_t *rptInfo, ScrollMsg *scrollMsg);    //详细内容
#endif //KUNRUI_TCPSERVER_H
