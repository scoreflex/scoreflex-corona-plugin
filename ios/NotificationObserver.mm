//
//  NSNotificationObserver.m
//  Plugin.scoreflex
//
//  Created by julien muniak on 8/5/13.
//
//

#import "NotificationObserver.h"
#import "Scoreflex.h"

@implementation NotificationObserver

-(id) initWithState:(lua_State *)l;
{
    self = [super init];
    if (self)
    {
        state = l;
    }
    return self;
}

-(void) forwardEvent:(NSNotification *) notification
{
    id challengeConfig = [[notification userInfo] objectForKey:SX_NOTIFICATION_START_CHALLENGE_CONFIG_KEY];
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:challengeConfig options:nil error:&error];
    NSString *challengJsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

    const char kNameKey[] = "name";
    const char kValueKey[] = "scoreflexStartChallenge";
    lua_newtable( state );
    lua_pushstring( state, kValueKey );
    lua_setfield( state, -2, kNameKey );
    lua_pushstring( state,  [challengJsonString UTF8String]);
    lua_setfield( state, -2, "config");
    UIViewController *rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
    [rootViewController dismissModalViewControllerAnimated:YES];
    Corona::Lua::RuntimeDispatchEvent( state, -1 );
}

@end