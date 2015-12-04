//
//  Cofiguration.h
//  mDisk
//
//  Created by Mike Hummel on 16/03/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import "const.h"

@interface Configuration : NSObject {
@public
	NSString* rootPath;   // The local root path, including the config and script directory
	NSXMLDocument *configDoc;	
}

- (id)initWithRootPath:(NSString *)rootPath;
- (NSString *)getConfigValue: (NSString *) xpath def:(NSString *)def;
- (void)loadConfig;

- (NSString *)getDirectoryPath:(NSString *)filePath;
- (NSString *)getFilePath:(NSString *)filePath;
-(BOOL) isDirectory:(NSString *)path;
-(BOOL) isFile:(NSString *)path;
- (BOOL)isValide;

@end
