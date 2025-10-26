#include "StrHashTable.h"

#include <stdio.h>

int main() {


 StrHashTable<char> *hash;
 StrHashIterator<char> *iter;

 hash = new StrHashTable<char>;

hash->put("a","Aloa");
hash->put("b","Blob");
hash->put("x","XXL");  

 printf("Size %i\n", hash->size() );
 printf("key b is %s\n",hash->get("b"));

 printf("Containts d: %i\n",hash->contains("d"));
 printf("Containts a: %i\n",hash->contains("a"));

 for (iter = hash->getIterator();iter->hasNext();
   printf("Value: %s\n",iter->getNext())) {}

 return 0;
}