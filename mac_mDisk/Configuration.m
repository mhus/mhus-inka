//
//  Cofiguration.m
//  mDisk
//
//  Created by Mike Hummel on 16/03/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import "Configuration.h"

@implementation Configuration

- (id)initWithRootPath:(NSString *)rootPath_ {
	if ((self = [super init])) {
		rootPath = [rootPath_ retain];
		[self loadConfig];
	}
	return self;
}

- (void)loadConfig {
	if (configDoc) {
		return;
	}
	
	NSString* configFile = [[rootPath stringByAppendingFormat:@"/config/mdisk.xml"] retain];
	NSURL *configUrl = [NSURL fileURLWithPath:configFile];
	NSError *err = nil;
	configDoc = [[NSXMLDocument alloc] initWithContentsOfURL:configUrl 
													 options:(NSXMLNodePreserveWhitespace|NSXMLNodePreserveCDATA) 
													   error:&err];	
	//[configFile release];
	//[configUrl release];
	
}

- (BOOL)isValide{
	return configDoc != NULL;	
}

- (NSString *)getConfigValue: (NSString *) xpath def:(NSString *)def {
	
	if (!configDoc) {
		return def;
	}
	NSError *err = nil;
	NSArray *nodes = [configDoc nodesForXPath:xpath error:&err];
	NSString *ret = def;
	if([nodes count] > 0) {
		ret = [[nodes objectAtIndex:0] stringValue];
	}
	//[nodes release];
	
	
	NSMutableString *s = [NSMutableString stringWithString:ret];
	[s replaceOccurrencesOfString:@"{rootPath}" withString:rootPath options:(NSCaseInsensitiveSearch) range:NSMakeRange(0,[s length])];
	//[ret release];
	ret = [[NSString stringWithString:s] retain];
	//[s release];
	
	return ret;
}

// Utility functions

- (NSString *)getDirectoryPath:(NSString *)filePath {
	// find the directory and create it in content
	NSRange searchRange;
	searchRange.location=(unsigned int)'/';
	searchRange.length=1;
	NSRange foundRange = [filePath rangeOfCharacterFromSet:[NSCharacterSet characterSetWithRange:searchRange] options:NSBackwardsSearch];
	int pos=foundRange.location;
	NSString* directory = [filePath substringToIndex:pos];	
	return directory;
}

- (NSString *)getFilePath:(NSString *)filePath {
	// find the directory and create it in content
	NSRange searchRange;
	searchRange.location=(unsigned int)'/';
	searchRange.length=1;
	NSRange foundRange = [filePath rangeOfCharacterFromSet:[NSCharacterSet characterSetWithRange:searchRange] options:NSBackwardsSearch];
	int pos=foundRange.location;
	NSString* directory = [filePath substringFromIndex:pos+1];	
	return directory;
}

- (BOOL) isDirectory:(NSString *)path {
	BOOL isDir = YES;
	return [[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir] && isDir;
}

- (BOOL) isFile:(NSString *)path {
	BOOL isDir = YES;
	return [[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDir] && !isDir;
}

- (void) dealloc {
	[rootPath release];
	[configDoc release];
	[super dealloc];
}

@end
