//
//  mDisk_Controller.m
//  mDisk
//
//  Created by Mike Hummel on 15/06/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//
#import "mDisk_Controller.h"
#import "Disk.h"
#import "DiskListener.h"
#import "DiskListView.h"
#import <MacFUSE/MacFUSE.h>

@implementation mDisk_Controller

- (void)applicationDidFinishLaunching:(NSNotification *)notification {
	
	//[self openDiskWithDialog];
	
}

- (void)onDiskMounted:(Disk *) disk {
	[diskList reloadData];
}
- (void)onDiskMountFailed:(Disk *) disk {
	[diskList reloadData];
}
- (void)onDiskUnmounted:(Disk *) disk {
	[diskList reloadData];
}

- (void)awakeFromNib{
	
	disks = [NSMutableArray new];
	[diskList reloadData];
	
}

- (void)openDisk:(NSString *)rootPath {
	Disk* newdisk = [[Disk alloc] initWithRootPath:rootPath listener:self];
	if ([newdisk isValide]) {
		[disks addObject:newdisk];
		[diskList reloadData];
	} else {
		NSRunAlertPanel(@"Open", @"Can't open disk", @"OK", nil, nil);
	}

}

- (NSApplicationTerminateReply)applicationShouldTerminate:(NSApplication *)sender {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
	
	for (int i=0; i < [disks count];i++) {
		Disk* d = [disks objectAtIndex:i];
		[d dealloc];
	}
	
  return NSTerminateNow;
}

-(IBAction)open:(id)sender {
	NSOpenPanel* panel = [NSOpenPanel openPanel];
	[panel setCanChooseFiles:NO];
	[panel setCanChooseDirectories:YES];
	[panel setAllowsMultipleSelection:NO];
	int ret = [panel runModalForDirectory:@"/Users/mikehummel/tmp" file:nil types:nil];
	if ( ret == NSCancelButton ) {
		return;
	}
	NSArray* paths = [panel filenames];
	if ( [paths count] != 1 ) {
		return;
	}
	NSString* rootPath = [paths objectAtIndex:0];
	
	[self openDisk:rootPath];
	
}

-(IBAction)close:(id)sender {
		//[disks removeObject:disk];
}

-(IBAction)mount:(id)sender {
	NSInteger selectedRow = [diskList selectedRow];
	if (selectedRow<0 || selectedRow >= [disks count])
		return;
	Disk* d = [disks objectAtIndex:selectedRow];
	if ([d isValide] && ![d isMounted]) {
		[d doMount];
	} else {
		NSRunAlertPanel(@"Mount", @"Mount failed", @"OK", nil, nil);
	}

}

-(IBAction)unmount:(id)sender {
	
}

- (int)numberOfRowsInTableView:(NSTableView *)diskList {	
	return ([disks count]);	
}

- (id)tableView:(NSTableView *)diskList objectValueForTableColumn:(NSTableColumn *)tableColumn row:(int)row{
		Disk* d = [disks objectAtIndex:row];
		NSString* name = [d getName];
		if (![d isValide]) {
			name = [NSString stringWithFormat:@"! %@",name];
		} else if ([d isMounted]) {
			name = [NSString stringWithFormat:@"* %@",name];			
		}
	return name;
}

- (void)dealloc{
	
	[disks release];	
	[super dealloc];
	
}

@end
