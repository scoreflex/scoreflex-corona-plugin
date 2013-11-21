//
//  PluginLibrary.mm
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "PluginAdUtils.h"
#import "Chartboost.h"
#import "GADBannerView.h"
#include "CoronaRuntime.h"
#include "Scoreflex.h"

#import <UIKit/UIKit.h>

// ----------------------------------------------------------------------------

class PluginAdUtils
{
	public:
		typedef PluginAdUtils Self;

	public:
		static const char kName[];
    
	protected:
		PluginAdUtils();

	public:
		static int Open( lua_State *L );

	protected:
		static int Finalizer( lua_State *L );

	public:
		static Self *ToLibrary( lua_State *L );
    
	public:
		static int startChartboostSession( lua_State *L );
        static int showChartboostInterstitial( lua_State *L );
        static int showAdMobAd( lua_State *L );
        static int hideAdMobAd( lua_State *L );
};

// ----------------------------------------------------------------------------

// This corresponds to the name of the library, e.g. [Lua] require "plugin.library"
const char PluginAdUtils::kName[] = "plugin.adutils";

static Chartboost *fChartboost;
static GADBannerView *fBannerView;

// This corresponds to the event name, e.g. [Lua] event.name
PluginAdUtils::PluginAdUtils()
{
}

int PluginAdUtils::Open( lua_State *L )
{
	// Register __gc callback
	const char kMetatableName[] = __FILE__; // Globally unique string to prevent collision
	CoronaLuaInitializeGCMetatable( L, kMetatableName, Finalizer );

	// Functions in library
	const luaL_Reg kVTable[] =
	{
        {"startChartboostSession", startChartboostSession},
        {"showChartboostInterstitial", showChartboostInterstitial},
        {"showAdMobAd", showAdMobAd},
        {"hideAdMobAd", hideAdMobAd},
		{ NULL, NULL }
	};

	// Set library as upvalue for each library function
	Self *library = new Self;
	CoronaLuaPushUserdata( L, library, kMetatableName );

	luaL_openlib( L, kName, kVTable, 1 ); // leave "library" on top of stack

	return 1;
}



int PluginAdUtils::startChartboostSession( lua_State *L )
{
    const char *appId = lua_tostring(L, 1);
    const char *appSignature = lua_tostring(L, 2);
    NSString *preloadInterstitialName = nil;
    if (lua_isstring(L, 3)) {
        preloadInterstitialName = [NSString stringWithUTF8String:lua_tostring(L,3)];
    }
    
    fChartboost = [Chartboost sharedChartboost];
    
    fChartboost.appId = [NSString stringWithUTF8String:appId];
    fChartboost.appSignature =[NSString stringWithUTF8String:appSignature];

    [fChartboost startSession];
    
    if (preloadInterstitialName) {
        [fChartboost cacheInterstitial:preloadInterstitialName];
    }
    return 0;
}

int PluginAdUtils::showChartboostInterstitial( lua_State *L )
{
    NSString *interstitialName = [NSString stringWithUTF8String:lua_tostring(L,1)];;
    if (fChartboost != nil && [fChartboost hasCachedInterstitial:interstitialName]) {
        [fChartboost showInterstitial:interstitialName];
    }
    return 0;
}

int PluginAdUtils::showAdMobAd( lua_State *L )
{
    id<CoronaRuntime> runtime = (id<CoronaRuntime>)CoronaLuaGetContext( L );
    NSString *campaignId = [NSString stringWithUTF8String:lua_tostring(L, 1)];
    int placement = lua_tointeger(L, 2);
    fBannerView = [[GADBannerView alloc] initWithAdSize:kGADAdSizeBanner];
    fBannerView.adUnitID = campaignId;//MY_BANNER_UNIT_ID;
    GADRequest *request = [GADRequest request];
    request.testDevices = [NSArray arrayWithObjects:GAD_SIMULATOR_ID, nil];
    
    fBannerView.rootViewController = runtime.appViewController;
    [runtime.appViewController.view addSubview:fBannerView];
    
    if (placement == AD_PLACEMENT_BOTTOM)
    {
        CGRect screenRect = [[UIScreen mainScreen] bounds];
        float x = 0;
        if (screenRect.size.width > fBannerView.frame.size.width)
        {
            x = (screenRect.size.width - fBannerView.frame.size.width) / 2;
        }
        CGRect frame = CGRectMake(x, screenRect.size.height - 50, fBannerView.frame.size.width, fBannerView.frame.size.height);
        fBannerView.frame = frame;
    }
    else
    {
        CGRect screenRect = [[UIScreen mainScreen] bounds];
        float x = 0;
        if (screenRect.size.width > fBannerView.frame.size.width)
        {
            x = (screenRect.size.width - fBannerView.frame.size.width) / 2;
        }
        CGRect frame = CGRectMake(x, 0, fBannerView.frame.size.width, fBannerView.frame.size.height);
        fBannerView.frame = frame;
    }
    [fBannerView loadRequest: request];    
    return 0;
}

int PluginAdUtils::hideAdMobAd( lua_State *L )
{
    if (fBannerView == nil)
    {
        return 0;
    }
    [fBannerView removeFromSuperview];
    [fBannerView release];
    fBannerView = nil;
    return 0;
}


int PluginAdUtils::Finalizer( lua_State *L )
{
	Self *library = (Self *)CoronaLuaToUserdata( L, 1 );

	delete library;

	return 0;
}

PluginAdUtils *PluginAdUtils::ToLibrary( lua_State *L )
{
	// library is pushed as part of the closure
	Self *library = (Self *)CoronaLuaToUserdata( L, lua_upvalueindex( 1 ) );
	return library;
}

// ----------------------------------------------------------------------------

CORONA_EXPORT int luaopen_plugin_adutils( lua_State *L )
{
    [[UIApplication sharedApplication] setStatusBarOrientation:UIInterfaceOrientationLandscapeLeft animated:NO];
	return PluginAdUtils::Open( L );
}
