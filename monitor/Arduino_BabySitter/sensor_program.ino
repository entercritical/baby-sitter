#include "Timer.h"
#include <MeetAndroid.h>

MeetAndroid meetAndroid;

Timer SMPeriod;
Timer BTPeriod;
Timer SensPeriod;

volatile int BPM;                   // used to hold the pulse rate

const int TempSensor_PIN = A0;
const int HumiditySensor_PIN = A1;
const int SoundSensor_PIN = A2;

int TempSensorValue;
long TempSensorVoltage;
int SoundSensorValue;

int Temperature;
int Humidity;
int Sound;
int SoundThreshCount;

char g_buf[100];
int g_data[4][10];
int g_count;
char *g_sensor[] = { "heat", "wet", "bpm", "mic"};

void setup()
{
  pinMode(SoundSensor_PIN, INPUT);
  pinMode(HumiditySensor_PIN, OUTPUT);
  digitalWrite(HumiditySensor_PIN, HIGH);
  Serial.begin(57600);
  BTPeriod.every(1000, BlueToothMonitor);
  SensPeriod.every(1000, ADSensing);
  meetAndroid.registerFunction(sendCurrentData, 'C');
}
 
void loop()
{
  BTPeriod.update();
  SensPeriod.update();
  
  SoundSensorValue = analogRead(SoundSensor_PIN);
  
  if(SoundSensorValue > 750){
    SoundThreshCount++;
  }
}

void ADSensing()
{
  TempSensing();
  HumiditySensing();
}

void BlueToothMonitor()
{
  int i, j;
  char tmp[5];
  
  meetAndroid.receive(); // you need to keep this in your loop() to receive events
  
  if (g_count >= 10) {
    g_count = 0;
    for (j = 0; j < 4; j++) {
      strcpy(g_buf, g_sensor[j]);
      strcat(g_buf, " ");
      for (i = 0; i < 10; i++) {
        itoa(g_data[j][i], tmp, 10);
        strcat(g_buf, tmp);
        strcat(g_buf, " ");
      }
      meetAndroid.send(g_buf);
    }
  }
  
  if(Temperature == 0){
    Temperature = 375;
  }
  if(Humidity == 0){
    Humidity = 33;
  }
  
  g_data[0][g_count] = Temperature;
  g_data[1][g_count] = Humidity;
  g_data[2][g_count] = 80;
  g_data[3][g_count] = SoundThreshCount;
  
  SoundThreshCount = 0;
  g_count++;
}

void sendCurrentData(byte flag, byte numOfValues)
{
  int j;
  char tmp[5];
  for (j = 0; j < 4; j++) {
    strcpy(g_buf, g_sensor[j]);
    strcat(g_buf, " ");
    itoa(g_data[j][0], tmp, 10);
    strcat(g_buf, tmp);
    strcat(g_buf, " ");
    meetAndroid.send(g_buf);
  }
}

void TempSensing()
{
  TempSensorValue = analogRead(TempSensor_PIN);
  TempSensorVoltage = TempSensorValue * 5000000/1024000;
  Temperature = int(TempSensorVoltage - 500);
}

void HumiditySensing()
{
  byte SensorData[5];
  byte SensorInput;
  byte i;
  
  digitalWrite(HumiditySensor_PIN, LOW);
  delay(18);
  digitalWrite(HumiditySensor_PIN, HIGH);
  delayMicroseconds(1);
  pinMode(HumiditySensor_PIN, INPUT);
  delayMicroseconds(40);
  
  if (digitalRead(HumiditySensor_PIN))
  {
    delay(200);
    return;
  }
  
  delayMicroseconds(80);
  
  if (!digitalRead(HumiditySensor_PIN))
  {
    return;
  }
    
  delayMicroseconds(80);// now ready for data reception
  
  for (i=0; i<5; i++){
    SensorData[i] = ReadHumidityData(HumiditySensor_PIN);
  }  //recieved 40 bits data. Details are described in datasheet
    
  pinMode(HumiditySensor_PIN, OUTPUT);
  digitalWrite(HumiditySensor_PIN, HIGH);
  byte SensorCheckSum = SensorData[0]+SensorData[2];// check check_sum
  if(SensorData[4]!= SensorCheckSum){
    ;
  }
  
  Humidity = int(SensorData[0]);
  
//  Serial.print("DHT11 Temperature is ");
//  Serial.print(SensorData[2], DEC);
//  Serial.println(" C");
}


byte ReadHumidityData(int Pin)
{
  byte i = 0;
  byte result=0;
  for(i=0; i< 8; i++)
  {
    while (!digitalRead(Pin));
    delayMicroseconds(30);
    if (digitalRead(Pin) != 0 )
      bitSet(result, 7-i);
    while (digitalRead(Pin));
  }
  return result;
}

