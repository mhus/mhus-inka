
#ifndef _CONFIGHELPER_H
#define _CONFIGHELPER_H

#include <stdlib.h>
#include <stdio.h>
#include <gnome-xml/tree.h>
#include <gnome-xml/parser.h>
#include "StrHashTable.h"

//#define DEBUG_CONFIGHELPER

class ConfigHelper {


  //other
  xmlDocPtr doc;       // original document 
  xmlNodePtr ref;    // linked references
  xmlNodePtr work;   //working tree;
  xmlNodePtr consts; //constances tree

  StrHashTable<xmlDoc>  docs;
  StrHashTable<xmlNode> roots;
  StrHashIterator<xmlDoc> *docIter;

  int  parseXML();  // init document from "doc"
  xmlDocPtr getDoc(char *_file); // get and parse xml file
  
  xmlNodePtr getNodeRaw(xmlNodePtr _tree, char *_element);
  char *getPropertyRaw(xmlNodePtr _tree, char *_name);
  bool existsPropertyRaw(xmlNodePtr _tree, char *_name);
  xmlNodePtr getNamedNodeRaw(xmlNodePtr _tree,
                             char *_element,
                             char *_property,
                             char *_value);
  
public:

  //static
  //single entry point
  static ConfigHelper *staticConfigHelper;

  ConfigHelper();
  ~ConfigHelper();

  xmlDocPtr newDocument(char *_name);
  int addDocument(char *_fname);
  int saveDocument(char *_name, char *_fname);

  int  parseXML(char *_fname); //parse and init document _fname

  xmlNodePtr getNode(xmlNodePtr _tree, char *_element);
  char *getProperty(xmlNodePtr _tree, char *_name, char *_def);
  char *getProperty(xmlNodePtr _tree, char *_name);
  bool existsProperty(xmlNodePtr _tree, char *_name);
  xmlNodePtr getNamedNode(xmlNodePtr _tree,
                             char *_element,
                             char *_property,
                             char *_value);
  xmlNodePtr getNodeAt(xmlNodePtr _tree,
                       char *_element,
                       int _nr);
  xmlNodePtr getNodeFrom(xmlNodePtr _tree, char *_path);  

  int getConstInt(char *_name);
  char *getConstStr(char *_name);
  
  xmlNodePtr getRoot(); //get work tree

};
#endif