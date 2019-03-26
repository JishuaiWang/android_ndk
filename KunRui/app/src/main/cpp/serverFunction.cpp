//
// Created by HYX on 2018/8/6.
//

#include "com_example_kunrui_kunrui_serverStart.h"

pthread_mutex_t count_mutex;
char buf[1024] = {0};

int strtok_t(char *buf, char stok, char *target) {
    char *p, *ptr;
    if((p = strtok_r(buf, &stok, &ptr))) {
        if(0 == strncmp(p, target, sizeof(p))){
            if((p = strtok_r(NULL, &stok, &ptr))){
                return atoi(p);
            }}}
    return -1;
}

int fileWriteType(char *target, int status) {
    FILE *fp_write;
    char buf[128]={0};
    char buffer[128] = {0};
    int length = 0, result = -1;
    if((fp_write = fopen(CONFIG_TXT,"r+"))==NULL)
    {
        LOGI("cannot open file");
        exit(0);
    }
    while(fgets(buf, sizeof(buf),fp_write)!=NULL)
    {
        length = static_cast<int>(strlen(buf));
        result = strtok_t(buf, ' ', target);
        if(result != -1) {
            fseek(fp_write, -length, SEEK_CUR);
            sprintf(buffer, "%s    %d\n", buf, status);
            fwrite(buffer, strlen(buffer), 1, fp_write);
            break;
        }
        bzero(buf, sizeof(buf));
    }
    fclose(fp_write);
    return result;
}

int fileReadType(char *target) {
    FILE *fp_read;
    int result = -1;
    char buf[128]={0};
    if((fp_read = fopen(CONFIG_TXT, "r+"))==NULL)
    {
        LOGI("cannot open file");
        exit(0);
    }
    while(fgets(buf, sizeof(buf),fp_read)!=NULL)
    {
        result = strtok_t(buf, ' ', target);
        if(result != -1) {
            LOGI("buf:%s， result：%d", buf, result);
            break;
        }
        bzero(buf, sizeof(buf));
    }
    fclose(fp_read);
    return result;
}

int mkframeSendThread(ScrollMsg *scrollMsg, smtpSendBody *SendBody, short FrameId, char *mac){
    LOGI("mkframeSendThread");
    uint32_t length;
    length = static_cast<uint32_t>(mkframe(scrollMsg->SendBuf, SendBody->header->Seq, FrameId, mac));
    if(length <= 0)
    {
        LOGI("报文发送失败");
        return 0;
    }
    length = static_cast<uint32_t >(send(SendBody->fd, scrollMsg->SendBuf, static_cast<size_t>(length), 0));
    if(length <= 0)
    {
        sqlite3_close(scrollMsg->dbSelect);
        pthread_exit(NULL);
    }
    return 0;
}

//时间转换函数
int cptTimeGet(char *nowtime) {

    time_t t;
    t = time(NULL);
    struct tm *lt;
    long ii = time(&t);
    printf("ii = %ld\n", ii);
    t = time(NULL);
    lt = localtime(&t);
    memset(nowtime, 0, sizeof(nowtime));
    strftime(nowtime, 24, "%Y-%m-%d %H:%M:%S", lt);
    LOGI("nowtime = %s\n", nowtime);
    return 1;
}

//时间转换
int standardToStamp(char *str_time)
{
    struct tm stm;
    int iY, iM, iD, iH, iMin, iS;
    memset(&stm,0,sizeof(stm));
    iY = atoi(str_time);
    iM = atoi(str_time+5);
    iD = atoi(str_time+8);
    iH = atoi(str_time+11);
    iMin = atoi(str_time+14);
    iS = atoi(str_time+17);

    stm.tm_year=iY-1900;
    stm.tm_mon=iM-1;
    stm.tm_mday=iD;
    stm.tm_hour=iH;
    stm.tm_min=iMin;
    stm.tm_sec=iS;

    /*printf("%d-%0d-%0d %0d:%0d:%0d\n", iY, iM, iD, iH, iMin, iS);*///标准时间格式例如：2016:08:02 12:12:30
    return (int)mktime(&stm);
}

//超时信息写入，判断网络环境
int fileWriteStatus(char *result, char *fileName)
{
// 检查wifi和IP是否配置正确
    int fd;
    if((fd = open(fileName, O_RDWR|O_CREAT|O_TRUNC, 0666))<0) {
        return -1;
    }
    write(fd, result, 5);
    close(fd);
    return 0;
}

//日志打印
int fileWriteLogs(char *result, char *fileName)
{
// 检查wifi和IP是否配置正确
    int fd;
    if((fd = open(fileName, O_RDWR|O_CREAT|O_APPEND, 0666))<0) {
        return -1;
    }
    time_t rawtime;
    struct tm * timeinfo;
    char timedata[128] = {0}, buffer[256] = {0};
    time(&rawtime);
    timeinfo = localtime(&rawtime);
    strftime(timedata, sizeof(timedata), "%Y-%m-%d %H:%M:%S", timeinfo);
    sprintf(buffer, "%s: %s", timedata, result);

    LOGI("RESULT:%s", buffer);

    write(fd, buffer, strlen(buffer));
    close(fd);
    return 0;
}

//用于查询临时表中收到的型号绑定的频段，无线参数，抓捕的IMSI号信息
int freqBandSelect(configMsg_t *configMsg, int statu, sqlite3 *db)
{
    fileWriteType(const_cast<char *>(SQLITE_LOCK), 1);
    pthread_mutex_lock(&count_mutex);
    LOGI("freqBandSelect");
    memset(buf, 0, sizeof(buf));
    int Code = 0, i = 0, ret;
    char delim = ',', *p, *ptr, *errmsg;
    int typeCode[8] = {0};
    char sqlCmd[128] = {0}, freqMsg[256] = {0}, modelCode[32] = {0};
    sprintf(sqlCmd, "select modelCode from freqMsg limit 1");
    ret = sqlite3_exec(db, sqlCmd, callback, NULL, &errmsg);
    if(0!=ret) {
        LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
        pthread_mutex_unlock(&count_mutex);
        return -1;
    }
    sprintf(sqlCmd, "select model from modelCode where type='%s';", buf);
    memset(buf, 0, sizeof(buf));
    ret = sqlite3_exec(db, sqlCmd, callback, NULL, &errmsg);
    if(0!=ret)
    {
        LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
        pthread_mutex_unlock(&count_mutex);
        return -1;
    }
    strcpy(configMsg->modelCode, buf);
    strcpy(modelCode, buf);
    memset(buf, 0, sizeof(buf));

    LOGI("modelCode:%s", modelCode);
    if((p = strtok_r(modelCode, &delim, &ptr))) {
        typeCode[Code] = atoi(p);
        LOGI("1modelCode:%s, typeCode[%d]:%d, atoi(p):%d", modelCode, typeCode[Code], Code, atoi(p));
    }
    while ((p = strtok_r(NULL, &delim, &ptr)))
    {
        Code++;
        typeCode[Code] = atoi(p);
        LOGI("2modelCode:%s, typeCode[%d]:%d, atoi(p):%d", modelCode, Code, typeCode[Code], atoi(p));
    }
    LOGI("configMsg->modelCode:%s, statu:%d, Code:%d", configMsg->modelCode, statu, Code);

    if(statu == 1) {
        for (i = 0; i <= Code; i++) {
            if(typeCode[i] == 0)
                continue;
            memset(configMsg->IMSI, 0, sizeof(configMsg->IMSI));
            sprintf(sqlCmd,
                    "select band, ulfreq, dlfreq, pci, modelType, imsi from freqMsg where typeCode = %d",
                    typeCode[i]);
            LOGI("%s", sqlCmd);
            memset(buf, 0, sizeof(buf));
            ret = sqlite3_exec(db, sqlCmd, callback2, NULL, &errmsg);
            if (0 != ret) {
                LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
                pthread_mutex_unlock(&count_mutex);
                return -1;
            }
            LOGI("buf:%s", buf);
            strcpy(freqMsg, buf);
            if((p = strtok_r(freqMsg, &delim, &ptr))) {
                configMsg->band[i].band = htonl(static_cast<uint32_t>(atoi(p)));
            }
            if((p = strtok_r(NULL, &delim, &ptr))) {
                configMsg->band[i].ulfreq = htonl(static_cast<uint32_t>(atoi(p)));
            }
            if((p = strtok_r(NULL, &delim, &ptr))) {
                configMsg->band[i].dlfreq = htonl(static_cast<uint32_t>(atoi(p)));
            }
            if((p = strtok_r(NULL, &delim, &ptr))) {
                configMsg->band[i].pci = htonl(static_cast<uint32_t>(atoi(p)));
            }
            if((p = strtok_r(NULL, &delim, &ptr))) {
                configMsg->Type = atoi(p);
                LOGI("configMsg->Type: %d", configMsg->Type);
            }
            if((p = strtok_r(NULL, &delim, &ptr))) {
                strcpy(configMsg->IMSI, p);
            }
            else{
                strcpy(configMsg->IMSI, "100000000000001");
            }
        }
    }
    pthread_mutex_unlock(&count_mutex);
    fileWriteType(const_cast<char *>(SQLITE_LOCK), 0);
    return 0;
}

//更新临时表中每个频段的的无线参数信息
int freqBandInsert(QueryRspInfo_t *cfgInfo, sqlite3 *db, int CodeType, flagSet flagSet_t)
{
    fileWriteType(const_cast<char *>(SQLITE_LOCK), 1);
    pthread_mutex_lock(&count_mutex);
    LOGI("freqBandInsert");
    int ret = 0;
    char sqlCmd[256] = {0};
    char *errmsg;
    switch (CodeType) {
        case 8:
            sprintf(sqlCmd, "update freqMsg set ulfreq = %d, dlfreq = %d, pci = %d, band = %d, cellStatu = %d where typeCode = %d" \
, htonl(cfgInfo->bandInfo.ulfreq), htonl(cfgInfo->bandInfo.dlfreq), htonl(cfgInfo->bandInfo.pci), htonl(cfgInfo->bandInfo.band),  flagSet_t.cellStaus[0][1], CodeType);
            break;
        case 12:
            sprintf(sqlCmd, "update freqMsg set ulfreq = %d, dlfreq = %d, pci = %d, band = %d, cellStatu = %d where typeCode = %d" \
, htonl(cfgInfo->bandInfo.ulfreq), htonl(cfgInfo->bandInfo.dlfreq), htonl(cfgInfo->bandInfo.pci), htonl(cfgInfo->bandInfo.band), flagSet_t.cellStaus[1][1], CodeType);
            break;
        default:
            break;
    }
    LOGI("sqlCmd:%s", sqlCmd);
    ret = sqlite3_exec(db, sqlCmd, NULL, NULL, &errmsg);
    if(0!=ret) {
        LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
        pthread_mutex_unlock(&count_mutex);
        return -1;
    }
    pthread_mutex_unlock(&count_mutex);
    fileWriteType(const_cast<char *>(SQLITE_LOCK), 0);
    return 0;
}

//根据模块类型表向临时表中存放模块型号、频段类型、频段编号和绑定信息
int freqBandMemory(char *type, sqlite3 *db)
{
    fileWriteType(const_cast<char *>(SQLITE_LOCK), 1);
    pthread_mutex_lock(&count_mutex);
    memset(buf, 0, sizeof(buf));
    int ret = 0;
    int typeCode[8] = {0};
    int Code = 0;
    char delim = ',';
    char *p;
    char *ptr;
    char modelType[32] = {0};
    char sqlCmd[128] = {0};
    char result[128] = {0};
    sprintf(sqlCmd, "select model from modelCode where type='%s';", type);
    char *errmsg;
    ret = sqlite3_exec(db, sqlCmd, callback, NULL, &errmsg);
    if(0 != ret)
    {
        LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
        pthread_mutex_unlock(&count_mutex);
        return -1;
    }
    LOGI("freqBandMemory:%s", buf);
    strcpy(result, buf);

    sprintf(sqlCmd, "delete from freqMsg;");
    ret = sqlite3_exec(db, sqlCmd, NULL, NULL, &errmsg);
    if(0 != ret)
    {
        LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
        pthread_mutex_unlock(&count_mutex);
        return -1;
    }

    if((p = strtok_r(result, &delim, &ptr)))
        typeCode[Code] = atoi(p);
    while ((p = strtok_r(NULL, &delim, &ptr)))
    {
        Code++;
        typeCode[Code] = atoi(p);
    }

    LOGI("Code:%d", Code);
    for(int i = 0; i<=Code; i++)
    {
        sprintf(sqlCmd, "select Msg from bandTable where typeCode = %d;", typeCode[i]);
        memset(buf, 0, sizeof(buf));
        ret = sqlite3_exec(db, sqlCmd, callback, NULL, &errmsg);
        if(0 != ret)
        {
            LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
            pthread_mutex_unlock(&count_mutex);
            return -1;
        }
        strcpy(result, buf);
        LOGI("result:%s, %s", buf, result);

        if((p = strtok_r(result, &delim, &ptr)))
            strcpy(modelType, p);
//        if((p = strtok_r(NULL, &delim, &ptr)))
//            band = atoi(p);
        sprintf(sqlCmd, "insert into freqMsg('modelCode', 'typeCode', 'modelType', 'cellStatu') values('%s', '%d', '%s', '%d');",type, typeCode[i], modelType, 0);
        LOGI("sqlCmd:%s", sqlCmd);
        ret = sqlite3_exec(db, sqlCmd, NULL, NULL, &errmsg);
        if(0 != ret)
        {
            LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
            pthread_mutex_unlock(&count_mutex);
            return -1;
        }
    }
    pthread_mutex_unlock(&count_mutex);
    fileWriteType(const_cast<char *>(SQLITE_LOCK), 0);
    return 0;
}

//归属地查询
char *sqliteQuery(char *phoneNum, sqlite3 *db)
{
    memset(buf, 0, sizeof(buf));
    int ret = 0;
    char sqlCmd[128] = {0};
    sprintf(sqlCmd, "select provincialLevel, municipalLevel from %s where phoneNumber='%s';", Attribution, phoneNum);
    char *errmsg;
    ret = sqlite3_exec(db, sqlCmd, callback, NULL, &errmsg);
    if(0 != ret)
    {
        LOGI("%s, errmsg = %s", sqlite3_errmsg(db), errmsg);
        pthread_mutex_unlock(&count_mutex);
        return const_cast<char *>("NULL");
    }
    return buf;
}

//回调函数,用于拼接归属地信息
int callback(void *ptr, int column_num, char **column_val, char **column_name)
{
    int i;
    for(i = 0; i < column_num; i++)
    {
//        LOGD("%s\t", column_name[i]);
    }
    for(i = 0; i < column_num; i++ )
    {
//        LOGD("%s\t", column_val[i]);
        strcat(buf, column_val[i]);
    }
//    LOGI("%s", buf);
    return 0;
}

//回调函数，用于获取无线参数和IMSI号用逗号隔开拼接后的信息
int callback2(void *ptr, int column_num, char **column_val, char **column_name)
{
    int i;
    for(i = 0; i < column_num; i++)
    {
//        LOGD("%s\t", column_name[i]);
    }
    for(i = 0; i < column_num; i++ )
    {
//        LOGD("%s\t", column_val[i]);
//        strcat(buf, column_val[i]);
        sprintf(buf, "%s,%s", buf, column_val[i]);
    }
    return 0;
}

//获取当前的时间戳，用于判断实时流水信息插入的时机
long getCurrentTime()
{
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

void *bulkDumpSql_pthread(void *arg){
    msgList *SendBody = (msgList *)arg;
    bulkDumpSql(SendBody->scrollMsg, SendBody->flagSet_t, SendBody->BandTdd, SendBody->BandFdd);
    pthread_exit(NULL);
}

int bulkDumpSql(ScrollMsg scrollMsg, flagSet flagSet_t, BandInfo_t BandTdd, BandInfo_t BandFdd){
    fileWriteType(const_cast<char *>(SQLITE_LOCK), 1);
    pthread_mutex_lock(&count_mutex);
    int id = 0, ret = 0;
    float rssi = 0;
    char RSSI[16] = {0};
    char IMSI_OLD[16] = {0};
    sqlite3 *dbQcell;
    sqlite3 *dbCreate;
    ret = sqlite3_open(SQLITE_DB, &dbQcell);
    if(ret != 0)
    {
        LOGI("Error Open Sqlite3 dbQcell");
        return 0;
    }
    ret = sqlite3_open(SQLITE_DB_CREATE, &dbCreate);
    if(ret != 0)
    {
        LOGI("Error Open Sqlite3 dbCreate");
        return 0;
    }
    LOGI("----------------INSERT ID:%d-------------", flagSet_t.ID);
    LOGI("BandTdd.ulfreq:%d, BandTdd.dlfreq:%d, BandTdd.pci:%d, BandTdd.band:%d", htonl(BandTdd.ulfreq), htonl(BandTdd.dlfreq), htonl(BandTdd.pci), htonl(BandTdd.band));
    LOGI("BandFdd.ulfreq:%d, BandFdd.dlfreq:%d, BandFdd.pci:%d, BandFdd.band:%d", htonl(BandFdd.ulfreq), htonl(BandFdd.dlfreq), htonl(BandFdd.pci), htonl(BandFdd.band));
    for(int i = 0; i<flagSet_t.ID; i++)
    {
        strcpy(scrollMsg.Attriution[i], sqliteQuery(scrollMsg.number[i], dbQcell));
    }

    sqlite3_stmt *stmt;
    char sqlCmd[256] = {0};
    sprintf(sqlCmd, "insert into %s (imsi, Attribution, cptTime, rssi, ulfreqTdd, dlfreqTdd, bandTdd, pciTdd, ulfreqFdd, dlfreqFdd, bandFdd, pciFdd, modelType) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", ImsiMsg);
    sqlite3_prepare_v2(dbCreate, sqlCmd, static_cast<int>(strlen(sqlCmd)), &stmt, 0);
    LOGI("当前抓捕的IMSI号是:%s", flagSet_t.IMSI);
    for(int i=0;i<flagSet_t.ID;++i)
    {

        LOGI("&&&&&%s", scrollMsg.Imsi[i]);
        if(strncmp(flagSet_t.IMSI, scrollMsg.Imsi[i], 15) == 0) {
            LOGI("抓捕IMSI数据:%s, flagSet_t.IMSI:%s, scrollMsg.Imsi[i]", scrollMsg.Rssi[i], flagSet_t.IMSI, scrollMsg.Imsi[i]);
            rssi += atof(scrollMsg.Rssi[i]);
            id++;
            continue;
        }
        if(strncmp(scrollMsg.Imsi[i], "460000000000001", 15) == 0 || strncmp(scrollMsg.Imsi[i], "000000000000000", 15) == 0) {
            continue;
        }
        if(strncmp(IMSI_OLD, scrollMsg.Imsi[i], 15) == 0) {
            continue;
        }
        strcpy(IMSI_OLD, scrollMsg.Imsi[i]);
        if(strlen(scrollMsg.Imsi[i]) != 15)
            continue;
        sqlite3_reset(stmt);
        sqlite3_bind_text(stmt, 1, scrollMsg.Imsi[i],
                          static_cast<int>(strlen(scrollMsg.Imsi[i])), NULL);
        sqlite3_bind_text(stmt, 2, scrollMsg.Attriution[i],
                          static_cast<int>(strlen(scrollMsg.Attriution[i])), NULL);
        sqlite3_bind_text(stmt, 3, scrollMsg.CptTime[i],
                          static_cast<int>(strlen(scrollMsg.CptTime[i])), NULL);
        sqlite3_bind_text(stmt, 4, scrollMsg.Rssi[i],
                          static_cast<int>(strlen(scrollMsg.Rssi[i])), NULL);
        sqlite3_bind_int(stmt, 5, htonl(BandTdd.ulfreq));
        sqlite3_bind_int(stmt, 6, htonl(BandTdd.dlfreq));
        sqlite3_bind_int(stmt, 7, htonl(BandTdd.band));
        sqlite3_bind_int(stmt, 8, htonl(BandTdd.pci));
        sqlite3_bind_int(stmt, 9, htonl(BandFdd.ulfreq));
        sqlite3_bind_int(stmt, 10, htonl(BandFdd.dlfreq));
        sqlite3_bind_int(stmt, 11, htonl(BandFdd.band));
        sqlite3_bind_int(stmt, 12, htonl(BandFdd.pci));
        sqlite3_bind_int(stmt, 13, scrollMsg.Type[i]);
        LOGI("scrollMsg.Type[flagSet_t.ID]: %d", scrollMsg.Type[i]);
        sqlite3_step(stmt);
    }
    sqlite3_finalize(stmt);

    //同一时间内有两条以上记录表示进入了抓捕模式
    if(id >= 1){
        rssi = rssi / id;
        sprintf(RSSI, "%f", rssi);
        fileWriteStatus(RSSI, const_cast<char *>(IMSI_RSSI));
        LOGI("一生一世RSSI:%s", RSSI);
    } else{
//        fileWriteStatus(const_cast<char *>("0.0000"), const_cast<char *>(IMSI_RSSI));
    }

    sqlite3_close(dbQcell);
    sqlite3_close(dbCreate);
    pthread_mutex_unlock(&count_mutex);
    fileWriteType(const_cast<char *>(SQLITE_LOCK), 0);
    return 0;
}
























