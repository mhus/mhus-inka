
#include "Genesis.h"
#include "Shape.h"
#include "DisplayHelper.h"
#include "Arrange.h"
#include "ConfigHelper.h"

#include <sys/timeb.h>

ConfigHelper *ConfigHelper::staticConfigHelper = new ConfigHelper();

/*
    arrange = new Arrange(20,20);
    arrange->setWarnings(1);
    arrange->addArea("a",Arrange::MOUNTAINS,3,0);
    arrange->addArea("b",Arrange::DESERT,3,0);
    arrange->addArea("c",Arrange::DESERT,3,0);
    arrange->addArea("d",Arrange::DESERT,3,0);
    arrange->addArea("e",Arrange::MOUNTAINS,2,Area::FWATHER);
    arrange->addArea("f",Arrange::MOUNTAINS,2,Area::FWATHER);
    arrange->addArea("g",Arrange::PLATO,1,0);
    arrange->addArea("h",Arrange::MOUNTAINS,2,Area::FWATHER);
    arrange->addArea("i",Arrange::MOUNTAINS,2,Area::FWATHER);

    arrange->addArea("j",Arrange::ISLANDS,2,Area::FISLAND);
    arrange->addArea("k",Arrange::ISLANDS,1,0);
    arrange->addArea("l",Arrange::ISLANDS,1,0);
    arrange->addArea("m",Arrange::ISLANDS,2,Area::FISLAND);
    arrange->addArea("n",Arrange::ISLANDS,1,0);
    arrange->addArea("o",Arrange::ISLANDS,1,Area::FISLAND);
  
    arrange->addArea("p",Arrange::MOUNTAINS,2,Area::FBREAK);
    arrange->addArea("q",Arrange::MOUNTAINS,3,0);
    arrange->addArea("r",Arrange::MOUNTAINS,2,0);
    arrange->addArea("s",Arrange::WOOD,2,0);
    arrange->addArea("t",Arrange::WOOD,2,0);
    arrange->addArea("u",Arrange::WOOD,2,0);
    arrange->addArea("v",Arrange::MARSH,2,0);
    arrange->addArea("w",Arrange::MARSH,2,0);
    arrange->addArea("x",Arrange::MARSH,2,0);
    arrange->addArea("y",Arrange::MARSH,2,0);
    arrange->addArea("z",Arrange::LAKES,2,0);
    arrange->addArea("A",Arrange::LAKES,2,0);
    arrange->addArea("B",Arrange::LAKES,2,0);
    arrange->addArea("C",Arrange::WOOD,2,0);
    arrange->addArea("D",Arrange::MOUNTAINS,3,0);
    arrange->addArea("E",Arrange::MOUNTAINS,2,0);
*/

void doGenesis() {

  DisplayHelper *display = new DisplayHelper();
  Genesis *g;

  if (ConfigHelper::staticConfigHelper->parseXML("genesis.xml") != 0) {
    printf("XML error\n");
    return;
  }

  if ((g = Genesis::fromXML("GENESIS=MAIN")) == NULL) {
    printf("Genesis is NULL\n");
    return;
  }

  g->generateAll();

  delete g;
  delete display;
};

void main() {

  doGenesis();

}