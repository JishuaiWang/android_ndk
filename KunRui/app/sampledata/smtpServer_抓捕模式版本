//
// Created by HYX on 2018/8/3.
//
#include "com_example_kunrui_kunrui_serverStart.h"
int smtpSocket = -1, smtpConnfd;
int ret, num = 0;
struct sockaddr_in smtpServerAddr, smtpClientAddr;
socklen_t addrlen = sizeof(addrlen);
ScrollMsg *scrollMsg;
pthread_t pthread;
pthread_t pthread_sql;
msgList msgList_t;
BandInfo_t BandTdd = {0};
BandInfo_t BandFdd = {0};

MacAddr_t  SmMac;
BlowFishStatus blowFishStatus;
smtpSendBody sendBody;
flagSet flagSet_t;  //一系列标志位设计



void wirelessInsert(BandInfo_t band) {
    if(ntohl(band.band) > 36) {
        BandTdd = band;
    } else {
        BandFdd = band;
    }
}

int threadSleepExit(sqlite3 *db, int i) {
    for(int j = 0; j<10 * i; j++) {
        usleep(100000);
        if (flagSet_t.pthread_statu == 0) {
            LOGI("线程退出：threadSleepExit");
            sqlite3_close(db);
            pthread_exit(NULL);
        }
    }
    return 0;
}

void *Imsi_Position(void *arg)
{
    flagSet_t.pthread_statu = 1;//表示线程开启状态
    flagSet_t.cellRes = 1;
    LOGI("*******************Imsi_Position*******************");
    smtpSendBody *SendBody = (smtpSendBody *)arg;
    char *p, *ptr, *delim = const_cast<char *>(",");
    int Code = 0, i;
    unsigned char mac[6];
    char macStr[20] = {0};
    char modelCode[32] = {0};
    char imsi[32] = {0};
    ret = sqlite3_open(SQLITE_DB_CREATE, &scrollMsg->dbSelect);
    if(ret != 0)
    {
        LOGI("Error Open Sqlite3 dbCreate");
        return 0;
    }

    p = SendBody->body;
    memcpy((char *)&SmMac,p+4,sizeof(MacAddr_t));
    memcpy((char *)&mac,p+4,6);
    Mac2Str(macStr, mac);
    LOGI("macStr:%s", macStr);

    threadSleepExit(scrollMsg->dbSelect, 3);  //延时函数
    fileWriteType(const_cast<char *>(GATHER_MODEL), 1);
    mkframeSendThread(scrollMsg, SendBody, SMTPS_FID_CFGREQ, (char *)mac);//恢复为默认值


    configMsg_t configMsg, configMsg1;
    freqBandSelect(&configMsg, 0, scrollMsg->dbSelect);
    strcpy(modelCode, configMsg.modelCode);
    if((strtok_r(modelCode, delim, &ptr)))
        while ((strtok_r(NULL, delim, &ptr))) {
            Code++;
        }
    LOGI("configMsg.modelCode:%s",configMsg.modelCode);

    LOGI("循环检测配置更新, Code:%d", Code);
    while(fileReadType(const_cast<char *>(SINGER_APP_CONNECTED))) {
        LOGI("检测Android页面切换, 发送查询配置");
        mkframeSendThread(scrollMsg, SendBody, SMTPS_FID_CFGQUERYREQ, (char *)mac);
        sprintf(scrollMsg->SendBuf, "1000"); //用于传入模块类型码8,12
        if(fileReadType(const_cast<char *>(WIRELESS_SET)) == 1){
            LOGI("修改频段频点信息,以及抓捕模式");
            memset(&configMsg1, 0, sizeof(configMsg1));
            freqBandSelect(&configMsg1, 1, scrollMsg->dbSelect);
            for(i=0;  i<=Code; i++) {
                if (!((configMsg.band[i].band == configMsg1.band[i].band) && \
                (configMsg.band[i].ulfreq == configMsg1.band[i].ulfreq) && \
                (configMsg.band[i].dlfreq == configMsg1.band[i].dlfreq) && \
                (configMsg.band[i].band == configMsg1.band[i].band))) {
                    if(configMsg1.band[i].band == 0 || configMsg1.band[i].ulfreq ==0 || configMsg1.band[i].dlfreq == 0)
                        continue;
                    sprintf(scrollMsg->SendBuf, "%d", i);
                    fileWriteType(const_cast<char *>(GATHER_MODEL), 100);
                    mkframeSendThread(scrollMsg, SendBody, SMTPS_FID_CFGREQ, (char *)mac);
                }
            }
            fileWriteType(const_cast<char *>(WIRELESS_SET),0);
        }

        freqBandSelect(&configMsg, 1, scrollMsg->dbSelect);
        threadSleepExit(scrollMsg->dbSelect, 3);  //延时函数
        memset(imsi, 0, sizeof(imsi));

        while (fileReadType(const_cast<char *>(POWER_ENTERED)) == 1) {
            if(fileReadType(const_cast<char *>(WIRELESS_SET)) == 1) {
                LOGI("抓捕IMSI号band切换");
                for (i = 0; i <= Code; i++) {
                    LOGI("配置当前抓捕IMSI号默认无线参数");
                    sprintf(scrollMsg->SendBuf, "%d", i);
                    fileWriteType(const_cast<char *>(GATHER_MODEL), 100);
                    mkframeSendThread(scrollMsg, SendBody, SMTPS_FID_CFGREQ, (char *) mac);
                }
                fileWriteType(const_cast<char *>(WIRELESS_SET),0);
            }
            freqBandSelect(&configMsg, 1, scrollMsg->dbSelect);
            flagSet_t.flag_position = 1;
            LOGI("IMSI号配置为:%s, old imsi:%s", configMsg.IMSI, imsi);
            strcpy(flagSet_t.IMSI, configMsg.IMSI);   //是为批量存储数据时对抓捕信息存储可选判断
            if(strlen(configMsg.IMSI) != 15) {
                LOGI("IMSI号不足15位");
                threadSleepExit(scrollMsg->dbSelect, 5);  //延时函数
                continue;
            }
            if(0 == strncmp(imsi, configMsg.IMSI, 15)) {
                LOGI("IMSI号未被更新");
                mkframeSendThread(scrollMsg, SendBody, SMTPS_FID_CFGQUERYREQ, (char *)mac);
                threadSleepExit(scrollMsg->dbSelect, 5);  //延时函数
                if(strncmp(imsi, flagSet_t.IMSI_SELECT_TDD, 15) != 0 || strncmp(imsi, flagSet_t.IMSI_SELECT_FDD, 15) != 0) {
                    LOGI("当前IMSI配置失效,重新配置");
                    memset(imsi, 0, sizeof(imsi));
                } else
                    continue;
            }
            LOGI("抓捕IMSI为,configMsg.IMSI:%s", configMsg.IMSI);
            strcpy(imsi, configMsg.IMSI);

            fileWriteStatus(const_cast<char *>("0.0000"), const_cast<char *>(IMSI_RSSI));
            fileWriteType(const_cast<char *>(GATHER_MODEL), 3);
            mkframeSendThread(scrollMsg, SendBody, SMTPS_FID_CFGREQ, (char *)mac);

            threadSleepExit(scrollMsg->dbSelect, 5);  //延时函数
        }

        //设置抓捕imsi为默认值
        if(flagSet_t.flag_position == 1) {
            fileWriteStatus(const_cast<char *>("0.0000"), const_cast<char *>(IMSI_RSSI));
            LOGI("恢复为默认");
            strcpy(flagSet_t.IMSI, IMSI_DEFAULT);
            flagSet_t.flag_position = 0;
            fileWriteType(const_cast<char *>(GATHER_MODEL), 1);
            mkframeSendThread(scrollMsg, SendBody, SMTPS_FID_CFGREQ, (char *)mac);//恢复为默认值
        }

        threadSleepExit(scrollMsg->dbSelect, 3);  //延时函数
    }
    sqlite3_close(scrollMsg->dbSelect);
    pthread_exit(NULL);
}

//配置参数报文组包,主要修改小区采集模式和抓捕IMSI号,以及无线参数的修改
static int mkelement(char *basic,char **dest,short elementid,short len,char *value, int code_t)
{
    configMsg_t configMsg;
    LOGI("开始组包, code_t:%d", code_t);
    int i, Code = 0, typeCode[8] = {0}, ret =0, length = 0;
    uint8_t result = 0;
    char *delim = const_cast<char *>(",");
    char *p, *ptr;
    char modelCode[16] = {0}, buffer[1024] = {0};
    if (((int)(*dest-basic)+2+2+len) > 5120) return 0;
    if (!mkshort(basic,dest,elementid)) return 0;
    if (!mkshort(basic,dest,len)) return 0;
    for (i=0; i < len; i++)
        if (!mkchar(basic,dest,value[i])) return 0;

    int modelType = fileReadType(const_cast<char *>(GATHER_MODEL));
    LOGI("modelType:%d", modelType);

    switch (modelType) {
        case 1:
//            strcpy(configMsg.IMSI, IMSI_DEFAULT);
            LOGI("GATHER_MODEL:1");
            ret = 1;
            result = 3;
            break;
        case 3:
            LOGI("GATHER_MODEL:3");
            ret = 2;
            result = 3;
            break;
        case 100:
            ret = 0;
            LOGI("WIRELESS_SET");
            break;
        default:
            LOGI("ERROR modelType");
            return -1;
    }

    freqBandSelect(&configMsg, 1, scrollMsg->dbSelect);
    LOGI("configMsg.modelCode:%s, configMsg.IMSI:%s", configMsg.modelCode, configMsg.IMSI);
    strncpy(modelCode, configMsg.modelCode, strlen(configMsg.modelCode));
    if ((p = strtok_r(modelCode, delim, &ptr))) {
        typeCode[Code] = atoi(p);
        LOGI("typeCode[Code]:%d", typeCode[Code]);
    }
    while ((p = strtok_r(NULL, delim, &ptr))) {
        Code++;
        typeCode[Code] = atoi(p);
        LOGI("typeCode[Code]:%d", typeCode[Code]);
    }
    switch (ret) {
        case 0:
            //频段频点修改设置
            LOGI("WIRELESS_SET");
            length = sizeof(configMsg.band[code_t]) + 4 \
                         +  sizeof(flagSet_t.cellRes) + 4;
            mkshort(basic, dest, static_cast<short>(typeCode[code_t]));
            mkshort(basic, dest, static_cast<short>(length));
            mkelement2(basic, dest, SMTPS_EID_CFG_BAND_INFO, sizeof(configMsg.band[code_t]), (char *)&configMsg.band[code_t]);
            mkelement2(basic, dest, SMTPS_EID_CFG_REBOOT_CELL, sizeof(flagSet_t.cellRes), (char *) &flagSet_t.cellRes);//重启小区

            LOGI("typeCode[code_t]:%d, band_Msg__________:%d, %d, %d, %d", typeCode[code_t], ntohl(configMsg.band[code_t].band), ntohl(configMsg.band[code_t].pci), ntohl(configMsg.band[code_t].ulfreq), ntohl(configMsg.band[code_t].dlfreq));
            sprintf(buffer, "typeCode[code_t]:%d, 频段频点修改设置:%d, %d, %d, %d \n", typeCode[code_t], ntohl(configMsg.band[code_t].band), ntohl(configMsg.band[code_t].pci), ntohl(configMsg.band[code_t].ulfreq), ntohl(configMsg.band[code_t].dlfreq));
//            fileWriteLogs(buffer, const_cast<char *>(LOG_FILE));
            break;
        case 1:
            strcpy(configMsg.IMSI, "100000000000001");
            LOGI("POWER_ENTERED：抓捕 默认");
            length = sizeof(configMsg.IMSI) + 4 \
                        + sizeof(result) + 4 \
                        + sizeof(configMsg.trsPower) + 4 \
                        + sizeof(flagSet_t.cellRes) + 4 \
                        ;

            sprintf(configMsg.trsPower, "0.65");
            for (i = 0; i <= Code; i++) {
                LOGI("typeCode[%d]:%d, code_t:%d, configMsg.IMSI:%s", i, typeCode[i], code_t, configMsg.IMSI);

                mkshort(basic, dest, static_cast<short>(typeCode[i]));
                mkshort(basic, dest, static_cast<short>(length));

                mkelement2(basic, dest, SMTPS_EID_CFG_POWER, sizeof(configMsg.trsPower), configMsg.trsPower);
                mkelement2(basic, dest, SMTPS_EID_CFG_MODE_TYPE, sizeof(result), (char *) &result);
                mkelement2(basic, dest, SMTPS_EID_CFG_LOCK_IMSI, sizeof(configMsg.IMSI), configMsg.IMSI);
                mkelement2(basic, dest, SMTPS_EID_CFG_REBOOT_CELL, sizeof(flagSet_t.cellRes), (char *) &flagSet_t.cellRes);//重启小区
            }
            break;
        case 2:
            LOGI("POWER_ENTERED：抓捕");
            length = sizeof(configMsg.IMSI) + 4 \
                        + sizeof(result) + 4 \
                        + sizeof(configMsg.trsPower) + 4 \
                        + sizeof(flagSet_t.cellRes) + 4 \
                        ;

            sprintf(configMsg.trsPower, "0.65");
            for (i = 0; i <= Code; i++) {
                LOGI("typeCode[%d]:%d, code_t:%d, configMsg.IMSI:%s", i, typeCode[i], code_t, configMsg.IMSI);

                mkshort(basic, dest, static_cast<short>(typeCode[i]));
                mkshort(basic, dest, static_cast<short>(length));

                if(typeCode[i] == configMsg.Type) {
                    LOGI("sprintf(configMsg.trsPower, \"1\");");
                    sprintf(configMsg.trsPower, "0.65");
                } else {
                    LOGI("sprintf(configMsg.trsPower, \"0\");");
                    sprintf(configMsg.trsPower, "0");
                }

                if(typeCode[i] == 8) {
                    if (strncmp(configMsg.IMSI, "46011", 5) == 0 || strncmp(configMsg.IMSI, "46001", 5) == 0) {
                        sprintf(configMsg.trsPower, "0");
                    } else if (strncmp(configMsg.IMSI, "46001", 5) == 0) {
                        sprintf(configMsg.trsPower, "0.65");
                    }
                } else if(typeCode[i] == 12) {
                    if (strncmp(configMsg.IMSI, "46011", 5) == 0 || strncmp(configMsg.IMSI, "46001", 5) == 0) {
                        sprintf(configMsg.trsPower, "0.65");
                    }else if (strncmp(configMsg.IMSI, "46001", 5) == 0) {
                        sprintf(configMsg.trsPower, "0");
                    }
                }

                if(configMsg.Type != 8 && configMsg.Type != 12) {
                    LOGI("configMsg.Type != 8 && configMsg.Type != 12");
                    sprintf(configMsg.trsPower, "0.65");
                }

                mkelement2(basic, dest, SMTPS_EID_CFG_POWER, sizeof(configMsg.trsPower), configMsg.trsPower);
                mkelement2(basic, dest, SMTPS_EID_CFG_MODE_TYPE, sizeof(result), (char *) &result);
                mkelement2(basic, dest, SMTPS_EID_CFG_LOCK_IMSI, sizeof(configMsg.IMSI), configMsg.IMSI);
                mkelement2(basic, dest, SMTPS_EID_CFG_REBOOT_CELL, sizeof(flagSet_t.cellRes), (char *) &flagSet_t.cellRes);//重启小区
            }
            break;
        default:
            break;
    }
    return 1;
}

static int mkelement2(char *basic,char **dest,short elementid,short len,char *value)
{
    int i;
    if (((int)(*dest-basic)+2+2+len) > 5120) return 0;
    if (!mkshort(basic,dest,elementid)) return 0;
    if (!mkshort(basic,dest,len)) return 0;
    for (i=0; i < len; i++)
        if (!mkchar(basic,dest,value[i])) return 0;
    return 1;
}

int cpylong(char *frame,char *dest,long value)
{
    char *p=dest;
    cpyshort(frame, p, static_cast<short>((value >> 16) & 0xFFFF));
    cpyshort(frame,p+2, static_cast<short>(value & 0xFFFF));
    return 1;

}

static int cpychar(char *frame,char *dest,char value)
{
    *dest = value;
    return 1;
}

static int cpyshort(char *frame,char *dest,short value)
{
    char *p=dest;
    cpychar(frame, p, static_cast<char>((value >> 8) & 0xFF));
    cpychar(frame,p+1, static_cast<char>(value & 0xFF));
    return 1;
}

static unsigned char getbyte(char *p)
{
    return (*(unsigned char*)p) ;
}

int Mac2Str(char *szMacAddress, unsigned char * MacAddr)
{
    sprintf(szMacAddress, "%02X%02X%02X%02X%02X%02X",
            MacAddr[0], MacAddr[1], MacAddr[2],
            MacAddr[3], MacAddr[4], MacAddr[5]);
    return 0;
}

static unsigned short getshort(short *p)
{
    return ((*(unsigned char*)p<<8) + *((unsigned char*)p+1));
}

static long getlong(long *p)
{
    unsigned long a,b;
    a = (unsigned long) getshort((short*)p) << 16;
    b = static_cast<unsigned long>(getshort((short*) ((unsigned char *)p + 2) ));
    return (a+b);
}

static int mkshort(char *frame,char **dest,short value)
{
    mkchar(frame, dest, static_cast<char>((value >> 8) & 0xFF));
    mkchar(frame, dest, static_cast<char>(value & 0xFF));
    return 1;
}

static int mkchar(char *frame,char **dest,char value)
{
    **dest=value;
    (*dest)++;
    return 1;
}

//套接字初始化
int tcpServerInit()
{
    int opt = 1000;
    flagSet_t.cellStaus[0][0] = 8;
    flagSet_t.cellStaus[1][0] = 12;
    smtpSocket = socket(AF_INET, SOCK_STREAM, 0);
    if(-1 == smtpSocket)
    {
        LOGI("smtpSocket creat failed");
        return -1;
    }
    memset(&smtpServerAddr, 0, sizeof(smtpServerAddr));
    smtpServerAddr.sin_family = AF_INET;
    smtpServerAddr.sin_port = htons(smtpServerPort);
    smtpServerAddr.sin_addr.s_addr = inet_addr(staticIp);
    setsockopt(smtpSocket,SOL_SOCKET,SO_REUSEADDR,&opt,sizeof( opt ));
    ret = bind(smtpSocket, (struct sockaddr *)&smtpServerAddr, sizeof(smtpServerAddr));
    if(-1 == ret)
    {
        LOGI("smtpServer bind failed");
        return -1;
    }
    ret = listen(smtpSocket, listenNum);
    if(-1 == ret)
    {
        LOGI("listen failed");
        return -1;
    }
    LOGI("smtpServer Init Success");
    return 0;
}

//创建监听
int tcpServerConnect()
{
    unsigned long timeStart = 0;
    unsigned long timeEnd = 0;
    int ret = 0;
    scrollMsg = (ScrollMsg *)malloc(sizeof(ScrollMsg));
    ret = sqlite3_open(SQLITE_DB, &scrollMsg->dbQcell);
    if(ret != 0)
    {
        LOGI("Error Open Sqlite3 dbQcell");
        return 0;
    }
    ret = sqlite3_open(SQLITE_DB_CREATE, &scrollMsg->dbCreate);

    if(ret != 0)
    {
        LOGI("Error Open Sqlite3 dbCreate");
        return 0;
    }
    int length = 0;
    char recvBuf[MAX_TCP_BUF_SIZE_RECV] = {0};
    smtpConnfd = accept(smtpSocket, (struct sockaddr *)&smtpClientAddr, &addrlen);
    if(-1 == smtpConnfd)
    {
        LOGI("smtpConnfd failed");
        return -1;
    }
    LOGI("into recv");
    timeStart = static_cast<unsigned int>(getCurrentTime());
    while (fileReadType(const_cast<char *>(SINGER_APP_CONNECTED)))
    {
//        s_tcpServerStatus = fileReadStatus(const_cast<char *>(FILE_M_STATUS));
        LOGI("循环开始");
        memset(&recvBuf,0,sizeof(recvBuf));
        length = static_cast<int>(recv(smtpConnfd, recvBuf, sizeof(recvBuf), 0));
        LOGI("接收到smtps消息，开始解析,length:%d\n", length);
        if(length>0)
        {
            SmtpsHeader smtpsHeader;
            int recvBufPosi = 0;
            while (fileReadType(const_cast<char *>(SINGER_APP_CONNECTED))) {
                if (!getSmtpHeader(&recvBuf[recvBufPosi], length - recvBufPosi, &smtpsHeader)) {
                    break;
                }
                if (smtpsHeader.Len < sizeof(smtpsHeader)) {
                    break;
                }
                if (length - recvBufPosi >= smtpsHeader.Len) {
                    ret = smtpsServerIndication(&recvBuf[recvBufPosi], smtpsHeader.Len, smtpConnfd);
                    if(ret == -1)
                    {
                        LOGI("tcpServerConnect return -1");
                        sqlite3_close(scrollMsg->dbQcell);
                        sqlite3_close(scrollMsg->dbCreate);
                        free(scrollMsg);
                        flagSet_t.pthread_statu = 0;
                        return ret;
                    }
                    recvBufPosi += smtpsHeader.Len;
                    LOGI("循环结束");
                } else {
                    LOGI("终止循环");
                    break;
                }
            }
        } else {
            LOGI("recv STOP");
            break;
        }
        timeEnd = static_cast<unsigned int>(getCurrentTime());

//        if(flagSet_t.ID == 0 )
//        {
//            fileWriteStatus(const_cast<char *>("0.0000"), const_cast<char *>(IMSI_RSSI));
//        }
        if((timeEnd - timeStart)/1000>2)
        {
            if((BandTdd.ulfreq != 0 && BandTdd.dlfreq != 0 && BandTdd.pci != 0 && BandTdd.band != 0) || \
 (BandFdd.ulfreq != 0 && BandFdd.dlfreq != 0 && BandFdd.pci != 0 && BandFdd.band != 0)) {
                memset(&msgList_t, 0, sizeof(msgList_t));
                memcpy(&msgList_t.scrollMsg, scrollMsg, sizeof(*scrollMsg));
                memcpy(&msgList_t.flagSet_t, &flagSet_t, sizeof(flagSet));
                memcpy(&msgList_t.BandTdd, &BandTdd, sizeof(BandInfo_t));
                memcpy(&msgList_t.BandFdd, &BandFdd, sizeof(BandInfo_t));
                LOGI("msgList_t.scrollMsg.Imsi:%s, flagSet_t.IMSI : %s,  msgList_t.scrollMsg.Type[0]: %d", msgList_t.scrollMsg.Imsi[0], flagSet_t.IMSI,  msgList_t.scrollMsg.Type[0]);
                pthread_create(&pthread_sql, NULL, bulkDumpSql_pthread, (void *) &msgList_t);
                pthread_detach(pthread_sql);
//            memset(scrollMsg->Imsi, 0, sizeof(scrollMsg->Imsi));  这样清空有问题，不空也没事
                flagSet_t.ID = 0;
                timeStart = static_cast<unsigned int>(getCurrentTime());
            }
        }
    }
    sqlite3_close(scrollMsg->dbQcell);
    sqlite3_close(scrollMsg->dbCreate);
    LOGI("END length:%d", length);
    free(scrollMsg);
    flagSet_t.pthread_statu = 0;
    return 0;
}

//关闭套接字
int socketServerClose()
{
    close(smtpConnfd);
    close(smtpSocket);
    LOGI("Socked Cloesd");
    return 0;
}

//解析包头，12字节
int getSmtpHeader(char *frame, int len, SmtpsHeader *header)
{
    if (len < (int)sizeof(SmtpsHeader))
    {
        LOGI("LEN<SmtpsHeader");
        return 0;
    }
    header->Flag = getshort((short*)frame);
    header->Code = getshort((short*)(frame+2));
    header->Version = getshort((short*)(frame+4));
    header->Seq = static_cast<unsigned int>(getlong((long*)(frame + 6)));
    header->Len = getshort((short*)(frame+10));

    return 1;
}

//加密认证
int smtpsServerIndication(char *frame, int len, int fd)
{
//    LOGI("smtpsServerIndication start");
    int outLen = WSTP_BUF_LEN;
    char outBuf[WSTP_BUF_LEN];
    char check_buf[16] = {0};
    char SendMsg[MAX_TCP_BUF_SIZE_RECV] = {0};
    char * p_check = (char*)check_buf;
    short gerneral_code = 1;
    short mac_length = 6;
    mkshort(p_check, &p_check, gerneral_code);
    mkshort(p_check, &p_check, mac_length);
    SmtpsHeader smtpsHeader;
    if(0 == getSmtpHeader(frame, len, &smtpsHeader))
    {
        LOGI("报文长度太短");
        return -1;
    }
    blowFishStatus.status = smtpsHeader.Flag;
    if(smtpsHeader.Flag == SMTPS_PROTOCOL_TYPE_DECODE)
    {
//        LOGI("收到的是加密报文");
        char *pdecode = frame+12;
        int decodeLen = len - 12;
        ret = BlowFishDecryptBin(pdecode,decodeLen, outBuf,outLen);
        if(0 != ret)
        {
            blowFishStatus.decode = 0;
            LOGI("解密失败,错误码:%d", blowFishStatus.decode = 0);
            return -1;
        }
        //对解密后的数据是否合法进行检测，
        if(memcmp(outBuf,check_buf,4))
        {
            blowFishStatus.decode = 0;
            LOGI("解密结果不存在,错误码:%d", blowFishStatus.decode = 0);
            return -1;
        }
        blowFishStatus.decode = 1;
        for(int i = 0; i < outLen; i++)
        {
            sprintf(SendMsg, "%s%02x", SendMsg, (uint8_t)outBuf[i]);
        }
        LOGI("decoded :[%s]", SendMsg);
        return SmtpsServerIndFrames(&smtpsHeader, outBuf, outLen, fd);
    } else if(smtpsHeader.Flag == SMTPS_PROTOCOL_TYPE_NONE_DECODE)
    {
//        LOGI("收到的是非加密报文");
        return 0;//待写
    }

    return 0;
}

int SmtpsServerIndFrames(SmtpsHeader *header,char *body,int bodylen, int fd)
{
    switch (header->Code)
    {
        case SMTPS_FID_AUTHREQ:
            LOGI("0x0001 WSTP_FID_AUTHREQ\n");
            if(flagSet_t.pthread_statu == 0) {
                LOGI("注册报文响应成功, ret = %d", ret);
            } else {
                LOGI("重复注册报文");
            }
            ret = Ind_AuthReq(header, body, bodylen, fd);
            CfgMsgSend(header, body, bodylen, fd);
            break;
        case SMTPS_FID_ECHOREQ:
            LOGI("0x0009 WSTP_FID_ECHOREQ\n");
            ret = Ind_EchoReq(header,body,bodylen,fd);
            LOGI("成功回应心跳");
            break;
        case SMTPS_FID_REPREQ:
            //报告请求
            LOGI("0x0003 SMTPS_FID_REPREQ\n");
            ret = Ind_ReportReq(header, body, bodylen, fd);
            break;
        case SMTPS_FID_WARNINGREQ:
            LOGI("0x000d WSTP_FID_WARNINGREQ\n");
            ret = Ind_WarningReq(header,body,bodylen,fd);
            LOGI("消息告警");
            break;
        case SMTPS_FID_CTRLRSP:
            //控制响应
            LOGI("0x0006 WSTP_FID_CTRLRSP\n");
            ret = Ind_CtrlRsp(header,body,bodylen,fd);
            break;
        case SMTPS_FID_CFGRSP:
            //配置响应
            LOGI("0x0008 WSTP_FID_CFGRSP\n");
            ret = Ind_ConfigRsp(header,body,bodylen,fd);
            break;
        case SMTPS_FID_CFGQUERYRSP:
            //配置查询响应
            LOGI("0X000C SMTPS_FID_CFGQUERYRSP：\n");
            ret = Ind_CfgQryRsp(header,body,bodylen,fd);
            break;
        default:
            LOGI("SmtpsServerIndFrames failed  %d\n",header->Code);
            return -1;
    }
    return 0;
}


static int Ind_CtrlRsp(SmtpsHeader *header,char *body,int bodylen,int fd)
{
    LOGI("Ind_CtrlRsp start\n");
    char *p;
    int datalen;
    unsigned char mac[6];
    char macStr[20] = {0};
    char sql_cmd[1024] = {0};
    CtrlRspInfo_t ctrlRsp;
    char nowTimeStr[20] = {0};

    GetTimeStr(nowTimeStr);

    p = body;
    memcpy((char *)&SmMac,p+4,sizeof(MacAddr_t));
    memcpy((char *)&mac,p+4,6);
    Mac2Str(macStr, mac);

    p += sizeof(short)+sizeof(short)+sizeof(MacAddr_t);
    datalen = static_cast<int>(p - body);
    memset(&ctrlRsp,0,sizeof(CtrlRspInfo_t));
    while(datalen < bodylen){
        short eid = getshort((short*)p);
        short vlen = getshort((short*)(p+2));
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;
        datalen = static_cast<int>(p - body);

        if(datalen > bodylen)
            break;
        ParseCtrlRspParam(eid, vlen, pvalue, &ctrlRsp);

    }
    LOGI("Ind_CtrlRsp  1####\n");
    if(ctrlRsp.exist.lte_fw_upgrade_result == 1)
    {
        /*保存 升级响应 信息*/
        memset(sql_cmd,0,sizeof(sql_cmd));
        sprintf(sql_cmd,"INSERT INTO smtps_ctrl_response(mac, type , result, sequence, response_time) VALUES('%s',%d,%d,%d,'%s');",
                macStr,SMTPS_EID_CTL_UPGRADE_RSP,ctrlRsp.lte_fw_upgrade_result,header->Seq,nowTimeStr);
    }
    if(ctrlRsp.exist.reboot_result == 1)
    {
        /*保存 重启响应 信息*/
        memset(sql_cmd,0,sizeof(sql_cmd));
        sprintf(sql_cmd,"INSERT INTO smtps_ctrl_response(mac, type , result, sequence, response_time) VALUES('%s',%d,%d,%d,'%s');",
                macStr,SMTPS_EID_CTL_REBOOT_RSP,ctrlRsp.reboot_result,header->Seq,nowTimeStr);
    }
    if(ctrlRsp.exist.reboot_and_default_result == 1)
    {
        /*保存 恢复出厂设置响应 信息*/
        memset(sql_cmd,0,sizeof(sql_cmd));
        sprintf(sql_cmd,"INSERT INTO smtps_ctrl_response(mac, type , result, sequence, response_time) VALUES('%s',%d,%d,%d,'%s');",
                macStr,SMTPS_EID_CTL_RESET_RSP,ctrlRsp.reboot_and_default_result,header->Seq,nowTimeStr);
    }
    if(ctrlRsp.exist.router_fw_upgrade_result == 1)
    {
        /*保存 恢复出厂设置响应 信息*/
        memset(sql_cmd,0,sizeof(sql_cmd));
        sprintf(sql_cmd,"INSERT INTO smtps_ctrl_response(mac, type , result, sequence, response_time) VALUES('%s',%d,%d,%d,'%s');",
                macStr,SMTPS_EID_CTL_UPGRADE_ROUTER_RSP,ctrlRsp.router_fw_upgrade_result,header->Seq,nowTimeStr);
    }
    return 0;
}

int ParseCtrlRspParam(short elementid, short bodyLen, char *pbody, CtrlRspInfo_t *ctrlRsp)
{
    LOGI("Ind_CtrlRsp  param : %04X  ####\n",elementid);

    switch(elementid)
    {
        case SMTPS_EID_CTL_UPGRADE_RSP:{

            if (bodyLen == 1) {
                ctrlRsp->exist.lte_fw_upgrade_result = 1;
                ctrlRsp->lte_fw_upgrade_result = getbyte(pbody);
            } else {
                LOGI("%s:%d, SMTPS_EID_CTL_UPGRADE_RSP len != 1",__FILE__,__LINE__);
            }
            break;
        }
        case SMTPS_EID_CTL_REBOOT_RSP:{
            if (bodyLen == 1) {
                ctrlRsp->exist.reboot_result = 1;
                ctrlRsp->reboot_result = getbyte(pbody);
            } else
            {
                LOGI("%s:%d, SMTPS_EID_CTL_REBOOT_RSP len != 1",__FILE__,__LINE__);
            }
            break;
        }
        case SMTPS_EID_CTL_RESET_RSP:{
            if (bodyLen == 1) {
                ctrlRsp->exist.reboot_and_default_result = 1;
                ctrlRsp->reboot_and_default_result = getbyte(pbody);
            } else {
                LOGI("%s:%d, SMTPS_EID_CTL_RESET_RSP len != 1",__FILE__,__LINE__);
            }
            break;
        }
        case SMTPS_EID_CTL_UPGRADE_ROUTER_RSP:{
            if (bodyLen == 1) {
                ctrlRsp->exist.router_fw_upgrade_result = 1;
                ctrlRsp->router_fw_upgrade_result = getbyte(pbody);
            } else {
                LOGI("%s:%d, SMTPS_EID_CTL_UPGRADE_ROUTER_RSP len != 1",__FILE__,__LINE__);
            }
            break;
        }
        default:
            return -1;
    }
    return 0;
}

//开启小区配置线程
int CfgMsgSend(SmtpsHeader *header,char *body,int bodylen,int fd)
{
    num = 0;
    sendBody.header = header;
    sendBody.body = body;
    sendBody.bodylen = bodylen;
    sendBody.fd = dup(fd);
    LOGI("开启线程成功");
    pthread_create(&pthread, NULL, Imsi_Position, (void *)&sendBody);
    pthread_detach(pthread);
    return 0;
}

static int Ind_ConfigRsp(SmtpsHeader *header,char *body,int bodylen,int fd)
{
    char result = -1;
    char *p;
    int datalen;
    unsigned char mac[6] = {0};
    char macStr[20] = {0};
    char nowTimeStr[20] = {0};

    GetTimeStr(nowTimeStr);

    p = body;
    memcpy((char *)&SmMac,p+4,sizeof(MacAddr_t));
    memcpy((char *)&mac,p+4,sizeof(MacAddr_t));
    Mac2Str(macStr, mac);
    p += sizeof(short)+sizeof(short)+sizeof(MacAddr_t);
    datalen = static_cast<int>(p - body);
    LOGI("****************ConfigRsp search****************\n");
    while(datalen < bodylen){
        short eid = getshort((short *)p);
        short vlen = getshort((short *)p+2);
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;
        datalen = static_cast<int>(p - body);

        if(datalen > bodylen)
            break;

        if(eid == 0X004)
        {
            result = getbyte(pvalue);
        }
        /*
        if(!ParseReportReq(eid, vlen, pvalue,cliMac))
        {
            return -1;
        }
        */
    }
    LOGI("result:%d", result);
    return 0;
}

static int Ind_CfgQryRsp(SmtpsHeader *header,char *body,int bodylen, int fd)
{
    LOGI("Ind_CfgQryRsp");
    char *p;
    int datalen, num = 0;
    unsigned char cliMac[6] = {0};
    int CodeType = 0;

    p = body;
    memcpy((char *)&SmMac,p+4,sizeof(MacAddr_t));
    memcpy((char *)&cliMac,p+4,sizeof(MacAddr_t));
    p += sizeof(short)+sizeof(short)+sizeof(MacAddr_t);
    datalen = static_cast<int>(p - body);

    while(datalen < bodylen){
        num++;
        short eid = getshort((short*)p);
        short vlen = getshort((short*)(p+2));
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;

        datalen = static_cast<int>(p - body);

        if(datalen > bodylen)
            break;

        LOGI("eid:%04x", eid);
        CodeType = eid;

        if(ParseCfgQryRspModelCode(header,eid, vlen, pvalue,cliMac,CodeType))
        {
            LOGI("%s:%d  ParseCfgQryRspModelCode failed!", __FILE__,__LINE__);
            return -1;
        }
    }
    return 0;
}

int ParseCfgQryRspModelCode(SmtpsHeader *header,short elementid, short bodyLen, char *pbody, unsigned char *cliMac, int CodeType)
{
    char *p;
    int datalen;
    QueryRspInfo_t cfgInfo;

    p = pbody;
    datalen = static_cast<int>(p - pbody);


    memset(&cfgInfo,0,sizeof(cfgInfo));
    Mac2Str((char *)(cfgInfo.macStr), cliMac);
    cfgInfo.modelCode = elementid;//模块类型码

    while(datalen < bodyLen){
        short eid = getshort((short*)p);
        short vlen = getshort((short*)(p+2));
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;
        datalen = static_cast<int>(p - pbody);

        if(datalen > bodyLen)
            break;
        if(ParseModelCfg(eid, vlen, pvalue,&cfgInfo, CodeType))
        {
            return -1;
        }
    }
    return 0;
}


/*解析每个模块的配置*/
int ParseModelCfg(short elementid, short len, char *pvalue, QueryRspInfo_t *cfgInfo, int CodeType)
{
    switch(elementid)
    {
        case SMTPS_EID_CFG_COLLECT_PERIOD:{
            if(len != 4) {
                LOGI("SMTPS_EID_CFG_COLLECT_PERIOD length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.capturePeriod = 1;
            cfgInfo->capturePeriod = static_cast<int>(getlong((long *)pvalue));
            break;
        }

        case SMTPS_EID_CFG_TAC:{
            if(len != 4)
            {
                LOGI("SMTPS_EID_CFG_TAC  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.tac = 1;
            cfgInfo->tac = static_cast<int>(getlong((long *)pvalue));
            break;
        }

        case SMTPS_EID_CFG_SYN_TYPE:{
            if(len != 1)
            {
                LOGI("SMTPS_EID_CFG_SYN_TYPE  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.sync_mode = 1;
            cfgInfo->sync_mode = getbyte(pvalue);
            break;
        }

        case SMTPS_EID_CFG_BAND_INFO:{
            if(len != sizeof(BandInfo_t)) {
                LOGI("SMTPS_EID_CFG_BAND_INFO length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.bandInfo = 1;
            memcpy(&(cfgInfo->bandInfo), pvalue, static_cast<size_t>(len));
            flagSet_t.band = ntohl(cfgInfo->bandInfo.band);
            LOGI("bandInfo:%d, %d, %d, %d", ntohl(cfgInfo->bandInfo.band), ntohl(cfgInfo->bandInfo.dlfreq), ntohl(cfgInfo->bandInfo.pci), ntohl(cfgInfo->bandInfo.ulfreq));
            wirelessInsert(cfgInfo->bandInfo);
            if(fileReadType(const_cast<char *>(WIRELESS_SET)) == 0)
                freqBandInsert(cfgInfo, scrollMsg->dbCreate, CodeType, flagSet_t);
            else
                LOGI("等待下发配置指令完成");
            break;
        }

        case SMTPS_EID_CFG_POWER:{
            if(len > 5) {
                LOGI("SMTPS_EID_CFG_POWER  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.trsPower = 1;
            memcpy(&(cfgInfo->trsPower), pvalue, static_cast<size_t>(len));
            LOGI("cfgInfo->trsPower: %s", cfgInfo->trsPower);
            break;
        }

        case SMTPS_EID_CFG_RESELECT_PRIORITY:{
            if(len != 1) {
                LOGI("SMTPS_EID_CFG_RESELECT_PRIORITY  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.resPriority = 1;
            cfgInfo->resPriority = getbyte(pvalue);
            break;
        }

        case SMTPS_EID_CFG_QRX_LEVEL_MIN:{
            if(len != 4) {
                LOGI("SMTPS_EID_CFG_QRX_LEVEL_MIN  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.qrxLevelMin = 1;
            cfgInfo->qrxLevelMin = static_cast<int>(getlong((long*)pvalue));
            break;
        }

        case SMTPS_EID_CFG_GPS_OFFSET_TIME:{
            if(len != 4) {
                LOGI("SMTPS_EID_CFG_GPS_OFFSET_TIME  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.gpsOffsetTime = 1;
            cfgInfo->gpsOffsetTime = static_cast<int>(getlong((long*)pvalue));
            break;
        }

        case SMTPS_EID_CFG_GPS_OFFSET_TIME_AUTO_CONFIG:{
            if(len != 4) {
                LOGI("SMTPS_EID_CFG_GPS_OFFSET_TIME_AUTO_CONFIG  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.gpsOffsetTimeSwitch = 1;
            cfgInfo->gpsOffsetTimeSwitch = static_cast<int>(getlong((long*)pvalue));
            break;
        }

        case SMTPS_EID_CFG_SET_LOG_LEVEL:{
            if(len != 4) {
                LOGI("SMTPS_EID_CFG_SET_LOG_LEVEL  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.logLevel = 1;
            cfgInfo->logLevel = static_cast<int>(getlong((long*)pvalue));
            break;
        }

        case SMTPS_EID_CFG_INITIAL_TAC:{
            if(len != 4) {
                LOGI("SMTPS_EID_CFG_INITIAL_TAC  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.initTac = 1;
            cfgInfo->initTac = static_cast<int>(getlong((long*)pvalue));
            break;
        }

        case SMTPS_EID_CFG_END_TAC:{
            if(len != 4) {
                LOGI("SMTPS_EID_CFG_END_TAC  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.endTac = 1;
            cfgInfo->endTac = static_cast<int>(getlong((long*)pvalue));
            break;
        }

        case SMTPS_EID_CFG_NTP_CFG:{
            if(len != sizeof(NtpCfg_t)) {
                LOGI("SMTPS_EID_CFG_NTP_CFG  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.ntpCfg = 1;
            memcpy(&(cfgInfo->ntpCfg), pvalue, static_cast<size_t>(len));
            break;
        }

        case SMTPS_EID_CFG_INTRA_INTERFERENCE_RESTRAIN_LEVEL:{
            if(len != 4) {
                LOGI("SMTPS_EID_CFG_INTRA_INTERFERENCE_RESTRAIN_LEVEL  length error : %d!",len);
                return -1;
            }

            cfgInfo->exist.irLevel = 1;
            cfgInfo->irLevel = static_cast<int>(getlong((long*)pvalue));
            break;
        }

        case SMTPS_EID_CFG_MODE_TYPE:{		//采集模式(0:只抓一次模式, 1:周期模式, 2:全抓模式, 3:抓捕模式)
            if (len != 1) {
                LOGI("SMTPS_EID_CFG_MODE_TYPE  length error : %d!",len);
                return -1;
            }
            cfgInfo->exist.captureType = 1;
            cfgInfo->captureType = getbyte(pvalue);
            LOGI("$$$$$$$$$$$$$$-------------$$$$$$$$$$$$$$$$$");
            LOGI("%d", CodeType);
//            if(cfgInfo->captureType != 3){
//                flagSet_t.type_statu = 1;
//            }
            break;
        }

        case SMTPS_EID_CFG_LOCK_IMSI:{		//定位IMISI
            if(len != 16) {
                LOGI("SMTPS_EID_CFG_LOCK_IMSI  length error : %d!",len);
                break;
            }
            cfgInfo->exist.lockImsi = 1;
            bzero(&(cfgInfo->lockImsi), sizeof(cfgInfo->lockImsi));
            memcpy(&(cfgInfo->lockImsi), pvalue, static_cast<size_t>(len));
            LOGI("当前定位IMSI为:%s", cfgInfo->lockImsi);
            if(CodeType == 8)
                strcpy(flagSet_t.IMSI_SELECT_TDD, cfgInfo->lockImsi);
            else if(CodeType == 12)
                strcpy(flagSet_t.IMSI_SELECT_FDD, cfgInfo->lockImsi);
            break;
        }

        case SMTPS_EID_CFG_WHITE_LIST:{		//白名单
            if(len <= 0 || len > 255) {
                LOGI("SMTPS_EID_CFG_WHITE_LIST  length error : %d!",len);
                return -1;
            }
            cfgInfo->exist.whiteList = 1;
            bzero(&(cfgInfo->whiteList), sizeof(cfgInfo->whiteList));
            memcpy(&(cfgInfo->whiteList), pvalue, static_cast<size_t>(len));
            break;
        }

        case SMTPS_EID_CFG_BLACK_LIST:{		//黑名单
            if(len <= 0 || len > 255) {
                LOGI("SMTPS_EID_CFG_BLACK_LIST  length error : %d!",len);
                return -1;
            }
            cfgInfo->exist.blackList = 1;
            bzero(&(cfgInfo->blackList), sizeof(cfgInfo->blackList));
            memcpy(&(cfgInfo->blackList), pvalue, static_cast<size_t>(len));
            break;
        }

        default:
            return 0;
    }
    return 0;
}


static int Ind_EchoReq(SmtpsHeader *header,char *body,int bodylen, int fd)
{
    char *p;
    uint32_t length;
    char SendBuf[WSTP_BUF_LEN] = {0};
    unsigned char mac[6] = {0};

    p = body;
    memcpy((char *)&SmMac, p+4,sizeof(MacAddr_t));
    memcpy((char *)&mac, p+4,6);

    length = static_cast<uint32_t>(mkframe(SendBuf, header->Seq, SMTPS_FID_ECHORSP, (char *)mac));
    if(length <= 0)
    {
        return -1;
    }

    length = static_cast<uint32_t>(send(fd, SendBuf, length, 0));
    if(length <= 0)
    {
        return -1;
    }
    return 0;
}

static int Ind_AuthReq(SmtpsHeader *header,char *body,int bodylen, int fd)
{
    char *p;
    int datalen;
    short length;
    uint8_t remstatus = 0;
    char SendBuf[WSTP_BUF_LEN] = {0};
    AuthReqInfo_t authInfo;
    unsigned char mac[6];

    memset(&authInfo,0,sizeof(AuthReqInfo_t));
    p = body;
    memcpy((char *)&SmMac,p+4,sizeof(MacAddr_t));
    memcpy(mac,p+4,6);
    Mac2Str(authInfo.macStr, (unsigned char *)mac);
    if (authInfo.macStr[12] != '\0')
    {
        LOGI("%s:%d,mac is illegal in Ind_AuthReq", __FILE__, __LINE__);
        return 0;
    }
    p += sizeof(short)+sizeof(short)+sizeof(MacAddr_t);
    datalen = static_cast<int>(p - body);


    while(datalen < bodylen){
        short eid = getshort((short*)p);
        short vlen = getshort((short*)(p+2));
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;

        datalen = static_cast<int>(p - body);

        if(datalen > bodylen)
            break;

        if(ParseAuthReqEid(eid, vlen, pvalue,&authInfo))
        {
            LOGI("%s:%d,ParseAuthReqEid failed.", __FILE__,__LINE__);
            return -1;
        }
        //判断认证报文是否完整
        if((authInfo.exist.productModel == 1)
           &&(authInfo.exist.bootType ==1)
           &&(authInfo.exist.lngLat == 1)
           &&(authInfo.exist.remStatus == 1)
           &&(authInfo.exist.routerFirwareVersion == 1)
           &&(authInfo.exist.firwareVersion == 1)) {
            LOGI("报文完整");
            LOGI("authInfo.macStr:%s, mac:%s", authInfo.macStr, mac);
            /* 回复认证响应 */
            remstatus = 1;
            length = static_cast<short>(mkframeValueUnit8(SendBuf, static_cast<short>(header->Seq), SMTPS_FID_AUTHRSP, remstatus, (char *)mac));
            if(length <= 0) {
                LOGI("auth -1\n");
                return -1;;
            }
            length = static_cast<short>(send(fd, SendBuf, static_cast<size_t>(length), 0));
            if(length <= 0) {
                LOGI("send auth rsp -1\n");
                return -1;
            }
            LOGI("发送了注册响应报文");
        }
    }
    return 0;
}

int ParseAuthReqEid(short elementid, short len, char *pvalue,AuthReqInfo_t *authInfo)
{
    switch(elementid)
    {
        case SMTPS_EID_AUTH_CIPHER_TEXT:{
            break;
        }
        case SMTPS_EID_AUTH_CLEAR_TEXT:{
            break;
        }
        case SMTPS_EID_COMM_MODEL:{
            if (len > 32) {
                LOGI("%s:%d SMTPS_EID_COMM_MODEL len > 32",__FILE__,__LINE__);
                return -1;
            }
            authInfo->exist.productModel = 1;
            memcpy(authInfo->productModel, pvalue, static_cast<size_t>(len));
            LOGI("authInfo->productModel:%s", authInfo->productModel);

            freqBandMemory(authInfo->productModel, scrollMsg->dbCreate);
        }
        case SMTPS_EID_COMM_FW:{
            if (len > 32) {
                LOGI("%s:%d SMTPS_EID_COMM_FW len > 32",__FILE__,__LINE__);
                return -1;
            }
            authInfo->exist.firwareVersion = 1;
            memcpy(authInfo->firwareVersion, pvalue, static_cast<size_t>(len));
            break;
        }
        case SMTPS_EID_COMM_BOOT_TYPE:{
            if (len > 1) {
                LOGI("%s:%d SMTPS_EID_COMM_BOOT_TYPE len > 1",__FILE__,__LINE__);
                return -1;
            }
            uint8_t boot_type = 100;
            boot_type = getbyte(pvalue);
            authInfo->exist.bootType = 1;
            authInfo->bootType = boot_type;
            break;
        }
        case SMTPS_EID_COMM_REM_STATUS:{
            if (len > 1) {
                LOGI("%s:%d SMTPS_EID_COMM_REM_STATUS len > 1",__FILE__,__LINE__);
                return -1;
            }
            uint8_t remStatus = 0;
            remStatus = getbyte(pvalue);
            authInfo->exist.remStatus = 1;
            authInfo->remStatus = remStatus;
            break;
        }
        case SMTPS_EID_COMM_ROUTER_VERSION:{
            if (len > 32) {
                LOGI("%s:%d SMTPS_EID_COMM_ROUTER_VERSION len > 32",__FILE__,__LINE__);
                return -1;
            }
            memcpy(authInfo->routerFirwareVersion, pvalue, static_cast<size_t>(len));
            authInfo->exist.routerFirwareVersion = 1;
            break;
        }
        case SMTPS_EID_RPT_GPS_LNG_LAT:{
            if(len != 32) {
                LOGI("%s:%d SMTPS_EID_RPT_GPS_LNG_LAT len != 32",__FILE__,__LINE__);
                return -1;
            }
            memcpy(&(authInfo->lngLat), pvalue, static_cast<size_t>(len));
            authInfo->exist.lngLat = 1;
            break;
        }
        default:{
            LOGI("%s:%d ParseAuthReqEid  default",__FILE__,__LINE__);
            return 1;
        }
    }
    return 0;
}

int mkframeValueUnit8(char *frame,short Sequence,short FrameId, uint8_t value,char * mac)
{
    char *pbasic;
    short length;
    char outBuf[WSTP_BUF_LEN] = {0};
    char decodeBuf[WSTP_BUF_LEN] = {0};
    int outLen = WSTP_BUF_LEN;
    int decodeLen = WSTP_BUF_LEN;
    int ret;

    cpyshort(frame,frame,WSTP_PROTOCOLID);
    cpyshort(frame,frame+2,FrameId);
    cpyshort(frame,frame+4,0x0004);
    cpylong(frame, frame+6,Sequence);
    pbasic = frame+12;

    switch(FrameId)
    {
        case SMTPS_FID_AUTHRSP:{
            uint8_t result = 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), mac)) return 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_RESULT, sizeof(uint8_t), (char *)&result)) return 0;
            if(!mkshort(frame,&pbasic,0X000C)) return 0;
            break;
        }
        case SMTPS_FID_CFGREQ:{
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), (char *)&SmMac)) return 0;
            break;
        }
        default:
            return -1;
    }

    length = static_cast<short>((int)(pbasic - frame));
    if(blowFishStatus.status == 0x5001) {
        LOGI("响应:报文加密");
        char *pencode = frame + 12;
        int encodeLen = length -12;
        ret = BlowFishEncryptBin(pencode, encodeLen, outBuf, outLen);
        if (0 != ret) {
            blowFishStatus.encrypt = 0;
            LOGI("加密失败,状态码:%d", blowFishStatus.encrypt);
            return -1;
        }
        blowFishStatus.encrypt = 1;
        memcpy(frame+12, outBuf, static_cast<size_t>(outLen));
        cpyshort(frame, frame+10, static_cast<short>(outLen + 12));

        //包头解密
        ret = BlowFishDecryptBin((const char*)outBuf,outLen, decodeBuf,decodeLen);
        if(0 != ret) {
            blowFishStatus.encrypt = 0;
            LOGI("解密失败,状态码:%d", blowFishStatus.decode);
        }
        blowFishStatus.decode = 1;
        LOGI("加密成功");
        return static_cast<uint32_t>(outLen + 12);
    }
    else if(blowFishStatus.status == 0x5000) {
        cpyshort(frame,frame+10, length);
        return length;
    }
    else {
        return -1;
    }
}
int mkframe(char *frame,int32_t Sequence,short FrameId,char * mac)
{
    int Code = atoi(frame);
    LOGI("Code:%d", Code);
    memset(frame, 0, sizeof(frame));
    char *pbasic;
    short length;
    char outBuf[WSTP_BUF_LEN] = {0};
    char decodeBuf[WSTP_BUF_LEN] = {0};
    int outLen = WSTP_BUF_LEN;
    int decodeLen = WSTP_BUF_LEN;
    int ret;

    cpyshort(frame,frame,WSTP_PROTOCOLID);
    cpyshort(frame,frame+2,FrameId);
    cpyshort(frame,frame+4,0X0004);
    cpylong(frame,frame+6,Sequence);
    pbasic = frame+12;
    LOGI("mkframe FrameId:%d", FrameId);
    switch(FrameId)
    {
        case SMTPS_FID_AUTHRSP:{
            uint8_t result = 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), mac)) return 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_RESULT, sizeof(uint8_t), (char *)&result)) return 0;
            break;
        }
        case SMTPS_FID_ECHORSP:{
            uint8_t result = 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), mac)) return 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_RESULT, sizeof(uint8_t), (char *)&result)) return 0;
            break;
        }
        case SMTPS_FID_REPRSP:{  		//回复报告请求响应
            uint8_t result = 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), mac)) return 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_RESULT, sizeof(uint8_t), (char *)&result)) return 0;
            break;
        }
        case SMTPS_FID_WARNINGRSP:{
            uint8_t result = 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), mac)) return 0;
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_RESULT, sizeof(uint8_t), (char *)&result)) return 0;
            break;
        }
        case SMTPS_FID_CFGREQ:{
            if(!mkelement(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), mac, Code)) return 0;
            break;
        }
        case SMTPS_FID_CFGQUERYREQ:{
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), mac)) return 0;
            break;
        }
        case SMTPS_EID_CTL_RESPOND: {
            if(!mkelement2(frame, &pbasic, WSTP_EID_COMM_MAC, sizeof(MacAddr_t), mac)) return 0;
            if(!mkelement2(frame, &pbasic, SMTPS_EID_CTL_RESPOND, sizeof(MacAddr_t), mac)) return 0;
        }
        default:
            return -1;
    }

    length = static_cast<short>((int)(pbasic - frame));

    if(blowFishStatus.status == SMTPS_PROTOCOL_TYPE_DECODE) {
        char *pencode = frame + 12;
        int encodeLen = length -12;
        ret = BlowFishEncryptBin(pencode, encodeLen, outBuf, outLen);
        if (0 != ret) {
            LOGI("encode auth error:%d!\n",ret);
            return -1;
        }
        memcpy(frame+12, outBuf, static_cast<size_t>(outLen));
        cpyshort(frame, frame+10, static_cast<short>(outLen + 12));

        ret = BlowFishDecryptBin((const char*)outBuf,outLen, decodeBuf,decodeLen);
        if(0 != ret) {
            LOGI("mkframe decode error:%d\n",ret);
        }
        return (outLen + 12);
    }
    else if(blowFishStatus.status == SMTPS_PROTOCOL_TYPE_NONE_DECODE) {
        cpyshort(frame,frame+10, length);
        return length;
    }
    else {
        return -1;
    }
}

static int Ind_ReportReq(SmtpsHeader *header,char *body,int bodylen,int fd)
{
    char *p;
    int datalen;
    uint32_t length;
    char SendBuf[WSTP_BUF_LEN] = {0};
    unsigned char cliMac[6] = {0};

    p = body;
    memcpy((char *)&SmMac,p+4,sizeof(MacAddr_t));
    memcpy((char *)&cliMac,p+4,sizeof(MacAddr_t));
    p += sizeof(short)+sizeof(short)+sizeof(MacAddr_t);
    datalen = static_cast<int>(p - body);

    while(datalen < bodylen){
        short eid = getshort((short*)p);
        short vlen = getshort((short*)(p+2));
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;

        datalen = static_cast<int>(p - body);

        if(datalen > bodylen)
            break;
        if(ParseReportEid(eid, vlen, pvalue,cliMac)) {
            LOGI("%s:%d  ParseReportEid failed!", __FILE__,__LINE__);
            return -1;
        }
    }
#if 1
    length = static_cast<uint32_t>(mkframe(SendBuf, header->Seq, SMTPS_FID_REPRSP, (char *)cliMac));
    if(length <= 0) {
        LOGI("faied mkframe");
        return -1;
    }
    length = static_cast<uint32_t>(send(fd, SendBuf, length, 0));
    if(length <= 0) {
        LOGI("send rpt rsp failed! fd: %d,",fd);
        return -1;
    }
#endif
    return 0;
}

int ParseReportEid(short elementid, short bodyLen, char *pbody, unsigned char *cliMac)
{
    char *p;
    int datalen;

    p = pbody;
    datalen = static_cast<int>(p - pbody);
    ReportInfo_t rptInfo;

    memset(&rptInfo,0,sizeof(rptInfo));
    Mac2Str((char *)(rptInfo.macStr), cliMac);
    rptInfo.modelCode = elementid;//模块类型码
    LOGI("模块类型码:ParseReportEid elementid:%x", elementid);
    scrollMsg->modelType = elementid;

    while(datalen < bodyLen){
        short eid = getshort((short*)p);
        short vlen = getshort((short*)(p+2));
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;
        datalen = static_cast<int>(p - pbody);

        if(datalen > bodyLen)
            break;
        if(ParseReportReq(eid, vlen, pvalue,&rptInfo, scrollMsg)) {
            LOGI("ParseReportReq failed");
            return -1;
        }
    }
    if(scrollMsg->Flag == 1) {
        scrollMsg->Type[flagSet_t.ID] = elementid;
        LOGI("scrollMsg:%d, %s, %s, %s, %d, %d",scrollMsg->Flag, scrollMsg->Imsi[flagSet_t.ID], scrollMsg->Attriution[flagSet_t.ID], scrollMsg->CptTime[flagSet_t.ID], scrollMsg->band[flagSet_t.ID], scrollMsg->Type[flagSet_t.ID]);
        scrollMsg->Flag = 0;
        flagSet_t.ID++;
    }
    return 0;
}

/* 格式化时间 */
int GetTimeStr(char *timeStr)
{
    time_t now;
    struct tm *t;
    time(&now);
    t = localtime(&now);
    sprintf(timeStr,"%d-%02d-%02d %02d:%02d:%02d",t->tm_year+1900,t->tm_mon+1,t->tm_mday,t->tm_hour,t->tm_min,t->tm_sec);
    return 0;
}

int ParseReportReq(short elementid, short len, char *pvalue, ReportInfo_t *rptInfo, ScrollMsg *scrollMsg)
{
    char nowTimeStr[20]={0};
    char number[16] = {0};

    GetTimeStr(nowTimeStr);
    LOGI("%x", elementid);
    switch(elementid)
    {
        //IMSI
        case SMTPS_EID_RPT_IMSI:{
            uint8_t imsi[16];
            if(len != 16) {
                LOGI("rpt imsi length != 16");
                if (len < 16) {
                    LOGI("error imsi %s",pvalue);
                }
                else {
                    LOGI("error imsi  len  %d,",len);
                    memcpy(imsi,pvalue,16);
                    imsi[15] = '\0';
                    LOGI("error imsi %s\n",imsi);
                }
                return -1;
            }
            memcpy(imsi, pvalue, static_cast<size_t>(len));
            rptInfo->exist.imsi = 1;
            memcpy(rptInfo->imsi, pvalue, static_cast<size_t>(len));
            get_phone_by_imsi(reinterpret_cast<const char *>(imsi), number, sizeof(number));
            scrollMsg->Flag = 1;
            strcpy(scrollMsg->Imsi[flagSet_t.ID], reinterpret_cast<const char *>(imsi));
            strcpy(scrollMsg->number[flagSet_t.ID], number);
            LOGI("number:%s", number);
            break;
        }
            //IMEI
        case SMTPS_EID_RPT_IMEI:{
            uint8_t imei[16];
            memcpy(imei, pvalue, static_cast<size_t>(len));
            LOGI("imei = %s,len = %d\n", imei, len);
            break;
        }
            //捕获时间
        case SMTPS_EID_RPT_CUR_TIME:{
            char time[20] = {0};

            if(len != 20) {
                LOGI("rpt capture time length != 20");
                return -1;
            }
            memcpy(time, pvalue, static_cast<size_t>(len));
            rptInfo->exist.captureTime = 1;
            memcpy(rptInfo->captureTime, pvalue, static_cast<size_t>(len));
            cptTimeGet(rptInfo->captureTime);
            strcpy(scrollMsg->CptTime[flagSet_t.ID], rptInfo->captureTime);

            scrollMsg->band[flagSet_t.ID] = flagSet_t.band;
            break;
        }
            //Band信息
        case SMTPS_EID_RPT_BAND_INFO:{
            int i = 0;
            band_t band[3] = {0};

            memcpy(band, pvalue, static_cast<size_t>(len));
            LOGI("band len = %d\n", len);//zq
            for(i = 0;  i < len/(int)sizeof(band_t); i++) {
                LOGI("band[%d].band = %u\n",i, band[i].band);
                LOGI("band[%d].ulfreq = %u\n",i, band[i].ulfreq);
                LOGI("band[%d].dlfreq = %u\n",i, band[i].dlfreq);
                LOGI("band[%d].pci = %u\n",i, band[i].pci);
            }
            break;
        }
            //TAC
        case SMTPS_EID_RPT_TAC:{
            uint32_t tac;

            tac = static_cast<uint32_t>(getlong((long *)pvalue));
            LOGI("tac = %u\n", tac);//zq
            break;
        }
            //频点表
        case SMTPS_EID_RPT_FREQ_LIST:{
            int i = 0;
            freq_t  freq[16] = {0};

            LOGI("freq len = %d\n", len);//zq
            memcpy(freq, pvalue, static_cast<size_t>(len));
            for(i = 0; i < len/(int)sizeof(freq_t); i++) {
                freq[i].upFreq = ntohl(freq[i].upFreq);
                freq[i].downFreq = ntohl(freq[i].downFreq);

                LOGI("freq[%d].upFreq = %u\n",i, freq[i].upFreq);
                LOGI("freq[%d].downFreq = %u\n",i, freq[i].downFreq);
            }
            break;
        }
            //邻区表
        case SMTPS_EID_RPT_NEIGHBOR_LIST:{
            int i = 0;
            neighbor_t neighbor[16] = {0};

            LOGI("neighbor len = %d\n", len);//zq
            memcpy(neighbor, pvalue, static_cast<size_t>(len));
            for(i = 0; i < len/(int)sizeof(neighbor_t); i++) {
                neighbor[i].pci = ntohl(neighbor[i].pci);
                neighbor[i].cgi = ntohl(neighbor[i].cgi);
                neighbor[i].type = ntohl(neighbor[i].type);
                neighbor[i].tac = ntohl(neighbor[i].tac);
                neighbor[i].freq = ntohl(neighbor[i].freq);
                neighbor[i].reselection_priority = ntohl(neighbor[i].reselection_priority);
                neighbor[i].gpsOffsetTime = ntohl(neighbor[i].gpsOffsetTime);


                LOGI("neighbor[%d].pci = %u\n",i, neighbor[i].pci);
                LOGI("neighbor[%d].cgi = %u\n",i, neighbor[i].cgi);
                LOGI("neighbor[%d].type = %u\n",i, neighbor[i].type);
                LOGI("neighbor[%d].tac = %u\n",i, neighbor[i].tac);
                LOGI("neighbor[%d].freq = %u\n",i, neighbor[i].freq);
                LOGI("neighbor[%d].reselection_priority = %d\n",i, neighbor[i].reselection_priority);
                LOGI("neighbor[%d].gpsOffsetTime = %d\n",i, neighbor[i].gpsOffsetTime);
                LOGI("neighbor[%d].f = %s\n",i, neighbor[i].plmn);
                LOGI("neighbor[%d].RSRP = %s\n",i, neighbor[i].RSRP);
            }
            break;
        }
            //PLMN
        case SMTPS_EID_RPT_PLMN:{
            char plmn[6] = {0};
            memcpy(plmn, pvalue, static_cast<size_t>(len));
            LOGI("plmn = %s,len = %d\n", plmn, len);//zq
            break;
        }
            //频点
        case SMTPS_EID_RPT_FREQ:{
            uint32_t freq = 0;
            freq = static_cast<uint32_t>(getlong((long *)pvalue));
            LOGI("freq = %u\n", freq);//zq
            break;
        }
            //PCI
        case SMTPS_EID_RPT_PCI:{
            uint32_t pci = 0;
            pci = static_cast<uint32_t>(getlong((long *)pvalue));
            LOGI("pci = %u\n", pci);//zq
            break;
        }
            //场强
        case SMTPS_EID_RPT_RSSI:{
            char rssi[8] = {0};
            if(len != 8) {
                LOGI("rpt rssi length != 8");
                return -1;
            }
            else {
                memcpy(rssi, pvalue, static_cast<size_t>(len));
                memcpy(rptInfo->rssi, pvalue, static_cast<size_t>(len));
                rptInfo->exist.rssi = 1;
            }
            LOGI("rptInfo->rssi:%s, ID:%d", rptInfo->rssi, flagSet_t.ID);
            strcpy(scrollMsg->Rssi[flagSet_t.ID], rptInfo->rssi);
            LOGI("IMSI:%s", scrollMsg->Imsi[flagSet_t.ID]);
            break;
        }
            //当前嗅探的频点
        case SMTPS_EID_RPT_CUR_FREQ:{
            uint8_t cur_freq = 0;

            cur_freq = getbyte(pvalue);
            LOGI("cur_freq = %u\n", cur_freq);
            break;
        }
            //状态信息定时上报
        case SMTPS_EID_RPT_DEVICE_STATE:{
            if(len != 29) {
                LOGI("%s:%d,SMTPS_EID_RPT_DEVICE_STATE len != 29",__FILE__,__LINE__);
                return -1;
            }
            memset(&(rptInfo->deviceState),0,sizeof(DeviceState_t));
            memcpy(&(rptInfo->deviceState), pvalue, static_cast<size_t>(len));
            rptInfo->exist.deviceState = 1;
            switch (scrollMsg->modelType) {
                case 8:
                    flagSet_t.cellStaus[0][0] = 8;
                    flagSet_t.cellStaus[0][1] = rptInfo->deviceState.cell_state;
                    break;
                case 12:
                    flagSet_t.cellStaus[1][0] = 12;
                    flagSet_t.cellStaus[1][1] = rptInfo->deviceState.cell_state;
                    break;
                default:
                    break;
            }
            LOGI("当前小区状态是rptInfo->deviceState.cell_state:%d", rptInfo->deviceState.cell_state);
            break;
        }
            //IMSI缓存与传输状态
        case SMTPS_EID_RPT_IMSI_CACHING_AND_TRANS_STATUS:{
            if(len != sizeof(ImsiTransmitInfoRpt_t)) {
                LOGI("%s:%d,SMTPS_EID_RPT_IMSI_CACHING_AND_TRANS_STATUS len != %d",__FILE__,__LINE__,sizeof(ImsiTransmitInfoRpt_t));
                return -1;
            }
            memcpy(&(rptInfo->imsiTransInfo), pvalue, static_cast<size_t>(len));
            rptInfo->imsiTransInfo.offlineCount = ntohl(rptInfo->imsiTransInfo.offlineCount);
            rptInfo->imsiTransInfo.retransmitCount = ntohl(rptInfo->imsiTransInfo.retransmitCount);
            rptInfo->imsiTransInfo.percentage = ntohl(rptInfo->imsiTransInfo.percentage);
            rptInfo->imsiTransInfo.realTimeCount = ntohl(rptInfo->imsiTransInfo.realTimeCount);
            rptInfo->imsiTransInfo.remainOfflineCount = ntohl(rptInfo->imsiTransInfo.remainOfflineCount);
            rptInfo->imsiTransInfo.rptSuccessCount = ntohl(rptInfo->imsiTransInfo.rptSuccessCount);
            rptInfo->exist.imsiTransInfo = 1;

            if(rptInfo->imsiTransInfo.remainOfflineCount < 1) {
                strcpy(rptInfo->imsiTransInfo.oldestTime,"null");
            }
            break;
        }
            //经纬度
        case SMTPS_EID_RPT_GPS_LNG_LAT:{
            GpsLngLat_t lngLat;
            if(len != 32) {
                LOGI("%s:%s SMTPS_EID_RPT_GPS_LNG_LAT len != 32",__FILE__,__LINE__);
                return -1;
            }
            memcpy(&(rptInfo->lngLat), pvalue, static_cast<size_t>(len));
            rptInfo->exist.lngLat = 1;
            memcpy(&lngLat,pvalue,sizeof(lngLat));
            LOGI("longitutde : %s,latitude : %s\n",lngLat.Longitude,lngLat.Latitude);
        }
            //系统运行时长
        case WSTP_EID_RPT_SYS_AND_CELL_UPTIME:{
            uptime_sys_and_cell_t uptime;
            if(len != sizeof(uptime_sys_and_cell_t)) {
                LOGI("%s:%s WSTP_EID_RPT_SYS_AND_CELL_UPTIME len != 32",__FILE__,__LINE__);
                return -1;
            }
            rptInfo->exist.sys_cell_uptime = 1;
            memcpy(&(rptInfo->sys_cell_uptime),pvalue,sizeof(uptime_sys_and_cell_t));
            memcpy(&uptime,pvalue,sizeof(uptime_sys_and_cell_t));
            LOGI("sys uptime : %s,cell_uptime : %s\n",uptime.sys_uptime,uptime.cell_uptime);
            break;
        }
        case SMTPS_EID_RPT_ANTENNA_ANGLE: {
            break;
        }
        default: {
            return 0;
        }
    }
    return 0;
}
static int Ind_WarningReq(SmtpsHeader *header,char *body,int bodylen, int fd)
{
    char *p;
    int datalen;
    uint32_t length;
    char SendBuf[WSTP_BUF_LEN] = {0};
    unsigned char mac[6];
    char macStr[20] = {0};

    p = body;
    memcpy((char *)&SmMac,p+4,sizeof(MacAddr_t));
    memcpy((char *)&mac,p+4,6);
    Mac2Str(macStr, mac);

    p += sizeof(short)+sizeof(short)+sizeof(MacAddr_t);
    datalen = static_cast<int>(p - body);

    while(datalen < bodylen){
        short eid = getshort((short*)p);
        short vlen = getshort((short*)(p+2));
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;

        datalen = static_cast<int>(p - body);

        if(datalen > bodylen)
            break;

        ParseWarningEid(eid, vlen, pvalue,macStr);

    }

    length = static_cast<uint32_t>(mkframe(SendBuf, header->Seq, SMTPS_FID_WARNINGRSP, (char *)mac));
    if(length <= 0) {
        return -1;
    }

    length = static_cast<uint32_t>(send(fd, SendBuf, length, 0));
    if(length <= 0) {
        return -1;
    }
    return 0;
}
int ParseWarningEid(short elementid, short bodyLen, char *pbody,char *macStr)
{
    char *p;
    int datalen;
    char warning_reason[32] = {0};
    char sql_cmd[512] = {0};
    char nowTimeStr[20] = {0};

    GetTimeStr(nowTimeStr);

    p = pbody;
    datalen = static_cast<int>(p - pbody);
    while(datalen < bodyLen){
        short eid = getshort((short*)p);
        LOGI("warning_id = %x\n",eid);
        short vlen = getshort((short*)(p+2));
        char *pvalue = p+4;

        p += sizeof(short)+sizeof(short)+vlen;
        datalen = static_cast<int>(p - pbody);

        if(datalen > bodyLen)
            break;
        if(!ParseWarningReq(eid, vlen, pvalue,warning_reason)) {
            return -1;
        }
    }
    return 0;
}

int ParseWarningReq(short elementid, short len, char *pvalue,char * reason)
{
    switch(elementid)
    {
        case SMTPS_EID_WARNING_VERSION:{
            if(len > 32) {
                LOGI("%s:%d,SMTPS_EID_WARNING_VERSION len > 32",__FILE__,__LINE__);
                return -1;
            }
            LOGI("WSTP_EID_WARNING_VERSION\n");
            char versionstr[128] = {0} ;
            memcpy(versionstr, pvalue, static_cast<size_t>(len));
            memcpy(reason, pvalue, static_cast<size_t>(len));
            LOGI("versionstr = %s,len = %d\n",versionstr,len);
            break;
        }

        case SMTPS_EID_WARNING_EXIT_ABNORMAL:{
            if(len > 32) {
                LOGI("%s:%d,SMTPS_EID_WARNING_EXIT_ABNORMAL len > 32",__FILE__,__LINE__);
                return -1;
            }
            LOGI("WSTP_EID_WARNING_EXIT_ABNORMAL\n");
            char exitstr[128] = {0};
            memcpy(exitstr, pvalue, static_cast<size_t>(len));
            memcpy(reason, pvalue, static_cast<size_t>(len));
            LOGI("exitstr = %s,len = %d\n",exitstr,len);
            break;
        }

        case SMTPS_EID_WARNING_REBOOT_ABNORMAL:{
            if(len > 32) {
                LOGI("%s:%d,SMTPS_EID_WARNING_REBOOT_ABNORMAL len > 32",__FILE__,__LINE__);
                return -1;
            }
            LOGI("WSTP_EID_WARNING_REBOOT_ABNORMAL\n");
            char rebootstr[128] = {0};
            memcpy(rebootstr, pvalue, static_cast<size_t>(len));
            memcpy(reason, pvalue, static_cast<size_t>(len));
            LOGI("rebootstr = %s,len = %d\n",rebootstr,len);
            break;
        }

        case SMTPS_EID_WARNING_UNPACK_WARNING:{
            if(len > 32) {
                LOGI("%s:%d,SMTPS_EID_WARNING_UNPACK_WARNING len > 32",__FILE__,__LINE__);
                return -1;
            }
            LOGI("SMTPS_EID_WARNING_UNPACK_WARNING\n");
            char upackstr[128] = {0};
            memcpy(upackstr, pvalue, static_cast<size_t>(len));
            memcpy(reason, pvalue, static_cast<size_t>(len));
            LOGI("upackstr = %s,len = %d\n",upackstr,len);
            break;
        }
        default:
            return -1;
    }
    return 0;
}

int smtpServerJudge()
{
    fileWriteType(const_cast<char *>(SMTPS_CONNECTED), 1);
    num = 0;
    fileWriteLogs(const_cast<char *>("APP第一次建立连接"), const_cast<char *>(LOG_FILE));
    while (fileReadType(const_cast<char *>(SINGER_APP_CONNECTED)) && num<5) {
        LOGI("smtpServerJudge Start");
        ret = tcpServerInit();
        if(-1 == ret) {
            num++;
            socketServerClose();
            sleep(1);
            continue;
        }
        tcpServerConnect();
        socketServerClose();
        LOGI("一次循环终止");
        pthread_join(pthread, NULL);
        fileWriteLogs(const_cast<char *>("smtp 连接异常， 重新建立连接"), const_cast<char *>(LOG_FILE));
    }
    if(fileReadType(const_cast<char *>(SINGER_APP_CONNECTED))) {
        fileWriteLogs(const_cast<char *>("SINGER_APP_CONNECTED 0, 网络原因连接失败"), const_cast<char *>(LOG_FILE));
        fileWriteType(const_cast<char *>(SMTPS_CONNECTED), 0);
        fileWriteType(const_cast<char *>(SINGER_APP_CONNECTED), 0);
    }
    return 0;
}