# include "Arrange.h"

Arrange::Arrange(){};

Arrange::Arrange(int _rasx, int _rasy) {
  rasx = _rasx;
  rasy = _rasy;


#ifdef DEBUG_ARRANGE
  printf("Arrange: x = %i y = %i\n",rasx,rasy);
#endif

  if (rasx < 1 || rasy < 1) {
    printf("ArrangeError: area to small\n");
    return;
  }

  if (!(raster = (unsigned char *)malloc(rasx*rasy)) ) {
    printf("ArrangeError: cant allocate mem \n");
    return;
  }


  //init areas
  areas=NULL;
};

Arrange::~Arrange() {

#ifdef DEBUG_ARRANGE
  printf("Arrange: delete\n");
#endif

  if (&areas) delete areas;

  free(raster);
};

int Arrange::getRasX() {
  return rasx;
};

int Arrange::getRasY() {
  return rasy;
};

Area *Arrange::getFirstArea() {
  return areas;
};

void Arrange::addArea(char *_name,
                     unsigned char _type,
                     int _size,
                     int _wather) {

  addArea(new Area(_name, _type, _size, _wather));
};

void Arrange::addArea(Area *_area) {
  if (areas == NULL) {
    areas = _area;
  } else {
   areas->addArea(_area);
  }
};

void Arrange::setWarnings(int _warnings) {
  warnings = _warnings;
};

int Arrange::arrangeAreas() {

  int xc,yc; //cursor  
  int xadd,yadd; //richtungsvectoren
  int xcnew,ycnew; //puffer
  int nr,k,anz,areanr;
  int r[4];
  Area *a;

#ifdef DEBUG_ARRANGE
  printf("Arrange Areas:\n");
#endif


  //init area
  for (int i=0;i<rasx*rasy;i++) set(i % rasx,i / rasx,0);
  for (int i=0;i<rasx;i++) {
    set(i,0,WATHER);
    set(i,rasy-1,WATHER);
  }
  for (int i=0;i<rasy;i++) {
    set(0,i,WATHER);
    set(rasx-1,i,WATHER);
  }

  //search startpoint
  switch((rand() % 5)) {
    case 0: xc=1;yc=1;break;
    case 1: xc=1;yc=rasy-2;break;
    case 2: xc=rasx-2;yc=1;break;
    case 3: xc=rasx-2;yc=rasy-2;break;
    case 4: xc=rasx/2;yc=rasy/2;break;
  }
  if (get(xc,yc)!=0) {
#ifdef WARNINGS_ARRANGE
    printf("ArrangeError: no startpoin\n");
#endif
    return 1;
  }

  //lets go
  areanr=0;
  a = areas;
  while (a != NULL) {

#ifdef DEBUG_ARRANGE
    printf("Area = %i size = %i type = %i\n",++areanr,a->build.size,a->build.type);
#endif

    if (a->build.master == NULL || a->build.master[0]=='\0') {
      if (a->build.fBreak==1) {
        //neuer startpoint
        xcnew=xc;ycnew=yc;
        switch((rand() % 5)) {
          case 0: xc=1;yc=1;break;
          case 1: xc=1;yc=rasy-2;break;
          case 2: xc=rasx-2;yc=1;break;
          case 3: xc=rasx-2;yc=rasy-2;break;
          case 4: xc=rasx/2;yc=rasy/2;break;
        }
        if (get(xc,yc)!=0) {
#ifdef WARNINGS_ARRANGE
          printf("ArrangeWarning: new startpoint not available\n");
#endif
          xc=xcnew;yc=ycnew;
        }
      }

      if (a->build.fBreak==2) {
        //neuer startpoint
        xcnew=xc;ycnew=yc;
        k=0;
        do {
          xc = rand() % rasx;
          yc = rand() % rasy;
          k++;
        } while (get(xc,yc)!=0 && k<10);
        if (get(xc,yc)!=0) {
#ifdef WARNINGS_ARRANGE
          printf("ArrangeWarning: new startpoint not available\n");
#endif
          xc=xcnew;yc=ycnew;
        }
      }



#ifdef DISPLAY
      vga_setcolor(80);
      vga_drawpixel(xc*10+5,yc*10+5);
//     vga_getch();
#endif

      //init richtungen
      for(int i=0;i<4;i++) r[i]=1;
      anz=4;
      //teste
      for (int i=0;i<4;i++) {
        xadd=0;yadd=0;
        switch(i) {
          case 0:yadd=-1;break;
          case 1:xadd=1;break;
          case 2:yadd=1;break;
          case 3:xadd=-1;break;
        }
        for (int j=0;j<a->build.size;j++) if (get(xc+xadd*j,yc+yadd*j)!=0) r[i]=0;
        if (r[i]==0) anz--;
      }
 
      //Area einzeichnen und werte in Area eintragen
      if (anz == 0) {
#ifdef WARNINGS_ARRANGE
        printf("ArrangeError: Area Size to big\n");
#endif
        return 2;
      } else {

        k = rand() % anz + 1;
        for (int i=0;k!=0;i++) if (r[i]!=0) {k--;nr=i;}

        xadd=0;yadd=0;
        switch(nr) {
          case 0:yadd=-1;break;
          case 1:xadd=1;break;
          case 2:yadd=1;break;
          case 3:xadd=-1;break;
        }
        for (int j=0;j<a->build.size;j++)
          set(xc+xadd*j,yc+yadd*j,a->build.type);
       
        xcnew=xc+xadd*(a->build.size-1);
        ycnew=yc+yadd*(a->build.size-1); //save last field for new cursor
  
        a->rasterx = (xadd < 0) ? xc+xadd*(a->build.size-1) : xc;
        a->rastery = (yadd < 0) ? yc+yadd*(a->build.size-1) : yc;
        a->rasterb = abs(xadd*a->build.size);
        a->rasterh = abs(yadd*a->build.size);
       if (a->rasterb == 0) a->rasterb = 1;
       if (a->rasterh == 0) a->rasterh = 1;

        r[nr] = 0;
        anz--;
  
        //wasser am rand benoetigt ? reservieren
        if (a->build.fWather!=0) {
          if (anz == 0) {
#ifdef WARNINGS_ARRANGE
            printf("ArangeWarning: No Wather available, ignore\n");
#endif
            if (warnings != 0) return 3;
          } else {

            k = rand() % anz + 1;
            for (int i=0;k!=0;i++) if (r[i]!=0) {k--;nr=i;}
  
            xadd=0;yadd=0;
            switch(nr) {
              case 0:yadd=-1;break;
              case 1:xadd=1;break;
              case 2:yadd=1;break;
              case 3:xadd=-1;break;
            }
            set(xc+xadd,yc+yadd,WATHER);          
          }
        } // END wasser einzeichnen

      } // END anz==0 (Area einzeichnen)

      //neue cursor position suchen
      xc=xcnew;yc=ycnew;

      //init richtungen
      for(int i=0;i<4;i++) r[i]=1;
      anz=4;
      //teste
      for (int i=0;i<4;i++) {
        xadd=0;yadd=0;
        switch(i) {
          case 0:yadd=-1;break;
          case 1:xadd=1;break;
          case 2:yadd=1;break;
          case 3:xadd=-1;break;
        } 
        if (get(xc+xadd,yc+yadd)!=0) r[i]=0;
        if (r[i]==0) anz--;
      }  
      //Cursor neu setzen
      if (anz == 0) {
#ifdef WARNINGS_ARRANGE
        printf("ArrangeError: Cant set new cursor\n");
#endif
        return 1;
      } else {

        k = rand() % anz + 1;
        for (int i=0;k!=0;i++) if (r[i]!=0) {k--;nr=i;} 
 
        xadd=0;yadd=0;
        switch(nr) {
          case 0:yadd=-1;break;
          case 1:xadd=1;break;
          case 2:yadd=1;break;
          case 3:xadd=-1;break;
        }
        xc+=xadd;yc+=yadd;
      }

    } else { //if AREA->build.MASTER == NULL

      //area hat master, holen und setzen
      Area *master = areas;
      while (master != NULL) {
        if (strcmp(master->name,a->build.master) == 0) {
          //wichtige werte von master uebernehmen
          a->rasterx = master->rasterx;
          a->rastery = master->rastery;
          a->rasterb = master->rasterb;
          a->rasterh = master->rasterh;
        }
        master = master->getNextArea();
      }
  
    } // if AREA->build.MASTER == NULL

    // neue Area holen 
    a = a->getNextArea();

  }
  return 0;  
};

  
void Arrange::set(int _x, int _y, unsigned char _v) {
  if (_x>-1 && _y>-1 && _x<rasx && _y<rasy)
  {
    raster[_x+_y*rasx] = _v;
#ifdef DISPLAY
    if (_x*10<320 && _y*10<400) {
      vga_setcolor(_v*10);
      for (int i=0;i<10;i++)
      for (int j=0;j<10;j++)
        vga_drawpixel(_x*10+i,_y*10+j);
    }
#endif
  }
};

unsigned char Arrange::get(int _x, int _y) {
  if (_x>-1 && _y>-1 && _x<rasx && _y<rasy)
    return raster[_x+_y*rasx];

  return WATHER;
};

Arrange *Arrange::fromXML(char *_path) {
  ConfigHelper *c = ConfigHelper::staticConfigHelper;

  xmlNodePtr element = c->getNodeFrom(c->getRoot(), _path);
  if (element == NULL) {
    printf("Arrange: ARRANGE \"%s\" not found\n",_path);
    return NULL;
  }

  return fromXML(element);
};

Arrange *Arrange::fromXML(xmlNodePtr _element) {
  ConfigHelper *c = ConfigHelper::staticConfigHelper;

  // create
  int xr = atoi(c->getProperty(_element,"x"));
  int yr = atoi(c->getProperty(_element,"y"));
  if (xr < 1 || yr < 1) {
    printf("Arrange: XML wrong XYRaster x = %1 y = %i\n",
      xr,yr);
    return NULL;
  }
  Arrange *arrange = new Arrange(xr,yr);

  // configure properties
  if (c->existsProperty(_element, "warnings"))
    arrange->setWarnings(atoi(c->getProperty(_element,"warnings")));

  // configure Areas
 xmlNodePtr cur;
 for (int i=0;(cur = c->getNodeAt(_element,"AREA",i)) != NULL;i++) {
   Area *a = Area::fromXML(cur);
   arrange->addArea(a);

   // check slaves
   while (a != NULL && a->build.slave != NULL && a->build.slave[0] != 0) {
     char *master = a->name;
     a = Area::fromXML(c->getNodeFrom(_element,a->build.slave));
     if (a != NULL) {
       a->build.master = master;
       arrange->addArea(a);
     }
   }
 }

  return arrange;
};