/*************
** Author   �� ����
** Date     :  2018/08/28
** Describe �� �������Ƶ�Ƭ�����ص�
**************/
/* Includes -----------*/
#include "stm8s.h"
#include <string.h>

/********************************************
**  TestLed        PB5  ����LED
**********************************************/

/**����1�������ݱ�־λ**/
unsigned char Usart1_bufStart_or_bufSotp = 0 ;
/**����1���ݣ�����10���ֽڹ��������ˣ���ҿ��Ը���ʵ����Ҫ�������鳤��**/
char Usart1BufData[10];
/**����������Ŀ������������ͳ����Ŀ�Ƿ�ﵽ������󳤶�**/
char Usart1BufConst;

/****�����Ƕ��������*****/
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
* ��ʼ������UART1
**************/
void initUart1()
{
  // ��������
  UART1_DeInit();
  // 9600�����ʣ�8λ����λ��һλֹͣλ��
  UART1_Init((u32)9600, UART1_WORDLENGTH_8D, UART1_STOPBITS_1, UART1_PARITY_NO, UART1_SYNCMODE_CLOCK_DISABLE, UART1_MODE_TXRX_ENABLE);//UART1_MODE_TX_ENABLE);
  // ʹ�ܴ��ڽ����ж�
  UART1_ITConfig(UART1_IT_RXNE_OR, ENABLE);
}

/*********
* �˿�����
**************/
void initGPIO(){
  // LED�Ƶ�GPIO�ڳ�ʼ��
  GPIO_Init(GPIOB, GPIO_PIN_5, GPIO_MODE_OUT_PP_LOW_FAST);
  GPIO_WriteHigh(GPIOB , GPIO_PIN_5); // �ز��Ե�
  
}

/*****************
*  �������
*******************/
void main(void)
{

  // ��ʼ��ϵͳʱ�ӣ�
  CLK_HSICmd(ENABLE);
  CLK_SYSCLKConfig(CLK_PRESCALER_HSIDIV1);
  //�ڲ�ʱ��16M��8��Ƶ
  CLK_HSIPrescalerConfig(CLK_PRESCALER_HSIDIV8);    
  
  initGPIO();
   
  initUart1();
  
  enableInterrupts(); // ʹ���ж�
       
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
       memset(Usart1BufData,0,10);//��0����   
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