#include "DS18b20.h"

unsigned int tvalue;//温度值
unsigned char tflag;//温度正负标志

void delay(unsigned int i)
{
 while(i--);
}

void Init_DS18B20(void)
{
	unsigned char x=0;
	DQ = 1;    
	delay(8);  
	DQ = 0;    
	delay(80); 
	DQ = 1;    
	delay(14);
	x=DQ;      
	delay(20);
}

unsigned char ReadOneChar(void)
{
	unsigned char i=0;
	unsigned char dat = 0;
	for (i=8;i>0;i--)
	{
		DQ = 0; 
		dat>>=1;
		DQ = 1; 
		if(DQ)
		 dat|=0x80;
		delay(4);
	}
	return(dat);
}

void WriteOneChar(unsigned char dat)
{
 unsigned char i=0;
 for (i=8; i>0; i--)
 {
  DQ = 0;
  DQ = dat&0x01;
  delay(5);
  DQ = 1;
  dat>>=1;
 }
}

int ReadTemperature(void)
{
	unsigned char a=0;
	unsigned char b=0;

	Init_DS18B20();
	WriteOneChar(0xCC); 
	WriteOneChar(0x44); 
	Init_DS18B20();
	WriteOneChar(0xCC);
	WriteOneChar(0xBE);
	a=ReadOneChar();
	b=ReadOneChar();
	tvalue = b;
	tvalue <<= 8;
	tvalue = tvalue|a;
	if(tvalue<0x0fff)
		tflag=0;
	else
  {
		tvalue=~tvalue+1;
		tflag=1;
  }
	tvalue = tvalue*(0.625);//温度值扩大10倍，精确到1位小数
	return(tvalue);
}