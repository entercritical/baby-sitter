/*
  Sends sensor data to Arduino
  (needs SensorGraph and Amarino app installed and running on Android)
*/
 
#include <MeetAndroid.h>

MeetAndroid meetAndroid;
int sensor = 0;

void setup()  
{
  // use the baud rate your bluetooth module is configured to 
  // not all baud rates are working well, i.e. ATMEGA168 works best with 57600
  Serial1.begin(57600); 
 
  // we initialize analog pin A0 as an input pin
  pinMode(sensor, INPUT);
}

void loop()
{
  meetAndroid.receive(); // you need to keep this in your loop() to receive events
  
  // read input pin and send result to Android
  char buf[20];
  //strcpy(buf, "CDS:");
  sscanf(buf, "CDS:%d", analogRead(sensor));
  
  meetAndroid.send(buf);
  //meetAndroid.send(text);
  
  // add a little delay otherwise the phone is pretty busy
  delay(1000);
}


