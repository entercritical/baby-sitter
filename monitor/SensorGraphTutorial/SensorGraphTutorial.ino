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

char g_buf[100];
int g_data[10];
int g_count;

void loop()
{
  int i;
  char tmp[5];
  
  meetAndroid.receive(); // you need to keep this in your loop() to receive events
  
  if (g_count >= 10) {
    g_count = 0;
    strcpy(g_buf, "CDS ");
    for (i = 0; i < 10; i++) {
      itoa(g_data[i], tmp, 10);
      strcat(g_buf, tmp);
      strcat(g_buf, " ");
    }
    meetAndroid.send(g_buf);
  }
      
  g_data[g_count++] = analogRead(sensor);
  
  // add a little delay otherwise the phone is pretty busy
  delay(1000);
}


