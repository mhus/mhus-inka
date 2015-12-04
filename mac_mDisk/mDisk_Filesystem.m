
#import <sys/xattr.h>
#import <sys/stat.h>
#import "mDisk_Filesystem.h"
#import <MacFUSE/MacFUSE.h>
#import "NSError+POSIX.h"
#import "Configuration.h"
#import "StoreFs.h"
#import "ContentFs.h"
#import "MetaFs.h"

@implementation mDisk_Filesystem

- (id)initWithAll:(Configuration *)configuration 
		   metafs:(MetaFs*) metafx 
		contentfs:(ContentFs*) contentfx
		  storefs:(StoreFs*) storefx
	{
	if ((self = [super init])) {
		config = configuration;
		metafs  = metafx;
		contentfs = contentfx;
		storefs = storefx;
	}
	return self;
}

- (void) dealloc {
		//	[rootPath_ release];
	[super dealloc];
}


#pragma mark Moving an Item

- (BOOL)moveItemAtPath:(NSString *)fsSource 
				toPath:(NSString *)fsDestination
				 error:(NSError **)error {
	LOG(@"moveItemAtPath %@ %@",fsSource,fsDestination);
		// We use rename directly here since NSFileManager can sometimes fail to 
		// rename and return non-posix error codes.
	
	if ( ![metafs moveItemAtPath:fsSource toPath:fsDestination] ) {
		return NO;
	}
	
	if (![contentfs existsFile:fsSource]) {
		return YES;
	}
	
	[contentfs prepareFolderForFile:fsDestination];
	
	[contentfs moveItemAtPath:fsSource toPath:fsDestination];
	
	return YES;
}

#pragma mark Removing an Item

- (BOOL)removeDirectoryAtPath:(NSString *)fsPath error:(NSError **)error {
	LOG(@"removeDirectoryAtPath %@",fsPath);
		// We need to special-case directories here and use the bsd API since 
		// NSFileManager will happily do a recursive remove :-(
	
	if (![metafs removeDirectoryAtPath:fsPath]) {
		return NO;
	}
	
	[contentfs removeDirectoryAtPath:fsPath];
	
	return YES;
}

- (BOOL)removeItemAtPath:(NSString *)fsPath error:(NSError **)error {
	LOG(@"removeItemAtPath %@",fsPath);
	
		// NOTE: If removeDirectoryAtPath is commented out, then this may be called
		// with a directory, in which case NSFileManager will recursively remove all
		// subdirectories. So be careful!
	
	
	if (![metafs removeItemAtPath:fsPath]) return NO;
	
	[contentfs removeItemAtPath:fsPath];
	
	return YES;
}

#pragma mark Creating an Item

- (BOOL)createDirectoryAtPath:(NSString *)fsPath 
				   attributes:(NSDictionary *)attributes
						error:(NSError **)error {
	LOG(@"createDirectoryAtPath %@",fsPath);
	
	return [metafs createDirectoryAtPath:fsPath attributes:attributes];
}

- (BOOL)createFileAtPath:(NSString *)fsPath 
			  attributes:(NSDictionary *)attributes
				userData:(id *)userData
				   error:(NSError **)error {
	LOG(@"createFileAtPath %@",fsPath);
	
	if([metafs existsFile:fsPath] && ! [contentfs existsFile:fsPath]) {
		*error = [NSError errorWithPOSIXCode:EPERM];
		return NO;
	}
	
	[metafs createMetaDate:fsPath];
	
	[contentfs prepareFolderForFile:fsPath];
	
	if(![contentfs createFileAtPath:fsPath attributes:attributes userData:userData]) {
		return NO;
	}
	
	[metafs updateMetaData:fsPath];
	
	return YES;
}


#pragma mark File Contents

- (BOOL)openFileAtPath:(NSString *)fsPath 
				  mode:(int)mode
			  userData:(id *)userData
				 error:(NSError **)error {
	LOG(@"openFileAtPath %@",fsPath);
	
		//	NSString* fileName = [config getFilePath:fsPath]; 
		//	if(mode == O_RDONLY && [fileName length] > 3 && [[fileName substringToIndex:3] isEqualToString:@".__"]) {
		//		NSString* path = [config getDirectoryPath:fsPath];
		//		return [commandHandler executeCall:[fileName substringFromIndex:3] fsPath:path userData:userData error:error];
		//	}
	
	if ([contentfs existsFile:fsPath]) {
		[contentfs prepareFolderForFile:fsPath];
		
		return [contentfs openFileAtPath:fsPath mode:mode userData:userData];
	}
	
	if (mode == O_RDONLY && [storefs existsFile:fsPath]) {
		return [storefs openFileAtPath:fsPath mode:mode userData:userData];
	}
	
	*error = [NSError errorWithPOSIXCode:ENOENT];
	return NO;
}

- (void)releaseFileAtPath:(NSString *)path userData:(id)userData {
	LOG(@"releaseFileAtPath %@",path);
	
	NSNumber* num = (NSNumber *)userData;
	int fd = [num longValue];
	close(fd);
		//	IoData* io = (IoData*)userData;
		//	[io releaseFileAtPath:path];
	
}

- (int)readFileAtPath:(NSString *)path 
             userData:(id)userData
               buffer:(char *)buffer 
                 size:(size_t)size 
               offset:(off_t)offset
                error:(NSError **)error {
	NSNumber* num = (NSNumber *)userData;
	int fd = [num longValue];
	int ret = pread(fd, buffer, size, offset);
	if ( ret < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return -1;
	}
	return ret;
}

- (int)writeFileAtPath:(NSString *)path 
              userData:(id)userData
                buffer:(const char *)buffer
                  size:(size_t)size 
                offset:(off_t)offset
                 error:(NSError **)error {
	NSNumber* num = (NSNumber *)userData;
	int fd = [num longValue];
	int ret = pwrite(fd, buffer, size, offset);
	if ( ret < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return -1;
	}
	return ret;
}

/*
- (int)readFileAtPath:(NSString *)path 
			 userData:(id)userData
			   buffer:(char *)buffer 
				 size:(size_t)size 
			   offset:(off_t)offset
				error:(NSError **)error {
	
		//	IoData* io = (IoData*)userData;
		//	return [io readFileAtPath:path buffer:buffer size:size offset:offset];
	NSNumber* num = (NSNumber *)userData;
	int fd = [num longValue];
	int ret = pread(fd, buffer, size, offset);
	if ( ret < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return -1;
	}
	return ret;
}

- (int)writeFileAtPath:(NSString *)path 
			  userData:(id)userData
				buffer:(const char *)buffer
				  size:(size_t)size 
				offset:(off_t)offset
				 error:(NSError **)error {
	
		//	IoData* io = (IoData*)userData;
		//	return [io writeFileAtPath:path buffer:buffer  size:size offset:offset];
	NSNumber* num = (NSNumber *)userData;
	int fd = [num longValue];
	int ret = pwrite(fd, buffer, size, offset);
	if ( ret < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return -1;
	}
	return ret;	
}

*/


- (BOOL)exchangeDataOfItemAtPath:(NSString *)path1
				  withItemAtPath:(NSString *)path2
						   error:(NSError **)error {
	LOG(@"exchangeDataOfItemAtPath %@ %@",path1,path2);
	
	return NO;    
}

#pragma mark Directory Contents

- (NSArray *)contentsOfDirectoryAtPath:(NSString *)fsPath error:(NSError **)error {
	LOG(@"contentsOfDirectoryAtPath %@",fsPath);
	
	return [metafs contentsOfDirectoryAtPath:fsPath];
}

#pragma mark Getting and Setting Attributes

- (NSDictionary *)attributesOfItemAtPath:(NSString *)fsPath
								userData:(id)userData
								   error:(NSError **)error {
	LOG(@"attributesOfItemAtPath %@",fsPath);
	
	NSString* fileName = [config getFilePath:fsPath]; 
	if([fileName length] > 3 && [[fileName substringToIndex:3] isEqualToString:@".__"]) {
		NSMutableDictionary* ret = [NSMutableDictionary dictionary];
		[ret setValue:[NSNumber numberWithInt:400] forKey:NSFilePosixPermissions];
		[ret setValue:[NSNumber numberWithInt:0] forKey:NSFileSize];
		
			//[ret setValue:[NSNumber numberWithInt:33554432]	forKey:NSFileDeviceIdentifier];
			//[ret setValue:[NSNumber numberWithInt:0]		forKey:NSFileGroupOwnerAccountID];
			//[ret setValue:@"wheel"						forKey:NSFileGroupOwnerAccountName];
			//[ret setValue:@"2010-03-17 21:20:08 +0100"		forKey:NSFileModificationDate];
			//[ret setValue:[NSNumber numberWithInt:0]		forKey:NSFileOwnerAccountID];
			//[ret setValue:@"root"						forKey:NSFileOwnerAccountName];
			//[ret setValue:[NSNumber numberWithInt:438]		forKey:NSFilePosixPermissions];
			//[ret setValue:[NSNumber numberWithInt:1]		forKey:NSFileReferenceCount];
			//[ret setValue:[NSNumber numberWithInt:0]		forKey:NSFileSize];
			//[ret setValue:[NSNumber numberWithInt:298]		forKey:NSFileSystemFileNumber];
			//[ret setValue:[NSNumber numberWithInt:136795956]	forKey:NSFileSystemNumber];
		[ret setValue:NSFileTypeRegular				forKey:NSFileType];
			//[ret setValue:NSFileTypeSocket				forKey:NSFileType];
		
			//NSDictionary* cAttribs = [[NSFileManager defaultManager] attributesOfItemAtPath:@"/dev/tty" error:error];
			//NSLog(@"x %@",cAttribs);
		return ret;
	}
	
	NSString* mPath = [metafs getPath:fsPath];
	
	if([config isDirectory:mPath]) {
		NSDictionary* mAttribs = [[NSFileManager defaultManager] attributesOfItemAtPath:mPath error:error];
		return mAttribs;
	}
	
	NSMutableDictionary* meta = [metafs getMetaData:fsPath];
	if (!meta) return nil;
	NSDictionary* mAttribs = [meta valueForKey:MDiAttributes];
	
	LOG(@"attributesOfItemAtPath %@ is %@",fsPath,mAttribs);
	return mAttribs;
}

- (NSDictionary *)attributesOfFileSystemForPath:(NSString *)fsPath
										  error:(NSError **)error {
	LOG(@"attributesOfFileSystemForPath %@",fsPath);
	
	
	NSString* mPath = [metafs getPath:fsPath];
	NSDictionary* dict = [[NSFileManager defaultManager] attributesOfFileSystemForPath:mPath error:error];
	if (dict) {
		NSMutableDictionary* attribs = [NSMutableDictionary dictionaryWithDictionary:dict];
		[attribs setObject:[NSNumber numberWithBool:YES]
					forKey:kGMUserFileSystemVolumeSupportsExtendedDatesKey];
		
		LOG(@"attributesOfFileSystemForPath %@ is %@",fsPath,attribs);
		return attribs;
	}
	LOG(@"attributesOfFileSystemForPath %@ is -",fsPath);
	return nil;
}

- (BOOL)setAttributes:(NSDictionary *)attributes 
		 ofItemAtPath:(NSString *)fsPath
			 userData:(id)userData
				error:(NSError **)error {
	LOG(@"setAttributes %@ to %@",fsPath,attributes);
	
	NSString* mPath = [metafs getPath:fsPath];
	
	if ([config isDirectory:mPath]) {
		return [[NSFileManager defaultManager] setAttributes:attributes
												ofItemAtPath:mPath
													   error:error];
	}
	
		// TODO: Handle other keys not handled by NSFileManager setAttributes call.
	
	NSMutableDictionary* meta = [metafs getMetaData:fsPath];
	if (!meta) return NO;
	NSMutableDictionary* dict = [NSMutableDictionary dictionaryWithDictionary:[meta valueForKey:MDiAttributes]];
	
	id key;
	for (NSEnumerator *e = [attributes keyEnumerator];(key = [e nextObject]);) {
		[dict setValue:[attributes valueForKey:key] forKey:key];
	}
	
	[meta setValue:dict forKey:MDiAttributes];
	BOOL ret = [metafs setMetaData:meta fsPath:fsPath];
	[metafs updateMetaData:fsPath];
	return ret;
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

#pragma mark Extended Attributes

- (NSArray *)extendedAttributesOfItemAtPath:(NSString *)fsPath error:(NSError **)error {
	LOG(@"extendedAttributesOfItemAtPath %@",fsPath);
	
	NSString* mPath = [metafs getPath:fsPath];
	
	
	ssize_t size = listxattr([mPath UTF8String], nil, 0, 0);
	if ( size < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return nil;
	}
	NSMutableData* data = [NSMutableData dataWithLength:size];
	size = listxattr([mPath UTF8String], [data mutableBytes], [data length], 0);
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
	
	LOG(@"extendedAttributesOfItemAtPath %@ is %@", fsPath ,contents);
	return contents;
}

- (NSData *)valueOfExtendedAttribute:(NSString *)name 
						ofItemAtPath:(NSString *)fsPath
							position:(off_t)position
							   error:(NSError **)error {  
	LOG(@"valueOfExtendedAttribute %@",fsPath);
	
		//if ([self isDirectory:fsPath]) {
	NSString* p = [metafs getPath:fsPath];
	
	ssize_t size = getxattr([p UTF8String], [name UTF8String], nil, 0,
							position, 0);
	if ( size < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return nil;
	}
	NSMutableData* data = [NSMutableData dataWithLength:size];
	size = getxattr([p UTF8String], [name UTF8String], 
					[data mutableBytes], [data length],
					position, 0);
	
	if ( size < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return nil;
	}
	return data;
		//}
	
	
		//NSMutableDictionary* meta = [self getMetaData:fsPath];
		//NSArray* contents = [meta valueForKey:MDiExtended];
	
		//NSMutableData* data = [NSMutableData dataWithLength:20];
		//if (TraceAttributes) NSLog(@"valueOfExtendedAttribute %@ %@ is %@", name, fsPath ,data);
		// return data;
		//return nil;
}

- (BOOL)setExtendedAttribute:(NSString *)name 
				ofItemAtPath:(NSString *)fsPath 
					   value:(NSData *)value
					position:(off_t)position
					 options:(int)options
					   error:(NSError **)error {
	LOG(@"setExtendedAttribute %@",fsPath);
	NSLog(@"Extended %@",value);
		// Setting com.apple.FinderInfo happens in the kernel, so security related 
		// bits are set in the options. We need to explicitly remove them or the call
		// to setxattr will fail.
		// TODO: Why is this necessary?
	
		//if ([self isDirectory:fsPath]) {
	options &= ~(XATTR_NOSECURITY | XATTR_NODEFAULT);
	NSString* p = [metafs getPath:fsPath];
	int ret = setxattr([p UTF8String], [name UTF8String], 
					   [value bytes], [value length], 
					   position, options);
	if ( ret < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return NO;
	}
	NSLog(@"setExtendedAttribute %@ %@ to %@", name, fsPath ,value);
		// [self updateMetaFile:path attributes:nil];
	return YES;
		//}
		//return NO;
}

- (BOOL)removeExtendedAttribute:(NSString *)name
				   ofItemAtPath:(NSString *)fsPath
						  error:(NSError **)error {
	LOG(@"removeExtendedAttribute %@",fsPath);
	
		//if ([self isDirectory:fsPath]) {
	
	NSString* p = [metafs getPath:fsPath];
	int ret = removexattr([p UTF8String], [name UTF8String], 0);
	if ( ret < 0 ) {
		*error = [NSError errorWithPOSIXCode:errno];
		return NO;
	}
	NSLog(@"removeExtendedAttribute %@ %@", name, fsPath);
	return YES;
		//}
		//return NO;
}

@end
