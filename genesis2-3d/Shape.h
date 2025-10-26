
#ifndef _SHAPE_H
#define _SHAPE_H

#include <vga.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define DISPLAY
#define RWHELPER

#ifdef RWHELPER
#include "RWHelper.h"
#endif

class Shape {

// local variables

#ifdef RWHELPER
RWHelper *rwHelper;
#else
unsigned char *area;
#endif

#ifdef DISPLAY
int displayX;
int displayY;
#endif

int maxx;
int maxy;
int fuseMode;
int diff;
int ownArea;

public:

Shape(int _maxx,int _maxy);
Shape(Shape *);

int init();
void finalize();
void set(int _x, int _y, unsigned char _v);
unsigned char get(int _x, int _y);
void fuse(int _x, int _y, unsigned char _v);

/******************************
 * FuseMode: 0 = set
 *           1 = mittelwert
 *           2 = set wenn groesser
 *           3 = set wenn kleiner
 *****************************/
void setFuseMode(int _mode);
int  getFuseMode();

/*****************************
 * diff wert: wenn > 0, randomize _v, only with fuse
 ****************************/
void setDiff(int _diff);
int  getDiff();

void fuseBox(int _x, int _y, int _b, int _h, unsigned char _v);
void setBox(int _x, int _y, int _b, int _h, unsigned char _v);
void fuseLine(int _xa, int _ya, int _xb, int _yb, unsigned char _v);

int getMaxX();
int getMaxY();

#ifdef RWHELPER
void setRWHelper(char *_path,char *_name,int _type);
RWHelper *getRWHelper();
#else
unsigned char *getArea();
void setLinear(int _a, unsigned char _v);
unsigned char getLinear(int _a);
#endif

#ifdef DISPLAY
void setDisplayXY(int _x, int _y);
int  getDisplayX();
int  getDisplayY();
#endif

};

#endif
