#ifndef _LQOLED_H
#define _LQOLED_H
#include "STC89C5xRC_RDP.h"
#include "intrins.h" 

#define byte  unsigned char
#define word  unsigned int
#define dword unsigned long  
 void LCD_Init(void);
 void LCD_CLS(void);
 void LCD_P6x8Str(byte x,byte y,byte ch[]);
 void LCD_P8x16Str(byte x,byte y,byte ch[]);
 void LCD_P14x16Ch(byte x,byte y,byte N);  	  
 void LCD_Fill(byte dat);
#endif