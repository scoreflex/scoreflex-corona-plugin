//
//  NSNotificationObserver.h
//  Plugin.scoreflex
//
//  Created by julien muniak on 8/5/13.
//
//


#include "CoronaLua.h"
#include "CoronaMacros.h"
#import <Foundation/Foundation.h>

@interface NotificationObserver : NSObject {
    lua_State *state;
}
-(void) forwardEvent:(NSNotification *) notification;
-(id)initWithState:(lua_State *)l;
@end

