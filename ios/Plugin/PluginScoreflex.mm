//
//  PluginLibrary.mm
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "PluginScoreflex.h"

#include "CoronaRuntime.h"
#include "Scoreflex.h"
#import "NotificationObserver.h"
#import <UIKit/UIKit.h>

// ----------------------------------------------------------------------------

class PluginScoreflex
{
	public:
		typedef PluginScoreflex Self;

	public:
		static const char kName[];
		static const char kEvent[];

	protected:
		PluginScoreflex();

	public:
		static int Open( lua_State *L );

	protected:
		static int Finalizer( lua_State *L );

	public:
		static Self *ToLibrary( lua_State *L );

	public:
		static int initialize( lua_State *L );
        static int api( lua_State *L );
        static int setForceMerge( lua_State *L);
        static int view(lua_State *L);
        static int presentResource( lua_State *L );
        static int preloadResource( lua_State *L );
        static int registerForPushNotification(lua_State *L);
        static int freePreloadedResource(lua_State *L);
        static int submitScoreAndShowRankbox(lua_State *L);
        static int hideRankbox(lua_State *L);
        static int nothing(lua_State *L);
};

// ----------------------------------------------------------------------------

// This corresponds to the name of the library, e.g. [Lua] require "plugin.library"
const char PluginScoreflex::kName[] = "plugin.scoreflex";

// This corresponds to the event name, e.g. [Lua] event.name
const char PluginScoreflex::kEvent[] = "pluginlibraryevent";

static UIView *rankbox;

PluginScoreflex::PluginScoreflex()
{
}


int PluginScoreflex::Open( lua_State *L )
{
	// Register __gc callback
	const char kMetatableName[] = __FILE__; // Globally unique string to prevent collision
	CoronaLuaInitializeGCMetatable( L, kMetatableName, Finalizer );

	// Functions in library
	const luaL_Reg kVTable[] =
	{
		{ "initialize", initialize },
        { "setForceMerge", setForceMerge},
        { "api", api},
        { "view", view},
        { "presentResource", presentResource},
        { "registerForPushNotification", registerForPushNotification},
        { "checkNotification", nothing},
        { "preload", preloadResource},
        { "freePreloadedResource", freePreloadedResource},
        { "submitScoreAndShowRankbox", submitScoreAndShowRankbox},
        { "hideRankbox", hideRankbox},
		{ NULL, NULL }
	};

	// Set library as upvalue for each library function
	Self *library = new Self;
	CoronaLuaPushUserdata( L, library, kMetatableName );

	luaL_openlib( L, kName, kVTable, 1 ); // leave "library" on top of stack
//    GPPSignIn *signIn = [GPPSignIn sharedInstance];
//    signIn.clientID = @"916327564793-1bnbkovkufjd4gnh787d1sn3v4bf06dd.apps.googleusercontent.com";
    
	return 1;
}

void handleCallback(lua_State *L, int callbackReferenceId, SXResponse *response, NSError *error)
{
    lua_rawgeti(L, LUA_REGISTRYINDEX, callbackReferenceId);
    lua_newtable(L);
    if (error != nil) {
        lua_pushstring(L, "isError");
        lua_pushboolean(L, true);
        lua_settable(L, -3);
        lua_pushstring(L, "errorMessage");
        lua_pushstring(L, [[error localizedDescription] UTF8String]);
        lua_settable(L, -3);
    } else {
        lua_pushstring(L, "isError");
        lua_pushboolean(L, false);
        lua_settable(L, -3);
    }
    
    if (response != nil) {
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:[response object] options:nil error:&error];
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        
        lua_pushstring(L, "response");
        lua_pushstring(L, [jsonString UTF8String]);
        lua_settable(L, -3);
        [jsonString release];
    }
    lua_call(L, 1, 0);
    luaL_unref(L, LUA_REGISTRYINDEX, callbackReferenceId);
}

int PluginScoreflex::view(lua_State *L)
{
    NSString *ressource = [NSString stringWithUTF8String:lua_tostring(L, 1)];
    NSMutableDictionary *params = [[NSMutableDictionary alloc] init];
    if (lua_istable(L, 2)) {
        lua_pushnil(L);
        while (lua_next(L, 2) != 0) {
            const char *key = lua_tostring(L, -2);
            const char *value = lua_tostring(L, -1);
            [params setObject:[NSString stringWithUTF8String:value] forKey:[NSString stringWithUTF8String:key]];
            //            NSLog(@"key: %@, value: %@", [NSString stringWithUTF8String:key], [NSString stringWithUTF8String:value]);
            lua_pop(L, 1);
        }
    }
    BOOL forceFullScreen = lua_toboolean(L, 3) == 1 ? YES:NO;
    if (forceFullScreen) {
        [Scoreflex showFullScreenView:ressource params:params];
    } else {
        [Scoreflex showPanelView:ressource params:params gravity:SXGravityTop];
    }
//    SXView *view = (SXView *)[Scoreflex view:ressource params:params forceFullScreen:forceFullScreen];
//    if (!forceFullScreen) {
//        view.frame = CGRectMake(0,380, 320, 100);
//        id<CoronaRuntime> runtime = (id<CoronaRuntime>)CoronaLuaGetContext( L );
//        [runtime.appViewController.view addSubview:view];
//    }
    return 0;
}

int PluginScoreflex::api(lua_State *L)
{
    const char *method = lua_tostring(L, 1);
    const char *ressource = lua_tostring(L, 2);
    int callbackReferenceId = -1;
    NSMutableDictionary *params = [[NSMutableDictionary alloc] init];
    if (lua_istable(L, 3)) {
        lua_pushnil(L);
        while (lua_next(L, 3) != 0) {
            const char *key = lua_tostring(L, -2);
            const char *value = lua_tostring(L, -1);
            [params setObject:[NSString stringWithUTF8String:value] forKey:[NSString stringWithUTF8String:key]];
//            NSLog(@"key: %@, value: %@", [NSString stringWithUTF8String:key], [NSString stringWithUTF8String:value]);
            lua_pop(L, 1);
        }
    }
    
    if (lua_isfunction(L, 4)) {
        lua_pushvalue(L, 4);
        callbackReferenceId = luaL_ref(L, LUA_REGISTRYINDEX);
    }
    int len = strlen(method);
    char *editableMethod = (char *)malloc((len + 1) * sizeof(char));
    for (int i = 0; i < len; i++) {
        editableMethod[i] = tolower(method[i]);
    }
    editableMethod[len] = 0;
    if (strcmp(editableMethod, "get") == 0) {
        [Scoreflex get:[NSString stringWithUTF8String:ressource] params:params handler:^(SXResponse *response, NSError *error) {
            handleCallback(L, callbackReferenceId, response, error);
        }];
    } else if (strcmp(editableMethod, "post")) {
        [Scoreflex post:[NSString stringWithUTF8String:ressource] params:params handler:^(SXResponse *response, NSError *error) {
            handleCallback(L, callbackReferenceId, response, error);
        }];
    } else if (strcmp(editableMethod, "delete")) {
        [Scoreflex delete:[NSString stringWithUTF8String:ressource] params:params handler:^(SXResponse *response, NSError *error) {
            handleCallback(L, callbackReferenceId, response, error);
        }];
    } else if (strcmp(editableMethod, "postEventually")) {
        [Scoreflex postEventually:[NSString stringWithUTF8String:ressource] params:params handler:^(SXResponse *response, NSError *error) {
            handleCallback(L, callbackReferenceId, response, error);
        }];
    }
    free(editableMethod);
    [params release];
    return 0;
}

int PluginScoreflex::presentResource( lua_State *L)
{
    NSString *leaderboardId = [NSString stringWithUTF8String:lua_tostring(L, 1)];
    NSString *Score = [NSString stringWithUTF8String:lua_tostring(L, 2)];
    id<CoronaRuntime> runtime = (id<CoronaRuntime>)CoronaLuaGetContext( L );
    CGRect frame = CGRectMake(0,380, 320, 100);
    SXView *view = [[SXView alloc] initWithFrame:frame];
    NSDictionary *params =  [NSDictionary dictionaryWithObjectsAndKeys:Score, @"score", nil];
    [view openResource:[NSString stringWithFormat:@"web/scores/%@/ranks", leaderboardId] params:params];
    [runtime.appViewController.view addSubview:view];
    return 0;
}

int PluginScoreflex::freePreloadedResource(lua_State *L)
{
    NSString *ressource =nil;
    if (lua_isstring(L, 1)) {
       ressource = [NSString stringWithUTF8String:lua_tostring(L, 1)];
    }
    [Scoreflex freePreloadedResource:ressource];
    return 0;
}

int PluginScoreflex::preloadResource( lua_State *L)
{
    NSString *ressource = [NSString stringWithUTF8String:lua_tostring(L, 1)];
    [Scoreflex preloadResource:ressource];
    return 0;
}

int PluginScoreflex::registerForPushNotification( lua_State *L )
{
    [[UIApplication sharedApplication] registerForRemoteNotificationTypes:
     (UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
    return 0;
}

int PluginScoreflex::nothing( lua_State *L )
{
    return 0;
}

int PluginScoreflex::setForceMerge( lua_State *L)
{
    BOOL forceMerge = lua_toboolean(L, 1) == 1 ? YES:NO;
    [Scoreflex setForceMerge:forceMerge];
    return 0;
}

int PluginScoreflex::Finalizer( lua_State *L )
{
	Self *library = (Self *)CoronaLuaToUserdata( L, 1 );

	delete library;

	return 0;
}

int PluginScoreflex::submitScoreAndShowRankbox(lua_State *L)
{
    NSString *leaderboardId = [NSString stringWithUTF8String:lua_tostring(L, 1)];
    NSMutableDictionary *params = [[NSMutableDictionary alloc] init];
    if (lua_istable(L, 2)) {
        lua_pushnil(L);
        while (lua_next(L, 2) != 0) {
            const char *key = lua_tostring(L, -2);
            const char *value = lua_tostring(L, -1);
            [params setObject:[NSString stringWithUTF8String:value] forKey:[NSString stringWithUTF8String:key]];
            //            NSLog(@"key: %@, value: %@", [NSString stringWithUTF8String:key], [NSString stringWithUTF8String:value]);
            lua_pop(L, 1);
        }
    }
    SXGravity gravity = lua_toboolean(L, 3) == 1 ? SXGravityTop:SXGravityBottom;

    rankbox = [Scoreflex submitScoreAndShowRanksPanel:leaderboardId params:params gravity:gravity];
//    SXView *view = (SXView *)[Scoreflex view:ressource params:params forceFullScreen:forceFullScreen];
    return 0;
}

int PluginScoreflex::hideRankbox(lua_State *L)
{
    if (rankbox != nil) {
        [rankbox removeFromSuperview];
        rankbox = nil;
    }
    return 0;
}

PluginScoreflex *PluginScoreflex::ToLibrary( lua_State *L )
{
	// library is pushed as part of the closure
	Self *library = (Self *)CoronaLuaToUserdata( L, lua_upvalueindex( 1 ) );
	return library;
}

// [Lua] library.init( listener )
int PluginScoreflex::initialize( lua_State *L )
{
    const char *clientId = lua_tostring(L, 1);
    const char *clientSecret = lua_tostring(L, 2);
    BOOL isSandbox = NO;
    if (lua_gettop(L) > 2) {
        isSandbox = lua_toboolean(L, 3) == 1 ? YES:NO;
    }
    [Scoreflex setClientId:[NSString stringWithUTF8String:clientId] secret:[NSString stringWithUTF8String:clientSecret] sandboxMode:isSandbox];
	return 0;
}

// ----------------------------------------------------------------------------



CORONA_EXPORT int luaopen_plugin_scoreflex( lua_State *L )
{
    NotificationObserver *observer = [[NotificationObserver alloc] initWithState:L];
    
    [[NSNotificationCenter defaultCenter] addObserver:observer selector:@selector(forwardEvent:) name:SX_NOTIFICATION_START_CHALLENGE object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:observer selector:@selector(forwardEvent:) name:SX_NOTIFICATION_INITIALIZED object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:observer selector:@selector(forwardEvent:) name:SX_NOTIFICATION_PLAY_LEVEL object:nil];
	return PluginScoreflex::Open( L );
}
