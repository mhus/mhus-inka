#ifndef _DISPLAYHELPER_H
#define _DISPLAYHELPER_H

#include "Shape.h"

#include <vga.h>
#include <stdio.h>
#include <stdlib.h>

class DisplayHelper {

private:

 int maxx,maxy;
 Shape *shape;
 int cheat;
 
 unsigned char *vscreen;
 static unsigned char avatarPic[10][10];
 static unsigned char digits[10][5][5];


 void set(int _x, int _y, unsigned char _f);
 void clear();
 void copy();

public:
 DisplayHelper();
 DisplayHelper(int _maxx,int _maxy);
 DisplayHelper(Shape *_shape);
 ~DisplayHelper();

 int init();
 void show3D(int _xp, int _yp, int _b, int _type);
 void show2D(int _xp, int _yp, int _type);
 void loop(int _xp, int _yb, int _type);

 void setShape(Shape *_shape);

};
#endif