//
//  PluginLibrary.h
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#ifndef _PluginScoreflex_H__
#define _PluginScoreflex_H__

#include "CoronaLua.h"

#include "CoronaMacros.h"

// This corresponds to the name of the library, e.g. [Lua] require "plugin.library"
// where the '.' is replaced with '_'
CORONA_EXPORT int luaopen_plugin_scoreflex( lua_State *L );

#endif // _PluginScoreflex_H__


