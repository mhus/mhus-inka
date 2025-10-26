

#include "RWHelper.h"

RWHelper::RWHelper() {};

RWHelper::RWHelper(char *_path, char *_name, int _type, bool _readonly){

  path = _path;
  name = _name;
  type = _type;
  readonly = _readonly;

  def = 0;

  for (int i=0;i<BUFFERS;i++)
   buffers[i].ready = 0;

};

RWHelper::~RWHelper() {};
  
void RWHelper::setReadonly(int _readonly) {
  readonly = _readonly;
};

int RWHelper::getReadonly() {
  return readonly;
};

void RWHelper::setName(char *_name) {
  name = _name;
};

char *RWHelper::getName() {
  return name;
};

void RWHelper::setPath(char *_path) {
  path = _path;
};

char *RWHelper::getPath() {
  return path;
};

void RWHelper::setDefault(int _default) {
  def = _default;
};

void RWHelper::set(int _x, int _y, int _v) {

  int nr;	

  if (readonly != 0) return;

//  if(_x<0 || _y<0) return;

  nr = find(_x / LEN, _y / LEN);  
//--printf("Put %i\n",nr);       
  if (buffers[nr].p[_x % LEN][_y % LEN] != _v) {
    buffers[nr].p[_x % LEN][_y % LEN] = _v;
    buffers[nr].change = 1;
  }

  setUpToDate(nr);

}; 

int RWHelper::get(int _x, int _y) {
  int nr;
  nr = find(_x / LEN, _y / LEN);
  setUpToDate(nr);
//--printf("Get %i\n",nr);
  return buffers[nr].p[_x % LEN][_y % LEN];
};

int RWHelper::find(int _bx,int _by) {
  int nr,count;

  //schon vorhanden
  for (nr=0;nr<BUFFERS;nr++)
    if (buffers[nr].ready != 0 &&
        buffers[nr].bx == _bx && 
        buffers[nr].by == _by) return nr;

  //find oldest or not ready
  nr = -1; count = -1;
  for (int i=0;i<BUFFERS;i++) {

    if (buffers[i].ready == 0) {
      count = 60000;
      nr = i;
    }

    if (buffers[i].count>count) {
      count = buffers[i].count;
      nr = i;
    }
  }
  
  if (nr == -1) {
    printf("RWHelper: no old found\n");
    nr=0;
  }

  load(nr,_bx,_by);

  return nr;
}
  
void RWHelper::save(int _nr) {

  char fname[100];
  char txt[20];
  FILE *handle;

//--  printf("Try to save buffer %i to %s %i %i\n",_nr,name,buffers[_nr].bx,buffers[_nr].by);

  if (readonly != 0) return;               //readonly ....

  if (buffers[_nr].change == 0 ||
       buffers[_nr].ready == 0) return; // no changes or not ready

  // save
  // get filename
  strcpy(fname,path);
  strcat(fname,name);
  strcat(fname,"_");
  sprintf(txt,"%i_%i",buffers[_nr].bx,buffers[_nr].by);
  strcat(fname,txt);
  // try to open
//--printf("Save %s\n",fname);
  handle = fopen(fname,"w");   //open
  if (handle != NULL) {

    fputc(type+'0', handle); //save type
    fputc('\n'    , handle);     //and a return to close

    // save to file
    for (int x=0;x<LEN;x++)
    for (int y=0;y<LEN;y++) {
      if (type == ASCII) {
        fprintf(handle, "%i ", buffers[_nr].p[x][y]);
        if (y == LEN-1) fprintf(handle,"\n");
      } else if (type == BYTE) {
        fputc(buffers[_nr].p[x][y], handle); 
      } else if (type == WORD) {
        fputc(buffers[_nr].p[x][y] % 256, handle);
        fputc(buffers[_nr].p[x][y] / 256, handle);
      }
    }

    // set no changes
    buffers[_nr].change = 0;
    fclose(handle);
  } else {
    // not savable
    printf("RWHelper: cant save %s\n",fname);
  }


};

void RWHelper::load(int _nr, int _bx, int _by) {

  char fname[100];
  char txt[20];
  FILE *handle;
  int myType;

  int i;

  // first save if needed
  save(_nr);

//--  printf("Try to load buffer %i %s %i %i\n",_nr,name,_bx,_by);

  // load  
  // get filename
  strcpy(fname,path);
  strcat(fname,name);
  strcat(fname,"_");
  sprintf(txt,"%i_%i",_bx,_by);
  strcat(fname,txt);
//--printf("Load: %s\n",fname);
  // try to open
  handle = fopen(fname,"r"); // open file
  if (handle != NULL) {

    // get file-format
    myType = fgetc(handle)-'0';
    fgetc(handle); //get return char

#ifdef DEBUG_RWHELPER
    if (myType  < 1 && myType > 3) printf("RWHelper: unknown type %i in %s\n",myType,fname);
#endif

    // load from file
    for (int x=0;x<LEN;x++)
    for (int y=0;y<LEN;y++) {
      if (myType == ASCII) {
        fscanf(handle, "%s",txt);
        buffers[_nr].p[x][y]=atoi(txt);
      } else if (myType == BYTE) {
        buffers[_nr].p[x][y] = (unsigned char)fgetc(handle); 
      } else if (myType == WORD) {
        buffers[_nr].p[x][y] = (unsigned char)fgetc(handle) +
                               (unsigned char)fgetc(handle)*256;
      }
    }
    fclose(handle);
  } else {

    // no file exist, set default
    for (int x=0;x<LEN;x++)
    for (int y=0;y<LEN;y++)
      buffers[_nr].p[x][y] = def;
  }
  buffers[_nr].bx = _bx;
  buffers[_nr].by = _by;
  buffers[_nr].ready = 1;
  buffers[_nr].change = 0;
  buffers[_nr].count = -1;
};

void RWHelper::saveAll() {
  for (int i=0;i<BUFFERS;i++) save(i);
};

void RWHelper::flush() {
  for (int i=0;i<BUFFERS;i++) buffers[i].ready=0;
};

void RWHelper::clear() {
  char txt[400];
  sprintf(txt,"rm %s%s_*",path,name);
printf("Clear: %s\n",txt);
  system(txt);
};

void RWHelper::setUpToDate(int _nr) {
  if (buffers[_nr].count == 0) return;
  for (int i=0;i<BUFFERS;i++) buffers[i].count++;
  buffers[_nr].count=0;
};