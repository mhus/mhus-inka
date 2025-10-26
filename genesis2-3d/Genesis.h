#ifndef _GENESIS_H
#define _GENESIS_H

#include "Arrange.h"
#include "Shape.h"
#include "ShapeFactory.h"
#include <gnome-xml/tree.h>
#include <stdlib.h>
#include <stdio.h>
#include <vga.h>
#include <sys/timeb.h>

#define DEBUG

class Genesis {

  Arrange *arrange;
  Shape   *shape;
  ShapeFactory *shapeFactory;
    
  int xpr,ypr;    //x,y per raster
  int arrangeTrials; //how much trials to generate a arrange (0 == needed)
  bool ownTools;  //shold I delete tools
  bool automatic; //create all automatic, no questions
  int xDiffMax, yDiffMax; //diffusion param x,y max
  int xDiffMin, yDiffMin; //diffusion param x,y min
  char *xmlSaveFile;
  int finalShapeRenderer;
  int finalShapeRendererAnz;
  int finalShapeRendererInt;
  
  int xa,ya,xb,yb; // working coordinates
  int bxa,bya,bxb,byb; // working coordinates for border generation  

public:  
  Genesis();
  Genesis(Arrange *_arrange, Shape *_shape, int _xpr, int _ypr);
  ~Genesis();

  void setOwnTools(bool _ot);
  void setAutomatic(bool _auto);
  
  void setDiffMax(int _x, int _y);
  void setDiffMin(int _x, int _y);
  void setXMLSaveFile(char *_fname);
  void setFinalShapeRenderer(int _fsr, int _anz, int _int);
    
  int generateArrange();
  void generateShape(Area *_a);
  void generateShapeBG(int _type, 
                       int _anz, 
                       int _int, 
                       int _base,int _min, int _max, int _diff);
                       
  void generateShapeMain(int _type, 
                         int _anz, 
                         int _int, 
                         int _base,int _min, int _max, int _diff);
  
  void generateShapeB(int _type, 
                      int _anz, 
                      int _int, 
                      int _base,int _min, int _max, int _diff);
  
  void generateShapeF(int _type, 
                      int _anz, 
                      int _int, 
                      int _base,int _min, int _max, int _diff);

  int generateAll();



  static Genesis *fromXML(char *_path);
  static Genesis *fromXML(xmlNodePtr _element);
  void toXML(xmlNodePtr _parent);
};

#endif
