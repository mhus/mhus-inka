
#include "Genesis.h"
#include "Shape.h"
#include "DisplayHelper.h"
#include "Arrange.h"
#include "ConfigHelper.h"

#include <sys/timeb.h>

ConfigHelper *ConfigHelper::staticConfigHelper = new ConfigHelper();

void testSaved() {

  int i,x,y;

  DisplayHelper *display;
  Shape         *shape;
  char          *shapePath;
  char          defShapePath[] = "/drv/big0/genesis";

  struct timeb *t = new timeb;
  ftime(t);
  printf("USEC: %i\n",t->time);
  srand(t->time);



  //init
printf("DisplayHelper\n");
  display = new DisplayHelper(0,0);


  //get start position
  ConfigHelper *c = ConfigHelper::staticConfigHelper;
  if (c->parseXML("genesis.xml") == 0) {
    c->addDocument("save.xml");

    char *aname = c->getProperty(c->getNodeFrom(c->getRoot(),"RUN.START"),
                                 "area"); //get area name

    xmlNodePtr pArea = c->getNamedNode(c->getNodeFrom(c->getRoot(),"SAVE:"),
                                      "AREA",
                                      "name",
                                      aname); // get area

    xmlNodePtr pGenesis = c->getNodeFrom(c->getRoot(),"SAVE:GENESIS");

    x = atoi(c->getProperty(pArea,"rasterx")) * atoi(c->getProperty(pGenesis,"xpr"));
    y = atoi(c->getProperty(pArea,"rastery")) * atoi(c->getProperty(pGenesis,"ypr"));

    x = x + atoi(c->getProperty(c->getNodeFrom(c->getRoot(),"RUN.START"),"x"));
    y = y + atoi(c->getProperty(c->getNodeFrom(c->getRoot(),"RUN.START"),"y"));

    shapePath = c->getProperty(c->getNodeFrom(c->getRoot(),"RUN.START"),"shapepath");

  } else {
   x = 160;y=200;
   shapePath = defShapePath;
  }

printf("Shape [%s]....\n",shapePath);
  shape = new Shape(320,400);
  display->setShape(shape);
  shape->setRWHelper(shapePath,"shape",RWHelper::BINARY);

  printf("run: XY %i %i\n",x,y);

  //loop
  display->loop(x,y,2);

  //bye
  delete shape;
  delete display;

}

void main() {


  testSaved();

}