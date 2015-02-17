
/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */


import processing.serial.*;
import processing.core.*;
import g4p_controls.*;

Comm comm; //Serial communication interface with robot
Map map; //Map element (drawing operations)

GDropList dropList_commports; 
GButton btnConnect; 

final static int ROBOT_MAP_SIZE = 15;
final static int WP_MAP_SIZE = 5;

void setup() 
{
  size(800, 600);
  frame.setResizable(true);
  
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setCursor(ARROW);
  
  dropList_commports = new GDropList(this, 10, 10, 130, 300, 15);
  dropList_commports.setItems(Serial.list(), 0);
  btnConnect = new GButton(this, 150, 10, 80, 20);
  btnConnect.setText("Connect");
  btnConnect.addEventHandler(this, "btnConnect_click");
  
  map = new Map(this, 0, 0, (height < width) ? height : width, (height < width) ? height : width);
}
  
void draw()
{
  background(255);
  
  if(comm != null)
  {
    int smallestScreenSize = (height < width) ? height : width;
  
    map.setScaledSizes(smallestScreenSize, smallestScreenSize);
    if(height < width)
    {
      map.setPosX(abs(width - height));
    }
    else
    {
      map.setPosX(0);
    }
    map.display();
  }
  /*fill(50);
  textSize(20);
  text(map[10][10][0], 300, 100);*/
  
 /* fill(0);
  textSize(10);
  text(debug, 10, 10, 600, 500);*/
}

void mouseReleased()
{
  if(comm != null)
  {
    map.mouseReleased();
  }
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
  if(comm != null)
  {
    map.mouseDragged();
  }
}

void mouseClicked()
{
  if(comm != null)
  {
    map.mouseClicked();
  }
}

void btnConnect_click(GButton source, GEvent event)
{
  if(comm == null)
  {
    try
    {
      comm = new Comm(this, dropList_commports.getSelectedText(), 460800);
      map.setComm(comm);
      comm.setDebug(true);
      comm.setConsole(false);
      btnConnect.setText("Disconnect"); //Switch function of button, now we want to be able to disconnect
    }
    catch(Exception e)
    {
      println("Error (exception occured): Can't connect to serial port" + dropList_commports.getSelectedText());
    }
  }
  else
  {
    comm.stopConnection();
    comm = null;
    btnConnect.setText("Connect"); //Switch function of button, now we want to be able to disconnect
  }
}

void stop()
{
  println("Stop");
  
  if(comm != null)
  {
    comm.stopConnection();
  }
}

void exit()
{
  println("Exit");
  
  if(comm != null)
  {
    comm.stopConnection();
  }
}
