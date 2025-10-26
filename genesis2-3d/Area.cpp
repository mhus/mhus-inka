
#include "Area.h"

Area::Area() {
  nextArea = NULL;
} 

Area::Area(char * _name,
           unsigned char _type, 
           int _size, 
           int _flags) {

  name = _name;
  build.type = _type;
  build.size = _size;
  nextArea = NULL;
  build.master = NULL;
 
  if ((_flags & FWATHER) == FWATHER) build.fWather=1; else build.fWather=0;
  if ((_flags & FBREAK)  == FBREAK)  build.fBreak=1; else build.fBreak=0;
  if ((_flags & FISLAND) == FISLAND) build.fBreak=2;
}
  
Area::~Area() {
  if (nextArea != NULL) delete nextArea;
}

void Area::addArea(Area *_a) {
  if (nextArea != NULL) nextArea->addArea(_a); else nextArea=_a;
}

Area *Area::getNextArea() {
  return nextArea;
}

Area *Area::fromXML(xmlNodePtr _element) {
  xmlNodePtr cur;
  char *type, *anz, *inte, *diff, *base, *min, *max, *offset;

  if (_element == NULL) return NULL;

  ConfigHelper *c = ConfigHelper::staticConfigHelper;

  if (_element == NULL) {
    printf("ConfigHelper: AREA is NULL");
    return NULL;
  }

  // create Area
  Area *area = new Area();

  // configure def attributes
  area->name    = c->getProperty(_element,"name");
  area->rasterx = atoi(c->getProperty(_element,"rasterx"));
  area->rastery = atoi(c->getProperty(_element,"rastery"));
  area->rasterb = atoi(c->getProperty(_element,"rasterb"));
  area->rasterh = atoi(c->getProperty(_element,"rasterh"));
  area->rasterpart = c->getConstInt(c->getProperty(_element,
                                                   "rasterpart",
                                                   "0"));

  area->xa = atoi(c->getProperty(_element,"xa"));
  area->ya = atoi(c->getProperty(_element,"ya"));
  area->xb = atoi(c->getProperty(_element,"xb"));
  area->yb = atoi(c->getProperty(_element,"yb"));

  // configure arrange
  cur = c->getNode(_element,"ARRANGE");
  area->build.fWather = c->getConstInt(c->getProperty(cur,"water"));
  area->build.fBreak  = c->getConstInt(c->getProperty(cur,"break"));

  // set type, can be set be AREA, too
  area->build.type = c->getConstInt(c->getProperty(_element,"type",
                                 c->getProperty(cur,"type")));
  // set size, can be set by AREA, too
  area->build.size = atoi(c->getProperty(_element,"size",
                          c->getProperty(cur,"size")));

  // configure shape
  xmlNodePtr shape = c->getNode(_element,"SHAPE");
  // background
  cur = c->getNode(shape,"BACKGROUND");
  area->build.bgType      = c->getConstInt(c->getProperty(cur,"type"));
  area->build.bgAnz       = atoi(c->getProperty(cur,"anz","1"));
  area->build.bgIntensity = c->getConstInt(c->getProperty(cur,"intensity","1"));
  area->build.bgDiff      = c->getConstInt(c->getProperty(cur,"diff","1"));
  area->build.bgBase      = c->getConstInt(c->getProperty(cur,"base","120"));
  area->build.bgMin      = c->getConstInt(c->getProperty(cur,"min","1"));
  area->build.bgMax      = c->getConstInt(c->getProperty(cur,"max","180"));
  // main
  cur = c->getNode(shape,"MAIN");
  area->build.mainType      = c->getConstInt(c->getProperty(cur,"type","1"));
  area->build.mainAnz       = atoi(c->getProperty(cur,"anz","1"));
  area->build.mainIntensity = c->getConstInt(c->getProperty(cur,"intensity","1"));
  area->build.mainDiff      = c->getConstInt(c->getProperty(cur,"diff","1"));
  area->build.mainBase      = c->getConstInt(c->getProperty(cur,"base","120"));
  area->build.mainMin       = c->getConstInt(c->getProperty(cur,"min","1"));
  area->build.mainMax       = c->getConstInt(c->getProperty(cur,"max","180"));
  // border others (def.)
  cur = c->getNode(shape,"BORDER");
  area->build.boType      = c->getConstInt(type = c->getProperty(cur,"type","1"));
  area->build.boAnz       = atoi(anz  = c->getProperty(cur,"anz","1"));
  area->build.boIntensity = c->getConstInt(inte =
c->getProperty(cur,"intensity","1"));
  area->build.boDiff      = c->getConstInt(diff = c->getProperty(cur,"diff","1"));
  area->build.boBase      = c->getConstInt(base =
c->getProperty(cur,"base","120"));
  area->build.boMin      = c->getConstInt(min = c->getProperty(cur,"min","1"));
  area->build.boMax      = c->getConstInt(max = c->getProperty(cur,"max","180"));
  area->build.boOffset   = atoi(offset = c->getProperty(cur,"offset","0"));

  // border wather
  cur = c->getNode(shape,"BORDERWATHER");
  area->build.bwType      = c->getConstInt(c->getProperty(cur,"type",type));
  area->build.bwAnz       = atoi(c->getProperty(cur,"anz",anz));
  area->build.bwIntensity = c->getConstInt(c->getProperty(cur,"intensity",inte));
  area->build.bwDiff      = c->getConstInt(c->getProperty(cur,"diff",diff));
  area->build.bwBase      = c->getConstInt(c->getProperty(cur,"base",base));
  area->build.bwMin      = c->getConstInt(c->getProperty(cur,"min",min));
  area->build.bwMax      = c->getConstInt(c->getProperty(cur,"max",max));
  area->build.bwOffset   = atoi(c->getProperty(cur,"offset",offset));

  // border same
  cur = c->getNode(shape,"BORDERSAME");
  area->build.bsType      = c->getConstInt(c->getProperty(cur,"type",type));
  area->build.bsAnz       = atoi(c->getProperty(cur,"anz",anz));
  area->build.bsIntensity = c->getConstInt(c->getProperty(cur,"intensity",inte));
  area->build.bsDiff      = c->getConstInt(c->getProperty(cur,"diff",diff));
  area->build.bsBase      = c->getConstInt(c->getProperty(cur,"base",base));
  area->build.bsMin      = c->getConstInt(c->getProperty(cur,"min",min));
  area->build.bsMax      = c->getConstInt(c->getProperty(cur,"max",max));
  area->build.bsOffset   = atoi(c->getProperty(cur,"offset",offset));

  // filter
  cur = c->getNode(shape,"FILTER");
  area->build.fType      = c->getConstInt(c->getProperty(cur,"type"));
  area->build.fAnz       = atoi(c->getProperty(cur,"anz","1"));
  area->build.fIntensity = c->getConstInt(c->getProperty(cur,"intensity","1"));
  area->build.fDiff      = c->getConstInt(c->getProperty(cur,"diff","1"));
  area->build.fBase      = c->getConstInt(c->getProperty(cur,"base","120"));
  area->build.fMin       = c->getConstInt(c->getProperty(cur,"min","1"));
  area->build.fMax       = c->getConstInt(c->getProperty(cur,"max","180"));
  area->build.fOffset    = atoi(c->getProperty(cur,"offset","0"));

  //order
  area->build.order = c->getConstStr(c->getProperty(shape,"order","xGMBF"));

  //master-slave
  area->build.master = c->getProperty(_element,"master",NULL);
  area->build.slave  = c->getProperty(_element,"slave",NULL);
  return area;
};

void Area::toXML(xmlNodePtr _parent) {

  //area
  xmlNodePtr area = xmlNewChild(_parent, NULL, (CHAR *)"AREA", NULL);
  xmlSetProp(area, (CHAR *)"name", (CHAR *)name);
  xmlSetProp(area, (CHAR *)"rasterx",  (CHAR *)Lib::toString(rasterx));
  xmlSetProp(area, (CHAR *)"rastery",  (CHAR *)Lib::toString(rastery));
  xmlSetProp(area, (CHAR *)"rasterb",  (CHAR *)Lib::toString(rasterb));
  xmlSetProp(area, (CHAR *)"rasterh",  (CHAR *)Lib::toString(rasterh));
  xmlSetProp(area, (CHAR *)"rasterpart",  (CHAR *)Lib::toString(rasterpart));

  xmlSetProp(area, (CHAR *)"xa",  (CHAR *)Lib::toString(xa));
  xmlSetProp(area, (CHAR *)"ya",  (CHAR *)Lib::toString(ya));
  xmlSetProp(area, (CHAR *)"xb",  (CHAR *)Lib::toString(yb));
  xmlSetProp(area, (CHAR *)"yb",  (CHAR *)Lib::toString(yb));

  //arrange
  xmlNodePtr arrange = xmlNewChild(area, NULL, (CHAR *)"ARRANGE", NULL);
  xmlSetProp(arrange, (CHAR *)"water", (CHAR *)Lib::toString(build.fWather));
  xmlSetProp(arrange, (CHAR *)"break", (CHAR *)Lib::toString(build.fBreak));
  xmlSetProp(arrange, (CHAR *)"type",  (CHAR *)Lib::toString(build.type));
  xmlSetProp(arrange, (CHAR *)"size",  (CHAR *)Lib::toString(build.size));

  //shape
  xmlNodePtr shape = xmlNewChild(area, NULL, (CHAR *)"SHAPE", NULL);
  //background
  xmlNodePtr bg = xmlNewChild(shape, NULL, (CHAR *)"BACKGROUND", NULL);
  xmlSetProp(bg, (CHAR *)"type",      (CHAR *)Lib::toString(build.bgType));
  xmlSetProp(bg, (CHAR *)"anz",       (CHAR *)Lib::toString(build.bgAnz));
  xmlSetProp(bg, (CHAR *)"intensity", (CHAR *)Lib::toString(build.bgIntensity));
  xmlSetProp(bg, (CHAR *)"diff",      (CHAR *)Lib::toString(build.bgDiff));
  xmlSetProp(bg, (CHAR *)"base",      (CHAR *)Lib::toString(build.bgBase));
  xmlSetProp(bg, (CHAR *)"min",      (CHAR *)Lib::toString(build.bgMin));
  xmlSetProp(bg, (CHAR *)"max",      (CHAR *)Lib::toString(build.bgMax));

  //Main
  xmlNodePtr m = xmlNewChild(shape, NULL, (CHAR *)"MAIN", NULL);
  xmlSetProp(m, (CHAR *)"type",      (CHAR *)Lib::toString(build.mainType));
  xmlSetProp(m, (CHAR *)"anz",       (CHAR *)Lib::toString(build.mainAnz));
  xmlSetProp(m, (CHAR *)"intensity", (CHAR *)Lib::toString(build.mainIntensity));
  xmlSetProp(m, (CHAR *)"diff",      (CHAR *)Lib::toString(build.mainDiff));
  xmlSetProp(m, (CHAR *)"base",      (CHAR *)Lib::toString(build.mainBase));
  xmlSetProp(m, (CHAR *)"min",      (CHAR *)Lib::toString(build.mainMin));
  xmlSetProp(m, (CHAR *)"max",      (CHAR *)Lib::toString(build.mainMax));


  //Border (other)
  xmlNodePtr bo = xmlNewChild(shape, NULL, (CHAR *)"BORDER", NULL);
  xmlSetProp(bo, (CHAR *)"type",      (CHAR *)Lib::toString(build.boType));
  xmlSetProp(bo, (CHAR *)"anz",       (CHAR *)Lib::toString(build.boAnz));
  xmlSetProp(bo, (CHAR *)"intensity", (CHAR *)Lib::toString(build.boIntensity));
  xmlSetProp(bo, (CHAR *)"diff",      (CHAR *)Lib::toString(build.boDiff));
  xmlSetProp(bo, (CHAR *)"base",      (CHAR *)Lib::toString(build.boBase));
  xmlSetProp(bo, (CHAR *)"min",       (CHAR *)Lib::toString(build.boMin));
  xmlSetProp(bo, (CHAR *)"max",       (CHAR *)Lib::toString(build.boMax));
  xmlSetProp(bo, (CHAR *)"offset",    (CHAR *)Lib::toString(build.boOffset));

  //Border (wather)
  xmlNodePtr bw = xmlNewChild(shape, NULL, (CHAR *)"BORDERWATHER", NULL);
  xmlSetProp(bw, (CHAR *)"type",      (CHAR *)Lib::toString(build.bwType));
  xmlSetProp(bw, (CHAR *)"anz",       (CHAR *)Lib::toString(build.bwAnz));
  xmlSetProp(bw, (CHAR *)"intensity", (CHAR *)Lib::toString(build.bwIntensity));
  xmlSetProp(bw, (CHAR *)"diff",      (CHAR *)Lib::toString(build.bwDiff));
  xmlSetProp(bw, (CHAR *)"base",      (CHAR *)Lib::toString(build.bwBase));
  xmlSetProp(bw, (CHAR *)"min",       (CHAR *)Lib::toString(build.bwMin));
  xmlSetProp(bw, (CHAR *)"max",       (CHAR *)Lib::toString(build.bwMax));
  xmlSetProp(bw, (CHAR *)"offset",    (CHAR *)Lib::toString(build.bwOffset));

  //Border Same
  xmlNodePtr bs = xmlNewChild(shape, NULL, (CHAR *)"BORDERSAME", NULL);
  xmlSetProp(bs, (CHAR *)"type",      (CHAR *)Lib::toString(build.bsType));
  xmlSetProp(bs, (CHAR *)"anz",       (CHAR *)Lib::toString(build.bsAnz));
  xmlSetProp(bs, (CHAR *)"intensity", (CHAR *)Lib::toString(build.bsIntensity));
  xmlSetProp(bs, (CHAR *)"diff",      (CHAR *)Lib::toString(build.bsDiff));
  xmlSetProp(bs, (CHAR *)"base",      (CHAR *)Lib::toString(build.bsBase));
  xmlSetProp(bs, (CHAR *)"min",       (CHAR *)Lib::toString(build.bsMin));
  xmlSetProp(bs, (CHAR *)"max",       (CHAR *)Lib::toString(build.bsMax));
  xmlSetProp(bs, (CHAR *)"offset",    (CHAR *)Lib::toString(build.bsOffset));


  //filter
  xmlNodePtr f = xmlNewChild(shape, NULL, (CHAR *)"FILTER", NULL);
  xmlSetProp(f, (CHAR *)"type",      (CHAR *)Lib::toString(build.fType));
  xmlSetProp(f, (CHAR *)"anz",       (CHAR *)Lib::toString(build.fAnz));
  xmlSetProp(f, (CHAR *)"intensity", (CHAR *)Lib::toString(build.fIntensity));
  xmlSetProp(f, (CHAR *)"diff",      (CHAR *)Lib::toString(build.fDiff));
  xmlSetProp(f, (CHAR *)"base",      (CHAR *)Lib::toString(build.fBase));
  xmlSetProp(f, (CHAR *)"min",      (CHAR *)Lib::toString(build.fMin));
  xmlSetProp(f, (CHAR *)"max",      (CHAR *)Lib::toString(build.fMax));
  xmlSetProp(f, (CHAR *)"offset",    (CHAR *)Lib::toString(build.fOffset));

  //order
  xmlSetProp(shape, (CHAR *)"order", (CHAR *)build.order);
  //master-slave
  xmlSetProp(area, (CHAR *)"master", (CHAR *)build.master);
  xmlSetProp(area, (CHAR *)"slave", (CHAR *)build.slave);
};