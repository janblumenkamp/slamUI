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
  
}
