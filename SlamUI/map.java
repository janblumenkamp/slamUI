import processing.core.*;

class Map
{
  private PApplet p;
  private Comm c;
  
  private int x; //Start position of the map
  private int y;
  private int scaledWidth; //Size of the map in pixel to what we want to scale it
  private int scaledHeight;
  private float scaleFacX;
  private float scaleFacY;
  
  private int rob_scaleFac; //Scale factor of robot arrow in relation to map size
  private int wp_scaleFac; //Scale factor of waypoint in relation to map size
  
  Map(PApplet parent, Comm comm, int _x, int _y, int w, int h)
  {
    p = parent;
    c = comm;
    
    x = _x;
    y = _y;
    scaledWidth = w;
    scaledHeight = h;
    
    rob_scaleFac = 15;
    wp_scaleFac = 5;
  }
  
  public void setPosX(int _x)
  {
    x = _x;
  }
  
  public void setPosY(int _y)
  {
    y = _y;
  }
  
  public void setScaledWidth(int w)
  {
    scaledWidth = w;
    scaleFacX = ((float)c.getMapSizeXPx() / scaledWidth);
  }
  
  public void setScaledHeight(int h)
  {
    scaledHeight = h;
    scaleFacY = ((float)c.getMapSizeYPx() / scaledHeight);
  }
  
  public void setScaledSizes(int h, int w)
  {
    scaledWidth = w;
    scaledHeight = h;
    scaleFacX = ((float)c.getMapSizeXPx() / scaledWidth);
    scaleFacY = ((float)c.getMapSizeYPx() / scaledHeight);
  }
  
  public void setRobScalefac(int f)
  {
    rob_scaleFac = f;
  }
  
  public void setWPScalefac(int f)
  {
    wp_scaleFac = f;
  }
  
  public int getPosX()
  {
    return x;
  }
  
  public int getPosY()
  {
    return y;
  }
  
  private void drawWP(Waypoint w) //x/y given in mm!
  {
    p.stroke(255, 255, 0); 
    p.noFill();
    p.ellipse((int)((w.getPosX()/c.getMapResolutionMM()) / scaleFacX), (int)(((c.getMapSizeYMM() - w.getPosY()) / c.getMapResolutionMM()) / scaleFacY), (int)(wp_scaleFac / scaleFacX), (int)(wp_scaleFac / scaleFacY));
  }
  
  private void connectWP(Waypoint a, Waypoint b) //Draws a line between this two waypoints
  {
    if(a != null && b != null)
    {
      p.stroke(0, 0, 255);
      p.line(x + (int)((a.getPosX()/c.getMapResolutionMM()) / scaleFacX),
                 y + (int)(((c.getMapSizeYMM() - a.getPosY()) / c.getMapResolutionMM()) / scaleFacY),
                 x + (int)((b.getPosX()/c.getMapResolutionMM()) / scaleFacX),
                 y + (int)(((c.getMapSizeYMM() - b.getPosY()) / c.getMapResolutionMM()) / scaleFacY));
    }
  }
  
  private void drawRobArr()
  {
    p.stroke(255, 0, 0); //Robot position/direction arrow
    p.line(x + (int)(c.getRobPosXPx() / scaleFacX),
         y + (int)((c.getMapSizeYPx() - c.getRobPosYPx()) / scaleFacY),
         x + (int)(c.getRobPosXPx() / scaleFacX) + (rob_scaleFac / scaleFacX) * p.sin((70 + c.getRobOrientation()) * (p.PI / 180)),
         x + (int)((c.getMapSizeYPx() - c.getRobPosYPx()) / scaleFacY) - (rob_scaleFac / scaleFacY) * p.cos((70 + c.getRobOrientation()) * (p.PI / 180)));
    p.line(x + (int)(c.getRobPosXPx() / scaleFacX),
         y + (int)((c.getMapSizeYPx() - c.getRobPosYPx()) / scaleFacY),
         x + (int)(c.getRobPosXPx() / scaleFacX) + (rob_scaleFac / scaleFacX) * p.sin((110 + c.getRobOrientation()) * (p.PI / 180)),
         y + (int)((c.getMapSizeYPx() - c.getRobPosYPx()) / scaleFacY) - (rob_scaleFac / scaleFacY) * p.cos((110 + c.getRobOrientation()) * (p.PI / 180)));
  }
  
  public void display()
  {
    if((c.getMapSizeXMM() != 0) && (c.getMapSizeYMM() != 0))
    {
      for (int _y = scaledWidth - 1; _y >= 0; _y--)
      {
        for (int _x = 0; _x < scaledHeight; _x++)
        {
          int x_scaled = (int)(_x * scaleFacX);
          int y_scaled = (c.getMapSizeYPx() - (int)(_y * scaleFacY) - 1);
          
          p.stroke(255 - (int)c.getVarAt(x_scaled, y_scaled, 0));
          p.fill(255 - (int)c.getVarAt(x_scaled, y_scaled, 0));
          p.point(x + _x, y + _y);
        }
      }
    
      drawRobArr();
      
      Waypoint wpMap = c.getWPstart();
      if(wpMap != null)
      {
        for(int i = 0; i < c.getWPamount(); i++)
        {
          drawWP(wpMap);
          connectWP(wpMap.getPrev(), wpMap);
          
          wpMap = wpMap.getNext();
        }
      }    
    }
  }
}
