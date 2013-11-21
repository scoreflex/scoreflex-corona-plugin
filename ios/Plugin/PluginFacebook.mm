//
//  PluginLibrary.mm
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "PluginFacebook.h"
#include "CoronaRuntime.h"
#include "Scoreflex.h"
#import "GAI.h"
#import <FacebookSDK/FacebookSDK.h>
#import <UIKit/UIKit.h>

// ----------------------------------------------------------------------------

class PluginFacebook
{
	public:
		typedef PluginFacebook Self;

	public:
		static const char kName[];
    
	protected:
		PluginFacebook();

	public:
		static int Open( lua_State *L );

	protected:
		static int Finalizer( lua_State *L );

	public:
		static Self *ToLibrary( lua_State *L );
    
	public:
        static int share(lua_State *L);
};

// ----------------------------------------------------------------------------

// This corresponds to the name of the library, e.g. [Lua] require "plugin.library"
const char PluginFacebook::kName[] = "plugin.facebook";

// This corresponds to the event name, e.g. [Lua] event.name
PluginFacebook::PluginFacebook()
{
}

int PluginFacebook::Open( lua_State *L )
{
	// Register __gc callback
	const char kMetatableName[] = __FILE__; // Globally unique string to prevent collision
	CoronaLuaInitializeGCMetatable( L, kMetatableName, Finalizer );
    [GAI sharedInstance].debug = YES;
	// Functions in library
	const luaL_Reg kVTable[] =
	{
        {"share", share},
		{ NULL, NULL }
	};

	// Set library as upvalue for each library function
	Self *library = new Self;
	CoronaLuaPushUserdata( L, library, kMetatableName );

	luaL_openlib( L, kName, kVTable, 1 ); // leave "library" on top of stack

	return 1;
}

int PluginFacebook::share(lua_State *L)
{
    const char *cUrl = lua_tostring(L, 1);
    const char *name = lua_tostring(L, 2);
    const char *caption = lua_tostring(L, 3);
    const char *description = lua_tostring(L, 4);
    
    NSString *nameString = [NSString stringWithUTF8String:name];
    NSString *captionString = [NSString stringWithUTF8String:caption];
    NSString *descriptionString = [NSString stringWithUTF8String:description];
    NSString *urlString = [NSString stringWithUTF8String:cUrl];
    FBShareDialogParams *p = [[FBShareDialogParams alloc] init];
    NSURL* url = [NSURL URLWithString:[NSString stringWithUTF8String:cUrl]];
    p.link = url;
    if ([FBDialogs canPresentShareDialogWithParams:p]) {

//        NSURL* url = [NSURL URLWithString:[NSString stringWithUTF8String:cUrl]];
        [FBDialogs presentShareDialogWithLink:url name:nameString caption:captionString description:descriptionString picture:nil clientState:nil handler:
                                    ^(FBAppCall *call, NSDictionary *results, NSError *error) {
                                          if(error) {
                                              NSLog(@"Error: %@", error.description);
                                          } else {
                                              NSLog(@"Success!");
                                          }
                                      }];
    } else {
        NSDictionary* params = @{@"name": nameString,
                                 @"caption":captionString,
                                 @"description": descriptionString,
                                 @"link": urlString};
        
        [FBWebDialogs presentFeedDialogModallyWithSession:nil
                                               parameters:params
                                                  handler:^(FBWebDialogResult result, NSURL *resultURL, NSError *error) {
                                                      // handle response or error
                                                  }];
    }
    return 0;
}

int PluginFacebook::Finalizer( lua_State *L )
{
	Self *library = (Self *)CoronaLuaToUserdata( L, 1 );

	delete library;

	return 0;
}

PluginFacebook *PluginFacebook::ToLibrary( lua_State *L )
{
	// library is pushed as part of the closure
	Self *library = (Self *)CoronaLuaToUserdata( L, lua_upvalueindex( 1 ) );
	return library;
}

// ----------------------------------------------------------------------------

CORONA_EXPORT int luaopen_plugin_facebook( lua_State *L )
{
	return PluginFacebook::Open( L );
}
