
#ifndef _AREA_H
#define _AREA_H

#include <stdlib.h>
#include <gnome-xml/tree.h>

#include "ConfigHelper.h"
#include "Lib.h"

class Area {

  Area *nextArea;

public:

  static const int FWATHER = 1;
  static const int FBREAK  = 2;
  static const int FISLAND = 4;

  struct build_ {
    //build structure
    unsigned char type;
    int size;    // number of fields
    int fWather; // number of wather areas around
    int fBreak; // 0 =none 1 = break 2 = island

    //build shape
    int bgType; //background
    int bgAnz;
    int bgIntensity;
    char *master; //name of master area (if != NULL is ignored in arrange)
    char *slave;  //name of slave, it will loaded too
    
    unsigned char bgDiff;
    unsigned char bgMin;
    unsigned char bgMax;
    unsigned char bgBase;

    int mainType; // Main
    int mainAnz;
    int mainIntensity;
    unsigned char mainDiff;
    unsigned char mainMin;
    unsigned char mainMax;
    unsigned char mainBase;
    
    int bsType; //Border Same Type
    int bsAnz;
    int bsIntensity;
    unsigned char bsDiff;
    unsigned char bsMin;
    unsigned char bsMax;
    unsigned char bsBase;
    int bsOffset;

    int bwType; //Border Wather
    int bwAnz;
    int bwIntensity;
    unsigned char bwDiff;
    unsigned char bwMin;
    unsigned char bwMax;
    unsigned char bwBase;
    int bwOffset;

    int boType; //Border Other Type
    int boAnz;
    int boIntensity;
    unsigned char boDiff;
    unsigned char boMin;
    unsigned char boMax;
    unsigned char boBase;
    int boOffset;
    
    int fType; //Filter
    int fAnz;
    int fIntensity;
    unsigned char fDiff;
    unsigned char fMin;
    unsigned char fMax;
    unsigned char fBase;
    int fOffset;
    
    char *order; //order of BG MAIN BORDER and FILTER
        
  };

  char *name;
  int rasterx,rastery,rasterb,rasterh;
  int rasterpart;
  struct build_ build;

  int xa,xb,ya,yb;


  Area();
  Area(char *_name,
       unsigned char _type, 
       int _size,
       int _flags);
  
  ~Area();
  void addArea(Area *_a);
  Area *getNextArea();

/*****************
 * XML
 ****************/
 
 static Area *fromXML(xmlNodePtr _element);
 void toXML(xmlNodePtr _parent);
  
};
#endif