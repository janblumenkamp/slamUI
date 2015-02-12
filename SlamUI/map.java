import processing.core.*;

class Map
{
  private PApplet p;
  PImage img;
  private Comm c;
  
  private int mapX; //Start position of the map
  private int mapY;
  private int scaledWidth; //Size of the map in pixel to what we want to scale it
  private int scaledHeight;
  private float scaleFacX;
  private float scaleFacY;
  
  private int rob_scaleFac; //Scale factor of robot arrow in relation to map size
  private int wp_scaleFac; //Scale factor of waypoint in relation to map size
  
  Waypoint wpHovered; //Hovered waypoint
  
  Map(PApplet parent, int _x, int _y, int w, int h)
  {
    p = parent;
    
    mapX = _x;
    mapY = _y;
    scaledWidth = w;
    scaledHeight = h;
    
    rob_scaleFac = 15;
    wp_scaleFac = 10;
    
    img = p.createImage(300, 300, p.GRAY);
  }
  
  public void setComm(Comm comm) //Set comm after connection was established!
  {
    c = comm;
  }
  
  public void setPosX(int _x)
  {
    mapX = _x;
  }
  
  public void setPosY(int _y)
  {
    mapY = _y;
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
    return mapX;
  }
  
  public int getPosY()
  {
    return mapY;
  }
  
  private void drawWP(Waypoint w) //x/y given in mm!
  {
    int x = (int)((w.getPosX()/c.getMapResolutionMM()) / scaleFacX);
    int y = (int)(((c.getMapSizeYMM() - w.getPosY()) / c.getMapResolutionMM()) / scaleFacY);
    int a = (int)(wp_scaleFac / scaleFacX);
    int b = (int)(wp_scaleFac / scaleFacY);
    
    p.stroke(255, 255, 0); 
    if(w == wpHovered)
      p.fill(p.color(255, 0, 0));
    else
      p.noFill();
    p.ellipse(x, y, a, b);
  }
  
  private boolean checkWPHovered(Waypoint w)
  {
    int x = (int)((w.getPosX()/c.getMapResolutionMM()) / scaleFacX);
    int y = (int)(((c.getMapSizeYMM() - w.getPosY()) / c.getMapResolutionMM()) / scaleFacY);
    int a = (int)(wp_scaleFac / scaleFacX);
    int b = (int)(wp_scaleFac / scaleFacY);
      
    return ellIsHovered(x, y, a, b);
  }
  
  private void connectWP(Waypoint a, Waypoint b) //Draws a line between this two waypoints
  {
    if(a != null && b != null)
    {
      p.stroke(0, 0, 255);
      p.line(mapX + (int)((a.getPosX()/c.getMapResolutionMM()) / scaleFacX),
                 mapY + (int)(((c.getMapSizeYMM() - a.getPosY()) / c.getMapResolutionMM()) / scaleFacY),
                 mapX + (int)((b.getPosX()/c.getMapResolutionMM()) / scaleFacX),
                 mapY + (int)(((c.getMapSizeYMM() - b.getPosY()) / c.getMapResolutionMM()) / scaleFacY));
    }
  }
  
  private void drawRobArr()
  {
    p.stroke(255, 0, 0); //Robot position/direction arrow
    p.line(mapX + (int)(c.getRobPosXPx() / scaleFacX),
         mapY + (int)((c.getMapSizeYPx() - c.getRobPosYPx()) / scaleFacY),
         mapX + (int)(c.getRobPosXPx() / scaleFacX) + (rob_scaleFac / scaleFacX) * p.sin((70 + c.getRobOrientation()) * (p.PI / 180)),
         mapX + (int)((c.getMapSizeYPx() - c.getRobPosYPx()) / scaleFacY) - (rob_scaleFac / scaleFacY) * p.cos((70 + c.getRobOrientation()) * (p.PI / 180)));
    p.line(mapX + (int)(c.getRobPosXPx() / scaleFacX),
         mapY + (int)((c.getMapSizeYPx() - c.getRobPosYPx()) / scaleFacY),
         mapX + (int)(c.getRobPosXPx() / scaleFacX) + (rob_scaleFac / scaleFacX) * p.sin((110 + c.getRobOrientation()) * (p.PI / 180)),
         mapY + (int)((c.getMapSizeYPx() - c.getRobPosYPx()) / scaleFacY) - (rob_scaleFac / scaleFacY) * p.cos((110 + c.getRobOrientation()) * (p.PI / 180)));
  }
  
  private boolean ellIsHovered(int _x, int _y, int a, int b) //Is the mouse in an area around r pixels around x/y?
  {
    float x = p.mouseX - _x;
    float y = p.mouseY - _y;
    a /= 2;
    b /= 2;
    
    if((((x * x) / (a * a)) + ((y * y) / (b * b))) < 1)
      return true;
    else
      return false;
  }
  
  public void display()
  {
    if((c.getMapSizeXMM() != 0) && (c.getMapSizeYMM() != 0))
    {
      img.resize(c.getMapSizeXPx(), c.getMapSizeYPx());
      img.loadPixels();
      for (int y = 0; y < c.getMapSizeYPx(); y++)
      {
        for (int x = 0; x < c.getMapSizeXPx(); x++)
        {
          int pos = (y * c.getMapSizeYPx()) + x;
          if(pos < img.pixels.length)
            img.pixels[pos] = p.color(255 - (int)c.getVarAt(x, c.getMapSizeYPx() - 1 - y, 0)); 
        }
      }
      img.updatePixels();
      img.resize(scaledWidth, scaledHeight);
      p.image(img, mapX, mapY);
    
      drawRobArr();
      
      wpHovered = null; //Assume no waypoint is hovered
        
      if(c.getWP(0) != null) //Startpoint exists!
      {
        for(int i = 0; i < c.getWPamount(); i++)
        {
          if(checkWPHovered(c.getWP(i))) //In case a wp is hovered, set wpHovered to that object
            wpHovered = c.getWP(i);
          
          drawWP(c.getWP(i));
          if(i > 0)
            connectWP(c.getWP(i-1), c.getWP(i));
        }
      }
    }
  }
  
  public void mouseDragged() //HAS TO BE CALLED FROM PApplet mouseClicked!
  {
    //c.setReceiveWP(false); //Dont receive Waypoints anymore
    if(wpHovered != null) //Comm connection established?
    {
      wpHovered.setPosX((int)((p.mouseX - mapX) * scaleFacX) * c.getMapResolutionMM());
      wpHovered.setPosY((int)(c.getMapSizeYPx() - (p.mouseY - mapY) * scaleFacY) * c.getMapResolutionMM());
    }
  }
  
  public void mouseClicked() //HAS TO BE CALLED FROM PApplet mouseClicked!
  {
    if(wpHovered != null) //Comm connection established?
    {
      if(p.mouseButton == p.RIGHT)
      {
        c.removeWPofID(wpHovered.getID());
        c.sendWPList();
      }
    }
  }
  
  public void mouseReleased() //HAS TO BE CALLED FROM PApplet mouseReleased!
  {
    if(wpHovered == null) //No waypoint was selected -> Add Waypoint!
    {
      c.addWP((int)((p.mouseX - mapX) * scaleFacX) * c.getMapResolutionMM(),
              (int)(c.getMapSizeYPx() - (p.mouseY - mapY) * scaleFacY) * c.getMapResolutionMM(),
              c.getRobPosZ());
    }
      
    c.sendWPList();
  }
}
