
#include "Shape.h"



Shape::Shape(int _maxx, int _maxy) {
  maxx = _maxx;
  maxy = _maxy;
  fuseMode = 0;
  diff = 0;
  ownArea=1;
  init();
}

Shape::Shape(Shape *_shape) {
 ownArea=0;
#ifdef RWHELPER
 rwHelper = _shape->getRWHelper();
#else
 area = _shape->getArea();
#endif
 maxx = _shape->getMaxX();
 maxy = _shape->getMaxY(); 

#ifdef DISPLAY
  displayX = _shape->getDisplayX();
  displayY = _shape->getDisplayY();
#endif

  fuseMode = _shape->getFuseMode();
  diff     = _shape->getDiff();

 init();
}

int Shape::init() {
  int i;

#ifdef RWHELPER
  if (ownArea==1) {
    rwHelper = new RWHelper("./",
                            "test",
                            RWHelper::ASCII,
                            false);
  }
#else
  if (ownArea==1) {
    // get area mem
    if (!(area = (unsigned char *)malloc(maxx * maxy))) {
      printf("ShapeError: alloc area\n");
      return 1;
    }

    // init area wather = 0
    for (i=0;i<maxx*maxy;i++) setLinear(i,0);
  }
#endif

  return 0;  
}

void Shape::finalize() {

#ifdef RWHELPER
  delete rwHelper;
#else
  if (ownArea==1) free(area);
#endif

}

#ifdef DISPLAY
void Shape::setDisplayXY(int _x, int _y) {
  displayX = _x;
  displayY = _y;

  for (int x=0;x<320;x++)
  for (int y=0;y<400;y++) {
    vga_setcolor(get(x+displayX,y+displayY));
    vga_drawpixel(x,y);
  }

};

int Shape::getDisplayX() {
  return displayX;
};
int Shape::getDisplayY() {
  return displayY;
};

#endif

void Shape::set(int _x, int _y, unsigned char _v) {

#ifdef RWHELPER
    rwHelper->set(_x,_y,_v);
#else
  if (_x>=0 && _y>=0 && _x<maxx && _y <maxy) 
    setLinear(_x + _y * maxx, _v);
#endif
#ifdef DISPLAY
  _x=_x-displayX;
  _y=_y-displayY;
  if (_x>=0 && _y>=0 && _x<320 && _y<400) {
    vga_setcolor(_v);
    vga_drawpixel(_x, _y);
  }
#endif  

}

unsigned char Shape::get(int _x, int _y) {
#ifdef RWHELPER
    return rwHelper->get(_x,_y);
#else
  if (_x>=0 && _y>=0 && _x<maxx && _y <maxy) 
    return getLinear(_x + _y * maxx);
#endif    
  return 0;
}

void Shape::fuse(int _x, int _y, unsigned char _v) {

  //diffuse wert
  if (diff>0) _v = _v + (rand() % diff);

  switch(fuseMode) {
    case 0: set(_x,_y,_v);break;                  //set
    case 1: set(_x,_y, (get(_x,_y)+_v)/2);break; //mittetwert
    case 2: if (get(_x,_y)<_v) set(_x,_y,_v);break; //wenn groesser
    case 3: if (get(_x,_y)>_v) set(_x,_y,_v);break; //wenn kleiner
  }
};

void Shape::setFuseMode(int _mode) {
  fuseMode = _mode;
};

int Shape::getFuseMode() {
  return fuseMode;
};

void Shape::setDiff(int _diff) {
  diff = _diff;
};

int Shape::getDiff() {
  return diff;
};

void Shape::fuseBox(int _x, int _y, int _b, int _h, unsigned char _v) {
  int x,y;
  
  for (x=0;x<_b;x++)
  for (y=0;y<_h;y++)
    fuse(x+_x,y+_y,_v);
}

void Shape::setBox(int _x, int _y, int _b, int _h, unsigned char _v) {
  int x,y;
  
  for (x=0;x<_b;x++)
  for (y=0;y<_h;y++)
    set(x+_x,y+_y,_v);
}

void Shape::fuseLine(int _xa, int _ya, int _xb, int _yb, unsigned char _v) {
  double w;
  int i,l;

  w = atan( (double)(_yb-_ya) / (double)(_xb-_xa) );

  if (_xa>_xb) w=w+M_PI;

  l = (int)sqrt( (double)(((_ya-_yb)*(_ya-_yb)) + 
                     ((_xa-_xb)*(_xa-_xb))) );

  for (i=0;i<l;i++) fuse((int)(cos(w)*i)+_xa,(int)(sin(w)*i)+_ya,_v);  

}

int Shape::getMaxX() {
  return maxx;
}

int Shape::getMaxY() {
  return maxy;
}


#ifdef RWHELPER
void Shape::setRWHelper(char *_path, char *_name, int _type) {
  delete rwHelper;
  rwHelper = new RWHelper(_path,_name,_type,false);
}

RWHelper *Shape::getRWHelper() {
  return rwHelper;
}

#else
unsigned char *Shape::getArea() {
  return area;
}

void Shape::setLinear(int _a, unsigned char _v) {
 if (_a<maxx*maxy && _a>=0)
   area[_a] = _v;


}

unsigned char Shape::getLinear(int _a) {
  if (_a<maxx*maxy && _a>=0)
    return area[_a];

  return 0;
}

#endif

