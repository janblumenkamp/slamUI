import processing.core.*;
import processing.serial.*;

class Comm
{
  private PApplet p;
  private Serial bt;
  private SerialProxy serialProxy;
  
  private boolean debug;
  
  private int sm_main;
  private int sm_getStart;
  private String msg_id;
  private int msg_chk; //Received checksum
  private int msg_chk_computed; //Computed checksum (has to be compared with msg_chk)
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

  private int msgBufCount = 0; //Messagebuffer index (the Serial.buffer function is not working correctly...)
  private int[] msgBuf; //message buffer
  
  private Waypoint[] wp;
  private Waypoint wpStart;
  private int wp_amount;
  
  Comm(PApplet parent, String port, int baud)
  {
    p = parent;
    serialProxy = new SerialProxy();
    bt = new Serial(serialProxy, port, baud);
    
    sm_main = 0;
    sm_getStart = 0;
    msg_id = "";
    msg_chk = 0;
    msg_chk_computed = 0;
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
  
  private void processLWP()
  {
    /// One waypoint contains:
    /// x (2 bytes)
    /// y (2 bytes)
    /// z (1 byte)
    /// id (2 bytes)
    /// id prev (2 bytes)
    /// -> 9 bytes per waypoint

    wp_amount = msgBuf[0] + (msgBuf[1] << 8);
    
    wp = new Waypoint[wp_amount]; //We reserve the memory for all waypoints to transmit
    
    for(int i = 0; i < wp_amount; i ++) //The list is transmitted in the order they are linked!
    {
      wp[i] = new Waypoint(p);
      wp[i].setPosX(msgBuf[(i * 9) + 2] + (msgBuf[(i * 9) + 3] << 8));
      wp[i].setPosY(msgBuf[(i * 9) + 4] + (msgBuf[(i * 9) + 5] << 8));
      wp[i].setPosZ((byte)msgBuf[(i * 9) + 6]);
      wp[i].setID(msgBuf[(i * 9) + 7] + (msgBuf[(i * 9) + 8] << 8));
      int wpID_prev = msgBuf[(i * 9) + 9] + (msgBuf[(i * 9) + 10] << 8);
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
  
  private void processMPD()
  {
    map_resolution_mm = msgBuf[0];
    map_size_X = msgBuf[1] + (msgBuf[2] << 8);
    map_size_Y = msgBuf[3] + (msgBuf[4] << 8);
    map_layers = msgBuf[5];
    rob_x = msgBuf[6] + (msgBuf[7] << 8);
    rob_y = msgBuf[8] + (msgBuf[9] << 8);
    rob_z = msgBuf[10];
    rob_dir = msgBuf[11] + (msgBuf[12] << 8);
    
    if(map == null)
      map = new int[map_size_X / map_resolution_mm][map_size_Y / map_resolution_mm][map_layers];
  }
  
  private void processMAP()
  {
    int y = msgBuf[1] + (msgBuf[2] << 8);
    int z = msgBuf[0];
     
    for(int x = 0; x < (map_size_X / map_resolution_mm); x ++)
    {
      map[x][y][z] = msgBuf[x + 3];
    }
  }
  
  public void processSerialIn(Serial btIn)
  {
    switch(sm_main)
    {
      case 0:
          if(getStart(btIn) == true)
          {
            prntLn("Start gef");
            sm_main ++;
          }
        //  else if(sm_getStart == 0)
          //  debug += btIn.readChar();
            
        break;
      case 1:   msg_len = btIn.read();            sm_main ++;    break;  //Lenght (2 bytes)
      case 2:
                msg_len += (btIn.read() << 8);
                prntLn("");
                if(msg_len < 512)
                {
                  prntLn("Lenght: " + msg_len);
                  sm_main ++;
                }
                else
                {
                  prntLn("ERR: length > 512 Bytes: " + msg_len);
                  sm_main = 0;
                }
                
        break;
      case 3:   msg_chk = btIn.read();           sm_main ++;    break; //Checksum (4 bytes)
      case 4:   msg_chk += (btIn.read() << 8);   sm_main ++;    break;
      case 5:   msg_chk += (btIn.read() << 16);  sm_main ++;    break;
      case 6:   msg_chk += (btIn.read() << 24);
                prntLn("Checksum: " + msg_chk);
                msg_id = ""; //clear last received ID
                sm_main ++;
                break;
      case 7:   msg_id += btIn.readChar();       sm_main ++;    break; //ID (3 bytes/chars)
      case 8:   msg_id += btIn.readChar();          sm_main ++;    break;
      case 9:   msg_id += btIn.readChar();
                msg_chk_computed = 0;
                msgBufCount = 0;
                msgBuf = new int[msg_len];
                sm_main ++;
                break;
      
      case 10: //Buffer Message
                if(msgBufCount < msg_len)
                {
                  msgBuf[msgBufCount] = btIn.read();
                  msg_chk_computed += msgBuf[msgBufCount];
                  msgBufCount ++;
                }
                else
                {
                  if(msg_chk_computed == msg_chk)
                  {
                    prntLn("checksum matches!");
                    
                    if(msg_id.equals("MPD"))
                    {
                      prntLn("Received Mapdata");
                      processMPD();
                    }
                    else if(msg_id.equals("MAP"))
                    {
                      if(map == null) //Not received mapData yet
                      {
                        prntLn("Received Map, but not Mapdatay yet...");
                      }
                      else
                      {
                        prntLn("Received Map");
                        processMAP();
                      }
                    }
                    else if(msg_id.equals("LWP"))
                    {
                      prntLn("Received Waypoint List");
                      processLWP();
                    }
                    else
                    {
                      prntLn("Failed to match ID: " + msg_id);
                    }
                    
                  }
                  else
                    prntLn("chk not matching! comp: " + msg_chk_computed);
                
                  sm_main = 0;
                }
          
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
