nativeLibraries = {}

if (IS_NATIVE) then
    nativeLibraries.scoreflex  = require "plugin.scoreflex"
    nativeLibraries.adutils    = require "plugin.adutils"
    nativeLibraries.ganalytics = require "plugin.ganalytics"
else 
    -- a cool way to make all libraries not generating any error if not in native mode 
    local mt = {
      __index = function (t,k)
        local retValue = {}
        local secondMt = {
            __index = function (t,k)
                return function() return -1 end
            end
        }
        setmetatable(retValue, secondMt)
        return retValue
      end,
    }
    setmetatable(nativeLibraries,mt)
end
