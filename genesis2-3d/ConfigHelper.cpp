#include "ConfigHelper.h"

/*******************************************************************
 * Constructor and destructor
 ******************************************************************/
ConfigHelper::ConfigHelper() {
  doc = newDocument("NEW");
};

ConfigHelper::~ConfigHelper() {
  for (docIter = docs.getIterator(); docIter->hasNext() == true;) {
    xmlFreeDoc(docIter->getNext());
  }
  delete docIter;
  docs.clear();

  work   = NULL;
  ref    = NULL;
  consts = NULL;
};

/**********************************************************************
 * Save a document from the document list
 * _name  name of root
 * _fname name to save file
 *********************************************************************/
int ConfigHelper::saveDocument(char *_name, char *_fname) {

  //find root
  if (roots.contains(_name) == false) {
    printf("ConfigHelper: cant save %s: root not found\n",_name);
    return 1;
  }
  xmlNodePtr root_ = roots.get(_name);
  int e;
  if ((e = xmlSaveFile(_fname,root_->doc)) <= 0) {
    printf("ConfigHelper: cant save %s to %s: xml error %i\n",_name,_fname,e);
    return 2;
  }

  return 0;
};

/**********************************************************************
 * Add a document to the document list
 *********************************************************************/
int ConfigHelper::addDocument(char *_fname) {

  StrHashTable<char> libs;
  StrHashIterator<char> *libsIter;
  char *ok;
  char *libname;
  bool loadone;
  xmlNodePtr cur;
  xmlDocPtr  doc_ = NULL;

  libs.clear();
  libs.put(_fname,"load");

  // lets load
  do {
    loadone = false;
  
    libsIter=libs.getIterator();
    while (libsIter->hasNext()) {
      ok = libsIter->getNext();
      libname = libsIter->getKey();
      if (strcmp(ok,"load") == 0 && (doc_ = getDoc(libname)) != NULL) {
        if (roots.contains((char *)doc_->root->name) == false) {
          loadone = true;
          printf("ConfigHelper: load lib %s as %s\n",libname,doc_->root->name);
          //add to system
          docs.put(libname,doc_);
          roots.put((char *)doc_->root->name,doc_->root);
          //search new libs
          for (int i=0;(cur = getNodeAt(doc_->root,"LIB",i)) != NULL;i++) {
            if (libs.contains(getPropertyRaw(cur,"FILE")) == false)
              libs.put(getPropertyRaw(cur,"FILE"),"load");
          }
        } else {
          printf("ConfigHelper: doc %s already included\n",doc_->root->name);
          printf("  File:   %s\n",libname);
          printf("  Caller: %s\n",cur->doc->root->name);
          xmlFreeDoc(doc_);
        } // END if in roots
      } else {
        if (strcmp(ok,"load") == 0) {
          printf("ConfigHelper: error in %s\n",libname);
          if (strcmp(libname,_fname) == 0) return 1;
        }
      }//END if doc loaded

      libsIter->setValue("ok"); //mark as loaded

    } // END for all libs
    delete libsIter;

  } while (loadone == true);


  return 0;
}

/**********************************************************************
 * Parse file with gnome-XML parser and create DOM
 *********************************************************************/
xmlDocPtr ConfigHelper::getDoc(char *_fname) {

  if (_fname == NULL) {
    printf("ConfigHelper: no Filename\n");
    return NULL;
  }

  xmlDocPtr doc_ = xmlParseFile(_fname);
  if (doc_ == NULL || doc_->root == NULL) {
    printf("ConfigHelper: parse error in %s\n",_fname);
    return NULL;
  }

  return doc_;
};

/**********************************************************************
 * Parse file and Init system for the document
 *********************************************************************/
int ConfigHelper::parseXML(char *_file) {

  if (docs.contains(_file)) {
    printf("ConfigHelper: file already loaded %s\n",_file);
    return -1;
  }

  if((doc = getDoc(_file)) == NULL) return 2;

  // add doc to docs
  docs.put(_file,doc);

  return parseXML();
};

/*********************************************************************
 * Main initializing routine after parsing
 ********************************************************************/
int ConfigHelper::parseXML() {

  xmlNodePtr cur;
  xmlDocPtr  doc_ = NULL;

#ifdef DEBUG_CONFIGHELPER
  printf("ConfigHelper: parse document\n");
#endif

  // is doc ready ?
  if (doc == NULL || doc->root == NULL) {
    printf("ConfigHelper: no DOM document parsed\n");

    doc = newDocument("GENESIS");
    work = xmlNewChild(doc->root,NULL, (CHAR *)"CONFIG",NULL);
    ref  = xmlNewChild(doc->root,NULL, (CHAR *)"REFERENCES",NULL);
    consts = xmlNewChild(doc->root,NULL, (CHAR *)"CONST",NULL);
    // docs.put("GENESIS", doc); already by new document

    return 2;
  }

  if (strcmp((char *)doc->root->name,"GENESIS") != 0) {
    printf("ConfigHelper: no GENESIS document\n");
    return 5;
  }

  // get CONFIG and REFERENCES
  work   = getNodeRaw(doc->root,"CONFIG");
  ref    = getNodeRaw(doc->root,"REFERENCES");
  consts = getNodeRaw(doc->root,"CONST");

  // no REFERENCES specified, create dummy
  if (ref == NULL) {
    printf("ConfigHelper: no REFERENCES found\n");
    ref = xmlNewChild(doc->root, NULL,(CHAR *)"REFERENCES", NULL);
  }

  // no CONST specified, create dummy
  if (consts == NULL) {
    printf("ConfigHelper: no CONST found\n");
    consts = xmlNewChild(doc->root, NULL,(CHAR *)"CONST", NULL);
  }

  // no CONFIG found
  if (work == NULL) {
    printf("ConfigHelper: no CONFIG found\n");
    work = xmlNewChild(doc->root, NULL,(CHAR *)"CONFIG", NULL);
    return 1;
  }



  //add libs, get root lib-files from GENESIS and add Documents
  for (int i=0;(cur = getNodeAt(doc->root,"LIB",i)) != NULL;i++)
    addDocument(getPropertyRaw(cur,"FILE"));


  // if a reference to a LIB ?
  if (existsPropertyRaw(ref,"LIB")) {
    printf("ConfigHelper: load LIB for REFERENCES\n");
    char *name = getPropertyRaw(ref,"LIB");
    if (roots.contains(name) == true) ref = roots.get(name);
    else printf("ConfigHelper: LIBREFERENCE for REFERENCE not found\n");
  }
  // if a const to a LIB ?
  if (existsPropertyRaw(consts,"LIB")) {
    printf("ConfigHelper: load LIB for CONST\n");
    char *name = getPropertyRaw(consts,"LIB");
    if (roots.contains(name) == true) consts = roots.get(name);
    else printf("ConfigHelper: LIBREFERENCE for CONST not found\n");
  }
  // if config to a LIB ?
  if (existsPropertyRaw(work,"LIB")) {
    printf("ConfigHelper: load LIB for CONFIG\n");
    char *name = getPropertyRaw(work,"LIB");
    if (roots.contains(name) == true) work = roots.get(name);
    else printf("ConfigHelper: LIBREFERENCE for CONFIG not found\n");
  }

  return 0;

};

/*******************************************************************
 * Initialize a new document
 *******************************************************************/
xmlDocPtr ConfigHelper::newDocument(char *_name) {

#ifdef DEBUG_CONFIGHELPER
  printf("ConfigHelper: create new document\n");
#endif

  if (roots.contains(_name) == true) return NULL;

  xmlDocPtr doc_ = xmlNewDoc((CHAR *)"1.0");
  doc_->root = xmlNewDocNode(doc_, NULL, (CHAR *)_name, NULL);

  docs.put("[new]",doc_);
  roots.put(_name,doc_->root);

  return doc_;

};




/*********************************************************************
 * gets a Node named _element, ignore references
 ********************************************************************/
xmlNodePtr ConfigHelper::getNodeRaw(xmlNodePtr _tree, char *_element) {

#ifdef DEBUG_CONFIGHELPER
  printf("  GetNodeRaw: %s\n",_element);
#endif

  if (_tree == NULL) return NULL;

  xmlNodePtr cur = _tree->childs;
  if (cur == NULL) return NULL;

  while (strcmp((char *)cur->name,_element) != 0) {
    cur = cur->next;
    if (cur == NULL) return NULL;
  }

#ifdef DEBUG_CONFIGHELPER
  printf("  GetNodeRaw (found) %s\n",_element);
#endif

  return cur;
};

/***********************************************************************
 * gets a property value, ignore references
 **********************************************************************/
char *ConfigHelper::getPropertyRaw(xmlNodePtr _tree, char *_property) {

#ifdef DEBUG_CONFIGHELPER
  printf("  GetPropertyRaw %s\n",_property);
#endif

  if (_tree == NULL) return "";

  // get first property
  xmlAttrPtr cur = _tree->properties;
  if (cur == NULL) return "";

  while(strcmp((char *)cur->name,_property) != 0) {
    cur = cur->next;
    if (cur == NULL) return "";
  }

#ifdef DEBUG_CONFIGHELPER
  printf("  GetPropertyRaw (found) %s = %s\n",_property,cur->val->content);
#endif

  return (char *)cur->val->content;
}

/*************************************************************************
 * test if exist a property (getPropertyRaw returns "" in this case)
 * ignboring references
 ************************************************************************/
bool ConfigHelper::existsPropertyRaw(xmlNodePtr _tree, char *_property) {

#ifdef DEBUG_CONFIGHELPER
  printf("  ExistsPropertyRaw %s\n",_property);
#endif

  if (_tree == NULL) return 0;

  xmlAttrPtr cur = _tree->properties;
  if (cur == NULL) return 0;

  while(strcmp((char *)cur->name,_property) != 0) {
    cur = cur->next;
    if (cur == NULL) return 0;
  }

  if (cur->val->content == NULL) return 0;
  if (strcmp((char *)cur->val->content,"") == 0) return 0;

  return 1;
}

/******************************************************************
 * gets a node with a specified property (e.g. ID="MAIN"),
 * ignores references
 *****************************************************************/
xmlNodePtr ConfigHelper::getNamedNodeRaw(xmlNodePtr _tree, 
                                            char *_element,
                                            char *_property,
                                            char *_value) {

#ifdef DEBUG_CONFIGHELPER
  printf("  GetNamedNodeRaw %s %s = %s\n",_element,_property,_value);
#endif

  if (_tree == NULL) return NULL;
  xmlNodePtr cur = _tree->childs;
  if (cur == NULL) return NULL;

  while (strcmp( (char *)cur->name, _element ) != 0 ||
         strcmp( getPropertyRaw( cur, _property ), _value ) != 0 ) {
    cur = cur->next;
    if (cur == NULL) return NULL;
  }

#ifdef DEBUG_CONFIGHELPER
  printf("  GetNamedNodeRaw (found) %s %s = %s\n",_element,_property,_value);
#endif

  return cur;

};





/*********************************************************************
 * gets a Node named _element
 ********************************************************************/
xmlNodePtr ConfigHelper::getNode(xmlNodePtr _tree, char *_element) {

#ifdef DEBUG_CONFIGHELPER
  printf("GetNode: %s\n",_element);
#endif

  if (_tree == NULL) return NULL;

  // get original node
  xmlNodePtr cur = getNodeRaw(_tree, _element);

  // no original node, check reference node
  if (cur == NULL &&
      ref != NULL &&
      existsPropertyRaw(_tree, "REF")) {

    // get Reference 
    xmlNodePtr ref_ = getNodeFrom(ref, getPropertyRaw(_tree, "REF"));
/*
    xmlNodePtr ref_ = getNamedNodeRaw(ref, 
                                      (char *)_tree->name,
                                      "ID",
                                      getPropertyRaw(_tree, "REF"));
*/
    // try question in reference node
    if (ref_ != NULL) cur = getNode(ref_, _element);
  }

#ifdef DEBUG_CONFIGHELPER
  printf("GetNode (found) %s\n",_element);
#endif

  return cur;
};

/***********************************************************************
 * gets a property value
 **********************************************************************/

char *ConfigHelper::getProperty(xmlNodePtr _tree,
                                char *_property,
                                char *_def) {
  if (existsProperty(_tree, _property)) {
    return getProperty(_tree, _property);
  } else {
    return _def;
  }
};

char *ConfigHelper::getProperty(xmlNodePtr _tree, char *_property) {
  char *cur = "";

#ifdef DEBUG_CONFIGHELPER
  printf("GetProperty %s\n",_property);
#endif

  if (_tree == NULL) return "";

  //try original property
  if (existsPropertyRaw(_tree, _property)) {
    cur = getPropertyRaw(_tree, _property);
  } else {

    // lets get reference
    if (ref != NULL &&
      existsPropertyRaw(_tree, "REF")) {

      // get Reference
      xmlNodePtr ref_ = getNodeFrom(ref, getPropertyRaw(_tree,"REF"));
/*
      xmlNodePtr ref_ = getNamedNodeRaw(ref, 
                                        (char *)_tree->name,
                                        "ID",
                                        getPropertyRaw(_tree,"REF"));
*/
      // try question in reference node
      if (ref_ != NULL) cur = getProperty(ref_, _property);
    }

  } //END if existsPropertyRaw

#ifdef DEBUG_CONFIGHELPER
  printf("GetProperty (found) %s = %s\n",_property, cur );
#endif

  return cur;
};

/*************************************************************************
 * test if exist a property (getPropertyRaw returns "" in this case)
 ************************************************************************/
bool ConfigHelper::existsProperty(xmlNodePtr _tree, char *_property) {

#ifdef DEBUG_CONFIGHELPER
  printf("ExistsProperty %s\n",_property);
#endif

  if (_tree == NULL) return "";

  //try original property
  if (existsPropertyRaw(_tree, _property)) {
    return 1;
  } else {

    // lets get reference
    if (ref != NULL &&
      existsPropertyRaw(_tree, "REF")) {

      // get Reference
      xmlNodePtr ref_ = getNodeFrom(ref, getPropertyRaw(_tree,"REF"));
/*
      xmlNodePtr ref_ = getNamedNodeRaw(ref,
                                        (char *)_tree->name,
                                        "ID",
                                        getPropertyRaw(_tree,"REF"));
*/
      // try question in reference node
      if (ref_ != NULL && 
          existsProperty(ref_, _property)) return 1;
    }

  } //END if existsPropertyRaw

  return 0;

};

/******************************************************************
 * gets a node with a specified property (e.g. ID="MAIN")
 *****************************************************************/
xmlNodePtr ConfigHelper::getNamedNode(xmlNodePtr _tree, 
                                            char *_element,
                                            char *_property,
                                            char *_value) {

#ifdef DEBUG_CONFIGHELPER
  printf("GetNamedNode %s %s = %s\n",_element,_property,_value);
#endif

  if (_tree == NULL) return NULL;

  // get original node
  xmlNodePtr cur = getNamedNodeRaw(_tree, _element, _property, _value);

  // no original node, check reference node
  if (cur == NULL &&
      ref != NULL &&
      existsPropertyRaw(_tree, "REF")) {

    // get Reference
      xmlNodePtr ref_ = getNodeFrom(ref, getPropertyRaw(_tree,"REF"));
/*
    xmlNodePtr ref_ = getNamedNodeRaw(ref, 
                                      (char *)_tree->name,
                                      "ID",
                                      getPropertyRaw(_tree,"REF"));
*/
    // try question in reference node
    if (ref_ != NULL) cur = getNamedNode(ref_, _element, _property, _value);
  }

#ifdef DEBUG_CONFIGHELPER
  printf("GetNamedNode (found) %s %s = %s\n",_element,_property,_value);
#endif

  return cur;
};

/******************************************************************
 * gets a Node with a specifed position, if not available its NULL
 *****************************************************************/
xmlNodePtr ConfigHelper::getNodeAt(xmlNodePtr _tree, 
                                        char *_element,
                                        int _nr) {

#ifdef DEBUG_CONFIGHELPER
  printf("GetNodeAt %s:%i\n",_element,_nr);
#endif

  if (_tree == NULL || _nr<0) return NULL;

  int count = 0;
  xmlNodePtr cur;
  xmlNodePtr ref_ = NULL;

  //first search in REF *************************************************
  if (ref != NULL && existsPropertyRaw(_tree,"REF")) {
    // get Reference
   ref_ = getNodeFrom(ref, getPropertyRaw(_tree,"REF"));
/*
   ref_ = getNamedNodeRaw(ref,
                           (char *)_tree->name,
                           "ID",
                           getPropertyRaw(_tree,"REF"));
*/
    if (ref_ != NULL) {
      //lets go
      cur = ref_->childs;                                //get first
      while (cur != NULL) {                             //is not NULL
        if (strcmp((char *)cur->name,_element) == 0 &&  //and TOP
            strcmp(getPropertyRaw(cur,"LOCATION"),"TOP") == 0 ) 
        {                                               //is it one
          //found the one ?
         if (count == _nr) return cur;                    //right nr
         count++;                                       //add found-counter
        } // if name == _element
        cur = cur->next;                                //next one
      } // while cur != NULL
    } // if ref_ != NULL
  } // if REF available

  //search in original *************************************************
  cur = _tree->childs;                               //get first
  while (cur != NULL) {                             //is not NULL
    if (strcmp((char *)cur->name,_element) == 0) {  //is it one
      //found the one ?
     if (count == _nr) return cur;                    //right nr
     count++;                                       //add found-counter
    } // if name == _element
    cur = cur->next;                                //next one
  } // while cur != NULL

  //finaly search in REF *************************************************
  if (ref_ != NULL) {
    //lets go
    cur = ref_->childs;                                //get first
    while (cur != NULL) {                             //is not NULL
      if (strcmp((char *)cur->name,_element) == 0 &&  //and not TOP
          strcmp(getPropertyRaw(cur,"LOCATION"),"TOP") != 0 ) 
      {                                               //is it one
        //found the one ?
       if (count == _nr) return cur;                    //right nr
       count++;                                       //add found-counter
      } // if name == _element
      cur = cur->next;                                //next one
    } // while cur != NULL
  } // if ref_ != NULL

  //not available
  return NULL;
};


/******************************************************************
 * gets a Node, use a path to find it, seperated with . for the
 * elements, use = to specifie an ID and a : to define a root
 * document name (if root is "" use current)
 * e.g
 *    ARRANGE.AREA=happyland.SHAPE to get shape-tree from happyland
 *****************************************************************/
xmlNodePtr ConfigHelper::getNodeFrom(xmlNodePtr _tree, char *_path) {

  int count = 0;
  int count_el = 0;
  int count_na = 0;
  int mode = 0;
  unsigned char ch;
  char element[200] = "";
  char name[200] = "";

#ifdef DEBUG_CONFIGHELPER
  printf("GetNodeFrom %s\n",_path);
#endif

  if (_tree == NULL || _path == NULL || _path[0] == 0 ) return NULL;

  do {
    ch = _path[count++];

    if (ch == ':') {
      // use "element" as root

      if (element[0] == 0) {
        // use "this" root as root
        _tree = _tree->doc->root;
      } else {
        //get a root from array of config roots
        if (roots.contains(element)) {
          _tree = roots.get(element);
        } else {
#ifdef DEBUG_CONFIGHELPER
          printf("GetNodeFrom (not found)\n  -> in %s\n  -> ROOT = %s\n",
          _path, element);
#endif
          return NULL;
        }
      }
      //init
      element[0]=0;
      count_el=0;
      name[0]=0;
      count_na=0;
      mode=0;  
    } else
    if (ch == '=') {
      mode=1;
    } else 
    if (ch == '.' || ch == 0) {

      //get new element
      if (element[0] != 0) {
        if (name[0] == 0) {
          _tree = getNode(_tree, element);
        } else {
          _tree = getNamedNode(_tree, element, "ID", name);
        }
      }

      //not found
      if (_tree == NULL) {
#ifdef DEBUG_CONFIGHELPER
        printf("GetNodeFrom (not found)\n  -> in %s\n  -> at %s  ID = %s\n",
          _path, element, name);
#endif
        return NULL;
      }

      //init
      element[0]=0;
      count_el=0;
      name[0]=0;
      count_na=0;
      mode=0;

    } else
    if (ch != 0) {
      if (mode == 0) {
        element[count_el++] = ch;
        element[count_el] = 0;
        if (count_el > 199) count_el = 199;
      } else
      if (mode == 1) {
        name[count_na++] = ch;
        name[count_na] = 0;
        if (count_na > 199) count_na = 199;
      }
    }
  } while (ch != 0);

#ifdef DEBUG_CONFIGHELPER
        printf("GetNodeFrom (found) %s in %s\n",
          _path,_tree->name);
#endif
  return _tree;

};




/**********************************************************************
 * gets constante definitions
 *********************************************************************/
int ConfigHelper::getConstInt(char *_name) {
  xmlNodePtr cur;

#ifdef DEBUG_CONFIGHELPER
  printf("  -> getConstInt %s\n",_name);
#endif

  if (_name == NULL) return 0;
  if (_name[0] == 0) return 0;
  if ((cur = getNodeFrom(consts,_name)) != NULL) {

#ifdef DEBUG_CONFIGHELPER
    printf("  -> getConstInt (found) %s\n",_name);
#endif

    return atoi(getPropertyRaw(cur,"int"));
  } else {

#ifdef DEBUG_CONFIGHELPER
    printf("  -> getConstInt (not found) %s\n",_name);
#endif

    return atoi(_name);
  }
};


/**********************************************************************
 * gets constante definitions
 *********************************************************************/
char *ConfigHelper::getConstStr(char *_name) {
  xmlNodePtr cur;

#ifdef DEBUG_CONFIGHELPER
    printf("  -> getConstStr %s\n",_name);
#endif

  if (_name == NULL) return 0;
  if (_name[0] == 0) return 0;
  if ((cur = getNodeFrom(consts,_name)) != NULL) {

#ifdef DEBUG_CONFIGHELPER
    printf("  -> getConstStr (found) %s\n",_name);
#endif

    return getPropertyRaw(cur,"str");
  } else {

#ifdef DEBUG_CONFIGHELPER
    printf("  -> getConstStr (not found) %s\n",_name);
#endif

    return _name;
  }
};

/**************************************************************************
 * get work tree (its the main root), used by fromXML
 *************************************************************************/

  xmlNodePtr ConfigHelper::getRoot() {
    return work;
  };

