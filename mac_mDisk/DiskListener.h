//
//  DiskListener.h
//  mDisk
//
//  Created by Mike Hummel on 16/06/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import <Cocoa/Cocoa.h>

@class Disk;

@interface DiskListener : NSObject {

}

- (void)onDiskMounted:(Disk *) disk;
- (void)onDiskMountFailed:(Disk *) disk;
- (void)onDiskUnmounted:(Disk *) disk;

@end
