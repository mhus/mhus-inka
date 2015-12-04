//
//  mDisk_Controller.h
//  mDisk
//
//  Created by Mike Hummel on 15/06/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//
#import <Cocoa/Cocoa.h>
#import "DiskListener.h"

@class GMUserFileSystem;
@class mDisk_Controller;
@class DiskListView;

@interface mDisk_Controller : DiskListener {
	IBOutlet NSWindow * mainWindow;
	IBOutlet NSTableView * diskList;
	NSMutableArray* disks;
}

- (void)openDisk:(NSString *)rootPath;

-(IBAction)open:(id)sender;
-(IBAction)close:(id)sender;
-(IBAction)mount:(id)sender;
-(IBAction)unmount:(id)sender;

@end
