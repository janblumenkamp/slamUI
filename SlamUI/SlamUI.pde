/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */


import processing.serial.*;

Serial bt;  // Create object from Serial class
int val;      // Data received from the serial port

void setup() 
{
  size(800, 600);
  frame.setResizable(true);
  
  frameRate(4);
  // I know that the first port in the serial list on my mac
  // is always my  FTDI adaptor, so I open Serial.list()[0].
  // On Windows machines, this generally opens COM1.
  // Open whatever port is the one you're using.
  println(Serial.list());
  String portName = Serial.list()[0];
  //String portName = "/dev/ttyUSB0";
  
  bt = new Serial(this, portName, 460800);
  bt.buffer(1); //Call serialEvent after every byte (looking for start)
}

void draw()
{
  background(0);
  
  int smallestScreenSize = (height < width) ? height : width;
  
  int mapscaled_sizeX = smallestScreenSize;
  int mapScaled_sizeY = smallestScreenSize;
  
  if((mpd_map_resolution_mm != 0) && (mpd_map_size_X != 0) && (mpd_map_size_Y != 0))
  {
    float mapScaleFacX = (float)(mpd_map_size_X / mpd_map_resolution_mm) / mapscaled_sizeX;
    float mapScaleFacY = (float)(mpd_map_size_Y / mpd_map_resolution_mm) / mapScaled_sizeY;
    
    for (int y = mapScaled_sizeY - 1; y >= 0; y--)
    {
      for (int x = 0; x < mapscaled_sizeX; x++)
      {
        int x_scaled = (int)(x * mapScaleFacX);
        int y_scaled = ((mpd_map_size_Y / mpd_map_resolution_mm) - (int)(y * mapScaleFacY) - 1);
        
        stroke(255 - map[x_scaled][y_scaled][0]);
        fill(255 - map[x_scaled][y_scaled][0]);
        point(x, y);
      }
    }
  
    stroke(255, 0, 0);
    noFill();
    ellipse((int)((mpd_rob_x/mpd_map_resolution_mm) * mapScaleFacX), (int)((mpd_map_size_Y - (mpd_rob_y/mpd_map_resolution_mm)) * mapScaleFacY), 20, 20);
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

//String debug = "";

int sm_main = 0;
int sm_getStart = 0;
String msg_id = "";
int msg_chk = 0;
int msg_len = 0;

int mpd_map_resolution_mm = 1;
int mpd_map_size_X = 0;
int mpd_map_size_Y = 0;
int mpd_map_layers = 0;
int mpd_rob_x = 0;
int mpd_rob_y = 0;
int mpd_rob_z = 0;
int mpd_rob_dir = 0;

char[][][] map = new char[300][300][1];

void serialEvent(Serial bt)
{
  switch(sm_main)
  {
    case 0:
        if(getStart(bt) == true)
        {
          println("Start gef");
          bt.buffer(2); //Buffer length (2 bytes)
          sm_main ++;
        }
      //  else if(sm_getStart == 0)
        //  debug += bt.readChar();
          
      break;
    case 1:
        msg_len = bt.read() + (bt.read() << 8);
        println("");
        println("Lenght: " + msg_len);
        bt.buffer(4); //Buffer Checksum (4 bytes)
        sm_main ++;
      break;
    case 2:
        msg_chk = bt.read() + (bt.read() << 8) + (bt.read() << 16) + (bt.read() << 24);
        println("Checksum: " + msg_chk);
        bt.buffer(3); //Buffer ID (3 bytes)
        sm_main ++;
      break;
    case 3:
        msg_id = "";
        msg_id += bt.readChar();
        msg_id += bt.readChar();
        msg_id += bt.readChar();
        
        if(msg_id.equals("MPD"))
        {
          println("Received Mapdata");
          bt.buffer(msg_len); //Buffer data
          sm_main = 4;
        }
        else if(msg_id.equals("MAP"))
        {
          println("Received Map");
          bt.buffer(msg_len);
          sm_main = 5;
        }
        else if(msg_id.equals("LWP"))
        {
          println("Received Waypoint List");
          bt.buffer(msg_len);
          sm_main = 6;
        }
        else
        {
          println("Failed to match ID: " + msg_id);
          bt.buffer(1); //Search start
          sm_main = 0;
        }
      break;
    case 4: //MPD
        
        char[] buf = new char[msg_len];
        int msg_chk_computed = 0;
        
        for(int i = 0; i < msg_len; i++) //Compute received checksum
        {
          buf[i] = bt.readChar();
          msg_chk_computed += buf[i];
        }
        
        if(msg_chk_computed == msg_chk)
        {
          println("checksum matches!");
          
          mpd_map_resolution_mm = buf[0];
          mpd_map_size_X = buf[1] + (buf[2] << 8);
          mpd_map_size_Y = buf[3] + (buf[4] << 8);
          mpd_map_layers = buf[5];
          mpd_rob_x = buf[6] + (buf[7] << 8);
          mpd_rob_y = buf[8] + (buf[9] << 8);
          mpd_rob_z = buf[10];
          mpd_rob_dir = buf[11] + (buf[12] << 8);
          
          //frame.setResizable(true);
          //frame.setSize(mpd_map_size_X/mpd_map_resolution_mm, mpd_map_size_Y/mpd_map_resolution_mm);
          //frame.setResizable(false);
        }
        else
          println("chk not matching! comp: " + msg_chk_computed);
          
        sm_main = 0;
      break;
    case 5: //MAP
        
        char[] mapBuf = new char[msg_len];
        int map_chk_computed = 0;
        
        for(int i = 0; i < msg_len; i++) //Compute received checksum
        {
          mapBuf[i] = bt.readChar();
          map_chk_computed += mapBuf[i];
        }
        
        if(map_chk_computed == msg_chk)
        {
          println("checksum matches!");
          
          for(int x = 0; x < (mpd_map_size_X/mpd_map_resolution_mm); x ++)
          {
            int y = mapBuf[1] + (mapBuf[2] << 8);
            int z = mapBuf[0];
            
            map[x][y][z] = mapBuf[x + 3];
          }
        }
        else
          println("chk not matching! comp: " + map_chk_computed);
          
        sm_main = 0;
      break;
    case 6: //LWP (Waypoint List)
        
        char[] wpBuf = new char[msg_len];
        int wp_chk_computed = 0;
        
        for(int i = 0; i < msg_len; i++) //Compute received checksum
        {
          wpBuf[i] = bt.readChar();
          wp_chk_computed += wpBuf[i];
        }
        
        if(wp_chk_computed == msg_chk)
        {
          println("checksum matches!");
          
          for(int x = 0; x < (mpd_map_size_X/mpd_map_resolution_mm); x ++)
          {
            int y = wpBuf[1] + (wpBuf[2] << 8);
            int z = wpBuf[0];
            
            //map[x][y][z] = mapBuf[x + 3];
          }
        }
        else
          println("chk not matching! comp: " + wp_chk_computed);
          
        sm_main = 0;
      break;
    default: sm_main = 0; break;
  }
}


boolean getStart(Serial bt)
{
  boolean retVar = false;
  
  switch(sm_getStart)
  {
    case 0:
        if(bt.read() == 'P')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 1:
        if(bt.read() == 'C')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 2:
        if(bt.read() == 'U')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 3:
        if(bt.read() == 'I')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 4:
        if(bt.read() == '_')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 5:
        if(bt.read() == 'M')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 6:
        if(bt.read() == 'S')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 7:
        if(bt.read() == 'G')
          retVar = true;
          
        sm_getStart = 0;
      break;
    default: sm_getStart = 0; break;
  }
  
  return retVar;
}

void stop()
{
  println("Stop");
  
  bt.clear();
  bt.stop();
}

void exit()
{
  println("Exit");
  
  bt.clear();
  bt.stop();
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
