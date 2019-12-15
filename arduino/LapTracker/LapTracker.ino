#include <SPI.h>
#include <avr/pgmspace.h>
#include <Wire.h>
#include "LSM6.h"
#include "ADNS9800.h"


LSM6 imuL, imuR;
ADNS9800 mouseL(10), mouseR(9); 

void setup() {
  //Serial.begin(9600);
  Serial.begin(115200);
  Wire.begin();
  Wire.setClock(400000L);
  SPI.begin();
  SPI.setDataMode(SPI_MODE3);
  SPI.setBitOrder(MSBFIRST);
  SPI.setClockDivider(8);
  

  mouseL.performStartup();
//  mouseL.dispRegisters();
  mouseR.performStartup();
//  mouseR.dispRegisters();

  delay(100);

  // turn on IMUs
  imuL.init(imuL.deviceType::device_DS33, imuL.sa0State::sa0_high);
  imuL.enableDefault();
  imuR.init(imuR.deviceType::device_DS33, imuR.sa0State::sa0_low);
  imuR.enableDefault();
  delay(5000);


}



unsigned long currTime;
unsigned long pollTimer;
int pollInterval = 5000; // 5 millisecond polling interval
int runs = 0;
int numRuns = 2000;

char report[100];
char settings[100];

bool charRead = false;
byte imuLOK, imuROK;

  

void loop() {
  char msg;


  if (Serial.available() > 0)  // check if any data in serial buffers
  {
    msg = Serial.read();  // read msg

      if (msg == 'd') // check if computer has received 'r' and replyed with 'd'
      {
        Serial.print("r");
        Serial.flush();
        charRead = true;
        snprintf(settings, sizeof(settings), "%d,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x",
        pollInterval/1000, getRightMouseID(), getLeftMouseID(),
        getRightImuID(), getLeftImuID(), getRightMouseRes(), getLeftMouseRes(),
        getRightAccCtrl(), getLeftAccCtrl(), getRightGyroCtrl(),
        getLeftGyroCtrl(), getimuLStatus(), getimuRStatus() 
       );
       Serial.println(settings);
       Serial.flush();
       currTime = micros();
       pollTimer = currTime + pollInterval;
      }
  }
  while(charRead){
    currTime = micros();
    if(currTime - pollTimer > pollInterval){
      mouseL.UpdatePointer();
      mouseR.UpdatePointer();

         imuLOK = imuL.readReg(0x1E);
         imuL.read();
         delay(1);
         imuROK = imuR.readReg(0x1E);
         imuR.read();

      snprintf(report, sizeof(report), "%x,%x,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
        imuLOK,imuROK,
        imuL.a.x, imuL.a.y, imuL.a.z,
        imuL.g.x, imuL.g.y, imuL.g.z,
        mouseL.getx(), mouseL.gety(),
        imuR.a.x, imuR.a.y, imuR.a.z,
        imuR.g.x, imuR.g.y, imuR.g.z,
        mouseR.getx(), mouseR.gety(), currTime
       );
      Serial.println(report);
      Serial.flush();
      pollTimer = currTime + pollInterval;
      }

      // Check if computer wants to stop reading data
      if (Serial.available() > 0){
        msg = Serial.read();
        if (msg == 's')
        {
         charRead = false;
         Serial.print("q");
         Serial.flush();
        }
      }
    }
}


void display_Mouse_Settings(){
  int Res, frame_Rate_Upper, frame_Rate_Lower;
  byte config_1, frame_Per_Max_Up, frame_Per_Max_low, frame_Per_Min_Up, frame_Per_Min_low;
  config_1 = mouseR.adns_read_reg(REG_Configuration_I);
  frame_Per_Max_Up = mouseR.adns_read_reg(REG_Frame_Period_Max_Bound_Upper);
  frame_Per_Max_low = mouseR.adns_read_reg(REG_Frame_Period_Max_Bound_Lower);
  frame_Per_Min_Up = mouseR.adns_read_reg(REG_Frame_Period_Min_Bound_Upper);
  frame_Per_Min_low = mouseR.adns_read_reg(REG_Frame_Period_Min_Bound_Lower);
  Res = config_1*200;
  frame_Rate_Lower = 50000000/(frame_Per_Max_Up <<8 | (frame_Per_Max_low));
  frame_Rate_Upper = 50000000/(frame_Per_Min_Up <<8 | (frame_Per_Min_low));
  Serial.print("Resolution: ");
  Serial.println(Res);
  Serial.print("Frame Rate Upper Bound: ");
  Serial.println(frame_Rate_Upper);
  Serial.print("Frame Rate Lower Bound: ");
  Serial.println(frame_Rate_Lower);
}

byte getRightMouseID(){
  byte ID;
  ID = mouseR.adns_read_reg(REG_Product_ID);

  return(ID);
}

byte getLeftMouseID(){
  byte ID;
  ID = mouseL.adns_read_reg(REG_Product_ID);

  return(ID);
}

byte getRightMouseRes(){
  int Res;
  byte config_1;
  config_1 = mouseR.adns_read_reg(REG_Configuration_I);
  Res = config_1*200;

  return(config_1);
}

byte getRightImuID(){
  byte ID;
  ID = imuR.readReg(0x0F);

  return(ID);
}

byte getLeftImuID(){
  byte ID;
  ID = imuL.readReg(0x0F);

  return(ID);
}

byte getLeftMouseRes(){
  int Res;
  byte config_1;
  config_1 = mouseL.adns_read_reg(REG_Configuration_I);
  Res = config_1*200;

  return(config_1);
}

byte getRightAccCtrl(){
  byte ctrl;
  ctrl = imuR.readReg(0x10);

  return(ctrl);
}

byte getLeftAccCtrl(){
  byte ctrl;
  ctrl = imuL.readReg(0x10);

  return(ctrl);
}

byte getRightGyroCtrl(){
  byte ctrl;
  ctrl = imuR.readReg(0x11);

  return(ctrl);
}

byte getLeftGyroCtrl(){
  byte ctrl;
  ctrl = imuL.readReg(0x11);

  return(ctrl);
}

byte getimuLStatus(){
  byte regValue;
  regValue = imuL.readReg(0x1E);

  return(regValue);
}

byte getimuRStatus(){
  byte regValue;
  regValue = imuR.readReg(0x1E);

  return(regValue);
}


byte getRightMouseCon2(){
  int Res;
  byte config_2;
  config_2 = mouseR.adns_read_reg(REG_Configuration_II);

  return(config_2);
}

byte getLeftMouseCon2(){
  int Res;
  byte config_2;
  config_2 = mouseL.adns_read_reg(REG_Configuration_II);

  return(config_2);
}


//"A,%d,%d,%d,G,%d,%d,%d,M,%d,%d,A,%d,%d,%d,G,%d,%d,%d,M,%d,%d"


