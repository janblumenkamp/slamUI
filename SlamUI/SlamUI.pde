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

final static int ROBOT_MAP_SIZE = 15;
final static int WP_MAP_SIZE = 5;

void setup() 
{
  size(800, 600);
  frame.setResizable(true);
  
  //frameRate(4);
  println(Serial.list());
  
  map = new Map(this, "/dev/rfcomm0", 460800);
  map.setDebug(true);
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
         (int)(map.getRobPosXPx() / mapScaleFacX) + (ROBOT_MAP_SIZE / mapScaleFacX) * sin((70 + map.getRobOrientation()) * (PI / 180)),
         (int)((map.getMapSizeYPx() - map.getRobPosYPx()) / mapScaleFacY) - (ROBOT_MAP_SIZE / mapScaleFacY) * cos((70 + map.getRobOrientation()) * (PI / 180)));
    line((int)(map.getRobPosXPx() / mapScaleFacX),
         (int)((map.getMapSizeYPx() - map.getRobPosYPx()) / mapScaleFacY),
         (int)(map.getRobPosXPx() / mapScaleFacX) + (ROBOT_MAP_SIZE / mapScaleFacX) * sin((110 + map.getRobOrientation()) * (PI / 180)),
         (int)((map.getMapSizeYPx() - map.getRobPosYPx()) / mapScaleFacY) - (ROBOT_MAP_SIZE / mapScaleFacY) * cos((110 + map.getRobOrientation()) * (PI / 180)));
    // ellipse((int)((mpd_rob_x/map.getMapResolutionMM()) / mapScaleFacX), (int)(((mpd_map_size_Y - mpd_rob_y) / map.getMapResolutionMM()) / mapScaleFacY), 40, 40);
    
    Waypoint wpMap = map.getWPstart();
    if(wpMap != null)
    {
      println("Waypoints: "+map.getWPamount());
      
      for(int i = 0; i < map.getWPamount(); i++)
      {
        ellipse((int)((wpMap.getPosX()/map.getMapResolutionMM()) / mapScaleFacX), (int)(((map.getMapSizeYMM() - wpMap.getPosY()) / map.getMapResolutionMM()) / mapScaleFacY), WP_MAP_SIZE / mapScaleFacX, WP_MAP_SIZE / mapScaleFacY);
        if(wpMap.getPrev() != null)
        {
          line((wpMap.getPrev().getPosX()/map.getMapResolutionMM()) / mapScaleFacX,
               (int)(((map.getMapSizeYMM() - wpMap.getPrev().getPosY()) / map.getMapResolutionMM()) / mapScaleFacY),
               (wpMap.getPosX()/map.getMapResolutionMM()) / mapScaleFacX,
               (int)(((map.getMapSizeYMM() - wpMap.getPosY()) / map.getMapResolutionMM()) / mapScaleFacY));
        }
        wpMap = wpMap.getNext();
      }
    }    
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
