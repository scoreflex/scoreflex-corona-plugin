//
//  PluginLibrary.mm
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "PluginGanalytics.h"
#include "CoronaRuntime.h"
#include "Scoreflex.h"
#import "GAI.h"

#import <UIKit/UIKit.h>

// ----------------------------------------------------------------------------

class PluginGanalytics
{
	public:
		typedef PluginGanalytics Self;

	public:
		static const char kName[];
    
	protected:
		PluginGanalytics();

	public:
		static int Open( lua_State *L );

	protected:
		static int Finalizer( lua_State *L );

	public:
		static Self *ToLibrary( lua_State *L );
    
	public:
        static int sendView(lua_State *L);
        static int sendEvent(lua_State *L);
};

// ----------------------------------------------------------------------------

// This corresponds to the name of the library, e.g. [Lua] require "plugin.library"
const char PluginGanalytics::kName[] = "plugin.ganalytics";

// This corresponds to the event name, e.g. [Lua] event.name
PluginGanalytics::PluginGanalytics()
{
}

int PluginGanalytics::Open( lua_State *L )
{
	// Register __gc callback
	const char kMetatableName[] = __FILE__; // Globally unique string to prevent collision
	CoronaLuaInitializeGCMetatable( L, kMetatableName, Finalizer );
    [GAI sharedInstance].debug = YES;
	// Functions in library
	const luaL_Reg kVTable[] =
	{
        {"sendView", sendView},
        {"sendEvent", sendEvent},
		{ NULL, NULL }
	};

	// Set library as upvalue for each library function
	Self *library = new Self;
	CoronaLuaPushUserdata( L, library, kMetatableName );

	luaL_openlib( L, kName, kVTable, 1 ); // leave "library" on top of stack

	return 1;
}

int PluginGanalytics::sendView(lua_State *L)
{
    NSString *campaignId = [NSString stringWithUTF8String:lua_tostring(L, 1)];
    NSString *viewName = [NSString stringWithUTF8String:lua_tostring(L, 2)];
    id tracker = [[GAI sharedInstance] trackerWithTrackingId:campaignId];
    [tracker sendView:viewName];
    return 0;
}
int PluginGanalytics::sendEvent(lua_State *L)
{
    NSString *campaignId = [NSString stringWithUTF8String:lua_tostring(L, 1)];
    NSString *category = [NSString stringWithUTF8String:lua_tostring(L, 2)];
    NSString *action = [NSString stringWithUTF8String:lua_tostring(L, 3)];
    NSString *label = [NSString stringWithUTF8String:lua_tostring(L, 4)];
    NSNumber *value = [NSNumber numberWithDouble:lua_tonumber(L, 5)];
    id tracker = [[GAI sharedInstance] trackerWithTrackingId:campaignId];
    [tracker sendEventWithCategory:category
                        withAction:action
                         withLabel:label
                         withValue:value];
    return 0;
}


int PluginGanalytics::Finalizer( lua_State *L )
{
	Self *library = (Self *)CoronaLuaToUserdata( L, 1 );

	delete library;

	return 0;
}

PluginGanalytics *PluginGanalytics::ToLibrary( lua_State *L )
{
	// library is pushed as part of the closure
	Self *library = (Self *)CoronaLuaToUserdata( L, lua_upvalueindex( 1 ) );
	return library;
}

// ----------------------------------------------------------------------------

CORONA_EXPORT int luaopen_plugin_ganalytics( lua_State *L )
{
	return PluginGanalytics::Open( L );
}
