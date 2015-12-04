//
//  StoreFs.h
//  mDisk
//
//  Created by Mike Hummel on 16/03/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "Configuration.h"
//#import "IoFile.h"

@interface StoreFs : NSObject {
	Configuration* config;
	NSString* path;
	NSString* seed;
}

- (id)initWithConfiguration:(Configuration *)configuration;

- (BOOL)enabled;
- (BOOL)isValide;

-(NSString*)getPath:(NSString*)fsPath;
-(BOOL)existsFile:(NSString *)fsPath;

- (BOOL)openFileAtPath:(NSString *)fsPath 
				  mode:(int)mode
			  userData:(id *)userData;

- (NSArray *)contentsOfDirectoryAtPath:(NSString *)fsPath;

- (BOOL)createDirectoryAtPath:(NSString *)fsPath 
				   attributes:(NSDictionary *)attributes;

@end
