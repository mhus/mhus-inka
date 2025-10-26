
#ifndef _ARRANGE_H
#define _ARRANGE_H

#include "Area.h"
#include "ConfigHelper.h"

#include <stdlib.h>
#include <stdio.h>
#include <gnome-xml/tree.h>

#define DISPLAY
//#define DEBUG_ARRANGE
//#define WARNINGS_ARRANGE

#ifdef DISPLAY
#include <vga.h>
#endif

class Arrange {

  unsigned char *raster;
  int rasx, rasy;
  Area *areas;
  int warnings; //if not 0 warnings are errors
  
public:

  static const unsigned char WATHER    = 1;

// depricated
  static const unsigned char MOUNTAINS = 2;
  static const unsigned char PLATO     = 3;
  static const unsigned char DESERT    = 4;
  static const unsigned char WOOD      = 5;
  static const unsigned char USE       = 6;
  static const unsigned char MARSH     = 7;
  static const unsigned char LAKES     = 8;
  static const unsigned char ISLANDS   = 9;
// end depricated

  Arrange();
  Arrange(int _rasx, int _rasy);
  ~Arrange();
  
  /*************************************************************
   * _name     name of area
   * _type     type of area
   * _size     number of needed areas
   * _wather   is a requirement for wather at a site
   *************************************************************/
  void addArea(char *_name,
               unsigned char _type,
               int _size,
               int _wather);

  void addArea(Area *_area);

  Area *getFirstArea();

  /*************************************************************
   * DO IT - arange the areas and tell me if all right
   *************************************************************/
  int arrangeAreas();

  /* set warnings are errors */
  void setWarnings(int _warnings);

  // set / get from raster !!  
  void set(int _rx, int _ry, unsigned char _v);
  unsigned char get(int _rx, int _ry);

  //stuff
  int getRasX();
  int getRasY();

  /***************************************************************
   * XML
   **************************************************************/
   static Arrange *fromXML(char *_path);
   static Arrange *fromXML(xmlNodePtr _tree);

};
#endif