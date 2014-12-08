import processing.core.*;
import processing.serial.*;

class Map
{
  PApplet p;
  Serial bt;
  SerialProxy serialProxy;
  
  private int sm_main;
  private int sm_getStart;
  private String msg_id;
  private int msg_chk;
  private int msg_len;
  
  private int map_resolution_mm;
  private int map_size_X;
  private int map_size_Y;
  private int map_layers;
  private int rob_x;
  private int rob_y;
  private int rob_z;
  private int rob_dir;
  
  public char[][][] map;

  Map(PApplet parent, String port, int baud)
  {
    p = parent;
    serialProxy = new SerialProxy();
    bt = new Serial(serialProxy, port, baud);
    bt.buffer(1); //Call serialEvent after every byte (looking for start)
    
    sm_main = 0;
    sm_getStart = 0;
    msg_id = "";
    msg_chk = 0;
    msg_len = 0;
    
    map_resolution_mm = 1;
    map_size_X = 0;
    map_size_Y = 0;
    map_layers = 0;
    rob_x = 0;
    rob_y = 0;
    rob_z = 0;
    rob_dir = 0;
    map = new char[300][300][1];
  }
  
  // We need a class descended from PApplet so that we can override the
  // serialEvent() method to capture serial data.  We can't use the Arduino
  // class itself, because PApplet defines a list() method that couldn't be
  // overridden by the static list() method we use to return the available
  // serial ports.  This class needs to be public so that the Serial class
  // can access its serialEvent() method.
  //SOURCE: http://playground.arduino.cc/Interfacing/Processing
  public class SerialProxy extends PApplet {
    public SerialProxy() {
    }

    public void serialEvent(Serial which) {
      try {
        // Notify the Arduino class that there's serial data for it to process.
        while (which.available() > 0)
          processSerialIn(which);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error inside map.serialEvent()");
      }
    }
  }

  public void processSerialIn(Serial btIn)
  {
    switch(sm_main)
    {
      case 0:
          if(getStart(btIn) == true)
          {
            p.println("Start gef");
            bt.buffer(2); //Buffer length (2 bytes)
            sm_main ++;
          }
        //  else if(sm_getStart == 0)
          //  debug += btIn.readChar();
            
        break;
      case 1:
          msg_len = btIn.read() + (btIn.read() << 8);
          p.println("");
          p.println("Lenght: " + msg_len);
          bt.buffer(4); //Buffer Checksum (4 bytes)
          sm_main ++;
        break;
      case 2:
          msg_chk = btIn.read() + (btIn.read() << 8) + (btIn.read() << 16) + (btIn.read() << 24);
          p.println("Checksum: " + msg_chk);
          bt.buffer(3); //Buffer ID (3 bytes)
          sm_main ++;
        break;
      case 3:
          msg_id = "";
          msg_id += btIn.readChar();
          msg_id += btIn.readChar();
          msg_id += btIn.readChar();
          
          if(msg_id.equals("MPD"))
          {
            p.println("Received Mapdata");
            bt.buffer(msg_len); //Buffer data
            sm_main = 4;
          }
          else if(msg_id.equals("MAP"))
          {
            p.println("Received Map");
            bt.buffer(msg_len);
            sm_main = 5;
          }
          else if(msg_id.equals("LWP"))
          {
            p.println("Received Waypoint List");
            bt.buffer(msg_len);
            sm_main = 6;
          }
          else
          {
            p.println("Failed to match ID: " + msg_id);
            bt.buffer(1); //Search start
            sm_main = 0;
          }
        break;
      case 4: //MPD
          
          char[] buf = new char[msg_len];
          int msg_chk_computed = 0;
          
          for(int i = 0; i < msg_len; i++) //Compute received checksum
          {
            buf[i] = btIn.readChar();
            msg_chk_computed += buf[i];
          }
          
          if(msg_chk_computed == msg_chk)
          {
            p.println("checksum matches!");
            
            map_resolution_mm = buf[0];
            map_size_X = buf[1] + (buf[2] << 8);
            map_size_Y = buf[3] + (buf[4] << 8);
            map_layers = buf[5];
            rob_x = buf[6] + (buf[7] << 8);
            rob_y = buf[8] + (buf[9] << 8);
            rob_z = buf[10];
            rob_dir = buf[11] + (buf[12] << 8);
            
            //frame.setResizable(true);
            //frame.setSize(map_size_X/map_resolution_mm, map_size_Y/map_resolution_mm);
            //frame.setResizable(false);
          }
          else
            p.println("chk not matching! comp: " + msg_chk_computed);
            
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
            p.println("checksum matches!");
            
            for(int x = 0; x < (map_size_X/map_resolution_mm); x ++)
            {
              int y = mapBuf[1] + (mapBuf[2] << 8);
              int z = mapBuf[0];
              
              map[x][y][z] = mapBuf[x + 3];
            }
          }
          else
            p.println("chk not matching! comp: " + map_chk_computed);
            
          sm_main = 0;
        break;
      case 6: //LWP (Waypoint List)
          
          char[] wpBuf = new char[msg_len];
          int wp_chk_computed = 0;
          
          for(int i = 0; i < msg_len; i++) //Compute received checksum
          {
            wpBuf[i] = btIn.readChar();
            wp_chk_computed += wpBuf[i];
          }
          
          if(wp_chk_computed == msg_chk)
          {
            p.println("checksum matches!");
            
            for(int x = 0; x < (map_size_X/map_resolution_mm); x ++)
            {
              int y = wpBuf[1] + (wpBuf[2] << 8);
              int z = wpBuf[0];
              
              //map[x][y][z] = mapBuf[x + 3];
            }
          }
          else
            p.println("chk not matching! comp: " + wp_chk_computed);
            
          sm_main = 0;
        break;
      default: sm_main = 0; break;
    }
  }
  
  private boolean getStart(Serial btSt)
  {
    boolean retVar = false;
    
    switch(sm_getStart)
    {
      case 0:
          if(btSt.read() == 'P')
            sm_getStart ++;
          else
            sm_getStart = 0;
        break;
      case 1:
          if(btSt.read() == 'C')
            sm_getStart ++;
          else
            sm_getStart = 0;
        break;
      case 2:
          if(btSt.read() == 'U')
            sm_getStart ++;
          else
            sm_getStart = 0;
        break;
      case 3:
          if(btSt.read() == 'I')
            sm_getStart ++;
          else
            sm_getStart = 0;
        break;
      case 4:
          if(btSt.read() == '_')
            sm_getStart ++;
          else
            sm_getStart = 0;
        break;
      case 5:
          if(btSt.read() == 'M')
            sm_getStart ++;
          else
            sm_getStart = 0;
        break;
      case 6:
          if(btSt.read() == 'S')
            sm_getStart ++;
          else
            sm_getStart = 0;
        break;
      case 7:
          if(btSt.read() == 'G')
            retVar = true;
            
          sm_getStart = 0;
        break;
      default: sm_getStart = 0; break;
    }
    
    return retVar;
  }
  
  void stopConnection()
  {
    bt.clear();
    bt.stop();
  }
  
  public int getMapResolutionMM()
  {
    return map_resolution_mm;
  }
  
  public int getMapSizeXMM()
  {
    return map_size_X;
  }
  
  public int getMapSizeXPx()
  {
    return map_size_X / map_resolution_mm;
  }
  
  public int getMapSizeYMM()
  {
    return map_size_Y;
  }
  
  public int getMapSizeYPx()
  {
    return map_size_Y / map_resolution_mm;
  }
  
  public int getMapLayers()
  {
    return map_layers;
  }
  
  public int getRobPosXMM()
  {
    return rob_x;
  }
  
  public int getRobPosXPx()
  {
    return rob_x / map_resolution_mm;
  }
  
  public int getRobPosYMM()
  {
    return rob_y;
  }
  
  public int getRobPosYPx()
  {
    return rob_y / map_resolution_mm;
  }
  
  public int getRobPosZ()
  {
    return rob_z;
  }
  
  public int getRobOrientation()
  {
    return rob_dir;
  }
}
