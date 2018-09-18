/****************************************
**       蓝牙串口接收数据
** 
**   作者：江涛
**   时间：2018/08/31
**   描述：串口发送数据兼用OLED显示
****************************************/
#include "STC89C5xRC_RDP.h"
#include "string.h"     // 要使用字符串对比函数，需要引入该头文件
#include "OLED.h"       // OLED显示屏头文件
#include "DS18b20.h"

// 定义系统时钟和串口波特率
#define FOSC 11059200L      // 系统时钟
#define BAUD 9600           // 串口波特率

/******变量声明*********/ 
char RECEIVED_CMD[10] ;       	// 暂定为10字节的指令
char RECEIVED_INDEX ;         	// 数组指示索引，当接收到一个数据之后，索引会跟随增加
unsigned char flag = 0 ;      	// 数据接收的标志位
unsigned char power_flag = 0 ;  // 电源开关的标志位

/******命令常量*******/
code const char* LED_ON = "ON\r\n" ;					// 电源开指令
code const char* LED_OFF = "OFF\r\n" ;				// 电源关指令
code const char* LED_01_ON = "L1ON\r\n" ;			// 灯组01开指令
code const char* LED_01_OFF = "L1OFF\r\n" ;		// 灯组01关指令
code const char* LED_02_ON = "L2ON\r\n" ;			// 灯组02开指令
code const char* LED_02_OFF = "L2OFF\r\n" ;		// 灯组02关指令
code const char* FAN_ON = "FANON\r\n" ;				// 风扇开指令，使用RGB灯循环来模拟风扇工作
code const char* FAN_OFF = "FANOFF\r\n" ;			// 风扇关指令
code const char* FAILD = "power_off\r\n" ;		// 返回失败原因，电源关闭了

extern unsigned int tvalue;			//温度值
extern unsigned char tflag;			//温度正负标志
unsigned char disdata[7]; 			// 温度数据，使用8字节数组来存储

char color_table[8][3] = { // 颜色表
	{0,0,0},{0,0,1},{0,1,0},{0,1,1},{1,0,0},{1,0,1},{1,1,0},{1,1,1}
};

/*******函数声明*********/
void Init_UART(); // 初始化串口
void UART_SendData(char dat); // 串口发送数据
void UART_SendStr(char* str); // 串口发送字符串
void RGB_Display(int index);  // RGB灯显示

//void split(char str[],char delims[]); // 字符串截取函数

void ds1820disp(); 	// 温度显示

void test_Fan(char flag);		// 模拟测试风扇运行
void Delay1ms();						//@11.0592MHz

/***********端口定义*************/
sbit LED_R = P0^0 ;
sbit LED_G = P0^1 ;
sbit LED_B = P0^2 ;

/*******程序入口*********/
void main() 
{

	unsigned int temperature , old ; // 保存温度数值
	
	Init_UART();  // 串口初始化
	
	LCD_Init();  // OLED 初始化
	LCD_CLS();   // 清屏
	
	LCD_P8x16Str(0 , 0 , "TEMP:");  // 温度开始位置
	
	temperature = ReadTemperature(); 
	old = temperature ; 
	ds1820disp(); // 显示温度
	UART_SendStr(disdata); // 向串口发送数据
	LCD_P8x16Str(5*8 , 0 , disdata); // 显示温度
	
	while(1)
	{
		
		temperature=ReadTemperature();  // 读取一次新的温度
    if (temperature != old )	  
	  {	 
			old = temperature;
			ds1820disp(); // 显示温度
			UART_SendStr(disdata); // 向串口发送数据
			LCD_P8x16Str(5*8 , 0 , disdata); // 显示温度

	  }
		
		if(flag) // 接收数据完毕一次，就会进入中断一次
		{
			flag = 0 ; // 将标志位还原，使得串口又可以重新接收数据
					
			if(strcmp(RECEIVED_CMD , LED_ON) == 0)
			{
				P2 = 0xFF ; // P2口全亮
				power_flag = 1 ; // 标志电源打开
			}
			else if(strcmp(RECEIVED_CMD , LED_OFF) == 0)
			{
				P2 = 0x00 ; // P2口全灭
				power_flag = 0 ;// 标志电源关闭
			}
			else if(strcmp(RECEIVED_CMD , LED_01_ON) == 0)
			{
				if(power_flag) 		// 如果电源开关是关闭的，就不执行以下操作
					P2 = 0x55 ; 		// ‭01010101‬ 为1位置的灯是亮着的
				else
					UART_SendStr(FAILD); // 向串口发送失败原因
			}
			else if(strcmp(RECEIVED_CMD , LED_01_OFF) == 0)
			{
				P2 = P2^0x55 ; // P2口01010101相应位置的灯要全灭，所以使用异或操作
			}
			else if(strcmp(RECEIVED_CMD , LED_02_ON) == 0)
			{
				if(power_flag) 		// 如果电源开关是关闭的，就不执行以下操作
					P2 = 0xAA ; 		// ‭10101010‬ 为1位置的灯是亮着的
				else
					UART_SendStr(FAILD); // 向串口发送失败原因
			}
			else if(strcmp(RECEIVED_CMD , LED_02_OFF) == 0)
			{
				P2 = P2^0xAA ; // P2口10101010相应位置的灯要全灭，所以使用异或操作
			}
			else if(strcmp(RECEIVED_CMD , FAN_ON) == 0)
			{
				test_Fan(1);
			}
			else if(strcmp(RECEIVED_CMD , FAN_OFF) == 0)
			{
				test_Fan(0);
			}			

			
			// 用完之后要记得数组清零处理
      RECEIVED_INDEX = 0 ;        // 数组指引复位
      memset(RECEIVED_CMD,0,10);  // 清0数组
		}
	}
}

/******************
** 初始化串口
*******************/
void Init_UART()
{
		SCON = 0x50;                     //设置8位数据位
		TMOD = 0x20;                     //8位自动重载
		TH1 = TL1 = -(FOSC/12/32/BAUD);  //设置重载值
		TR1 = 1;                         //使能时钟
		ES = 1;                          //使能串口中断
		EA = 1;                          //开中断开关
}

/********************
** 串口中断处理
*********************/
void UART_Isr() interrupt 4 using 1
{
	// 串口接收中断处理
	if(RI) 
	{
		RI = 0 ;                              // 清除中断标志位
		RECEIVED_CMD[RECEIVED_INDEX] = SBUF ; // 保存串口接收的数据
		if(RECEIVED_CMD[RECEIVED_INDEX] == 0x0A ){ // 遇到了结束符号
			 flag = 1 ;           // 接收结束，到循环中处理接收的数据
		}else {
			 RECEIVED_INDEX ++ ;   // 继续接收数据
		}
	}

	// 串口发送中断处理
	if(TI)
	{
		TI = 0 ;  // 清发送中断标志位
	}
		
}

/**************************
** 通过串口发送一位数据
***************************/
void UART_SendData(char dat)
{
	ES = 0 ;      // 串口工作的时候禁止中断
	SBUF = dat ;  // 待发送的数据放到SBUF中
	while(!TI) ;  // 等待发送完毕
	TI = 0 ;      // 清TI中断
	ES = 1 ;      // 打开中断
}

/*****************************
**  通过串口发送字符串
******************************/
void UART_SendStr(char *str)
{
		do
		{
			UART_SendData(*str);
		}while(*str ++  != '\0' ); // 一直到字符串结束
}

/****************************
**  显示RGB灯的颜色
*****************************/
void RGB_Display(int index)
{
	LED_R = color_table[index%8][0];
	LED_G = color_table[index%8][1];
	LED_B = color_table[index%8][2];
}

void Delay1ms()		//@11.0592MHz
{
	unsigned char i, j;

	_nop_();
	i = 2;
	j = 199;
	do
	{
		while (--j);
	} while (--i);
}

/****************
** 模拟风扇运行
****************/
void test_Fan(char flag)
{
	unsigned int t , count = 500 ;

	if(!flag) return ; // 如果传入的是0，表示停止，不往下继续进行
	
	for(t=0 ; t<8 ; t++)
	{
		RGB_Display(t);		// 风扇
		for( ; count > 0 ; count --)
			Delay1ms();
	}	
}

///***********************************
//** 字符串截取函数
//***********************************/
//void split(char str[],char delims[])
//{
//	char *result = NULL; 
//	result = strtok( str, delims );  
//	while( result != NULL ) {  
//		 result = strtok( NULL, delims );  
//	}
//} 

/***************************
** 温度值显示
***************************/
void ds1820disp()
{ 	
	  unsigned char flagdat;
	
		if(tflag==0)
//			flagdat=0x20;//正温度不显示符号
		  flagdat=0x2b;//正温度显示符号
		else
		  flagdat=0x2d;//负温度显示负号:-
		
//		disdata[0] = flagdat; //符号位
	
		disdata[1]=tvalue/1000+0x30;//百位数
		disdata[2]=tvalue%1000/100+0x30;//十位数
		disdata[3]=tvalue%100/10+0x30;//个位数
		disdata[4]= 0x2E ;//小数点
		disdata[5]=tvalue%10/1+0x30;//小数位

		if(disdata[1]==0x30) // 如果百位为0
		{
			disdata[0]= 0x20; // 第一位不显示
			disdata[1]= flagdat; // 百位显示符号
			if(disdata[2]==0x30) //如果百位为0，十位为0
			{
				disdata[1]=0x20; // 百位不显示符号
				disdata[2]=flagdat; // 10位显示符号
			}
		}
}