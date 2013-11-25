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
  
  meetAndroid.registerFunction(sendCurrentData, 'C');
  // we initialize analog pin A0 as an input pin
  pinMode(sensor, INPUT);
}

char g_buf[100];
int g_data[4][10];
int g_count;
char *g_sensor[] = { "heat", "wet", "bpm", "mic"};

void loop()
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
  int val = analogRead(sensor);
  
  g_data[0][g_count] = map(val, 0, 1023, 300, 400);
  g_data[1][g_count] = map(val, 0, 1023, 0, 100);
  g_data[2][g_count] = map(val, 0, 1023, 0, 200);
  g_data[3][g_count] = val;
  g_count++;
  // add a little delay otherwise the phone is pretty busy
  delay(1000);
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

