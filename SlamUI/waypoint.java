import processing.core.*;

class Waypoint
{
  private PApplet p;
  
  private int x, y;
  private byte z;
  private int id;
  
  Waypoint(PApplet parent, int _x, int _y, byte _z, int _id)
  {
    p = parent;
    
    x = _x;
    y = _y;
    z = _z;
    id = _id;
  }
  
  Waypoint(PApplet parent)
  {
    p = parent;
    
    x = 0;
    y = 0;
    z = 0;
    id = -1;
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
}
