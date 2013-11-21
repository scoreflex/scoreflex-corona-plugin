//
//  PluginLibrary.h
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#ifndef _PluginAdutils_H__
#define _PluginAdutils_H__

#include "CoronaLua.h"

#include "CoronaMacros.h"

#define AD_PLACEMENT_TOP 1
#define AD_PLACEMENT_BOTTOM 2

CORONA_EXPORT int luaopen_plugin_adutils( lua_State *L );

#endif // _PluginAdutils_H__
