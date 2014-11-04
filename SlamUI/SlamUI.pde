/**
 * Simple Read
 * 
 * Read data from the serial port and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 */


import processing.serial.*;

Serial myPort;  // Create object from Serial class
int val;      // Data received from the serial port

void setup() 
{
  size(800, 600);
  
  frameRate(4);
  // I know that the first port in the serial list on my mac
  // is always my  FTDI adaptor, so I open Serial.list()[0].
  // On Windows machines, this generally opens COM1.
  // Open whatever port is the one you're using.
  println(Serial.list());
  String portName = Serial.list()[0];
  //String portName = "/dev/rfcomm0";
  
  myPort = new Serial(this, portName, 1382400);
  myPort.buffer(1); //Call serialEvent after every byte (looking for start)
}

void draw()
{
  for (int y = (mpd_map_size_Y / mpd_map_resolution_mm) - 1; y >= 0; y--)
  {
    for (int x = 0; x < (mpd_map_size_X / mpd_map_resolution_mm); x++)
    {
      stroke(255 - map[x][y][0]);
      point(x, (mpd_map_size_Y / mpd_map_resolution_mm) - 1 - y);
    }
  }
  
  stroke(255, 0, 0);
  noFill();
  ellipse(mpd_rob_x/mpd_map_resolution_mm, 200 - mpd_rob_y/mpd_map_resolution_mm, 20, 20);
  
  /*fill(50);
  textSize(20);
  text(map[10][10][0], 300, 100);*/
  
 /* fill(0);
  textSize(10);
  text(debug, 10, 10, 600, 500);*/
}

//String debug = "";

int sm_main = 0;
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

char[][][] map = new char[200][200][1];

void serialEvent(Serial myPort)
{
  switch(sm_main)
  {
    case 0:
        if(getStart(myPort) == true)
        {
          println("Start gef");
          myPort.buffer(2); //Buffer length (2 bytes)
          sm_main ++;
        }
      //  else if(sm_getStart == 0)
        //  debug += myPort.readChar();
          
      break;
    case 1:
        msg_len = myPort.read() + (myPort.read() << 8);
        println("");
        println("Lenght: " + msg_len);
        myPort.buffer(4); //Buffer Checksum (4 bytes)
        sm_main ++;
      break;
    case 2:
        msg_chk = myPort.read() + (myPort.read() << 8) + (myPort.read() << 16) + (myPort.read() << 24);
        println("Checksum: " + msg_chk);
        myPort.buffer(3); //Buffer ID (3 bytes)
        sm_main ++;
      break;
    case 3:
        msg_id = "";
        msg_id += myPort.readChar();
        msg_id += myPort.readChar();
        msg_id += myPort.readChar();
        
        if(msg_id.equals("MPD"))
        {
          println("Received Mapdata");
          myPort.buffer(msg_len); //Buffer data
          sm_main = 4;
        }
        else if(msg_id.equals("MAP"))
        {
          println("Received Map");
          myPort.buffer(msg_len);
          sm_main = 5;
        }
        else
        {
          println("Failed to match ID: " + msg_id);
          myPort.buffer(1); //Search start
          sm_main = 0;
        }
      break;
    case 4: //MPD
        
        char[] buf = new char[msg_len];
        int msg_chk_computed = 0;
        
        for(int i = 0; i < msg_len; i++) //Compute received checksum
        {
          buf[i] = myPort.readChar();
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
          
          /*frame.setResizable(true);
          frame.setSize(mpd_map_size_X/mpd_map_resolution_mm, mpd_map_size_Y/mpd_map_resolution_mm);
          frame.setResizable(false);*/
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
          mapBuf[i] = myPort.readChar();
          map_chk_computed += mapBuf[i];
        }
        
        if(map_chk_computed == msg_chk)
        {
          println("checksum matches!");
          
          for(int x = 0; x < (mpd_map_size_X/mpd_map_resolution_mm); x ++)
          {
            int y = mapBuf[200] + (mapBuf[201] << 8);
            int z = mapBuf[202];
            print("BUFFER Y: "+y);
            
            //map[x][y][z] = mapBuf[x + 3];
          }
        }
        else
          println("chk not matching! comp: " + map_chk_computed);
          
        sm_main = 0;
      break;
    default: sm_main = 0; break;
  }
}


int sm_getStart = 0;
boolean getStart(Serial myPort)
{
  boolean retVar = false;
  
  switch(sm_getStart)
  {
    case 0:
        if(myPort.read() == 'P')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 1:
        if(myPort.read() == 'C')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 2:
        if(myPort.read() == 'U')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 3:
        if(myPort.read() == 'I')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 4:
        if(myPort.read() == '_')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 5:
        if(myPort.read() == 'M')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 6:
        if(myPort.read() == 'S')
          sm_getStart ++;
        else
          sm_getStart = 0;
      break;
    case 7:
        if(myPort.read() == 'G')
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
  
  myPort.clear();
  myPort.stop();
}

void exit()
{
  println("Exit");
  
  myPort.clear();
  myPort.stop();
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
