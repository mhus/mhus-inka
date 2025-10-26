

#ifndef _RWHELPER_H
#define _RWHELPER_H

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define BUFFERS 200
#define LEN     50

class RWHelper {


  struct buffer_ {
    int change;
    int ready;
    int count;
    int bx;
    int by;
    int p[LEN][LEN];
  };

  buffer_ buffers[BUFFERS];
  char *name;
  char *path;
  int readonly;
  int  type;
  
  int def;
  

public:

  static const int ASCII  = 1;
  static const int BINARY = 2; //depricated
  static const int BYTE = 2;
  static const int WORD = 3;
  static const int COMPRIMIERT = 4; //not in use yet

  RWHelper();
  RWHelper(char *_path, char *_name, int _type, bool _readonly);
  ~RWHelper();
  
  void setReadonly(int _readonly);
  int getReadonly();
  void setName(char *_name);
  char *getName();
  void setPath(char *_path);
  char *getPath();
  void setDefault(int _default);

  void set(int _x, int _y, int _v);
  int get(int _x, int _y);
  
  void save(int _nr);
  void load(int _nr, int _bx, int _by);
  void saveAll();
  int  find(int _bx, int _by);
  void flush();
  void clear();
  void setUpToDate(int _nr);
};

#endif