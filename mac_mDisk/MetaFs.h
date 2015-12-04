//
//  mDisk_Meta.h
//  mDisk
//
//  Created by Mike Hummel on 16/03/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "Configuration.h"
#import "ContentFs.h"
#import "StoreFs.h"
#import <sys/xattr.h>
#import <sys/stat.h>

@interface MetaFs : NSObject {
	Configuration* config;
	NSString* path;
	ContentFs* contentfs;
	StoreFs* storefs;
}

- (id)initWithConfiguration:(Configuration *)configuration contentfs:(ContentFs*)contentfilesystem storefs:(StoreFs*)storefilesystem;
-(NSString*)getPath:(NSString*)fsPath;
-(BOOL)existsFile:(NSString *)fsPath;

- (NSArray *)contentsOfDirectoryAtPath:(NSString *)fsPath;
- (BOOL)createDirectoryAtPath:(NSString *)fsPath 
				   attributes:(NSDictionary *)attributes;

-(BOOL)setMetaData:(NSMutableDictionary*)data fsPath:(NSString*)fsPath;
- (NSMutableDictionary*)getMetaData:(NSString*)fsPath;
-(BOOL)updateMetaData:(NSString*)fsPath;
-(BOOL)createMetaDate:(NSString*)fsPath;
-(NSArray *)extendedAttributesOfRealPath:(NSString *)p error:(NSError **)error;

- (BOOL)moveItemAtPath:(NSString *)fsSource 
				toPath:(NSString *)fsDestination;

- (BOOL)removeDirectoryAtPath:(NSString *)fsPath;
- (BOOL)removeItemAtPath:(NSString *)fsPath;

- (BOOL)isValide;

@end
