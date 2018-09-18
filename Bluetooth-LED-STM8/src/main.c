/*************
** Author   ： 江涛
** Date     :  2018/08/28
** Describe ： 蓝牙控制单片机开关灯
**************/
/* Includes -----------*/
#include "stm8s.h"
#include <string.h>

/********************************************
**  TestLed        PB5  测试LED
**********************************************/

/**串口1接收数据标志位**/
unsigned char Usart1_bufStart_or_bufSotp = 0 ;
/**串口1数据，这里10个字节够放命令了，大家可以根据实际需要调整数组长度**/
char Usart1BufData[10];
/**串口数据数目自增量，用来统计数目是否达到数组最大长度**/
char Usart1BufConst;

/****以下是定义的命令*****/
char LED_ON[10] = "ON\n";  
char LED_OFF[10] = "OFF\n";

void Delay(uint16_t nCount)
{
  /* Decrement nCount value */
  while (nCount != 0)
  {   
    nCount--;
  }
}

/*************
* 初始化串口UART1
**************/
void initUart1()
{
  // 串口设置
  UART1_DeInit();
  // 9600波特率，8位数据位，一位停止位，
  UART1_Init((u32)9600, UART1_WORDLENGTH_8D, UART1_STOPBITS_1, UART1_PARITY_NO, UART1_SYNCMODE_CLOCK_DISABLE, UART1_MODE_TXRX_ENABLE);//UART1_MODE_TX_ENABLE);
  // 使能串口接收中断
  UART1_ITConfig(UART1_IT_RXNE_OR, ENABLE);
}

/*********
* 端口配置
**************/
void initGPIO(){
  // LED灯的GPIO口初始化
  GPIO_Init(GPIOB, GPIO_PIN_5, GPIO_MODE_OUT_PP_LOW_FAST);
  GPIO_WriteHigh(GPIOB , GPIO_PIN_5); // 关测试灯
  
}

/*****************
*  程序入口
*******************/
void main(void)
{

  // 初始化系统时钟，
  CLK_HSICmd(ENABLE);
  CLK_SYSCLKConfig(CLK_PRESCALER_HSIDIV1);
  //内部时钟16M，8分频
  CLK_HSIPrescalerConfig(CLK_PRESCALER_HSIDIV8);    
  
  initGPIO();
   
  initUart1();
  
  enableInterrupts(); // 使能中断
       
  while (1)
  {    
    if(Usart1_bufStart_or_bufSotp)
    {
       Usart1_bufStart_or_bufSotp = 0 ; 
              
       if(strcmp(Usart1BufData,LED_ON)==0)
       {
          GPIO_WriteLow(GPIOB , GPIO_PIN_5);         
       }
       else if(strcmp(Usart1BufData,LED_OFF)==0)
       {
          GPIO_WriteHigh(GPIOB , GPIO_PIN_5);
       }
              
       Usart1BufConst = 0 ;
       memset(Usart1BufData,0,10);//清0数组   
    }    
  }
}


#ifdef USE_FULL_ASSERT

/**
  * @brief  Reports the name of the source file and the source line number
  *   where the assert_param error has occurred.
  * @param file: pointer to the source file name
  * @param line: assert_param error line source number
  * @retval : None
  */
void assert_failed(u8* file, u32 line)
{ 
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

  /* Infinite loop */
  while (1)
  {
  }
}
#endif