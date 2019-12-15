#include "ADNS9800.h"
#include <SPI.h>


ADNS9800::ADNS9800(byte CableSelect){
  _CS = CableSelect;
  pinMode(_CS, OUTPUT);
}

ADNS9800::~ADNS9800(){};

void ADNS9800::performStartup(void){
  adns_com_end(); // ensure that the serial port is reset
  adns_com_begin(); // ensure that the serial port is reset
  adns_com_end(); // ensure that the serial port is reset
  adns_write_reg(REG_Power_Up_Reset, 0x5a); // force reset
  delay(50); // wait for it to reboot
  // read registers 0x02 to 0x06 (and discard the data)
  adns_read_reg(REG_Motion);
  adns_read_reg(REG_Delta_X_L);
  adns_read_reg(REG_Delta_X_H);
  adns_read_reg(REG_Delta_Y_L);
  adns_read_reg(REG_Delta_Y_H);
  // upload the firmware
  adns_upload_firmware();
  delay(10);
  //enable laser(bit 0 = 0b), in normal mode (bits 3,2,1 = 000b)
  // reading the actual value of the register is important because the real
  // default value is different from what is said in the datasheet, and if you
  // change the reserved bytes (like by writing 0x00...) it would not work.
  byte laser_ctrl0 = adns_read_reg(REG_LASER_CTRL0);
  adns_write_reg(REG_LASER_CTRL0, laser_ctrl0 & 0xf0 );
  // Set the resolution to 8000 counts per inch (0x28)
  adns_write_reg(REG_Configuration_I,0x28);
  delay(1);
}

void ADNS9800::UpdatePointer(void){
    digitalWrite(_CS,LOW);
  //  x += convTwosComp((int)adns_read_reg(REG_Delta_X_L));
 //   y += convTwosComp((int)adns_read_reg(REG_Delta_Y_L));
    xL += adns_read_reg(REG_Delta_X_L);
    xH += adns_read_reg(REG_Delta_X_H);
    yL += adns_read_reg(REG_Delta_Y_L);
    yH += adns_read_reg(REG_Delta_Y_H);

    x += convTwosComp((int)(xH*256 + xL));
    y += convTwosComp((int)(yH*256 + yL));
    digitalWrite(_CS,HIGH);     
}


void ADNS9800::dispRegisters(void){
  int oreg[7] = {
    0x00,0x3F,0x2A,0x02  };
  char const* oregname[] = {
    "Product_ID","Inverse_Product_ID","SROM_Version","Motion"  };
  byte regres;

  digitalWrite(_CS,LOW);

  int rctr=0;
  for(rctr=0; rctr<4; rctr++){
    SPI.transfer(oreg[rctr]);
    delay(1);
    Serial.println("---");
    Serial.println(oregname[rctr]);
    Serial.println(oreg[rctr],HEX);
    regres = SPI.transfer(0);
    Serial.println(regres,BIN);  
    Serial.println(regres,HEX);  
    delay(1);
  }
  digitalWrite(_CS,HIGH);
}


byte ADNS9800::adns_read_reg(byte reg_addr){
  adns_com_begin();
  
  // send adress of the register, with MSBit = 0 to indicate it's a read
  SPI.transfer(reg_addr & 0x7f );
  delayMicroseconds(100); // tSRAD
  // read data
  byte data = SPI.transfer(0);
  
  delayMicroseconds(1); // tSCLK-NCS for read operation is 120ns
  adns_com_end();
  delayMicroseconds(19); //  tSRW/tSRR (=20us) minus tSCLK-NCS

  return data;
}

void ADNS9800::adns_write_reg(byte reg_addr, byte data){
  adns_com_begin();
  
  //send adress of the register, with MSBit = 1 to indicate it's a write
  SPI.transfer(reg_addr | 0x80 );
  //sent data
  SPI.transfer(data);
  
  delayMicroseconds(20); // tSCLK-NCS for write operation
  adns_com_end();
  delayMicroseconds(100); // tSWW/tSWR (=120us) minus tSCLK-NCS. Could be shortened, but is looks like a safe lower bound 
}

int ADNS9800::getx(){
  int temp = x;
  x = 0;
  xL = 0;
  xH = 0;
  return temp;
}

int ADNS9800::gety(){
  int temp = y;
  y = 0;
  yL = 0;
  yH = 0;
  return temp;
}

// PRIVATE FUNCTIONS

void ADNS9800::adns_com_begin(){
  digitalWrite(_CS, LOW);
}

void ADNS9800::adns_com_end(){
  digitalWrite(_CS, HIGH);
}

void ADNS9800::adns_upload_firmware(){
  // send the firmware to the chip, cf p.18 of the datasheet
  //Serial.println("Uploading firmware...");
  // set the configuration_IV register in 3k firmware mode
  adns_write_reg(REG_Configuration_IV, 0x02); // bit 1 = 1 for 3k mode, other bits are reserved 
  
  // write 0x1d in SROM_enable reg for initializing
  adns_write_reg(REG_SROM_Enable, 0x1d); 
  
  // wait for more than one frame period
  delay(10); // assume that the frame rate is as low as 100fps... even if it should never be that low
  
  // write 0x18 to SROM_enable to start SROM download
  adns_write_reg(REG_SROM_Enable, 0x18); 
  
  // write the SROM file (=firmware data) 
  adns_com_begin();
  SPI.transfer(REG_SROM_Load_Burst | 0x80); // write burst destination adress
  delayMicroseconds(15);
  
  // send all bytes of the firmware
  unsigned char c;
  for(int i = 0; i < firmware_length; i++){ 
    c = (unsigned char)pgm_read_byte(firmware_data + i);
    SPI.transfer(c);
    delayMicroseconds(15);
  }
  adns_com_end();
}




int ADNS9800::convTwosComp(int b){
  //Convert from 2's complement
//  if(b & 0x80){
//    b = -1 * ((b ^ 0xff) + 1);
//    }
//  return b;
 if(b & 0x8000){
    b = -1 * ((b ^ 0xffff) + 1);
    }
  return b;
  }

