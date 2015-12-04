//
//  disk.h
//  mDisk
//
//  Created by Mike Hummel on 16/06/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@class GMUserFileSystem;
@class mDisk_Filesystem;
@class DiskListener;
@class MetaFs;
@class StoreFs;
@class ContentFs;
@class Configuration;

@interface Disk : NSObject {
	GMUserFileSystem* fs_;
	NSString* rootPath;
	NSObject* fs_delegate_;
	DiskListener* diskListener;
	Configuration* config;
	ContentFs* contentfs;
	StoreFs* storefs;
	MetaFs* metafs;
}

- (id)initWithRootPath:(NSString *)rootPathx listener:(DiskListener*) listener;
- (void)dealloc;
- (NSString*)getName;
- (BOOL)isValide;
- (BOOL)isMounted;
- (void)doMount;


@end
