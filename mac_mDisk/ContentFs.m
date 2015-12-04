//
//  ContentFs.m
//  mDisk
//
//  Created by Mike Hummel on 16/03/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import "ContentFs.h"


@implementation ContentFs

- (id)initWithConfiguration:(Configuration *)configuration {
	if ((self = [super init])) {
		config = configuration;
		path    = [config getConfigValue:@"/mdisk/pathes/content" def:[[config->rootPath stringByAppendingString:@"/content"] retain]];
	}
	return self;
}

- (BOOL)isValide{
	return [[NSFileManager defaultManager] isWritableFileAtPath:path];
}

-(NSString*)getPath:(NSString*)fsPath {
	return [path stringByAppendingString:fsPath];	
}

-(BOOL)existsFile:(NSString *)fsPath {
	NSString* cPath = [self getPath:fsPath];
	return [[NSFileManager defaultManager] isWritableFileAtPath:cPath]	;
}

- (BOOL)prepareFolderForFile:(NSString*)fsFilePath {
	NSString* dir = [config getDirectoryPath:fsFilePath];
	if ([dir length] == 0 ) {
		return YES;
	}
	NSString* cDir = [self getPath:dir];
	if ([[NSFileManager defaultManager] isWritableFileAtPath:cDir]) {
		return YES;
	}
	
	
	NSDictionary *attributes = [NSDictionary dictionary];
	NSError** err = nil;
	BOOL ret = [[NSFileManager defaultManager] 
				createDirectoryAtPath:cDir 
				withIntermediateDirectories:YES
				attributes:attributes
				error:err];
	
	return ret;
}

- (BOOL)moveItemAtPath:(NSString *)fsSource 
				toPath:(NSString *)fsDestination {

	NSString* cSrc = [self getPath:fsSource];
	NSString* cDst = [self getPath:fsDestination];
	int ret = rename([cSrc UTF8String], [cDst UTF8String]);
	if ( ret < 0 ) {
		//*error = [NSError errorWithPOSIXCode:errno];
		return NO;
	}
	return YES;
}

- (NSArray *)contentsOfDirectoryAtPath:(NSString *)fsPath {
	NSString* mPath = [self getPath:fsPath];
	NSError** error = nil;
	NSArray* ret = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:mPath error:error];
	return ret;	
}

- (BOOL)removeDirectoryAtPath:(NSString *)fsPath {
	NSString* mPath = [self getPath:fsPath];
	NSError ** error = nil;
	return [[NSFileManager defaultManager] removeItemAtPath:mPath error:error];
/*
	int ret = rmdir([mPath UTF8String]);
	
	if (ret < 0) {
		// *error = [NSError errorWithPOSIXCode:errno];
		return NO;
	}
	return YES;
 */
}

- (BOOL)removeItemAtPath:(NSString *)fsPath {
	NSString* mPath = [self getPath:fsPath];
	NSError ** error = nil;
	return [[NSFileManager defaultManager] removeItemAtPath:mPath error:error];
}

- (BOOL)createFileAtPath:(NSString *)fsPath 
			  attributes:(NSDictionary *)attributes
				userData:(id *)userData {
	
	mode_t mode = [[attributes objectForKey:NSFilePosixPermissions] longValue];  
	NSString* cPath = [self getPath:fsPath];
	int cfd = creat([cPath UTF8String], mode);
	
	if ( cfd < 0 ) {
		// *error = [NSError errorWithPOSIXCode:errno];
		return NO;
	}
	
//	*userData = [[IoFile alloc] iofile:cfd];
	*userData = [NSNumber numberWithLong:cfd];
	return YES;
}

- (BOOL)openFileAtPath:(NSString *)fsPath 
				  mode:(int)mode
			  userData:(id *)userData {
	NSString* cPath = [self getPath:fsPath];
	int fd = open([cPath UTF8String], mode);
	if ( fd < 0 ) {
		//*error = [NSError errorWithPOSIXCode:errno];
		return NO;
	}
	*userData = [NSNumber numberWithLong:fd];
//	*userData = [[IoFile alloc] iofile:fd];
	
	return YES;	
}
@end
