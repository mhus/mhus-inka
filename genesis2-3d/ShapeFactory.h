#ifndef _SHAPEFACTORY_H
#define _SHAPEFACTORY_H

#include "Shape.h"
#include "Arrange.h"
#include <stdlib.h>
#include <stdio.h>

#define DEBUG

class ShapeFactory {

  Arrange *arrange;
  Shape   *shape;

  int posxa,posya,posxb,posyb;
  int oldType, oldxb, oldyb;
    
public:  
  ShapeFactory(Arrange *_arrange, Shape *_shape);
  ~ShapeFactory();

  void generateShape(  int _xa,
                       int _ya,
                       int _xb,
                       int _yb,
                       int _type, 
                       int _anz, 
                       int _int, 
                       int _base,int _min, int _max, int _diff);
   
   void setArrangePos(int _xa, int _ya, int _xb, int _yb);
   void setOldPos(int _type, int _xb, int _yb);
   
/**************************************************************************/

void doBackgroundBox(int _x, int _y, int _b, int _h, 
                   unsigned char _base);


/************************************************
 * see doBackgroundBox, it only fills fields with
 * _def and do a shaked border with len _border.
 ***********************************************/

void doShakedBox(int _x, int _y, int _b, int _h,
                 int _border, unsigned char _def,
                   unsigned char _base);
                                     
/************************************************
 * _x,_y  Startpoint (center)
 * _b     Breite der punkte
 * _steps     Anzahl steps bis zum nexten Child
 * _nextsteps Steps fuer nextes Child
 * _childsub  Wird bei jedem neuen child von _stepsnext abgezogen
 * _childs    Anzahl der Childs
 * _direction immer 0
 * _base      Start-Hoehe
 * _min       Min Hoehe
 * _max       Max Hoehe
 * _diff      diff bei jedem schritt
 **************************************************/
void doBackgroundSpider(int _x, int _y, int _b,int _steps, int _nextsteps,
                         int _childsub, int _childs, int _direction,
                         unsigned char _base, unsigned char _min,
                         unsigned char _max,unsigned char _diff);

/**************************************************
 * _xa,_xb Startpoint
 * _xb,_yb Endpoint
 * _midlen Mittlere Lenge der schritte
 * _childs Schilds je schritt
 * _lensub Wird bei neuem child von _midlen subrahiert (bei 0 keine
 *           neuen childs)
 * _direction Zielstrebigkeit (pi*10) 
 * (Siehe oben)
 ***************************************************/
void doMountain(int _xa, int _ya, int _xb, int _yb, int _midlen,
                 int _childs, int _lensub, int _direction,
                 unsigned char _base, unsigned char _min, unsigned char _max,
                 unsigned char _diff);

void doSoften(int _x, int _y, int _b, int _h);
void doSoftenRaster(int _x, int _y, int _b, int _h);
void doSoftenWatherRaster(int _x, int _y, int _b, int _h, int _int);

void removeSee(int _xa, int _ya, int _xb, int _yb);
void setSeeBorder(int _xa, int _ya, int _xb, int _yb);
};
#endif