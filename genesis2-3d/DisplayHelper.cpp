

#include "DisplayHelper.h"


unsigned char DisplayHelper::avatarPic[10][10]=
                                       {{ 0, 0, 0,50,80,80,50, 0, 0, 0},
                                        { 0, 0,50,80,80,80,80,50, 0, 0},
                                        { 0, 0, 0,50,80,80,50, 0, 0, 0},
                                        { 0, 0,50,80,80,80,80,50, 0, 0},
                                        { 0,50,80,50,80,80,50,80,50, 0},
                                        { 0,50,80,50,80,80,50,80,50, 0},
                                        { 0, 0,50,50,80,80,50,50, 0, 0},
                                        { 0, 0, 0,50,80,80,50, 0, 0, 0},
                                        { 0, 0,50,80,50,50,80,50, 0, 0},
                                        { 0,50,80,50, 0, 0,50,80,50, 0}}
                                        ;
                             

unsigned char DisplayHelper::digits[10][5][5]=
{
 {{0,1,1,0,0},
  {1,0,0,1,0},
  {1,0,0,1,0},
  {1,0,0,1,0},
  {0,1,1,0,0}},

 {{0,0,1,1,0},
  {0,1,0,1,0},
  {0,0,0,1,0},
  {0,0,0,1,0},
  {0,0,0,1,0}},

 {{0,1,1,1,0},
  {0,1,0,1,0},
  {0,0,0,1,0},
  {0,0,1,0,0},
  {0,1,1,1,0}},

 {{0,1,1,1,0},
  {0,0,0,1,0},
  {0,0,1,1,0},
  {0,0,0,1,0},
  {0,0,1,1,0}},

 {{0,0,1,1,0},
  {0,1,0,1,0},
  {0,1,1,1,0},
  {0,0,0,1,0},
  {0,0,0,1,0}},

 {{0,1,1,1,0},
  {0,1,0,0,0},
  {0,1,1,0,0},
  {0,0,0,1,0},
  {0,0,1,0,0}},

 {{0,1,0,0,0},
  {0,1,0,0,0},
  {0,1,1,1,0},
  {0,1,0,1,0},
  {0,0,1,0,0}},

 {{0,1,1,1,0},
  {0,0,0,1,0},
  {0,0,1,1,0},
  {0,0,0,1,0},
  {0,0,0,1,0}},

 {{0,1,1,1,0},
  {0,1,0,1,0},
  {0,1,1,1,0},
  {0,1,0,1,0},
  {0,1,1,1,0}},

 {{0,1,1,1,0},
  {0,1,0,1,0},
  {0,1,1,1,0},
  {0,0,0,1,0},
  {0,0,0,1,0}}
};


DisplayHelper::DisplayHelper() {
  maxx=1;
  maxy=1;
  init();
};

DisplayHelper::DisplayHelper(int _maxx,int _maxy) {
  maxx = _maxx;
  maxy = _maxy;
  init();
};

DisplayHelper::DisplayHelper(Shape *_shape) {
  maxx = _shape->getMaxX();
  maxy = _shape->getMaxY();
  shape = _shape;
  init();
};

void DisplayHelper::setShape(Shape *_shape) {
  maxx = _shape->getMaxX();
  maxy = _shape->getMaxY();
  shape = _shape;
};


void DisplayHelper::set(int _x, int _y, unsigned char _f) {

  if (_x>-49 && _y>-49 && _x<350 && _y <450)
    vscreen[_x + 50 + (_y+50) * 400] = _f;
    
/*    
  vga_setcolor(_f);
  vga_drawpixel(_x,_y);
  */
};

void DisplayHelper::clear() {

  int i;
  for(i=0;i<400*500;i++) vscreen[i]=0;
  
};

void DisplayHelper::copy() {

  int i;
  for (i=0;i<400;i++) vga_drawscansegment(&vscreen[i*400 + 50 + 50*400],0,i,320);
  
};



int DisplayHelper::init() {

  int i;

  vga_init();
  vga_setmode(G320x400x256);

  //init palette
  vga_setpalette(0,0,0,40); //meer
  
  for (i=1;i<40;i++)    vga_setpalette(i,0,i+20,0);        //gruen
  for (i=40;i<80;i++)   vga_setpalette(i,i-20,60,0);       //nach gelb
  for (i=80;i<140;i++)  vga_setpalette(i,60,60-(i-80),0);  //nach rot
  for (i=140;i<200;i++) vga_setpalette(i,60,i-140,i-140); //nach weiss
  for (i=200;i<250;i++) vga_setpalette(i,260-i,260-i,260-i); //nach schwarz

  vga_setpalette(1,0,0,50); //meer  

  if (!(vscreen = (unsigned char *)malloc(400*500))) {
    printf("DisplayError: alloc area\n");
    return 1;  
  }

  for (i = 0;i<256;i++) {vga_setcolor(i);vga_drawpixel(i,100);}

//--  vga_getch();

  cheat=0;

  return 0;  
};

DisplayHelper::~DisplayHelper() {
  free(vscreen);

  vga_setmode(TEXT);
};


void DisplayHelper::show3D(int _xp, int _yp, int _b, int _type) {

  int x,y,xg,yg,yt;
  int i,j,f;
  
  int xadd,yadd;
    
  int y1,xg1,yg1,yt1;
  int x2,xg2,yg2,yt2;
  
  int xa,ya,xb,yb;

  int avatary;

  clear();

  avatary = shape->get(_xp,_yp);

  xadd = _b*(_xp + (maxy-_yp)) -160;
  yadd = _b*_yp                -200;

  for(y=_yp-25;y<_yp+25;y++)
  for(x=_xp-25;x<_xp+25;x++) {
    xg = _b*(x + (maxy-y)) -  xadd;
    yg = _b*y -             yadd ;             

    y1=y+1;
    yg1 = _b*y1 -             yadd ;              

    x2=x+1;
    // yg2 = _b*y -             yadd;              
    // yt2 = yg;
  
    if (_type == 0) { //show numbers
    
      if (xg>-49 && xg+_b*2<450 && yg>-49 && yg+_b<450) {
        xa = x;
        for (i=0;i<4;i++) {
          ya = xa % 10;
          for (xb=0;xb<5;xb++)
          for (yb=0;yb<5;yb++) {
            f= (digits[ya][yb][xb]) ? 120 : 10;
            set(xg+xb+(4-i)*5,yg+yb,f);
          }
          xa = xa / 10;
        }

        xa = y;
        for (i=0;i<4;i++) {
          ya = xa % 10;
          for (xb=0;xb<5;xb++)
          for (yb=0;yb<5;yb++) {
            f= (digits[ya][yb][xb]) ? 120 : 10;
            set(xg+xb+(4-i)*5,yg+yb+7,f);
          }
          xa = xa / 10;
        }

      }
    } else if (_type == 2) { //show ground only
      if (xg>-49 && xg+_b*2<450 && yg>-49 && yg+_b<450) {
        yt = yg - shape->get(x,y)      + avatary;
        yt1 = yg1 - shape->get(x,y1)   + avatary;
        yt2 = yg - shape->get(x2,y)    + avatary;
      
      if (xg>-49 && xg+_b*2<450 && yt>-49 && yt+_b<450) {
        f=shape->get(x,y);
        
        for (i=0;i<_b;i++)
        for (j=0;j<_b;j++)
          set(xg+i+(_b-j),yt+j,f);

        for (i=yt+_b;i<yt1;i++)
        for (j=0;j<_b;j++) 
          if (i<450) set(xg+j,i,f);
        
        for (i=yt+_b;i<yt2+_b;i++)
        for (j=0;j<_b;j++) 
          if (i-j<450) set(xg+_b+j,i-j,f); 
        
        vga_setcolor(0);
        for (j=0;j<_b;j++) {
          set(xg+j,yt+_b,0);
          set(xg+j+_b,yt,0);
          set(xg+(_b-j),yt+j,0);
          set(xg+(_b-j)+_b,yt+j,0);
        }
  
      } //if yt
   
      if (x==_xp && y==_yp) { //if avatar
        for (xa=0;xa<_b;xa++)
        for (ya=0;ya<_b;ya++) {
          i = avatarPic[ya*10/_b][xa*10/_b];
          if (i!=0) set(xa+xg+_b/2,ya+yt-_b/2,i);
        }
      } //if avatar

      } //if yg
 
    }  //if type
  } // for x,y

  //mini map

  if (_type == 2) {
    for(x=0;x<50;x++)
    for(y=0;y<100;y++)
      set(x,y+300, shape->get(_xp-25+x,_yp-50+y));
    set(25,300+50,80);
  }

  copy();
};

void DisplayHelper::show2D(int _x, int _y, int _type) {

  clear();

  if (_type==50) {
    _x-=160;
    _y-=200;
    for (int x=0;x<320;x++)
    for (int y=0;y<400;y++) {
      set(x,y,shape->get(x+_x, y+_y));
    }
  } else if (_type==52) {
    _x-=(160*5);
    _y-=(200*5);
    for (int x=0;x<320;x++)
    for (int y=0;y<400;y++) {
      set(x,y,shape->get(x*5+_x, y*5+_y));
    }
  }
  copy();
};


void DisplayHelper::loop(int _xp, int _yp, int _type) {

  int i,x,y;
  int old,oldx,oldy;
  int ende=0;

  //loop
  i=0;x=_xp;y=_yp;
  while (ende==0) { //wait for esc
    
    if (i!=27) {
      if (_type<50) {
        show3D(x,y,15,_type);
      } else {
        show2D(x,y,_type);
      }
    }
    
    i=vga_getkey();
//--    printf("%i\n",i);
    
    if (i==91) { //special
      i = vga_getkey();
//--      printf("Special %i\n\n",i);
      if (_type<50) {

        old = shape->get(x,y);
        oldx=x;oldy=y;

        switch(i) {
          case 65: y=y-1;break;
          case 66: y=y+1;break;
          case 67: x=x+1;break;
          case 68: x=x-1;break;
        }
        if (cheat==0 && 
           ((old+20)<shape->get(x,y) ||
             shape->get(x,y)==0)) {x=oldx;y=oldy;}
      } else {
        switch(i) {
          case 65: y=y-100;break;
          case 66: y=y+100;break;
          case 67: x=x+100;break;
          case 68: x=x-100;break;
        }
      }
      switch(i) {
        case 49: cheat = (cheat+1) % 2;break; //Pos1
        case 52: ende=1;break; //Ende
        case 91: //F taste
          i=vga_getkey();
          switch(i) {
            case 65:_type=2;break; //F1
            case 66: _type=50;break; //F2
            case 67: _type=52;break; //F3
          }
         break;
      }
      i=0;
    }
  }
};

