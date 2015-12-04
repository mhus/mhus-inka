//
//  StoreFs.m
//  mDisk
//
//  Created by Mike Hummel on 16/03/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import "StoreFs.h"


@implementation StoreFs

- (id)initWithConfiguration:(Configuration *)configuration {
	if ((self = [super init])) {
		config = configuration;
		path    = [[config getConfigValue:@"/mdisk/pathes/store/path" def:MDISK_DEFAUTL_STORE] retain];
		seed    = [[config getConfigValue:@"/mdisk/pathes/store/seed" def:@""] retain];
		if ([seed length] != 0) {
			seed = [[path stringByAppendingFormat:@"/.%@",seed] retain];
		}
	}
	return self;
}

- (BOOL)isValide {
	return YES;
}

- (BOOL)enabled {
	if ([seed length] == 0) {
		return [config isDirectory:path];
	} else {
		return [config isFile:seed];
	}
	
}

-(NSString*)getPath:(NSString*)fsPath {
	return [path stringByAppendingString:fsPath];	
}

-(BOOL)existsFile:(NSString *)fsPath {
	if (![self enabled]) return NO;
	NSString* sPath = [self getPath:fsPath];
	return [[NSFileManager defaultManager] isWritableFileAtPath:sPath]	;
}

- (NSArray *)contentsOfDirectoryAtPath:(NSString *)fsPath {
	NSString* mPath = [self getPath:fsPath];
	NSError** error = nil;
	NSArray* ret = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:mPath error:error];
	return ret;	
}

// Import all from store into local cache
- (BOOL)exchangeImportAll:(NSString*)fsPath overwriteLocal:(BOOL)overwriteLocal {
	if (![self enabled]) return NO;
	return NO;
}

// Export all to store
- (BOOL)exchangeExportAll:(NSString*)fsPath removeCache:(BOOL)removeCache {
	if (![self enabled]) return NO;
	return NO;
}

- (NSDictionary*)listOfChanges:(NSString*)fsPath {
	return nil;
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

- (BOOL)createDirectoryAtPath:(NSString *)fsPath 
				   attributes:(NSDictionary *)attributes {
	
	NSString* mPath = [self getPath:fsPath];
	NSError** error = nil;
	BOOL ret = [[NSFileManager defaultManager] 
				createDirectoryAtPath:mPath 
				withIntermediateDirectories:YES
				attributes:attributes
				error:error];
	
	return ret;
}

@end
