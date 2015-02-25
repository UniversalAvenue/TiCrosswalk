/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2015 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiUIWebViewProxy+Crosswalk.h"

@implementation TiUIWebViewProxy (Crosswalk)

- (void)evalAsync:(id)args
{
    NSString *code;
    NSString *res;
    KrollCallback *callback;
    
    /*
     Using GCD either through dispatch_async/dispatch_sync or TiThreadPerformOnMainThread
     does not work reliably for evalJS on 5.0 and above. See sample in TIMOB-7616 for fail case.
     */
    if ([NSThread isMainThread]) {
        ENSURE_ARG_AT_INDEX(code, args, 0, NSString);
        ENSURE_ARG_OR_NIL_AT_INDEX(callback, args, 1, KrollCallback);
        
        code = [[NSString stringWithFormat:@"JSON.stringify(%@)", code] retain];

        if (callback != nil) {
            [callback retain];
        }

        res = [[(TiUIWebView*)[self view] stringByEvaluatingJavaScriptFromString:code] retain];
        NSLog(@"evalAsync: \"%@\" -> %@", code, res);
        
        if (callback != nil) {
            [callback call:[NSArray arrayWithObject:res] thisObject:self];
            [callback release];
        }
        
        [code release];
        [res release];
    } else {
        [self performSelectorOnMainThread:@selector(evalAsync:) withObject:args waitUntilDone:NO];
    }
}

@end
