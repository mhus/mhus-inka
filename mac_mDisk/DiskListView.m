//
//  DiskListView.m
//  mDisk
//
//  Created by Mike Hummel on 16/06/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import "DiskListView.h"


@implementation DiskListView

- (id) initWithCoder: (NSCoder *) decoder
{
	if (self = [super initWithCoder:decoder])
	{
		[self setDelegate: self];
		disks = [NSMutableArray array];
			//		MFFilesystemCell* cell = [MFFilesystemCell new];
			//[[[self tableColumns] objectAtIndex:0] setDataCell: cell];
		[self setDataSource: self];
	}
	
	return self;
}

@end
