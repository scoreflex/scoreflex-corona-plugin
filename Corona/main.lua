-- local scoreflex = require "plugin.scoreflex"
-- local adutils = require "plugin.adutils"
-- local ganalytics = require "plugin.ganalytics"
require "nativeLibraries"
-- gameConfiguration = {}

toto = {}
function toto:test(table)
    print("self" .. tostring(self) .. " string: " .. table.name)
end

Runtime:addEventListener("initialized", function(event)
    print("initialed recevied: " .. event.name .. " testL " .. event.test .. " 2:" .. event.test2)
end)


Runtime:addEventListener("scoreflexWebviewCallback", function(event)
    print("webViewCallback recevied: " .. event.name .." data: " .. event.data .. " message: " .. event.message)
end)


nativeLibraries.scoreflex.initialize("", "", true)

-- -- scoreflex.presentResource("global", 200)
-- -- scoreflex.afterLevel("global", 2, {score=200})
-- nativeLibraries.scoreflex.api("GET", "/v1/players/me", {test="test", a="2", d="3"}, function(event)
--   print(event.response)
-- end)
-- timer.performWithDelay(2000, function()
--     nativeLibraries.scoreflex.view("/web/players/me", {}, true)
-- end)

