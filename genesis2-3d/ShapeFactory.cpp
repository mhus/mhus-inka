#include "ShapeFactory.h"

ShapeFactory::ShapeFactory(Arrange *_arrange, 
                 Shape *_shape) {
  arrange = _arrange;
  shape   = _shape;
  oldType = 0;
};

ShapeFactory::~ShapeFactory() {
};

void ShapeFactory::setArrangePos(int _rxa, int _rya, int _rxb, int _ryb) {
  posxa = _rxa;
  posya = _rya;
  posxb = _rxb;
  posyb = _ryb;
};

void ShapeFactory::setOldPos(int _type, int _xb, int _yb) {
  oldType = _type;
  oldxb   = _xb;
  oldyb   = _yb;
};

void ShapeFactory::generateShape(int xa,
                                int ya,
                                int xb,
                                int yb,
                                int _type, 
                                int _anz, 
                                int _int, 
                                int _base,int _min,int _max,int _diff) {


int b, a;
int xadd, yadd, x, y, w;
unsigned char f;

#ifdef DEBUG
  printf("SHAPE: AT %i %i %i %i\n    TYPE %i\n    COL: %i %i %i %i\n",
          xa,ya,xb,yb, _type, _base, _min, _max, _diff);
#endif

//set standart fuse mode to mittelwert
  shape->setFuseMode(1);
  shape->setDiff(-1);

  switch(_type) {

/***********************************************************************
 * Simple Background
 **********************************************************************/
    case 1:
      //simple background box
      shape->setDiff(_diff);
      doBackgroundBox(xa, ya, xb-xa, yb-ya, _base);
    break;

/***********************************************************************
 * Simple Shaked Background _int = border, _int==-1 -> all border
 **********************************************************************/
    case 2:
      //simle Shaked box
      shape->setDiff(_diff);
      doShakedBox(xa,ya,xb-xa,yb-ya, _int, 0, _base);
    break;

/***********************************************************************
 * Switching pixel, _anz = anzahl, _int = haeufigkeit
 **********************************************************************/
    case 3:
      //switching
      if (xa>xb) {b=xa;xa=xb;xb=b;}
      if (ya>yb) {b=ya;ya=yb;yb=b;}

      for (a=0;a<_anz;a++)
      for (x=xa;x<=xb;x++)
      for (y=ya;y<=yb;y++) {
        if (rand() % _int == 0) {
          b = rand() % 4;
          xadd = 0; yadd = 0;
          switch(b) {
            case 0: xadd=1;break;
            case 1: xadd=-1;break;
            case 2: yadd=1;break;
            case 3: yadd=-1;break;
          }
          f = shape->get(x+xadd,y+yadd);
          shape->set(x+xadd,y+yadd,shape->get(x,y));
          shape->set(x,y,f);
        }
      }
    break;

/***********************************************************************
 * Soften Raster in 4 zuegen
 **********************************************************************/
    case 10:
      //simple soften box
      shape->setDiff(_diff);
      for (a=0;a<_anz;a++) {
        doSoftenRaster(xa, ya, xb-xa, yb-ya);
        doSoftenRaster(xa+1, ya, xb-xa-1, yb-ya);
        doSoftenRaster(xa+1, ya+1, xb-xa-1, yb-ya-1);
        doSoftenRaster(xa, ya+1, xb-xa, yb-ya-1);
      }
    break;

/***********************************************************************
 * Soften einen bereich
 **********************************************************************/
    case 11:
      //simple soften box
      shape->setDiff(_diff);
      doSoften(xa, ya, xb-xa, yb-ya);
    break;

/***********************************************************************
 * Ebene flaeche, anz = tiefe (anz. childs)
 **********************************************************************/
    case 12:
      //simple spider
      if ((xb-xa)>(yb-ya)) {
        b = (yb-ya);
        xadd = b*2/3;yadd = 0;
        x = xa + b/2; y = ya + b/2;
        
      } else {
        b = (xb-xa); //breite
        xadd = 0; yadd = b*2/3;
        x = xa + b/2; y = ya + b/2;
       
      }

      b = b/2;

#ifdef DEBUG
      printf("    B %i XYADD %i %i XY %i %i\n",b,xadd,yadd,x,y);
#endif
      while (x>xa && x<xb && y>ya && y<yb) {
        doBackgroundSpider(x,y, 1,
                                  b/_anz + _int*_anz , b/_anz , _int , _anz,
                                  0,
                                  _base, _min, _max, _diff);

        x+=xadd;
        y+=yadd;
      }
    break;

/***********************************************************************
 * Simple Mountains von einer ecke zur anderen,
 * _anz = anzahl, neg. wird undo mountain mit eingeschalten
 **********************************************************************/
    case 13:
      //simple mountains
      for (int a=0;a<_anz;a++) {
        doMountain(xa,ya,xb,yb, 
                     20, 3, 2, abs(_int),
                     _base,_min,_max,_diff);
        if (_int<0) {
          doMountain(xa,ya,xb,yb,
                       20, 3, 2, abs(_int),
                       0,0,0,1);
        }
      }
    break;

/***********************************************************************
 * Mountain von links nach rechts,
 * _anz = anzahl, neg. wird undo mountain mit eingeschalten
 **********************************************************************/
    case 14:

      //simple mountains
      for (int a=0;a<_anz*((xb-xa)/100);a++) {
        doMountain(xa+a*100/_anz,ya,xa+a*100/_anz,yb,   
                     20, 3, 2, abs(_int),
                     _base,_min,_max,_diff);

        if (_int<0) {
          doMountain(xa+a*100/_anz,ya,xa+a*100/_anz,yb,
                       20, 3, 2, abs(_int),
                       1,1,1,1);
        }
      }
    break;


/***********************************************************************
 * Mountain von oben nach unten,
 * _anz = anzahl, neg. wird undo mountain mit eingeschalten
 **********************************************************************/
    case 15:

      //simple mountains
      for (int a=0;a<_anz*((yb-ya)/100);a++) {
        doMountain(xa,ya+a*100/_anz,xb,ya+a*100/_anz,   
                     20, 3, 2, abs(_int),
                     _base,_min,_max,_diff);

        if (_int<0) {
          doMountain(xa,ya+a*100/_anz,xb,ya+a*100/_anz,
                       20, 3, 2, abs(_int),
                       1,1,1,1);
        }
      }
    break;

/***********************************************************************
 * Mountain von mitte nach aussen
 * _anz = anzahl, neg. wird undo mountain mit eingeschalten
 **********************************************************************/
    case 16:
      xadd = (xb-xa)/2 +xa;
      yadd = (yb-ya)/2 +ya;

      for (a=0;a<_anz*((xb-xa)/100);a++) {
        //simple links
        doMountain(xa+a*100/_anz,ya,xadd,yadd,   
                     20, 3, 2, abs(_int),
                     _base,_min,_max,_diff);
        //simple rechts
        doMountain(xa+a*100/_anz,yb,xadd,yadd,   
                     20, 3, 2, abs(_int),
                     _base,_min,_max,_diff);

        if (_int<0) {
          doMountain(xa+a*100/_anz,ya,xadd,yadd,
                       20, 3, 2, abs(_int),
                       0,0,0,1);
          doMountain(xa+a*100/_anz,yb,xadd,yadd,
                       20, 3, 2, abs(_int),
                       1,1,1,1);
        }
      }
      for (a=0;a<_anz*((yb-ya)/100);a++) {
        //links
        doMountain(xa,ya+a*100/_anz,xadd,yadd,
                     20, 3, 2, abs(_int),
                     _base,_min,_max,_diff);
        //rechts
        doMountain(xb,ya+a*100/_anz,xadd,yadd,   
                     20, 3, 2, abs(_int),
                     _base,_min,_max,_diff);

        if (_int<0) {
          doMountain(xa,ya+a*100/_anz,xadd,yadd,
                       20, 3, 2, abs(_int),
                       0,0,0,1);
          doMountain(xb,ya+a*100/_anz,xadd,yadd,
                       20, 3, 2, abs(_int),
                       1,1,1,1);
        }
      }

    break;

/***********************************************************************
 * Soften Wather - Raster in 4 zuegen
 **********************************************************************/
    case 17:
      //wather soften box
      shape->setDiff(_diff);
      for (a=0;a<_anz;a++) {
        doSoftenWatherRaster(xa-50, ya-50, xb-xa+100, yb-ya+100,_int);
        doSoftenWatherRaster(xa+1-50, ya-50, xb-xa-1+100, yb-ya+100,_int);
        doSoftenWatherRaster(xa+1-50, ya+1-50, xb-xa-1+100, yb-ya-1+100,_int);
        doSoftenWatherRaster(xa-50, ya+1-50, xb-xa+100, yb-ya-1+100,_int);
      }
    break;

/***********************************************************************
 * Soften Wather - Raster in 4 zuegen - fuer finalRendering
 **********************************************************************/
    case 18:
      //wather soften box
      shape->setDiff(_diff);
      for (a=0;a<_anz;a++) {
        doSoftenWatherRaster(xa, ya, xb-xa, yb-ya,_int);
        doSoftenWatherRaster(xa+1, ya, xb-xa-1, yb-ya,_int);
        doSoftenWatherRaster(xa+1, ya+1, xb-xa-1, yb-ya-1,_int);
        doSoftenWatherRaster(xa, ya+1, xb-xa, yb-ya-1,_int);
      }
      doSoften(xa, ya, xb-xa, yb-ya);
    break;

/***********************************************************************
 * Set Pixel Random Positions
 **********************************************************************/
    case 19:
      if (xa>xb) {x=xa;xa=xb;xb=x;}
      if (ya>yb) {y=ya;ya=yb;yb=y;}
      for (a=0;a<_anz;a++) {
        for (x = xa; x <= xb; x++)
        for (y = ya; y <= yb; y++)
        if (rand() % _int == 0) shape->set(x,y,_base);
      }
    break;

/***********************************************************************
 * Spitzer mittelberg (exponential)
 * _int mittelradius
 * _base hoehe
 **********************************************************************/
    case 41:
      shape->setFuseMode(2);

      xadd = (xb-xa)/2;
      yadd = (yb-ya)/2;

      for (a = 1; a<_int;a++)
      for (float w = 0;w<3.15*2;w=w+0.4) {
        if (a<_anz) {
          shape->setDiff(0);
          shape->fuseLine((int)(cos(w) * (xadd*a/_int)) + xadd + xa,
                        (int)(sin(w) * (yadd*a/_int)) + yadd + ya,
                        (int)(cos(w+0.4) * (xadd*a/_int)) + xadd + xa,
                        (int)(sin(w+0.4) * (yadd*a/_int)) + yadd + ya,
                        _base);          
        } else {
          shape->setDiff(_diff);
          shape->fuseLine((int)(cos(w) * (xadd*a/_int)) + xadd + xa,
                        (int)(sin(w) * (yadd*a/_int)) + yadd + ya,
                        (int)(cos(w+0.4) * (xadd*a/_int)) + xadd + xa,
                        (int)(sin(w+0.4) * (yadd*a/_int)) + yadd + ya,
                        _base - (_base*a/_int));
        }  
      }
    break;

/***********************************************************************
 * Senke fuer see (quadratisch, oder sin)
 * _int  mittelradius
 * _base tiefe
 **********************************************************************/
    case 42:
      shape->setFuseMode(3);

      xadd = (xb-xa)/2;
      yadd = (yb-ya)/2;

      for (a = 1; a<_int;a++)
      for (float w = 0;w<3.15*2;w=w+0.4) {
        if (a<_anz) {
          shape->setDiff(0);
          shape->fuseLine((int)(cos(w) * (xadd*a/_int)) + xadd + xa,
                        (int)(sin(w) * (yadd*a/_int)) + yadd + ya,
                        (int)(cos(w+0.4) * (xadd*a/_int)) + xadd + xa,
                        (int)(sin(w+0.4) * (yadd*a/_int)) + yadd + ya,
                        _min);
        } else {
          shape->setDiff(_diff);
          shape->fuseLine((int)(cos(w) * (xadd*a/_int)) + xadd + xa,
                        (int)(sin(w) * (yadd*a/_int)) + yadd + ya,
                        (int)(cos(w+0.4) * (xadd*a/_int)) + xadd + xa,
                        (int)(sin(w+0.4) * (yadd*a/_int)) + yadd + ya,
                        (_base-_min)*a/_int + _min);
        }  
      }

    break;

/***********************************************************************
 * Teil eines apfelbaums :)
 **********************************************************************/
    case 43:

    break;


/***********************************************************************
 * Krater
 **********************************************************************/
    case 44:
      xadd = (xb-xa)/2;
      yadd = (yb-ya)/2;
      shape->setFuseMode(0);
      shape->setDiff(_diff);

      for (float w = 0; w < 3.15*2; w = w + 0.1)
        shape->fuseLine(cos(w)*xadd + xadd + xa, sin(w)*yadd + yadd + ya,
                        cos(w+0.1)*xadd + xadd + xa, sin(w+0.1)*yadd + yadd + ya,
                        _base);

      for (a=0;a<_anz;a++) {
        x = (rand() % (xb - xa)) + xa;
        y = (rand() % (yb - ya)) + ya;
        xadd = yadd = (rand() % _int) + 1;
        f = (rand() % (_max - _min)) + _min;

        for (float w = 0; w < 3.15*2; w = w + 0.1)
        shape->fuseLine(cos(w)*xadd + x, sin(w)*yadd + y,
                        cos(w+0.1)*xadd + x, sin(w+0.1)*yadd + y, f);

      }
    break;
/***********************************************************************
 * Inseln
 **********************************************************************/
    case 45:
      xadd = (xb-xa)/2;
      yadd = (yb-ya)/2;
      shape->setFuseMode(2);
      shape->setDiff(_diff);


      for (b=1;b<xadd;b++)
      for (float w = 0; w < 3.15*2; w = w + 0.1)
        shape->fuseLine(cos(w)*(_int*b/xadd) + xadd + xa, 
                        sin(w)*(_int*b/xadd) + yadd + ya,
                        cos(w+0.1)*(_int*b/xadd) + xadd + xa, 
                        sin(w+0.1)*(_int*b/xadd) + yadd + ya,
                        _base - (_base*b/xadd));

      for (a=0;a<_anz;a++) {
        x = (rand() % (_int*2)) + xa + (xb-xa)/2 - _int;
        y = (rand() % (_int*2)) + ya + (yb-ya)/2 - _int;
        xadd = (rand() % (_int/3)) + 1;
        yadd = (rand() % (_int/3)) + 1;

        f = (rand() % (_max - _min)) + _min;

        for (b=1;b<xadd;b++)
        for (float w = 0; w < 3.15*2; w = w + 0.1)
          shape->fuseLine(cos(w)*(xadd*b/xadd) + xadd + x, 
                        sin(w)*(yadd*b/xadd) + yadd + y,
                        cos(w+0.1)*(xadd*b/xadd) + xadd + x, 
                        sin(w+0.1)*(yadd*b/xadd) + yadd + y,
                        _base - (_base*b/xadd));
      }
    break;
/***********************************************************************
 * Seen
 **********************************************************************/
    case 46:
      xadd = (xb-xa)/2;
      yadd = (yb-ya)/2;
      shape->setFuseMode(3);
      shape->setDiff(_diff);


      for (b=1;b<xadd;b++)
      for (float w = 0; w < 3.15*2; w = w + 0.1)
        shape->fuseLine(cos(w)*(_int*b/xadd) + xadd + xa, 
                        sin(w)*(_int*b/xadd) + yadd + ya,
                        cos(w+0.1)*(_int*b/xadd) + xadd + xa, 
                        sin(w+0.1)*(_int*b/xadd) + yadd + ya,
                        (_base-_min)*b/xadd + _min);

      for (a=0;a<_anz;a++) {
        x = (rand() % (_int*2)) + xa + (xb-xa)/2 - _int;
        y = (rand() % (_int*2)) + ya + (yb-ya)/2 - _int;
        xadd = yadd = (rand() % (_int/3)) + 1;
        f = (rand() % (_max - _min)) + _min;

        for (b=1;b<xadd;b++)
        for (float w = 0; w < 3.15*2; w = w + 0.1)
          shape->fuseLine(cos(w)*(xadd*b/xadd) + xadd + x, 
                        sin(w)*(yadd*b/xadd) + yadd + y,
                        cos(w+0.1)*(xadd*b/xadd) + xadd + x, 
                        sin(w+0.1)*(yadd*b/xadd) + yadd + y,
                        (_base - _min)*b/xadd + _min);
      }
    break;

    case 47:

      if (oldType == arrange->get(posxa,posya)) {
        xa = oldxb;
        ya = oldyb;
      }

      //simple mountains
      for (int a=0;a<_anz;a++) {
        doMountain(xa,ya,xb,yb, 
                     20, 3, 2, abs(_int),
                     _base,_min,_max,_diff);
        if (_int<0) {
          doMountain(xa,ya,xb,yb,
                       20, 3, 2, abs(_int),
                       0,0,0,1);
        }
      }
    
    break;

  } //switch

};

/**************************************************************************/

void ShapeFactory::doBackgroundBox(int _x, int _y, int _b, int _h, 
                   unsigned char _base) {
  int xa,xb,ya,yb;
  int x,y;
  
//  if (_x>maxx || _y>maxy || _h<1 || _b<1 || _x+_b<0 || _y+_h<0) return;
  
  xa = (_x<0) ? 0 : _x;
  ya = (_y<0) ? 0 : _y;
  
  xb = _x+_b;
  yb = _y+_h;
  
  //do it
  for (x=xa;x<xb;x++)
  for (y=ya;y<yb;y++) {
    shape->fuse(x,y,_base);
  }   

}

/**************************************************************************/

void ShapeFactory::doShakedBox(int _x, int _y, int _b, int _h,
                 int _border, unsigned char _def,
                   unsigned char _base) {
  int xa,xb,ya,yb;
  int x,y;
  
//  if (_x>maxx || _y>maxy || _h<1 || _b<1 || _x+_b<0 || _y+_h<0) return;
  
  xa = (_x<0) ? 0 : _x;
  ya = (_y<0) ? 0 : _y;
  
  xb = _x+_b;
  yb = _y+_h;
  
  //do it
  for (x=xa;x<xb;x++)
  for (y=ya;y<yb;y++) {
    if (((x-xa)<_border || (y-ya)<_border || 
         (xb-x)<_border || (yb-y)<_border || _border==-1) &&
        (rand() % 2 == 0)) {
    } else {
      if (shape->get(x,y) == _def)
        shape->set(x,y,_base);
   }
  }   
};

/**************************************************************************/

void ShapeFactory::doBackgroundSpider(int _x, int _y, int _b,int _steps, int _nextsteps,
                         int _childsub, int _childs, int _direction,
                         unsigned char _base, unsigned char _min,
                         unsigned char _max,unsigned char _diff) {

if (_x<0 || _y<0) return; //dont work elsewhere

  _base+=(rand() % _diff*2) - (_diff-1);
  if (_base > _max) _base=_max;
  if (_base < _min) _base=_min;
  shape->fuseBox(_x,_y,_b,_b,_base);
  
  if (_steps == 0) {
    if (_childs == 0) return;
    doBackgroundSpider(_x,_y,_b,_nextsteps,_nextsteps,_childsub,_childs-1,_direction,_base,_min,_max,_diff);
    if (_nextsteps - _childsub >0) _nextsteps-=_childsub;
    doBackgroundSpider(_x,_y,_b,_nextsteps,_nextsteps,_childsub,_childs-1,0,_base,_min,_max,_diff); 
  } else {
    if (_direction == 0) {
      doBackgroundSpider(_x,_y,_b,_steps,_nextsteps,_childsub,_childs,1,_base,_min,_max,_diff);
      doBackgroundSpider(_x,_y,_b,_steps,_nextsteps,_childsub,_childs,2,_base,_min,_max,_diff);
      doBackgroundSpider(_x,_y,_b,_steps,_nextsteps,_childsub,_childs,3,_base,_min,_max,_diff);
      doBackgroundSpider(_x,_y,_b,_steps,_nextsteps,_childsub,_childs,4,_base,_min,_max,_diff);
      doBackgroundSpider(_x,_y,_b,_steps,_nextsteps,_childsub,_childs,5,_base,_min,_max,_diff);
      doBackgroundSpider(_x,_y,_b,_steps,_nextsteps,_childsub,_childs,6,_base,_min,_max,_diff);
      doBackgroundSpider(_x,_y,_b,_steps,_nextsteps,_childsub,_childs,7,_base,_min,_max,_diff);
      doBackgroundSpider(_x,_y,_b,_steps,_nextsteps,_childsub,_childs,8,_base,_min,_max,_diff);
    } else {
      switch (_direction) {
        case 1: _x++;break;
        case 2: _y++;break;
        case 3: _x--;break;
        case 4: _y--;break;
        case 5: _x++;_y++;break;
        case 6: _x++;_y--;break;
        case 7: _x--;_y++;break;
        case 8: _x--;_y--;break;
      }
      doBackgroundSpider(_x,_y,_b,_steps-1,_nextsteps,_childsub,_childs,_direction,_base,_min,_max,_diff);
    }
  }
}

/**************************************************************************/

void ShapeFactory::doSoften(int _x, int _y, int _b, int _h) {
  int x,y,i,j,w;

  for(x=_x;x<_x+_b;x++)
  for(y=_y;y<_y+_h;y++) {
  
    w=0;
    for(i=-1;i<2;i++)
    for(j=-1;j<2;j++) {
      w=w+shape->get(x+i,y+j);
    }

    shape->set(x,y,w/9);
  }
};

/**************************************************************************/

void ShapeFactory::doMountain(int _xa, int _ya, int _xb, int _yb, int _midlen,
                 int _childs, int _lensub, int _direction,
                 unsigned char _base, unsigned char _min, unsigned char _max,
                 unsigned char _diff) {

  int x,y,l;
  double w;

  unsigned char f;

if (_xa<0 || _ya<0 || _xb<0 || _yb<0) return; //dont work elsewhere

  //calculate new target

  l = (int)sqrt( (double)(((_ya-_yb)*(_ya-_yb)) + 
                     ((_xa-_xb)*(_xa-_xb))) );
  w = atan( ((double)(_ya-_yb)) / ((double)(_xa-_xb)) );
  if (_xa>_xb) w=w+M_PI;

  if (l>_midlen) {
    l = _midlen;
    l = rand() % _direction - _direction/2 + l; //neue laenge
    w = (double)(rand() % _direction - _direction/2) / 10 + w;

    x = _xa + (int)(cos(w)*l);
    y = _ya + (int)(sin(w)*l);
  } else {
    x = _xb;
    y = _yb;
  }

  //draw it

  f = _base + rand() % _diff*2 - _diff;

  if (f>_max) f=_max;
  if (f<_min) f=_min;

  shape->fuseLine(_xa,_ya,x,y,f);

  //childs

  if (_midlen-_lensub>1 && _childs-1>0) {
    for (int i=0;i<_childs;i++) {
      doMountain(_xa,_ya,_xa+(int)(cos(w)*l*3),_ya+(int)(sin(w)*l*3),
                  _midlen-_lensub, _childs-1, 0,
                  _direction, f, _min, _max, _diff);
    }
  }

  //Main Child
  if (_lensub != 0)
    if (_xa != _xb || _ya != _yb)
      doMountain(x,y,_xb,_yb,_midlen,_childs,_lensub,
                  _direction,f,_min,_max,_diff);

};

void ShapeFactory::doSoftenRaster(int _x, int _y, int _b, int _h) {

  int w;

  for (int x=0;x<_b;x=x+2)
  for (int y=0;y<_h;y=y+2) {
    w = 0;
    for (int i=-1;i<2;i++)
    for (int j=-1;j<2;j++) w = w + shape->get(_x+x+i,_y+y+j);
    w = w/9;
    shape->set(_x+x,_y+y,w);
  }

};

void ShapeFactory::doSoftenWatherRaster(int _x, int _y, int _b, int _h, int _int) {

  int w, b;

  for (int x=0;x<_b;x=x+2)
  for (int y=0;y<_h;y=y+2) {
    w = 0; b = 0;
    for (int i=-1;i<2;i++)
    for (int j=-1;j<2;j++) {
      w = w + shape->get(_x+x+i,_y+y+j);
      if ( shape->get(_x+x+i, _y+y+j) == 0) b++;
    }
    w = w/15;
    if ((b != 0) && (b != 9)) {
      if (b < 4 ) {
        if (rand() > RAND_MAX/_int)
          shape->set(_x+x,_y+y,w);
        else
          shape->set(_x+x,_y+y,0);
      } else {
        shape->set(_x+x,_y+y,0);
      }
    }
  }

};

void ShapeFactory::removeSee(int _xa, int _ya, int _xb, int _yb) {

  for (int x = _xa;x<=_xb;x++)
  for (int y = _ya;y<=_yb;y++)
  if (shape->get(x,y) == 0) shape->set(x,y,1);

};

void ShapeFactory::setSeeBorder(int _xa,int _ya, int _xb, int _yb) {

  for (int x = _xa;x<=_xb;x++)
  for (int y = _ya;y<=_yb;y++)
  if (rand() < RAND_MAX/10) shape->set(x,y,0);

};

