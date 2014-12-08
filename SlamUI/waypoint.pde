class Waypoint
{
  int x, y;
  byte z;
  int id, id_next, id_prev;
  Waypoint previous, next;
  
  Waypoint(int _x, int _y, byte _z, int _id, int _id_next, int _id_prev)
  {
    x = _x;
    y = _y;
    z = _z;
    id = _id;
    id_next = _id_next;
    id_prev = _id_prev;
  }
  
  void drawInMap(int x, int y) //x/y given in mm!
  {
    stroke(255, 255, 0); //Robot position/direction arrow
    noFill();
   // ellipse((int)((mpd_rob_x/mpd_map_resolution_mm) / mapScaleFacX), (int)(((mpd_map_size_Y - mpd_rob_y) / mpd_map_resolution_mm) / mapScaleFacY), 40, 40);
  }
}
