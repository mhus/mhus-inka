//
//  ContentFs.h
//  mDisk
//
//  Created by Mike Hummel on 16/03/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "Configuration.h"
//#import "IoFile.h";

@interface ContentFs : NSObject {
	Configuration* config;
	NSString* path;
}

- (id)initWithConfiguration:(Configuration *)configuration;

-(NSString*)getPath:(NSString*)fsPath;
-(BOOL)existsFile:(NSString *)fsPath;
-(BOOL)prepareFolderForFile:(NSString*)fsFilePath;

- (BOOL)moveItemAtPath:(NSString *)fsSource 
				toPath:(NSString *)fsDestination;
- (BOOL)removeDirectoryAtPath:(NSString *)fsPath;
- (BOOL)removeItemAtPath:(NSString *)fsPath;
- (BOOL)createFileAtPath:(NSString *)fsPath 
			  attributes:(NSDictionary *)attributes
				userData:(id *)userData;
- (BOOL)openFileAtPath:(NSString *)fsPath 
				  mode:(int)mode
			  userData:(id *)userData;
- (NSArray *)contentsOfDirectoryAtPath:(NSString *)fsPath;
- (BOOL)isValide;

@end
