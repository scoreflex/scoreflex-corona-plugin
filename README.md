# Scoreflex Corona Plugin

## Overview

You should start all new Corona projects by copying this project.
Be aware that this plugins and by the fact that it requires to change the network library of Coronalabs might
actually break some network functionnalities of Corona (lua side)

### iOS

Put the content of the iOS sdk repository located here:
https://github.com/scoreflex/scoreflex-ios-sdk
in the ios-scoreflex-sdk directory

Link your CoronaEnterprise directory in the ios fodler as CoronaEnterprise

#### Android

Download the async http library available here
http://loopj.com/android-async-http/
and put it in your CoronaEnterprise directory in the subfolder:
Corona/android/lib/Corona/libs
move the network.jar file to another location (in case you want to switch back to the corona version of the async http library)

Put the content of the android sdk repository located here:
https://github.com/scoreflex/scoreflex-android-sdk
in the android-scoreflex-sdk directory
