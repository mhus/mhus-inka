//
//  disk.m
//  mDisk
//
//  Created by Mike Hummel on 16/06/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import "Disk.h"
#import "mDisk_Filesystem.h"
#import "Direct_Filesystem.h"
#import "DiskListener.h"
#import <MacFUSE/MacFUSE.h>
#import "Configuration.h"
#import "StoreFs.h"
#import "ContentFs.h"
#import "MetaFs.h"

@implementation Disk

- (id)initWithRootPath:(NSString *)rootPathx listener:(DiskListener*)listener {
	if ((self = [super init])) {
		rootPath = [rootPathx retain];
		config = [[Configuration alloc] initWithRootPath:rootPath];
		contentfs = [[ContentFs alloc] initWithConfiguration:config];
		storefs  = [[StoreFs alloc] initWithConfiguration:config];
		metafs  = [[MetaFs alloc] initWithConfiguration:config contentfs:contentfs storefs:storefs];
		
		NSNotificationCenter* center = [NSNotificationCenter defaultCenter];
		[center addObserver:self selector:@selector(mountFailed:)
					   name:kGMUserFileSystemMountFailed object:self];
		[center addObserver:self selector:@selector(didMount:)
					   name:kGMUserFileSystemDidMount object:self];
		[center addObserver:self selector:@selector(didUnmount:)
					   name:kGMUserFileSystemDidUnmount object:self];
		
	}
	return self;
}

- (BOOL)isValide{
	NSLog(@"Valide: %i %i %i %i",[config isValide], [contentfs isValide], [storefs isValide], [metafs isValide]);
	return [config isValide] && [contentfs isValide] && [storefs isValide] && [metafs isValide];
}

- (BOOL)isMounted{
	return fs_ != NULL; 
}

- (void)doMount{
	if (fs_ != NULL) return;
	
	//		NSString* mountPath = @"/Volumes/mDisk";
	NSString* mountPath = [config getConfigValue:@"/mdisk/pathes/mount" def:@"/Volumes/mDisk"];
	if ([mountPath characterAtIndex:0] != '/') {
		mountPath = [@"/Volumes/" stringByAppendingString:mountPath];
	}
	NSString* icnsPath     = [config getConfigValue:@"/mdisk/display/icon" def:[[NSBundle mainBundle] pathForResource:@"mDisk" ofType:@"icns"]];
	NSString* volName     = [config getConfigValue:@"/mdisk/display/label" def:@"mDisk"]; 
	
	NSMutableArray* options = [NSMutableArray array];
	NSString* volArg = [NSString stringWithFormat:@"volicon=%@", icnsPath];
	[options addObject:volArg];
		//[options addObject:@"volname=mDisk"];
	[options addObject:[@"volname=" stringByAppendingString:volName]];
	[options addObject:@"native_xattr"];
	[options addObject:@"daemon_timeout=120"];
		//[options addObject:@"local"];
		//[options addObject:@"debug"];
	 
		//	fs_delegate_ = [[mDisk_Filesystem alloc] initWithRootPath:rootPath];
	// fs_delegate_ = [[mDisk_Filesystem alloc] initWithAll:config metafs:metafs contentfs:contentfs storefs:storefs];
	fs_delegate_ = [[Direct_Filesystem alloc] initWithAll:config metafs:metafs contentfs:contentfs storefs:storefs];
	fs_ = [[GMUserFileSystem alloc] initWithDelegate:fs_delegate_ isThreadSafe:NO];
	[fs_ mountAtPath:mountPath withOptions:options];
}

- (void) dealloc {
	NSNotificationCenter* center = [NSNotificationCenter defaultCenter];
	[center removeObserver:self];
	if (fs_ != NULL) {
		[fs_ unmount];
		[fs_ release];
		[fs_delegate_ release];
		fs_ = NULL;
		fs_delegate_ = NULL;
	}
	[super dealloc];
}

- (void)mountFailed:(NSNotification *)notification {
	NSDictionary* userInfo = [notification userInfo];
	NSError* error = [userInfo objectForKey:kGMUserFileSystemErrorKey];
	NSLog(@"kGMUserFileSystem Error: %@, userInfo=%@", error, [error userInfo]);  
	NSRunAlertPanel(@"Mount Failed", [error localizedDescription], nil, nil, nil);
	//[[NSApplication sharedApplication] terminate:nil];
	[diskListener onDiskMountFailed:self];
	
	[fs_ release];
	[fs_delegate_ release];
	fs_ = NULL;
	fs_delegate_ = NULL;
}

- (void)didMount:(NSNotification *)notification {
	NSDictionary* userInfo = [notification userInfo];
	NSString* mountPath = [userInfo objectForKey:kGMUserFileSystemMountPathKey];
	NSString* parentPath = [mountPath stringByDeletingLastPathComponent];
	[[NSWorkspace sharedWorkspace] selectFile:mountPath
					 inFileViewerRootedAtPath:parentPath];
	[diskListener onDiskMounted:self];
}

- (void)didUnmount:(NSNotification*)notification {
	//[[NSApplication sharedApplication] terminate:nil];
	[diskListener onDiskUnmounted:self];
	[fs_ release];
	[fs_delegate_ release];
	fs_ = NULL;
	fs_delegate_ = NULL;
}

- (NSString*)getName{
	return rootPath;
}

@end
