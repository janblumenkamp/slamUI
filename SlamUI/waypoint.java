import processing.core.*;

class Waypoint
{
  PApplet p;
  
  private int x, y;
  private byte z;
  private int id;
  private Waypoint previous, next;
  
  Waypoint(PApplet parent, int _x, int _y, byte _z, int _id, Waypoint _next, Waypoint _prev)
  {
    p = parent;
    
    x = _x;
    y = _y;
    z = _z;
    id = _id;
    next = _next;
    previous = _prev;
  }
  
  Waypoint(PApplet parent)
  {
    p = parent;
    
    x = 0;
    y = 0;
    z = 0;
    id = -1;
    previous = null;
    next = null;
  }
  
  public void setPosX(int _x)
  {
    x = _x;
  }
  
  public void setPosY(int _y)
  {
    y = _y;
  }
  
  public void setPosZ(byte _z)
  {
    z = _z;
  }
  
  public void setID(int _id)
  {
    id = _id;
  }
  
  public void setNext(Waypoint _n)
  {
    next = _n;      
  }
  
  public void setPrev(Waypoint _p)
  {
    previous = _p;
  }
  
  public int getPosX()
  {
    return x;
  }
  
  public int getPosY()
  {
    return y;
  }
  
  public int getPosZ()
  {
    return z;
  }
  
  public int getID()
  {
    return id;
  }
  
  public Waypoint getNext()
  {
    return next;
  }
  
  public Waypoint getPrev()
  {
    return previous;
  }
  
  
  void drawInMap(int x, int y) //x/y given in mm!
  {
    p.stroke(255, 255, 0); //Robot position/direction arrow
    p.noFill();
   // ellipse((int)((mpd_rob_x/mpd_map_resolution_mm) / mapScaleFacX), (int)(((mpd_map_size_Y - mpd_rob_y) / mpd_map_resolution_mm) / mapScaleFacY), 40, 40);
  }
}
