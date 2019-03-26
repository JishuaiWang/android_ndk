/**************************************************************************************************************
* Copyright (C), 2010, ZDC Corporation.
* All right reserved.
*
* Filename: 	imsi2phone.c
* Description:	根据IMSI计算手机号码头七位，以便查数据库获取IMSI归属地
				算法简介：根据imsi2phone_table数组找出对应IMSI的号码头三位，再根据IMSI以及算法规则整合号码的后四位
				算法来源：lteserver.e 易语言实现
						  http://blog.csdn.net/wiker_yong/article/details/51919232
				封装说明：算法主要采用正则表达式匹配执行，源码主要由java以及易语言等高级语言实现，本文采用C语言
						  实现逻辑，但目前C语言的正则不支持字表达式位置索引，但根据该算法的正则子表达式位置基本
						  固定的特性，故将子表达式匹配后字符串起始角标以及匹配后字符串长度人为的标注在 imsi2phone
						  结构中便于使用。
				注    意：该算法目前只支持中国移动、中国联通归属地查询，中国电信暂不支持
* Author:		杨传坤
* History:		2017/11/16
**************************************************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <regex.h>

#define MAX_SUBNUM 	3		//默认每个正则表达式中最多3个字表达式

//子表达式坐标结构
struct subcoord	
{
	unsigned int from;	//子表达式匹配后字符串起始角标
	unsigned int len;		//子表达式实际匹配字符串长度	
};

//号段和IMSI正则表达式结构
struct imsi2phone
{
	const char *regular;	//正则段
	const char *numhead;	//号段编码
	unsigned int subnum;	//字表达式个数，即正则字符串中"()"的个数
	struct subcoord subindex[MAX_SUBNUM];	//字表达式角标数组
};



//手机正则数组
const struct imsi2phone imsi2phone_table[] = {

	[0] = {"^46001([0-9]{3})([0-9])[0,1][0-9]+",  		"130" , 2, {{5,3},{8,1}} },
	[1] = {"^46001([0-9]{3})([0-9])9[0-9]+", 			"131" , 2, {{5,3},{8,1}} },
	[2] = { "^46001([0-9]{3})([0-9])2[0-9]+", 			"132" ,	2, {{5,3},{8,1}} },
	[3] = { "^460020([0-9])([0-9]{3})[0-9]+", 			"134" , 2, {{6,1},{7,3}} },
	[4] = { "^46000([0-9]{3})([5,6,7,8,9])[0-9]+", 		"13x0", 2, {{5,3},{8,1}} },
	[5] = { "^46000([0-9]{3})([0,1,2,3,4])([0-9])[0-9]+","13x", 3, {{5,3},{8,1},{9,1}} },
	[6] = { "^460023([0-9])([0-9]{3})[0-9]+", 			"150" , 2, {{6,1},{7,3}} },
	[7] = { "^460021([0-9])([0-9]{3})[0-9]+", 			"151" , 2, {{6,1},{7,3}} },
	[8] = { "^460022([0-9])([0-9]{3})[0-9]+", 			"152" , 2, {{6,1},{7,3}} },
	[9] = { "^46001([0-9]{3})([0-9])4[0-9]+", 			"155" , 2, {{5,3},{8,1}} },
	[10] = { "^46001([0-9]{3})([0-9])3[0-9]+", 			"156" , 2, {{5,3},{8,1}} },
	[11] = { "^460077([0-9])([0-9]{3})[0-9]+", 			"157" , 2, {{6,1},{7,3}} },
	[12] = { "^460028([0-9])([0-9]{3})[0-9]+", 			"158" , 2, {{6,1},{7,3}} },
	[13] = { "^460029([0-9])([0-9]{3})[0-9]+", 			"159" , 2, {{6,1},{7,3}} },
	[14] = { "^460079([0-9])([0-9]{3})[0-9]+", 			"147" , 2, {{6,1},{7,3}} },
	[15] = { "^46001([0-9]{3})([0-9])5[0-9]+", 			"185" , 2, {{5,3},{8,1}} },
	[16] = { "^46001([0-9]{3})([0-9])6[0-9]+", 			"186" , 2, {{5,3},{8,1}} },
	[17] = { "^460027([0-9])([0-9]{3})[0-9]+", 			"187" , 2, {{6,1},{7,3}} },
	[18] = { "^460078([0-9])([0-9]{3})[0-9]+", 			"188" , 2, {{6,1},{7,3}} },
	[19] = { "^460070([0-9])([0-9]{3})[0-9]+", 			"1705", 2, {{6,1},{7,3}} },
	[20] = { "^46001([0-9]{3})([0-9])8[0-9]+", 			"170x", 2, {{5,3},{8,1}} },		
	[21] = { "^460075([0-9])([0-9]{3})[0-9]+", 			"178" , 2, {{6,1},{7,3}} },
	[22] = { "^46001([0-9]{3})([0-9])7[0-9]+",  		"145" , 2, {{5,3},{8,1}} },
	[23] = { "^460026([0-9])([0-9]{3})[0-9]+", 			"182" , 2, {{6,1},{7,3}} },
	[24] = { "^460025([0-9])([0-9]{3})[0-9]+", 			"183" , 2, {{6,1},{7,3}} },
	[25] = { "^460024([0-9])([0-9]{3})[0-9]+", 			"184" , 2, {{6,1},{7,3}} },
	[26] = { "^46003([0-9])([0-9]{3})7[0-9]+", 			"180" , 2, {{5,3},{8,1}} },
	[27] = { "^46003([0-9])([0-9]{3})8[0-9]+", 			"153" , 2, {{5,3},{8,1}} },
	[28] = { "^46003([0-9])([0-9]{3})9[0-9]+", 			"189" , 2, {{5,3},{8,1}} }

};


/**************************************************************
* Function:     get_phone_by_imsi
* Description:  根据IMSI号获取手机号码前7位
* Parameter:    imsi:查询的IMSI字符串号码(建议大写),
				number:用于存储手机号码前7位字符串的数组
				size:数组长度，建议大于8字节
* Return:       成功:返回号码字符串长度	
				失败:返回-1
* Auther:       杨传坤, 2017-11-16, 
						2017-11-30(正则表达式匹配成功后释放)
*************************************************************/
int get_phone_by_imsi(const char *imsi, char *number, size_t size)
{
	size_t i;
	int ret;
	regex_t reg;			//正则表达式结构
    size_t nmatch = 1;		//正则表达式预计匹配数量
	regmatch_t pmatch[1];	//存放正则表达式匹配结构角标
	
	//判断接收数组长度是否小于最小数据长度
	if (size < 8)
	{
		printf("缓存数据数组太小！\n");
		return -1;
	}
	
	
	//遍历匹配所有的正则表达式
	for (i=0 ;i<sizeof(imsi2phone_table)/sizeof(imsi2phone_table[0]); i++)
	{	
		//编译正则表达式(使用扩展正则和忽略大小写)
		ret = regcomp(&reg, imsi2phone_table[i].regular, REG_EXTENDED|REG_ICASE);
		if(ret != 0)
        {
			printf("正则编译失败！\n");
			//释放正则
			regfree(&reg);		//修复：表达式匹配失败也要释放
            return -1;
        }
		
		//匹配正则表达式
		ret = regexec(&reg, imsi, nmatch, pmatch, 0);
		if (0 == ret)		
		{
			//释放正则
			regfree(&reg);		//修复：表达式匹配到了需要释放
			break;
		}
		
		//释放正则
		regfree(&reg);				
	}

	
	//如果遍历搜索完，则返回空
	if (i >= sizeof(imsi2phone_table)/sizeof(imsi2phone_table[0]))
	{
		return 0;
	}

	//清除字符串
	bzero(number, size);
	
	//字表达式数为2的
	if (2 == imsi2phone_table[i].subnum)
	{
		//根据号码头返回对应组合的号码段
		if (!strncmp(imsi2phone_table[i].numhead, "130", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "131", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "132", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "155", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "156", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "185", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "186", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "145", 3)
			)
		{
			
			//返回 (手机正则 [i].号段编码 ＋ 正则表达式.取子匹配文本 (1, 2) ＋ 正则表达式.取子匹配文本 (1, 1))			
			
			strncpy(number, imsi2phone_table[i].numhead, 3);	
			strncat(number, imsi+imsi2phone_table[i].subindex[1].from, imsi2phone_table[i].subindex[1].len);
			strncat(number, imsi+imsi2phone_table[i].subindex[0].from, imsi2phone_table[i].subindex[0].len);		
			return strlen(number);
		}
		
		if (!strncmp(imsi2phone_table[i].numhead, "134", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "150", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "151", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "152", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "157", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "158", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "159", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "147", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "187", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "188", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "178", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "182", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "183", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "184", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "180", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "153", 3) ||
			!strncmp(imsi2phone_table[i].numhead, "189", 3) 
			)
		{
			//返回 (手机正则 [i].号段编码 ＋ 正则表达式.取子匹配文本 (1, 1) ＋ 正则表达式.取子匹配文本 (1, 2))
			
			strncpy(number, imsi2phone_table[i].numhead, 3);		
			strncat(number, imsi+imsi2phone_table[i].subindex[0].from, imsi2phone_table[i].subindex[0].len);
			strncat(number, imsi+imsi2phone_table[i].subindex[1].from, imsi2phone_table[i].subindex[1].len);		
			return strlen(number);
		}
		
		if (!strncmp(imsi2phone_table[i].numhead, "13x0", 4))
		{
			//返回 ("13" ＋ 正则表达式.取子匹配文本 (1, 2) ＋ "0" ＋ 正则表达式.取子匹配文本 (1, 1))
			
			strncpy(number, "13", 2);		
			strncat(number, imsi+imsi2phone_table[i].subindex[1].from, imsi2phone_table[i].subindex[1].len);
			strncat(number, "0", 1);
			strncat(number, imsi+imsi2phone_table[i].subindex[0].from, imsi2phone_table[i].subindex[0].len);	
			return strlen(number);
		}

		if (!strncmp(imsi2phone_table[i].numhead, "1705", 4))
		{
			//返回 ("170" ＋ 正则表达式.取子匹配文本 (1, 1) ＋ 正则表达式.取子匹配文本 (1, 2))
			
			strncpy(number, "170", 3);		
			strncat(number, imsi+imsi2phone_table[i].subindex[0].from, imsi2phone_table[i].subindex[0].len);
			strncat(number, imsi+imsi2phone_table[i].subindex[1].from, imsi2phone_table[i].subindex[1].len);		
			return strlen(number);
		}


		if (!strncmp(imsi2phone_table[i].numhead, "170x", 4))
		{
			//返回 ("170" ＋ 正则表达式.取子匹配文本 (1, 2) ＋ 正则表达式.取子匹配文本 (1, 1))
			
			strncpy(number, "170", 3);		
			strncat(number, imsi+imsi2phone_table[i].subindex[1].from, imsi2phone_table[i].subindex[1].len);
			strncat(number, imsi+imsi2phone_table[i].subindex[0].from, imsi2phone_table[i].subindex[0].len);		
			return strlen(number);
		}
		
	}
	
	//字表达式数为3的
	if (3 == imsi2phone_table[i].subnum)
	{	
		if (!strncmp(imsi2phone_table[i].numhead, "13x", 3))
		{
			//返回 (“13” ＋ (正则表达式.取子匹配文本 (1, 2)) ＋ 5) ＋ 正则表达式.取子匹配文本 (1, 3) ＋ 正则表达式.取子匹配文本 (1, 1))
			
			strncpy(number, "13", 2);	
			strncat(number, imsi+imsi2phone_table[i].subindex[1].from, imsi2phone_table[i].subindex[1].len);
			number[2] += 5;
			strncat(number, imsi+imsi2phone_table[i].subindex[2].from, imsi2phone_table[i].subindex[2].len);	
			strncat(number, imsi+imsi2phone_table[i].subindex[0].from, imsi2phone_table[i].subindex[0].len);
	
			return strlen(number);
		}
	}
	
	return 0;
}




