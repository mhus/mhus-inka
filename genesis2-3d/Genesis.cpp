#include "Genesis.h"

Genesis::Genesis() {
  arrange = new Arrange(20,20);
  xpr = 200;
  ypr = 200;
  shape = new Shape(20*xpr, 20*ypr);
  shape->setRWHelper("","default_shape",RWHelper::BINARY);
  ownTools = true;
  shapeFactory = new ShapeFactory(arrange, shape);
};

Genesis::Genesis(Arrange *_arrange, 
                 Shape *_shape,
                 int _xpr, 
                 int _ypr) {
  arrange = _arrange;
  shape   = _shape;
  xpr     = _xpr;
  ypr     = _ypr;

  ownTools = false;
  shapeFactory = new ShapeFactory(arrange, shape);
};

Genesis::~Genesis() {

  delete shapeFactory;

  if (ownTools) {
    delete shape;
    delete arrange;
  }
};

void Genesis::setOwnTools(bool _ot) {
  ownTools = _ot;
};

void Genesis::setAutomatic(bool _auto) {
  automatic = _auto;
};

void Genesis::setDiffMax(int _x, int _y) {
  xDiffMax = _x;
  yDiffMax = _y;
};

void Genesis::setDiffMin(int _x, int _y) {
  xDiffMin = _x;
  yDiffMin = _y;
};

void Genesis::setXMLSaveFile(char *_fname) {
  xmlSaveFile = _fname;
};

void Genesis::setFinalShapeRenderer(int _fsr,int _anz,int _int) {
  finalShapeRenderer    = _fsr;
  finalShapeRendererAnz = _anz;
  finalShapeRendererInt = _int;
};
  
int Genesis::generateArrange() {
  int count = -1;
  Area *cur;

  //generate arrange
  do {  
    while (arrange->arrangeAreas() != 0 && count < arrangeTrials)
      if (arrangeTrials != 0) count++;

    if (count==arrangeTrials) {
      printf("Genesis: arrange fails\n");
      return 1;
    }
#ifdef DISPLAY
  } while (automatic == false && vga_getch() != 10);
#else
  } while (1 == 2); //no automatic
#endif


  //calculate real xy positions of areas
  cur = arrange->getFirstArea();
  while (cur != NULL) {

    cur->xa = cur->rasterx * xpr;
    cur->ya = cur->rastery * ypr;
    cur->xb = cur->xa + cur->rasterb * xpr;
    cur->yb = cur->ya + cur->rasterh * ypr;

    if (cur->rasterpart > 0 && cur->rasterpart < 10) {
      cur->xb = cur->xa + (cur->rasterb * xpr) * ((cur->rasterpart-1) % 3 + 1) / 3;
      cur->xa = cur->xa + (cur->rasterb * xpr) * ((cur->rasterpart-1) % 3) / 3;

      cur->yb = cur->ya + (cur->rasterh * ypr) * ((cur->rasterpart-1) / 3 + 1) / 3;
      cur->ya = cur->ya + (cur->rasterh * ypr) * ((cur->rasterpart-1) / 3) / 3;

    } else if(cur->rasterpart == -1) {
      cur->xa = cur->xa + (cur->rasterb * xpr) / 4;
      cur->xb = cur->xa + (cur->rasterb * xpr) * 3 / 4;

      cur->ya = cur->ya + (cur->rasterh * ypr) / 4;
      cur->yb = cur->ya + (cur->rasterh * ypr) * 3 / 4;
    }

    cur = cur->getNextArea();
  }


  //save Areas
  ConfigHelper::staticConfigHelper->newDocument("SAVE");
  xmlNodePtr saveXML = ConfigHelper::staticConfigHelper->getNodeFrom(
                         ConfigHelper::staticConfigHelper->getRoot(),
                         "SAVE:");
  if (saveXML == NULL) {
    printf("Genesis: load XML NODE fails\n");
    return 2;
  }

  toXML(saveXML);

  cur = arrange->getFirstArea();
  while (cur != NULL) {
    cur->toXML(saveXML);
    cur = cur->getNextArea();
  }
  // end save

  if (ConfigHelper::staticConfigHelper->saveDocument("SAVE", xmlSaveFile) != 0)
  {
    printf("Genesis: xml save fails\n");
    return 3;
  }    

  return 0;
};




void Genesis::generateShape(Area *_a) {
  int b1xa,b2xa,b1ya,b2ya; //for Border coordinates puffer
  int b1xb,b2xb,b1yb,b2yb;

  int raster[3][3];
  int x,y,i,j;

  //set x,y for field
  xa = _a->xa - xDiffMin - (rand() % (xDiffMax - xDiffMin));
  ya = _a->ya - yDiffMin - (rand() % (yDiffMax - yDiffMin));

  xb = _a->xb + xDiffMin + (rand() % (xDiffMax - xDiffMin));
 
  yb = _a->yb + yDiffMin + (rand() % (yDiffMax - yDiffMin));

  shape->setDisplayXY(xa-10,ya-10);

  shapeFactory->setArrangePos(_a->rasterx, _a->rastery,
                              _a->rasterx+_a->rasterb-1,
                              _a->rastery+_a->rasterh-1);

  for (int ordercnt=0;_a->build.order[ordercnt] != 0;ordercnt++)
  switch(_a->build.order[ordercnt]) {

    case 'G':
      //background
      shapeFactory->generateShape(xa,ya,xb,yb,
                  _a->build.bgType, 
                  _a->build.bgAnz,
                  _a->build.bgIntensity,
                  _a->build.bgBase,
                  _a->build.bgMin,
                  _a->build.bgMax,
                  _a->build.bgDiff);
    break;
    case 'M':
      //main
      shapeFactory->generateShape(xa,ya,xb,yb,
                  _a->build.mainType, 
                  _a->build.mainAnz,
                  _a->build.mainIntensity,
                  _a->build.mainBase,
                  _a->build.mainMin,
                  _a->build.mainMax,
                  _a->build.mainDiff);

    break;
    case 'B':
      //borders horizontal
      b1xb = xa;b1yb = ya;  //ceate the first "old" coordinates
      b2xb = xa;b2yb = yb;
      for (int i = 0; i < _a->rasterb; i++) {
        //new x,y

        b1xa = b1xb;b1ya = b1yb; //move old coordinates

 //       b1xa = (_a->rasterx+i+1) * xpr;
 //       b1ya = (_a->rastery) * ypr;

        // generate new ones
        b1xb = (_a->rasterx+i+1) * xpr + xDiffMin + (rand() % (xDiffMax - xDiffMin));
        b1yb = _a->rastery * ypr + yDiffMin + (rand() % (yDiffMax - yDiffMin));
  
        b2xa = b2xb;b2ya = b2yb; //alte coordinaten verschieben

//        b1xa = (_a->rasterx+i+1) * xpr;
//        b1ya = (_a->rastery+_a->rasterh) * ypr;

        // generate new ones
        b2xb = (_a->rasterx+i+1) * xpr + xDiffMin + (rand() % (xDiffMax - xDiffMin));
        b2yb = (_a->rastery+_a->rasterh) * ypr + yDiffMin + (rand() % (yDiffMax - yDiffMin));

        //oberer rand
        bxa = b1xa;bya = b1ya;   //temporaer uebernehmen
        bxb = b1xb;byb = b1yb;
        if (arrange->get(_a->rasterx+i,_a->rastery-1) == _a->build.type) {
          // Border Same
          shapeFactory->generateShape(bxa,
                                      bya-_a->build.bsOffset,
                                      bxb,
                                      byb-_a->build.bsOffset,
                  _a->build.bsType, 
                  _a->build.bsAnz,
                  _a->build.bsIntensity,
                  _a->build.bsBase,
                  _a->build.bsMin,
                  _a->build.bsMax,
                  _a->build.bsDiff);
        } else {
        if (arrange->get(_a->rasterx+i,_a->rastery-1) < 2) {
          // border Wather
          shapeFactory->generateShape(bxa,
                   bya-_a->build.bwOffset,
                   bxb,
                   byb-_a->build.bwOffset,
                  _a->build.bwType, 
                  _a->build.bwAnz,
                  _a->build.bwIntensity,
                  _a->build.bwBase,
                  _a->build.bwMin,
                  _a->build.bwMax,
                  _a->build.bwDiff);
        } else { 
          // Border Other
          shapeFactory->generateShape(bxa,
                   bya-_a->build.boOffset,
                   bxb,
                   byb-_a->build.boOffset,
                  _a->build.boType, 
                  _a->build.boAnz,
                  _a->build.boIntensity,
                  _a->build.boBase,
                  _a->build.boMin,
                  _a->build.boMax,
                  _a->build.boDiff);
        }    
        }

        //unterer rand
        bxa = b2xa;bya = b2ya;   //temporaer uebernehmen
        bxb = b2xb;byb = b2yb;
        if (arrange->get(_a->rasterx+i,_a->rastery+_a->rasterh) == _a->build.type) {
          // Border Same
          shapeFactory->generateShape(bxa,
                   bya+_a->build.bsOffset,
                   bxb,
                   byb+_a->build.bsOffset,
                  _a->build.bsType, 
                  _a->build.bsAnz,
                  _a->build.bsIntensity,
                  _a->build.bsBase,
                  _a->build.bsMin,
                  _a->build.bsMax,
                  _a->build.bsDiff);
        } else {
        if (arrange->get(_a->rasterx+i,_a->rastery+_a->rasterh) < 2) {
          // border Wather
          shapeFactory->generateShape(bxa,
                   bya-_a->build.bwOffset,
                   bxb,
                   byb-_a->build.bwOffset,
                  _a->build.bwType, 
                  _a->build.bwAnz,
                  _a->build.bwIntensity,
                  _a->build.bwBase,
                  _a->build.bwMin,
                  _a->build.bwMax,
                  _a->build.bwDiff);
        } else { 
          // Border Other
          shapeFactory->generateShape(bxa,
                   bya+_a->build.boOffset,
                   bxb,
                   byb+_a->build.boOffset,
                  _a->build.boType, 
                  _a->build.boAnz,
                  _a->build.boIntensity,
                  _a->build.boBase,
                  _a->build.boMin,
                  _a->build.boMax,
                  _a->build.boDiff);
        }
        }
      }

      //border vertikal
      b1xb = xa;b1yb = ya;  //ceate the first "old" coordinates
      b2xb = xb;b2yb = ya;
      for (int i = 0; i < _a->rasterh; i++) {
        //new x,y
        b1xa = b1xb;b1ya = b1yb; //move old coordinates
//       b1xa = _a->rasterx * ypr;
//       b1ya = (_a->rastery+i+1) * ypr;
 
        // generate new ones
        b1xb = _a->rasterx * xpr + xDiffMin + (rand() % (xDiffMax - xDiffMin));
        b1yb = (_a->rastery+i+1) * ypr + yDiffMin + (rand() % (yDiffMax - yDiffMin));
  
        b2xa = b2xb;b2ya = b2yb; //alte coordinaten verschieben
//        b2xa = (_a->rasterx+_a->rasterb) * xpr;
//        b2ya = (_a->rastery+i+1) * ypr;

        // generate new ones
        b2xb = (_a->rasterx+_a->rasterb) * xpr + xDiffMin + (rand() % (xDiffMax - xDiffMin));
        b2yb = (_a->rastery+i+1) * ypr + yDiffMin + (rand() % (yDiffMax - yDiffMin));

        //linker rand
        bxa = b1xa;bya = b1ya;   //temporaer uebernehmen
        bxb = b1xb;byb = b1yb;
        if (arrange->get(_a->rasterx-1,_a->rastery+i) == _a->build.type) {
          // Border Same
          shapeFactory->generateShape(bxa-_a->build.bsOffset,
                   bya,
                   bxb-_a->build.bsOffset,
                   byb,
                  _a->build.bsType, 
                  _a->build.bsAnz,
                  _a->build.bsIntensity,
                  _a->build.bsBase,
                  _a->build.bsMin,
                  _a->build.bsMax,
                  _a->build.bsDiff);
        } else {
        if (arrange->get(_a->rasterx-1,_a->rastery+i) < 2) {
          // border Wather
          shapeFactory->generateShape(bxa,
                   bya-_a->build.bwOffset,
                   bxb,
                   byb-_a->build.bwOffset,
                  _a->build.bwType, 
                  _a->build.bwAnz,
                  _a->build.bwIntensity,
                  _a->build.bwBase,
                  _a->build.bwMin,
                  _a->build.bwMax,
                  _a->build.bwDiff);
        } else { 
          // Border Other
          shapeFactory->generateShape(bxa-_a->build.boOffset,
                  bya,
                  bxb-_a->build.boOffset,
                  byb,
                  _a->build.boType, 
                  _a->build.boAnz,
                  _a->build.boIntensity,
                  _a->build.boBase,
                  _a->build.boMin,
                  _a->build.boMax,
                  _a->build.boDiff);
        }
        }    

        //rechter rand
        bxa = b2xa;bya = b2ya;   //temporaer uebernehmen
        bxb = b2xb;byb = b2yb;
        if (arrange->get(_a->rasterx+_a->rasterb,_a->rastery+i) == _a->build.type) {
          // Border Same
          shapeFactory->generateShape(bxa+_a->build.bsOffset,
                  bya,
                  bxb+_a->build.bsOffset,
                  byb,
                  _a->build.bsType, 
                  _a->build.bsAnz,
                  _a->build.bsIntensity,
                  _a->build.bsBase,
                  _a->build.bsMin,
                  _a->build.bsMax,
                  _a->build.bsDiff);
        } else {
        if (arrange->get(_a->rasterx+_a->rasterb,_a->rastery+i) < 2) {
          // border Wather
          shapeFactory->generateShape(bxa,
                   bya-_a->build.bwOffset,
                   bxb,
                   byb-_a->build.bwOffset,
                  _a->build.bwType, 
                  _a->build.bwAnz,
                  _a->build.bwIntensity,
                  _a->build.bwBase,
                  _a->build.bwMin,
                  _a->build.bwMax,
                  _a->build.bwDiff);
        } else { 
          // Border Other
          shapeFactory->generateShape(bxa + _a->build.boOffset,
                  bya,
                  bxb + _a->build.boOffset,
                  byb,
                  _a->build.boType, 
                  _a->build.boAnz,
                  _a->build.boIntensity,
                  _a->build.boBase,
                  _a->build.boMin,
                  _a->build.boMax,
                  _a->build.boDiff);
        }    
        }
      }
    break;
    case 'F':
      //filter
      shapeFactory->generateShape(xa-_a->build.fOffset,
                  ya-_a->build.fOffset,
                  xb+_a->build.fOffset,
                  yb+_a->build.fOffset,
                  _a->build.fType, 
                  _a->build.fAnz,
                  _a->build.fIntensity,
                  _a->build.fBase,
                  _a->build.fMin,
                  _a->build.fMax,
                  _a->build.fDiff);
    break;
    case 's': //remove See
      //set all 0 to 1 in this area !!! (0 is nothing = big see)
  
      shapeFactory->removeSee(xa-_a->build.fOffset,
                          ya-_a->build.fOffset,
                          xb+_a->build.fOffset,
                          yb+_a->build.fOffset);
    break;
    case 'c': // cut beach

      for (int ii = 0; ii < _a->rasterb; ii++)
      for (int jj = 0; jj < _a->rasterh; jj++) {

      for (i = 0; i < 3; i++)
      for (j = 0; j < 3; j++) raster[i][j] = 0;
   
      if (arrange->get(_a->rasterx-1+ii,_a->rastery+jj) < 2)
        for(i = 0; i < 3; i++) raster[0][i] = 1;
      if (arrange->get(_a->rasterx+1+ii,_a->rastery+jj) < 2)
        for(i = 0; i < 3; i++) raster[2][i] = 1;
      if (arrange->get(_a->rasterx+ii,_a->rastery-1+jj) < 2)
        for(i = 0; i < 3; i++) raster[i][0] = 1;
      if (arrange->get(_a->rasterx+ii,_a->rastery+1+jj) < 2)
        for(i = 0; i < 3; i++) raster[i][2] = 1;
      if (arrange->get(_a->rasterx-1+ii,_a->rastery-1+jj) < 2) raster[0][0] = 1;
      if (arrange->get(_a->rasterx+1+ii,_a->rastery-1+jj) < 2) raster[2][0] = 1;
      if (arrange->get(_a->rasterx+1+ii,_a->rastery+1+jj) < 2) raster[2][2] = 1;
      if (arrange->get(_a->rasterx-1+ii,_a->rastery+1+jj) < 2) raster[0][2] = 1;

      for (i = 0; i < 3; i++)
      for (j = 0; j < 3; j++) 
      if (raster[i][j] == 1)
      shapeFactory->generateShape(xa + (xb-xa)/3*i - _a->build.fOffset,
                  ya + (yb-ya)/3*j -_a->build.fOffset,
                  xa + (xb-xa)/3*(i+1)+_a->build.fOffset,
                  ya + (yb-ya)/3*(j+1)+_a->build.fOffset,
                  19, 
                  1,
                  50,
                  0,
                  0,
                  0,
                  0);
    }

    break;
  } //switch order


  //set old Type
  shapeFactory->setOldPos((int)_a->build.type, xb, yb);

};


int Genesis::generateAll() {

  int r;
  struct timeb *t = new timeb;

  // init random
  ftime(t);
  printf("Genesis: srand(%i)\n",t->time);
  srand(t->time);

  if ((r = generateArrange()) != 0) return r;



  //generate shape
  shape->getRWHelper()->clear();
  Area *cur = arrange->getFirstArea();
  while (cur != NULL) {
    generateShape(cur);
    cur = cur->getNextArea();
//-- vga_getch();
  }
  //last soften over all
  shapeFactory->generateShape(10,
                              10,
                              shape->getMaxX()-10,
                              shape->getMaxY()-10,
                              finalShapeRenderer,
                              finalShapeRendererAnz,
                              finalShapeRendererInt,
                              0,0,255,2);
  //save
  shape->getRWHelper()->saveAll();

};

Genesis *Genesis::fromXML(char *_path) {

  ConfigHelper *c = ConfigHelper::staticConfigHelper;

  xmlNodePtr element = c->getNodeFrom(c->getRoot(), _path);
  if (element == NULL) {
    printf("Genesis: GENESIS \"%s\" not found\n",_path);
    return NULL;
  }
  return fromXML(element);
}; 

Genesis *Genesis::fromXML(xmlNodePtr _element) {
  ConfigHelper *c = ConfigHelper::staticConfigHelper;

  // get arrange
  Arrange *arrange = Arrange::fromXML(c->getNode(_element,"ARRANGE"));
  if (arrange == NULL) {
    printf("Genesis: ARRANGE is NULL\n");
    return NULL;
  }

  int xpr = atoi(c->getProperty(_element,"xpr"));
  int ypr = atoi(c->getProperty(_element,"ypr"));

  if (xpr < 10 || ypr < 10) {
    printf("Genesis: wrong size per raster %i %i\n",xpr,ypr);
    delete arrange;
    return NULL;
  }

  Shape *shape = new Shape(arrange->getRasX()*xpr, arrange->getRasY()*ypr);

  if (shape == NULL) {
    printf("Genesis: SHAPE is NULL\n");
    delete arrange;
    return NULL;
  }

  char *path = c->getProperty(_element,"path","");

  shape->setRWHelper(path,"shape",RWHelper::BYTE);

  Genesis *g = new Genesis(arrange,shape,xpr,ypr);
  g->setDiffMax( atoi(c->getProperty(_element,"diffmaxx","20")),
                 atoi(c->getProperty(_element,"diffmaxy","20")));
  g->setDiffMin( atoi(c->getProperty(_element,"diffminx","1")),
                 atoi(c->getProperty(_element,"diffminy","1")));

  g->setAutomatic(c->getConstInt(c->getProperty(_element,"automatic","0")));
  g->setXMLSaveFile(c->getProperty(_element,"xmlsavefile","save.xml"));
  g->setFinalShapeRenderer(atoi(c->getProperty(_element,"finalshaperenderer")),
                           atoi(c->getProperty(_element,"finalshaperendereranz")),
                           atoi(c->getProperty(_element,"finalshaperendererint"))
  );

  g->setOwnTools(true); //genesis remove all objects in final

  return g;
};

void Genesis::toXML(xmlNodePtr _parent) {

  xmlNodePtr g = xmlNewChild(_parent, NULL, (CHAR *)"GENESIS", NULL);
  xmlSetProp(g, (CHAR *)"xpr", (CHAR *)Lib::toString(xpr));
  xmlSetProp(g, (CHAR *)"ypr", (CHAR *)Lib::toString(ypr));
 
};