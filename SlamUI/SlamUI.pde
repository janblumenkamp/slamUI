/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */


import processing.serial.*;
import processing.core.*;

Map map;

void setup() 
{
  size(800, 600);
  frame.setResizable(true);
  
  frameRate(4);
  println(Serial.list());
  
  map = new Map(this, "/dev/rfcomm0", 460800);
}

void draw()
{
  background(0);
  
  int smallestScreenSize = (height < width) ? height : width;
  int mapscaled_sizeX = smallestScreenSize;
  int mapscaled_sizeY = smallestScreenSize;
  
  if((map.getMapSizeXMM() != 0) && (map.getMapSizeYMM() != 0))
  {
    float mapScaleFacX = ((float)map.getMapSizeXPx() / mapscaled_sizeX);
    float mapScaleFacY = ((float)map.getMapSizeYPx() / mapscaled_sizeY);
    
    for (int y = mapscaled_sizeY - 1; y >= 0; y--)
    {
      for (int x = 0; x < mapscaled_sizeX; x++)
      {
        int x_scaled = (int)(x * mapScaleFacX);
        int y_scaled = (map.getMapSizeYPx() - (int)(y * mapScaleFacY) - 1);
        
        stroke(255 - (int)map.getVarAt(x_scaled, y_scaled, 0));
        fill(255 - (int)map.getVarAt(x_scaled, y_scaled, 0));
        point(x, y);
      }
    }
  
    stroke(255, 0, 0); //Robot position/direction arrow
    noFill();
    line((int)(map.getRobPosXPx() / mapScaleFacX),
         (int)((map.getMapSizeYPx() - map.getRobPosYPx()) / mapScaleFacY),
         (int)(map.getRobPosXPx() / mapScaleFacX) + 20 * sin((70 + map.getRobOrientation()) * (PI / 180)),
         (int)((map.getMapSizeYPx() - map.getRobPosYPx()) / mapScaleFacY) - 20 * cos((70 + map.getRobOrientation()) * (PI / 180)));
    line((int)(map.getRobPosXPx() / mapScaleFacX),
         (int)((map.getMapSizeYPx() - map.getRobPosYPx()) / mapScaleFacY),
         (int)(map.getRobPosXPx() / mapScaleFacX) + 20 * sin((110 + map.getRobOrientation()) * (PI / 180)),
         (int)((map.getMapSizeYPx() - map.getRobPosYPx()) / mapScaleFacY) - 20 * cos((110 + map.getRobOrientation()) * (PI / 180)));
    // ellipse((int)((mpd_rob_x/map.getMapResolutionMM()) / mapScaleFacX), (int)(((mpd_map_size_Y - mpd_rob_y) / map.getMapResolutionMM()) / mapScaleFacY), 40, 40);
  }
  //bt.write('A');
    
  /*fill(50);
  textSize(20);
  text(map[10][10][0], 300, 100);*/
  
 /* fill(0);
  textSize(10);
  text(debug, 10, 10, 600, 500);*/
}

void mouseReleased()
{
  /*println("Re-init...");
  bt.clear();
  bt.stop();
  String portName = Serial.list()[0];
  bt = new Serial(this, portName, 460800);
  bt.buffer(1); //Call serialEvent after every byte (looking for start)
  sm_main = 0;
  sm_getStart = 0;*/
}

void stop()
{
  println("Stop");
  
  map.stopConnection();
}

void exit()
{
  println("Exit");
  
  map.stopConnection();
}

/*

// Wiring / Arduino Code
// Code for sensing a switch status and writing the value to the serial port.

int switchPin = 4;                       // Switch connected to pin 4

void setup() {
  pinMode(switchPin, INPUT);             // Set pin 0 as an input
  Serial.begin(9600);                    // Start serial communication at 9600 bps
}

void loop() {
  if (digitalRead(switchPin) == HIGH) {  // If switch is ON,
    Serial.write(1);               // send 1 to Processing
  } else {                               // If the switch is not ON,
    Serial.write(0);               // send 0 to Processing
  }
  delay(100);                            // Wait 100 milliseconds
}

*/
