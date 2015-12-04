//
//  mDiskApp.h
//  mDisk
//
//  Created by Mike Hummel on 16/06/2010.
//  Copyright 2010 L & S Software GmbH. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface mDiskApp : NSApplication {
	IBOutlet NSWindow * mainWindow;

}

-(IBAction)open:(id)sender;
-(IBAction)close:(id)sender;
-(IBAction)mount:(id)sender;
-(IBAction)unmount:(id)sender;


@end
