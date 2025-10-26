#ifndef _STRHASHTABLE_H
#define _STRHASHTABLE_H

#include <stdlib.h>
#include <String.h>


template<class T>
class StrHashContainer {

  char *key;
  T *value;
  StrHashContainer *next;
  
public:
  
  StrHashContainer() {
    next = NULL;
    value = NULL;
    key = NULL;
  };
  
  ~StrHashContainer() {
    if (next != NULL) delete next;
  };
  
  void setKey(char *_key) {
    key = _key;
  };
  
  char *getKey() {
    return key;
  };
  
  void setValue(T *_value) {
    value = _value;
  };
  
  void setNext(StrHashContainer *_next) {
    next = _next;
  };
  
  T *getValue() {
    return value;
  };
  
  StrHashContainer *getNext() {
    return next;
  };
  
  T *get(char *_key) {
    if (strcmp(_key,key) == 0) return value;
    if (next == NULL) return NULL;
    return next->get(_key);
  };
  
  void remove(char *_key) {
    if (next == NULL) return;
    if (strcmp(next->getkey(),_key) == 0) {
      StrHashContainer<T> old = next;
      next = old->getNext();
      delete old;
      return;
    }
    next->remove(_key);
  };

};

template <class T>
class StrHashIterator {

  StrHashContainer<T> *next;
  StrHashContainer<T> *old;
  
public:
  StrHashIterator(StrHashContainer<T> *_next) {
    next = _next;
    old = NULL;
  };
  
  bool hasNext() {
    if (next != NULL) return true;
    return false;
  }
  
  T *getNext() {
    if (next == NULL) return NULL;
    old  = next;
    next = next->getNext();
    return old->getValue();
  };

  char *getKey() {
    if (old == NULL) return "";
    return old->getKey();
  };

  void setValue(T *_value) {
    if (old == NULL) return;
    old->setValue(_value);
  };
  
};


template <class T>
class StrHashTable {

  StrHashContainer<T> *container;

public:

  StrHashTable() {
    container = NULL;
  };
  ~StrHashTable() {
    if (container != NULL) delete container;
  };

  void put(char *_key, T *_val) {
    if (_key == NULL || _val == NULL) return;
    
    StrHashContainer<T> *old = container;
    container = new StrHashContainer<T>();
    container->setKey(_key);
    container->setValue(_val);
    container->setNext(old);
  };

  T *get(char *_key) {
    if (container == NULL) return NULL;
    return container->get(_key);
  };
  
  void remove(char *_key) {
    if (container==NULL) return;
    if (strcmp(container->getkey(),_key) == 0) {
      StrHashContainer<T> *old = container;
      container = old->getNext();
      delete old;
      return;
    }
    container->remove(_key);
  };
  bool contains(char *_key) {
    if (container == NULL) return false;
    StrHashContainer<T> *cur = container;
    while (strcmp(cur->getKey(),_key) != 0) {
      cur = cur->getNext();
      if (cur == NULL) return false;
    }
    return true;
  }; 
  
  int size() {
    StrHashContainer<T> *cur = container;
    int i=0;
    while (cur != NULL) {i++;cur = cur->getNext();}
    return i;
  };
  int clear() {
    if (container != NULL) delete container;
  };

  StrHashIterator<T> *getIterator() {
    return new StrHashIterator<T>(container);
  };
  
};


#endif