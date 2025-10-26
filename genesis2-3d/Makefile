


CC = cc
LIBS = -lvga -lxml -lz
OBJ = ShapeFactory.o Lib.o Shape.o DisplayHelper.o Arrange.o Area.o RWHelper.o ConfigHelper.o Genesis.o

#init:
#	mount /dev/sda1 /drv/big0

DisplayHelper.o: DisplayHelper.cpp DisplayHelper.h
	$(CC) -c DisplayHelper.cpp

Shape.o: Shape.cpp Shape.h RWHelper.o
	$(CC) -c Shape.cpp

ShapeFactory.o: ShapeFactory.cpp ShapeFactory.h Shape.o Arrange.o
	$(CC) -c ShapeFactory.cpp

Arrange.o: Arrange.cpp Arrange.h Area.o
	$(CC) -c Arrange.cpp

Area.o: Area.cpp Area.h
	$(CC) -c Area.cpp

RWHelper.o: RWHelper.cpp RWHelper.h
	$(CC) -c RWHelper.cpp

ConfigHelper.o: ConfigHelper.h ConfigHelper.cpp
	$(CC) -c ConfigHelper.cpp

StrHashTable: StrHashTable.h StrHashTable.cpp
	$(CC) StrHashTable.cpp -o testStrHashTable

Genesis.o: Genesis.h Genesis.cpp Area.o ShapeFactory.o
	$(CC) -c Genesis.cpp

Lib.o: Lib.h Lib.cpp
	$(CC) -c Lib.cpp

run: $(OBJ) Run.cpp StrHashTable.h
	$(CC) -o run Run.cpp $(OBJ) $(LIBS)

main: $(OBJ) Main.cpp StrHashTable.h
	$(CC) -o main Main.cpp $(OBJ) $(LIBS)
