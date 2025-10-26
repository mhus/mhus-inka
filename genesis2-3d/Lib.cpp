
#include "Lib.h"

char *Lib::toString(int i) {
  char *txt;
  if (!(txt = (char *)malloc(15))) {
    printf("Lib: cant allocate mem\n");
  }

  sprintf(txt,"%i",i);
  return txt;
};