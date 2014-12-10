import processing.core.*;
import processing.serial.*;

class Map
{
  private PApplet p;
  private Serial bt;
  private SerialProxy serialProxy;
  
  private boolean debug;
  
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
  
  private int[][][] map;

  private int mapMsgCount = 0; //Map has to be buffered seperately, because the Serial.buffer function is not working correctly with huge numbers...
  private int[] mapBuf;
  
  private Waypoint[] wp;
  private Waypoint wpStart;
  private int wp_amount;
  
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
    
    debug = true;
  }
  
  // We need a class descended from PApplet so that we can override the
  // serialEvent() method to capture serial data.  We can't use the Arduino
  // class itself, because PApplet defines a list() method that couldn't be
  // overridden by the static list() method we use to return the available
  // serial ports.  This class needs to be public so that the Serial class
  // can access its serialEvent() method.
  //SOURCE: http://playground.arduino.cc/Interfacing/Processing
  public class SerialProxy extends PApplet
  {
    public SerialProxy() {
    }

    public void serialEvent(Serial which)
    {
      try
      {
        // Notify the Arduino class that there's serial data for it to process.
        while (which.available() > 0)
          processSerialIn(which);
      } catch (Exception e) 
      {
        e.printStackTrace();
        throw new RuntimeException("Error inside map.serialEvent()");
      }
    }
  }

  private void prntLn(String out)
  {
    if(debug)
      p.println(out);
  }
  
  public void processSerialIn(Serial btIn)
  {
    switch(sm_main)
    {
      case 0:
          if(getStart(btIn) == true)
          {
            prntLn("Start gef");
            btIn.buffer(2); //Buffer length (2 bytes)
            sm_main ++;
          }
        //  else if(sm_getStart == 0)
          //  debug += btIn.readChar();
            
        break;
      case 1:
          msg_len = btIn.read() + (btIn.read() << 8);
          prntLn("");
          prntLn("Lenght: " + msg_len);
          btIn.buffer(4); //Buffer Checksum (4 bytes)
          sm_main ++;
        break;
      case 2:
          msg_chk = btIn.read() + (btIn.read() << 8) + (btIn.read() << 16) + (btIn.read() << 24);
          prntLn("Checksum: " + msg_chk);
          btIn.buffer(3); //Buffer ID (3 bytes)
          sm_main ++;
        break;
      case 3:
          msg_id = "";
          msg_id += btIn.readChar();
          msg_id += btIn.readChar();
          msg_id += btIn.readChar();
          
          if(msg_id.equals("MPD"))
          {
            prntLn("Received Mapdata");
            btIn.buffer(msg_len); //Buffer data
            sm_main = 4;
          }
          else if(msg_id.equals("MAP"))
          {
            if(map == null) //Not received mapData yet
            {
              prntLn("Received Map, but not Mapdatay yet...");
              btIn.buffer(1); //Search start
              sm_main = 0;
            }
            else
            {
              prntLn("Received Map");
               
              mapMsgCount = 0;
              mapBuf = new int[msg_len];
              btIn.buffer(1); //buffer each received byte in the mapBuf,...
              
              sm_main = 5;
            }
          }
          else if(msg_id.equals("LWP"))
          {
            prntLn("Received Waypoint List");
            btIn.buffer(msg_len);
            sm_main = 6;
          }
          else
          {
            prntLn("Failed to match ID: " + msg_id);
            btIn.buffer(1); //Search start
            sm_main = 0;
          }
        break;
      case 4: //MPD
          
          int[] buf = new int[msg_len];
          int msg_chk_computed = 0;
          
          for(int i = 0; i < msg_len; i++) //Compute received checksum
          {
            buf[i] = btIn.read();
            msg_chk_computed += buf[i];
          }
          
          if(msg_chk_computed == msg_chk)
          {
            prntLn("checksum matches!");
            
            map_resolution_mm = buf[0];
            map_size_X = buf[1] + (buf[2] << 8);
            map_size_Y = buf[3] + (buf[4] << 8);
            map_layers = buf[5];
            rob_x = buf[6] + (buf[7] << 8);
            rob_y = buf[8] + (buf[9] << 8);
            rob_z = buf[10];
            rob_dir = buf[11] + (buf[12] << 8);
            
            if(map == null)
              map = new int[map_size_X / map_resolution_mm][map_size_Y / map_resolution_mm][map_layers];
          }
          else
            prntLn("chk not matching! comp: " + msg_chk_computed);
            
          sm_main = 0;
        break;
      case 5: //MAP
        
          if(mapMsgCount < msg_len)
          {
            mapBuf[mapMsgCount] = btIn.read();
            mapMsgCount ++;
          }
          else
          {
            //int[] mapBuf = new int[msg_len];
            int map_chk_computed = 0;
            
            for(int i = 0; i < msg_len; i++) //Compute received checksum
            {
              map_chk_computed += mapBuf[i];
            }
            
            if(map_chk_computed == msg_chk)
            {
              prntLn("checksum matches!");
              
              int y = mapBuf[1] + (mapBuf[2] << 8);
              int z = mapBuf[0];
               
              for(int x = 0; x < (map_size_X / map_resolution_mm); x ++)
              {
                map[x][y][z] = mapBuf[x + 3];
              }
            }
            else
              prntLn("chk not matching! comp: " + map_chk_computed);
            
            sm_main = 0;
          }
          
        break;
      case 6: //LWP (Waypoint List)
          
          /// One waypoint contains:
          /// x (2 bytes)
          /// y (2 bytes)
          /// z (1 byte)
          /// id (2 bytes)
          /// id_next (2 bytes)
          /// id prev (2 bytes)
          /// -> 11 bytes per waypoint

          int[] wpBuf = new int[msg_len];
          int wp_chk_computed = 0;
          
          for(int i = 0; i < msg_len; i++) //Compute received checksum
          {
            wpBuf[i] = btIn.read();
            
            wp_chk_computed += wpBuf[i];
          }
          
          if(wp_chk_computed == msg_chk)
          {
            prntLn("checksum matches!");
            wp_amount = wpBuf[0] + (wpBuf[1] << 8);
            
            wp = new Waypoint[wp_amount]; //We reserve the memory for all waypoints to transmit
            
            for(int i = 0; i < wp_amount; i ++) //The list is transmitted in the order they are linked!
            {
              wp[i] = new Waypoint(p);
              wp[i].setPosX(wpBuf[(i * 9) + 2] + (wpBuf[(i * 9) + 3] << 8));
              wp[i].setPosY(wpBuf[(i * 9) + 4] + (wpBuf[(i * 9) + 5] << 8));
              wp[i].setPosZ((byte)wpBuf[(i * 9) + 6]);
              wp[i].setID(wpBuf[(i * 9) + 7] + (wpBuf[(i * 9) + 8] << 8));
              int wpID_prev = wpBuf[(i * 9) + 9] + (wpBuf[(i * 9) + 10] << 8);
              if(wpID_prev != -1 && i != 0) //There is a waypoint in the list before this one, otherwise it represents the start of the list
              {
                wp[i-1].setNext(wp[i]);
                wp[i].setPrev(wp[i-1]);
              }
              else
              {
                wpStart = wp[i];
              }
            }
          }
          else
            prntLn("chk not matching! comp: " + wp_chk_computed);
            
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
  
  void setDebug(boolean state) //Un/activate debug
  {
    debug = state;
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
  
  public int getVarAt(int x, int y, int z)
  {
    if(map == null)
      return -1;
    else
      return map[x][y][z];
  }
  
  public int getWPamount()
  {
     return wp_amount;
  }
  
  public Waypoint getWPofID(int i)
  {
     if(i > 0 && i < wp_amount)
       return wp[i];
     else
       return null;
  }
  
  public Waypoint getWPstart()
  {
     return wpStart;
  }
  
  public void setVarAt(int var, int x, int y, int z)
  {
    map[x][y][z] = var;
  }
}
