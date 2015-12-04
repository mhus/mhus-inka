//
//  DiskListView.h
//  mDisk
//
//  Created by Mike Hummel on 16/06/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface DiskListView : NSTableView {
	NSMutableArray* disks;
}

	//@property(retain, readwrite) NSMutableArray* disks;

@end
