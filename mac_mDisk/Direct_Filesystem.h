	//
	//  mDisk_Filesystem.h
	//  mDisk
	//
	//  Created by Mike Hummel on 15/06/2010.
	//  Copyright 2010 L & S Software GmbH. All rights reserved.
	//
	// Filesystem operations.
	//
#import <Foundation/Foundation.h>
@class MetaFs;
@class StoreFs;
@class ContentFs;
@class Configuration;

	// The core set of file system operations. This class will serve as the delegate
	// for GMUserFileSystemFilesystem. For more details, see the section on 
	// GMUserFileSystemOperations found in the documentation at:
	// http://macfuse.googlecode.com/svn/trunk/core/sdk-objc/Documentation/index.html
@interface Direct_Filesystem : NSObject  {
	Configuration* config;
	MetaFs* metafs;
	ContentFs* contentfs;
	StoreFs* storefs;
	NSString* rootPath_;
}

- (id)initWithAll:(Configuration *)configuration 
		   metafs:(MetaFs*) metafx 
		contentfs:(ContentFs*) contentfx
		  storefs:(StoreFs*) storefx;

- (void)dealloc;

@end
