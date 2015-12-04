//
//  mDisk_Meta.m
//  mDisk
//
//  Created by Mike Hummel on 16/03/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import "MetaFs.h"
#import "NSError+POSIX.h"

@implementation MetaFs

- (id)initWithConfiguration:(Configuration *)configuration contentfs:(ContentFs*)contentfilesystem storefs:(StoreFs*)storefilesystem{
	if ((self = [super init])) {
		config = configuration;
		contentfs = contentfilesystem;
		storefs = storefilesystem;
		path    = [config getConfigValue:@"/mdisk/pathes/meta" def:[[config->rootPath stringByAppendingString:@"/meta"] retain]];
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

-(BOOL)setMetaData:(NSMutableDictionary*)data fsPath:(NSString*)fsPath {
	LOG(@"setMetaData %@ %@",fsPath,data);
	NSString* mPath = [self getPath:fsPath];
	return [data writeToFile:mPath atomically:NO];
}

- (NSArray *)contentsOfDirectoryAtPath:(NSString *)fsPath {
	NSString* mPath = [self getPath:fsPath];
	NSError** error = nil;
	NSArray* ret = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:mPath error:error];
	return ret;	
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

- (NSMutableDictionary*)getMetaData:(NSString*)fsPath {
	LOG(@"getMetaData %@",fsPath);
	
	NSString* mPath = [self getPath:fsPath];
	NSError** error = nil;
	NSMutableDictionary* ret = [NSMutableDictionary dictionaryWithContentsOfFile:mPath];
	if(!ret) {
		return nil;
	}
	
	if (! ret || ! [MDISK_META_VERSION isEqualToString: [ret valueForKey:MDiVersion]]) {
		//create
		
		NSDictionary* attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:mPath error:error];	
		if(!attributes) attributes = [NSDictionary dictionaryWithObjectsAndKeys:nil];
		NSArray* extended = [self extendedAttributesOfRealPath:mPath error:error];
		
		if ([contentfs existsFile:fsPath]) {
			NSString* cPath = [contentfs getPath:fsPath];
			NSDictionary* cAttribs = [[NSFileManager defaultManager] attributesOfItemAtPath:cPath error:error];
			
			
			NSNumber *size = (NSNumber *)[cAttribs valueForKey:NSFileSize];
			// NSLog(@"SIZE: %@", size );
			if (size != nil) {
				
				NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithDictionary:attributes];
				[dict setValue:size forKey:NSFileSize];
				attributes = dict;
				
			}
			
			
		} else
			if ([storefs existsFile:fsPath]) {
				NSString* cPath = [storefs getPath:fsPath];
				NSDictionary* cAttribs = [[NSFileManager defaultManager] attributesOfItemAtPath:cPath error:error];
				
				
				NSNumber *size = (NSNumber *)[cAttribs valueForKey:NSFileSize];
				// NSLog(@"SIZE: %@", size );
				if (size != nil) {
					
					NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithDictionary:attributes];
					[dict setValue:size forKey:NSFileSize];
					attributes = dict;
					
				}
				
				
			}
		
		ret = [NSMutableDictionary dictionary];
		[ret setValue:MDISK_META_VERSION forKey:MDiVersion];
		[ret setValue:attributes forKey:MDiAttributes];
		[ret setValue:extended forKey:MDiExtended];
		[ret setValue:fsPath forKey:MDiLocation];
		[ret setValue:NO forKey:MDiLocked];
		
		LOG(@"getMetaData created for %@ %@", fsPath, ret);
		// Write new generated meta to file
		[self setMetaData:ret fsPath:fsPath];
		
	}
	
	return ret;
}

-(BOOL)createMetaDate:(NSString*)fsPath {
	LOG(@"createMetaDate %@",fsPath);
	
	NSDictionary* attributes = [NSDictionary dictionary];
	NSArray* extended = [NSArray array];
	
	NSMutableDictionary* ret = [NSMutableDictionary dictionary];
	[ret setValue:MDISK_META_VERSION forKey:MDiVersion];
	[ret setValue:attributes forKey:MDiAttributes];
	[ret setValue:extended forKey:MDiExtended];
	[ret setValue:fsPath forKey:MDiLocation];
	[ret setValue:NO forKey:MDiLocked];
	
	return [self setMetaData:ret fsPath:fsPath];
	
}

- (BOOL)updateMetaData:(NSString*)fsPath {
	LOG(@"updateMetaData %@",fsPath);
	if (![contentfs existsFile:fsPath] && ![storefs existsFile:fsPath]) {
		return NO;
	}
	
	NSError** error = nil;
	
	NSMutableDictionary* meta = [self getMetaData:fsPath];
	if (!meta) return NO;
	NSDictionary* attributes = [meta valueForKey:MDiAttributes];
	NSNumber*    mSize = [attributes valueForKey:NSFileSize];
	
	NSString* cPath = nil;
	if ([contentfs existsFile:fsPath]) {
		cPath = [contentfs getPath:fsPath];
	} else
		if ([storefs existsFile:fsPath]) {
			cPath = [storefs getPath:fsPath];
		}
			
	NSDictionary* cAttribs = [[NSFileManager defaultManager] attributesOfItemAtPath:cPath error:error];
	if (!cAttribs) return NO;
	
	if ([attributes count] == 0) {
		// if attributes is empty, use the content attributes
		[meta setValue:cAttribs forKey:MDiAttributes];
		return [self setMetaData:meta fsPath:fsPath];
	}
	
	NSNumber *cSize = (NSNumber *)[cAttribs valueForKey:NSFileSize];
	if (!cSize) return NO;
	// NSLog(@"SIZE: %@", size );
	if (mSize == nil || ![cSize isEqualToNumber:mSize]) {
		
		NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithDictionary:attributes];
		[dict setValue:cSize forKey:NSFileSize];
		[meta setValue:dict forKey:MDiAttributes];
		return [self setMetaData:meta fsPath:fsPath];
	}
	
	return NO;
	
}

- (NSArray *)extendedAttributesOfRealPath:(NSString *)p error:(NSError **)error {
	LOG(@"extendedAttributesOfRealPath %@",p);
	
	ssize_t size = listxattr([p UTF8String], nil, 0, 0);
	if ( size < 0 ) {
		// *error = [NSError errorWithPOSIXCode:errno];
		return nil;
	}
	NSMutableData* data = [NSMutableData dataWithLength:size];
	size = listxattr([p UTF8String], [data mutableBytes], [data length], 0);
	if ( size < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return nil;
	}
	NSMutableArray* contents = [NSMutableArray array];
	char* ptr = (char *)[data bytes];
	while ( ptr < ((char *)[data bytes] + size) ) {
		NSString* s = [NSString stringWithUTF8String:ptr];
		[contents addObject:s];
		ptr += ([s length] + 1);
	}
	//NSLog(@"extendedAttributesOfItemAtPath %@ is %@", p ,contents);
	
	return contents;
}

- (BOOL)moveItemAtPath:(NSString *)fsSource 
				toPath:(NSString *)fsDestination {

	NSString* mSrc = [self getPath:fsSource];
	NSString* mDst = [self getPath:fsDestination];
	int ret = rename([mSrc UTF8String], [mDst UTF8String]);
	if ( ret < 0 ) {
		//*error = [NSError errorWithPOSIXCode:errno];
		return NO;
	}
	return YES;
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

@end
