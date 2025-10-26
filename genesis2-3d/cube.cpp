#ifdef linux
#include <GL/glut.h>
#else
#include <GLUT/glut.h>
#endif

#include "Genesis.h"
#include "Shape.h"
#include "DisplayHelper.h"
#include "Arrange.h"
#include "ConfigHelper.h"

#include <stdio.h>
#include <stdlib.h>
#include <math.h>


#define SHAPESIZE 100
#define SHAPEALERT 30
#define SCALE 10
#define YSCALE 1
#define DETAILSIZE SCALE*30

ConfigHelper *ConfigHelper::staticConfigHelper = new ConfigHelper();
Shape *shape;

int shapeX;
int shapeY;

int shapeBuffer[SHAPESIZE*2+2][SHAPESIZE*2+2];
 
/* ######################################################################################## */

typedef struct {
    double r,g,b;
} Color;

typedef struct {
    double x,y,z;
    Color *color;
} Vertex3D;

typedef struct {
    int segment_ind[4];
} Area3D;

/* ######################################################################################## */

Color colr = {1.0, 0.0, 0.0},
      colg = {0.0, 1.0, 0.0},
      colb = {0.0, 0.0, 1.0},
      colrg = {1.0, 1.0, 0.0},
      colrb = {1.0, 0.0, 1.0},
      colgb = {0.0, 1.0, 1.0},
      colBlack = {0.0, 0.0, 0.0},
      colWhite = {1.0, 1.0, 1.0};

Color cols[256];

/* Eckpunkte eines Wuerfels + RGB Farben */
Vertex3D gVertex[8] = {{-1.0, -1.0,  1.0, &colr},
                       { 1.0, -1.0,  1.0, &colrg},
                       { 1.0,  1.0,  1.0, &colWhite},
                       {-1.0,  1.0,  1.0, &colrb},
                       {-1.0, -1.0, -1.0, &colBlack},
                       { 1.0, -1.0, -1.0, &colg},
                       { 1.0,  1.0, -1.0, &colgb},
                       {-1.0,  1.0, -1.0, &colb}};

Area3D gSegments[] = {/* vordere Seitenflaeche */
                    {0,1,2,3},
                    /* hintere Seitenflaeche */
                    {5,6,7,4},
                    /* rechte Seitenflaeche */
                    {1,5,6,2},
                    /* linke Seitenflaeche */
                    {4,0,3,7},
                    /* Deckel */
                    {7,3,2,6},
                    /* Boden */
                    {5,1,0,4}};
                       
/* ######################################################################################## */

GLenum gMode = GL_QUADS,
       gShade = GL_SMOOTH;

double gRadius = 5.0;

double viewX = 0.0;
double viewY = 30.0;
double viewZ = 0.0;

double viewHRad = 0;
double viewVRad = 0;

int screenWidth = 10;
int screenHeight = 10;
int mouse_x = 1;
int mouse_y = 1;

float viewSpeed = 0.0;

void init(void) 
{
    glClearColor (0.5, 0.5, 0.5, 0.0);

    glEnable(GL_DEPTH_TEST);
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
}


void drawCube(void)
{
    int i,j;
    int vIndex;
    Color *vColorPtr;
        
    glShadeModel(gShade);
    for (i=0; i<6; i++)
    {
        glBegin(gMode);
        for (j=0;j <4;j++)
        {
            vIndex = gSegments[i].segment_ind[j];
            vColorPtr = gVertex[vIndex].color;
            
            glColor3f(vColorPtr->r, vColorPtr->g, vColorPtr->b);
            glVertex3f(gVertex[vIndex].x, gVertex[vIndex].y, gVertex[vIndex].z);
        }
        glEnd();
    }
}

void drawGround(void)
{
  int x,y;
  glShadeModel(GL_FLAT);
  for (x=-SHAPESIZE; x<SHAPESIZE; x++)
    for (y=-SHAPESIZE; y<SHAPESIZE; y++) {
      glBegin(GL_LINE_LOOP);
      glColor3f(colr.r,colr.g,colr.b);
      glVertex3f(x,0,y);
      glVertex3f(x+1,0,y);
      glVertex3f(x+1,0,y+1);
      glVertex3f(x,0,y+1);
      glEnd();
    }
}

void fillShapeBuffer(void) {
  int x,y;
  for (x=-SHAPESIZE; x<SHAPESIZE+2; x++)
    for (y=-SHAPESIZE; y<SHAPESIZE+2; y++)
      shapeBuffer[x+SHAPESIZE][y+SHAPESIZE] = shape->get(x+shapeX,y+shapeY);
}

void drawShape(void)
{
  int x,y;
  int i,j;
  int x1,y1;
  Color *c;

  int maxX,maxY,minX,minY;
  maxX = minX = (int)viewX/SCALE;
  maxY = minY = (int)viewZ/SCALE;

  glShadeModel(gShade);
  for (x=-SHAPESIZE; x<SHAPESIZE; x+=2)
    for (y=-SHAPESIZE; y<SHAPESIZE; y+=2) {
      
      if ((abs((int)viewX-(x*SCALE))<=DETAILSIZE) && (abs((int)viewZ-(y*SCALE))<=DETAILSIZE)) {

	if (x>maxX) maxX=x;
	if (x<minX) minX=x;
	if (y>maxY) maxY=y;
	if (y<minY) minY=y;

	x1 = x;
	for (i=0;i<2;i++) {
	  y1 = y;
	  for (j=0;j<2;j++) {
	    glBegin(gMode);

	    c = &cols[shapeBuffer[x1+SHAPESIZE][y1+SHAPESIZE] % 255];
	    glColor3f(c->r,c->g,c->b);
	    glVertex3f(x1*SCALE,   shapeBuffer[x1+SHAPESIZE][y1+SHAPESIZE]*YSCALE  ,  y1*SCALE);

	    c = &cols[shapeBuffer[x1+1+SHAPESIZE][y1+SHAPESIZE] % 255];
	    glColor3f(c->r,c->g,c->b);
	    glVertex3f(x1*SCALE+SCALE, shapeBuffer[x1+1+SHAPESIZE][y1+SHAPESIZE]*YSCALE,  y1*SCALE);

	    c = &cols[shapeBuffer[x1+1+SHAPESIZE][y1+1+SHAPESIZE] % 255];
	    glColor3f(c->r,c->g,c->b);
	    glVertex3f(x1*SCALE+SCALE, shapeBuffer[x1+1+SHAPESIZE][y1+1+SHAPESIZE]*YSCALE, y1*SCALE+SCALE);

	    c = &cols[shapeBuffer[x1+SHAPESIZE][y1+1+SHAPESIZE] % 255];
	    glColor3f(c->r,c->g,c->b);
	    glVertex3f(x1*SCALE,   shapeBuffer[x1+SHAPESIZE][y1+1+SHAPESIZE]*YSCALE,   y1*SCALE+SCALE);
	    glEnd();

	    y1++;
	  }
	  x1++;
	}
      } else {

	glBegin(gMode);

	c = &cols[shapeBuffer[x+SHAPESIZE][y+SHAPESIZE] % 255];
	glColor3f(c->r,c->g,c->b);
	glVertex3f(x*SCALE,   shapeBuffer[x+SHAPESIZE][y+SHAPESIZE]*YSCALE  ,  y*SCALE);
	
	c = &cols[shapeBuffer[x+2+SHAPESIZE][y+SHAPESIZE] % 255];
	glColor3f(c->r,c->g,c->b);
	glVertex3f(x*SCALE+SCALE*2, shapeBuffer[x+2+SHAPESIZE][y+SHAPESIZE]*YSCALE,  y*SCALE);
	
	c = &cols[shapeBuffer[x+2+SHAPESIZE][y+2+SHAPESIZE] % 255];
	glColor3f(c->r,c->g,c->b);
	glVertex3f(x*SCALE+SCALE*2, shapeBuffer[x+2+SHAPESIZE][y+2+SHAPESIZE]*YSCALE, y*SCALE+SCALE*2);
	
	c = &cols[shapeBuffer[x+SHAPESIZE][y+2+SHAPESIZE] % 255];
	glColor3f(c->r,c->g,c->b);
	glVertex3f(x*SCALE,   shapeBuffer[x+SHAPESIZE][y+2+SHAPESIZE]*YSCALE,   y*SCALE+SCALE*2);
	glEnd();
	
      }
    } 
     
  // draw border at DETAIL border x direction
  y  = minY - 2;
  y1 = maxY + 2;
  for (x=minX;x<=maxX;x+=2) {
    
    glBegin(GL_POLYGON);

    c = &cols[shapeBuffer[x+SHAPESIZE][y+SHAPESIZE+2] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE,   shapeBuffer[x+SHAPESIZE][y+2+SHAPESIZE]*YSCALE  ,  y*SCALE+SCALE*2);
	  
    c = &cols[shapeBuffer[x+2+SHAPESIZE][y+2+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE+SCALE*2, shapeBuffer[x+2+SHAPESIZE][y+2+SHAPESIZE]*YSCALE, y*SCALE+SCALE*2);

    c = &cols[shapeBuffer[x+1+SHAPESIZE][y+2+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE+SCALE, shapeBuffer[x+1+SHAPESIZE][y+2+SHAPESIZE]*YSCALE, y*SCALE+SCALE*2);

    glEnd();
    
    glBegin(GL_POLYGON);

    c = &cols[shapeBuffer[x+SHAPESIZE][y1+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE,   shapeBuffer[x+SHAPESIZE][y1+SHAPESIZE]*YSCALE  ,  y1*SCALE);
	  
    c = &cols[shapeBuffer[x+2+SHAPESIZE][y1+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE+SCALE*2, shapeBuffer[x+2+SHAPESIZE][y1+SHAPESIZE]*YSCALE, y1*SCALE);

    c = &cols[shapeBuffer[x+1+SHAPESIZE][y1+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE+SCALE, shapeBuffer[x+1+SHAPESIZE][y1+SHAPESIZE]*YSCALE, y1*SCALE);

    glEnd();
    
	
  }

  // and for y direction
  x  = minX - 2;
  x1 = maxX + 2;
  for (y=minY;y<=maxY;y+=2) {

    glBegin(GL_POLYGON);

    c = &cols[shapeBuffer[x+SHAPESIZE+2][y+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE+SCALE*2,   shapeBuffer[x+2+SHAPESIZE][y+SHAPESIZE]*YSCALE  ,  y*SCALE);
	  
    c = &cols[shapeBuffer[x+2+SHAPESIZE][y+2+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE+SCALE*2, shapeBuffer[x+2+SHAPESIZE][y+2+SHAPESIZE]*YSCALE, y*SCALE+SCALE*2);

    c = &cols[shapeBuffer[x+2+SHAPESIZE][y+1+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x*SCALE+SCALE*2, shapeBuffer[x+2+SHAPESIZE][y+1+SHAPESIZE]*YSCALE, y*SCALE+SCALE);

    glEnd();
    
    glBegin(GL_POLYGON);

    c = &cols[shapeBuffer[x1+SHAPESIZE][y+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x1*SCALE,   shapeBuffer[x1+SHAPESIZE][y+SHAPESIZE]*YSCALE  ,  y*SCALE);
	  
    c = &cols[shapeBuffer[x1+SHAPESIZE][y+2+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x1*SCALE, shapeBuffer[x1+SHAPESIZE][y+2+SHAPESIZE]*YSCALE, y*SCALE+SCALE*2);

    c = &cols[shapeBuffer[x1+SHAPESIZE][y+1+SHAPESIZE] % 255];
    glColor3f(c->r,c->g,c->b);
    glVertex3f(x1*SCALE, shapeBuffer[x1+SHAPESIZE][y+1+SHAPESIZE]*YSCALE, y*SCALE+SCALE);

    glEnd();
    
  }

  // shape check - load new shape is recomended
  if ( (abs((int)viewX) > SHAPEALERT*SCALE) || (abs((int)viewZ) > SHAPEALERT*SCALE)) {
    shapeX = shapeX + (int)(viewX/SCALE);
    shapeY = shapeY + (int)(viewZ/SCALE);
    viewX = viewX - (int)viewX;
    viewZ = viewZ - (int)viewZ;
    fillShapeBuffer();
  }


}

void display(void)
{
 
    double x,y,z;
    double sv, cv;

    
            
    sv = sin(viewVRad);
    cv = cos(viewVRad);
    
    x = 2.0 * cos(viewHRad) * cv + viewX;
    y = 2.0 * cos(viewHRad) * sv + viewY;
    z = 2.0 * sin(viewHRad)      + viewZ;
    
    

    

    glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glLoadIdentity ();             /* clear the matrix */
           /* viewing transformation  */
    
    //gluLookAt ( x,   y,   z, 	/* Kamera */
    //        0.0, 0.0, 0.0,     /* Zielpunkt */
    //            -sv, cv, 0.0); 	/* Richtung der Kamera */
	
	gluLookAt ( viewX, viewY, viewZ,
		x, y, z,
		-sv, cv, 0.0);
	

	//	drawCube();
	// drawGround();
	drawShape();
    
    glutSwapBuffers();
}

void reshape (int w, int h)
{
  screenWidth  = w / 3;
  screenHeight = h / 3;

  glViewport (0, 0, (GLsizei) w, (GLsizei) h); 
  glMatrixMode (GL_PROJECTION);
  glLoadIdentity ();
  glFrustum (-1.0, 1.0, -1.0,
	     1.0, 1.5, 700.0);
  glMatrixMode (GL_MODELVIEW);

  //  glEnable(GL_FOG);  
    glClearColor(0.80, 0.20, 0.20, 1.0);
 
}

void keyboard(unsigned char key, int x, int y)
{
    switch (key) {
        case 27:
            exit(0);
            break;
         
        case 'i':
        case 'I':
            viewHRad = viewVRad = 0.0;
            viewX = viewY = viewZ = 0;
	    viewSpeed = 0;
            break;
            
        case 'u':
            viewHRad -= 0.1;
            if (viewHRad < 0.0) viewHRad = 2*M_PI;
            break;

        case 'U':
            viewHRad += 0.1;
            if (viewHRad > 2*M_PI) viewHRad = 0.0;
            break;
    
        case 'v':
            viewVRad -= 0.1;
            if (viewVRad < 0.0) viewVRad = 2*M_PI;
            break;

        case 'V':
            viewVRad += 0.1;
            if (viewVRad > 2*M_PI) viewVRad = 0.0;
            break;
    
        case 'z':
            gRadius -= 0.5;
            if (gRadius < 3.0) gRadius = 3.0;
            break;
        
        case 'Z':
            gRadius += 0.5;
            if (gRadius > 15) gRadius = 15.0;
            break;
            
        case 'M':
        case 'm':
            if (gMode == GL_QUADS)
                gMode = GL_LINE_LOOP;
            else
                gMode = GL_QUADS;
            break;
            
        case 'S':
        case 's':
            if (gShade == GL_SMOOTH)
                gShade = GL_FLAT;
            else
                gShade = GL_SMOOTH;
            break;
            
    }
    //    display();
}

void special(int key, int x, int y)
{
    switch (key) {
    case GLUT_KEY_LEFT:
        viewHRad -= 0.2;
        break;
    case GLUT_KEY_UP:
        viewY += 0.5;
        break;
    case GLUT_KEY_RIGHT:
        viewHRad += 0.2;
        break;
    case GLUT_KEY_DOWN:
        viewY -= 0.5;
        break;
    case GLUT_KEY_PAGE_UP:
      viewSpeed += 0.05;
      break;
    case GLUT_KEY_PAGE_DOWN:
      viewSpeed -= 0.05;
      break;
    }
    //    display();

}         

void motion(int x, int y)
{
    mouse_x = x;
    mouse_y = y;

}

void fly(void) {

  if (mouse_x < screenWidth)
    viewHRad -= ((float)(screenWidth-mouse_x)/(float)screenWidth)*viewSpeed/5;
  if (mouse_x > screenWidth*2)
    viewHRad += ((float)(mouse_x-screenWidth*2)/(float)screenWidth)* viewSpeed/5;
  if (mouse_y < screenHeight)
    viewY += ((float)(screenHeight-mouse_y)/(float)screenHeight)*viewSpeed*7;
  if (mouse_y > screenHeight*2)
    viewY -= ((float)(mouse_y-screenHeight*2)/(float)screenHeight) * viewSpeed*7;

  viewX += viewSpeed * cos(viewHRad) * cos(viewVRad) * SCALE;
  viewZ += viewSpeed * sin(viewHRad) * SCALE;

  if (viewY-10 < shapeBuffer[(int)viewX/SCALE+SHAPESIZE][(int)viewZ/SCALE+SHAPESIZE]*YSCALE)
    viewY = shapeBuffer[(int)viewX/SCALE+SHAPESIZE][(int)viewZ/SCALE+SHAPESIZE]*YSCALE+10;
  if (viewY-10 < shapeBuffer[(int)viewX/SCALE+SHAPESIZE+1][(int)viewZ/SCALE+SHAPESIZE]*YSCALE)
    viewY = shapeBuffer[(int)viewX/SCALE+SHAPESIZE+1][(int)viewZ/SCALE+SHAPESIZE]*YSCALE+10;
  if (viewY-10 < shapeBuffer[(int)viewX/SCALE+SHAPESIZE+1][(int)viewZ/SCALE+SHAPESIZE+1]*YSCALE)
    viewY = shapeBuffer[(int)viewX/SCALE+SHAPESIZE+1][(int)viewZ/SCALE+SHAPESIZE+1]*YSCALE+10;
  if (viewY-10 < shapeBuffer[(int)viewX/SCALE+SHAPESIZE][(int)viewZ/SCALE+SHAPESIZE+1]*YSCALE)
    viewY = shapeBuffer[(int)viewX/SCALE+SHAPESIZE][(int)viewZ/SCALE+SHAPESIZE+1]*YSCALE+10;

  if (viewY > 300*YSCALE) viewY=300*YSCALE;

  display();
}

void setpalette (int f, int r, int g, int b) {
  cols[f].r = (float)r/64;
  cols[f].g = (float)g/64;
  cols[f].b = (float)b/64;
}

int main(int argc, char** argv)
{

  int x,y,i;
  char *shapePath;

  //get start position
  ConfigHelper *c = ConfigHelper::staticConfigHelper;
  if (c->parseXML("genesis.xml") == 0) {
    c->addDocument("save.xml");

    char *aname = c->getProperty(c->getNodeFrom(c->getRoot(),"RUN.START"),
                                 "area"); //get area name

    xmlNodePtr pArea = c->getNamedNode(c->getNodeFrom(c->getRoot(),"SAVE:"),
                                      "AREA",
                                      "name",
                                      aname); // get area

    xmlNodePtr pGenesis = c->getNodeFrom(c->getRoot(),"SAVE:GENESIS");

    x = atoi(c->getProperty(pArea,"rasterx")) * atoi(c->getProperty(pGenesis,"xpr"));
    y = atoi(c->getProperty(pArea,"rastery")) * atoi(c->getProperty(pGenesis,"ypr"));

    x = x + atoi(c->getProperty(c->getNodeFrom(c->getRoot(),"RUN.START"),"x"));
    y = y + atoi(c->getProperty(c->getNodeFrom(c->getRoot(),"RUN.START"),"y"));

    shapePath = c->getProperty(c->getNodeFrom(c->getRoot(),"RUN.START"),"shapepath");
  } else {
    printf("cant load genesis config\n");
    exit(1);
  }

printf("Shape [%s]....\n",shapePath);
  shape = new Shape(320,400);
  shape->setRWHelper(shapePath,"shape",RWHelper::BINARY);

  printf("run: XY %i %i\n",x,y);
  shapeX = x;
  shapeY = y;
  fillShapeBuffer();

  // farben definieren

  setpalette(0,0,0,40); //meer

  for (i=1;i<40;i++)    setpalette(i,0,i+20,0);        //gruen
  for (i=40;i<80;i++)   setpalette(i,i-20,60,0);       //nach gelb
  for (i=80;i<140;i++)  setpalette(i,60,60-(i-80),0);  //nach rot
  for (i=140;i<200;i++) setpalette(i,60,i-140,i-140); //nach weiss
  for (i=200;i<250;i++) setpalette(i,260-i,260-i,260-i); //nach schwarz

  setpalette(1,0,0,50); //meer
  

// openGL init

   GLboolean fullscreen = GL_TRUE;

   glutInit(&argc, argv);

  if (argc > 1 && !strcmp(argv[1], "-w"))
      fullscreen = GL_FALSE;

  glutInitDisplayMode(GLUT_RGB | GLUT_DOUBLE | GLUT_DEPTH | GLUT_MULTISAMPLE);
  if (fullscreen) {
      glutGameModeString("640x480:16@60");
      glutEnterGameMode();
  } else {
      glutInitWindowSize(400, 400);
      glutInitWindowPosition (200, 50);
      glutCreateWindow (argv[0]);
  }

   init ();
   glutDisplayFunc(display); 
   glutReshapeFunc(reshape);
   glutKeyboardFunc(keyboard);
   glutSpecialFunc(special);
   glutMotionFunc(motion);
   glutPassiveMotionFunc(motion);
   glutIdleFunc(fly);
   glutMainLoop();
   return 0;
}
