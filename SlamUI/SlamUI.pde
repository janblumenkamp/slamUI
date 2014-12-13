/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */


import processing.serial.*;
import processing.core.*;

Comm comm; //Serial communication interface with robot
Map map; //Map element (drawing operations)

final static int ROBOT_MAP_SIZE = 15;
final static int WP_MAP_SIZE = 5;

void setup() 
{
  size(800, 600);
  frame.setResizable(true);
  
  //frameRate(4);
  println(Serial.list());
  
  comm = new Comm(this, "/dev/rfcomm0", 460800);
  comm.setDebug(true);
  comm.setConsole(false);
  
  map = new Map(this, comm, 0, 0, (height < width) ? height : width, (height < width) ? height : width);
}
  
void draw()
{
  background(0);
  
  int smallestScreenSize = (height < width) ? height : width;
  
  map.setScaledSizes(smallestScreenSize, smallestScreenSize);
  map.display();
  /*fill(50);
  textSize(20);
  text(map[10][10][0], 300, 100);*/
  
 /* fill(0);
  textSize(10);
  text(debug, 10, 10, 600, 500);*/
}

void mouseReleased()
{
  map.mouseReleased();
  /*println("Re-init...");
  bt.clear();
  bt.stop();
  String portName = Serial.list()[0];
  bt = new Serial(this, portName, 460800);
  bt.buffer(1); //Call serialEvent after every byte (looking for start)
  sm_main = 0;
  sm_getStart = 0;*/
}

void mouseDragged()
{
  map.mouseDragged();
}

void mouseClicked()
{
  map.mouseClicked();
}

void stop()
{
  println("Stop");
  
  comm.stopConnection();
}

void exit()
{
  println("Exit");
  
  comm.stopConnection();
}
