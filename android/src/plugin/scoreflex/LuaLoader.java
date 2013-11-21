//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package plugin.scoreflex;

import com.mycompany.app.*;
import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.NamedJavaFunction;
import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;

import com.scoreflex.*;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;


public class LuaLoader implements JavaFunction, CoronaRuntimeListener {

  private CoronaActivity fParentActivity;
  private BroadcastReceiver fMessageReceiver;
  private BroadcastReceiver fConnectivityReceiver;
  private BroadcastReceiver fInitializedReceiver;
  private BroadcastReceiver fPlayLevelReceiver;
  private View fRankbox;

  // This corresponds to the event name, e.g. [Lua] event.name
  // private static final String EVENT_NAME = "pluginlibraryevent";

  /**
   * Creates a new object for displaying banner ads on the CoronaActivity
   */
  public LuaLoader() {
    CoronaActivity activity = CoronaEnvironment.getCoronaActivity();

    // Validate.
    if (activity == null) {
      throw new IllegalArgumentException("Activity cannot be null.");
    }

    // Initialize member variables.
    fParentActivity = activity;
  }

  /**
   * Warning! This method is not called on the main UI thread.
   */
  @Override
  public int invoke(final LuaState L) {
    // LuaState L = runtime.getLuaState();
    // Add functions to library
    NamedJavaFunction[] luaFunctions = new NamedJavaFunction[] {
      new ScoreflexInitWrapper(),
      new ScoreflexAfterLevelWrapper(),
      new ScoreflexApiWrapper(),
      new ScoreflexViewWrapper(),
      new ScoreflexRegisterPushWrapper(),
      new ScoreflexCheckNotificationWrapper(),
      new ScoreflexPreloadWrapper(),
      new ScoreflexDisposePreloadedResourceWrapper(),
      new SubmitScoreAndShowRankboxWrapper(),
      new HideRankboxWrapper(),
    };



    String libName = L.toString( 1 );
    L.register(libName, luaFunctions);
    // ScoreflexEventManager.getInstance().dispatchEvent(Scoreflex.SCOREFLEX_WEBCALLBACK_EVENTNAME, new HashMap<String, String>());
    return 1;
  }

  // CoronaRuntimeListener
  @Override
  public void onLoaded(final CoronaRuntime runtime) {
     // ScoreflexEventManager.getInstance().addEventListener(Scoreflex.SCOREFLEX_WEBCALLBACK_EVENTNAME, new ScoreflexEventManager.ScoreflexEventListener() {
     //    @Override
     //    public void onEvent(final ScoreflexEvent event) {
     //    }
     //  });
    final com.ansca.corona.CoronaRuntimeTaskDispatcher dispatcher = new com.ansca.corona.CoronaRuntimeTaskDispatcher(runtime.getLuaState());

    fMessageReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(final Context context,final Intent intent) {
        // Get extra data included in the Intent
        com.ansca.corona.CoronaRuntimeTask task = new com.ansca.corona.CoronaRuntimeTask() {
          @Override
            public void executeUsing(com.ansca.corona.CoronaRuntime runtime) {
              String config = intent.getStringExtra(Scoreflex.INTENT_START_CHALLENGE_EXTRA_CONFIG);
              String configId = intent.getStringExtra(Scoreflex.INTENT_START_CHALLENGE_EXTRA_CONFIG_ID);
              HashMap<String, String> parameters = new HashMap<String, String>();
              parameters.put("config", config);
              parameters.put("configId", configId);
              CoronaHelperFunction.sendRuntimeEvent(runtime.getLuaState(), Scoreflex.INTENT_START_CHALLENGE , parameters);
          }
        };
        dispatcher.send(task);

        }
    };

    fConnectivityReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(final Context context,final Intent intent) {
        // Get extra data included in the Intent
        com.ansca.corona.CoronaRuntimeTask task = new com.ansca.corona.CoronaRuntimeTask() {
          @Override
            public void executeUsing(com.ansca.corona.CoronaRuntime runtime) {
          		boolean state = intent.getBooleanExtra(Scoreflex.INTENT_CONNECTIVITY_EXTRA_CONNECTIVITY, false);
              HashMap<String, String> parameters = new HashMap<String, String>();
              parameters.put("state", Boolean.toString(state));
              CoronaHelperFunction.sendRuntimeEvent(runtime.getLuaState(), Scoreflex.INTENT_CONNECTIVITY_CHANGED , parameters);
          }
        };
        dispatcher.send(task);

        }
    };

    fInitializedReceiver =  new BroadcastReceiver() {
      @Override
      public void onReceive(final Context context,final Intent intent) {
        // Get extra data included in the Intent
        com.ansca.corona.CoronaRuntimeTask task = new com.ansca.corona.CoronaRuntimeTask() {
          @Override
            public void executeUsing(com.ansca.corona.CoronaRuntime runtime) {
              HashMap<String, String> parameters = new HashMap<String, String>();
              CoronaHelperFunction.sendRuntimeEvent(runtime.getLuaState(), Scoreflex.INTENT_SCOREFLEX_INTIALIZED , parameters);
          }
        };
        dispatcher.send(task);

        }
    };

    fPlayLevelReceiver =  new BroadcastReceiver() {
      @Override
      public void onReceive(final Context context,final Intent intent) {
        // Get extra data included in the Intent
        com.ansca.corona.CoronaRuntimeTask task = new com.ansca.corona.CoronaRuntimeTask() {
          @Override
            public void executeUsing(com.ansca.corona.CoronaRuntime runtime) {
              HashMap<String, String> parameters = new HashMap<String, String>();
              parameters.put("leaderboardId", intent.getStringExtra(Scoreflex.INTENT_PLAY_LEVEL_EXTRA_LEADERBOARD_ID));
              CoronaHelperFunction.sendRuntimeEvent(runtime.getLuaState(), Scoreflex.INTENT_PLAY_LEVEL , parameters);
          }
        };
        dispatcher.send(task);

        }
    };

    LocalBroadcastManager.getInstance(CoronaApplication.getAppContext()).registerReceiver(fMessageReceiver,
      new IntentFilter(Scoreflex.INTENT_START_CHALLENGE));

    LocalBroadcastManager.getInstance(CoronaApplication.getAppContext()).registerReceiver(fConnectivityReceiver,
        new IntentFilter(Scoreflex.INTENT_CONNECTIVITY_CHANGED));

    LocalBroadcastManager.getInstance(CoronaApplication.getAppContext()).registerReceiver(fInitializedReceiver,
        new IntentFilter(Scoreflex.INTENT_SCOREFLEX_INTIALIZED));

    LocalBroadcastManager.getInstance(CoronaApplication.getAppContext()).registerReceiver(fPlayLevelReceiver,
        new IntentFilter(Scoreflex.INTENT_PLAY_LEVEL));
  }

  // CoronaRuntimeListener
  @Override
  public void onStarted(CoronaRuntime runtime) {
  }

  // CoronaRuntimeListener
  @Override
  public void onSuspended(CoronaRuntime runtime) {
  }

  // CoronaRuntimeListener
  @Override
  public void onResumed(CoronaRuntime runtime) {
  }

  // CoronaRuntimeListener
  @Override
  public void onExiting(CoronaRuntime runtime) {

  }

  // library.init( listener )
  public int init(LuaState L) {

    return 0;
  }

  private class ScoreflexApiWrapper implements NamedJavaFunction {
    @Override
    public String getName() {
      return "api";
    }
    @Override
    public int invoke(LuaState luaState) {

      final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
      final String method = luaState.checkString(1);
      final String ressource = luaState.checkString(2);
      final Scoreflex.RequestParams scoreflexParams  = new Scoreflex.RequestParams();

      try {
        // optionnal parameter
        Map params = luaState.checkJavaObject(3, Map.class);
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry)it.next();
          scoreflexParams.put(pairs.getKey().toString() , pairs.getValue().toString());
        }
      } catch (Exception e)  {
      }

      try {
        luaState.checkType(4, com.naef.jnlua.LuaType.FUNCTION);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        return 0;
      }

      luaState.pushValue(4);
      final int callbackFunctionRefId = luaState.ref(com.naef.jnlua.LuaState.REGISTRYINDEX);


      final com.ansca.corona.CoronaRuntimeTaskDispatcher dispatcher = new com.ansca.corona.CoronaRuntimeTaskDispatcher(luaState);

      mainActivity.runOnUiThread(new Runnable() {

        public void handleResponse(final Scoreflex.Response response) {
          CoronaHelperFunction.RunOnCoronaThread(dispatcher, callbackFunctionRefId, new CoronaHelperFunction.CoronaArgumentListener() {
            @Override
            public int pushArguments(com.naef.jnlua.LuaState luaState) {
              luaState.newTable();
              int luaTableStackIndex = luaState.getTop();

              if (response == null) {
                luaState.pushBoolean(true);
                luaState.setField(luaTableStackIndex, "isError");
                luaState.pushString("network error");
                luaState.setField(luaTableStackIndex, "errorMessage");

                luaState.pushInteger(-1);
                luaState.setField(luaTableStackIndex, "errorStatus");
                return 1;
              }

              luaState.pushBoolean(response.isError());
              luaState.setField(luaTableStackIndex, "isError");

              if (response.isError()) {
                luaState.pushString(response.getErrorMessage());
                luaState.setField(luaTableStackIndex, "errorMessage");

                luaState.pushInteger(response.getErrorStatus());
                luaState.setField(luaTableStackIndex, "errorStatus");
              }


              luaState.pushJavaObject(response.getJSONObject());
              luaState.setField(luaTableStackIndex, "response");

              return 1;
            }
          });
        }


        Scoreflex.ResponseHandler responseHandler = new Scoreflex.ResponseHandler() {
           @Override
              public void onFailure(Throwable e, Scoreflex.Response errorResponse) {
                handleResponse(errorResponse);
              }

              @Override
              public void onSuccess(Scoreflex.Response response) {
                handleResponse(response);
              }
        };
        @Override
        public void run() {
          String lowercaseMethod = method.toLowerCase();
          if (lowercaseMethod.equals("get")) {
            Scoreflex.get(ressource, scoreflexParams,responseHandler);
          } else if (lowercaseMethod.equals("post")) {
            Scoreflex.post(ressource, scoreflexParams,  responseHandler);
          } else if (lowercaseMethod.equals("posteventually")) {
            Scoreflex.postEventually(ressource, scoreflexParams,  responseHandler);
          } else if (lowercaseMethod.equals("put")) {
            Scoreflex.put(ressource, scoreflexParams,  responseHandler);
          } else if (lowercaseMethod.equals("delete")) {
            Scoreflex.delete(ressource, responseHandler);
          }

        }
      });


      return 0;
    }
  }

  private class ScoreflexAfterLevelWrapper implements NamedJavaFunction {
    @Override
    public String getName() {
      return "afterLevel";
    }
    /**
     * Warning! This method is not called on the main UI thread.
     */
    @Override
    public int invoke(LuaState luaState) {
      final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
      final String leaderboardId = luaState.checkString(1);
      int gravity =  Gravity.BOTTOM;
      try {
        gravity = luaState.checkInteger(2) == 1 ? Gravity.BOTTOM:Gravity.TOP;
      } catch (Exception e) {

      }
      final int scoreflexGravity = gravity;
      boolean hasScore = false;
      final Scoreflex.RequestParams scoreflexParams  = new Scoreflex.RequestParams();
      long score = -1;
      try {
        score = (long) luaState.checkNumber(3);
        scoreflexParams.put("score", ""+score);
        hasScore = true;
      } catch (Exception e) {

      }
      if (hasScore == false) {
        try {
          Map params = luaState.checkJavaObject(3, Map.class);
          Iterator it = params.entrySet().iterator();
          while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            scoreflexParams.put(pairs.getKey().toString() , pairs.getValue().toString());
          }
        } catch (Exception e) {

        }
      }
      mainActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Scoreflex.showRanksPanel(mainActivity, leaderboardId, scoreflexGravity, scoreflexParams);
          // Scoreflex.afterLevel(mainActivity, leaderboardId, scoreflexGravity, scoreflexParams);
          return;
        }

      });
      return 0;
      // return show(L);
    }
  }

  private class ScoreflexViewWrapper implements NamedJavaFunction {
    @Override
    public String getName() {
      return "view";
    }
    /**
     * Warning! This method is not called on the main UI thread.
     */
    @Override
    public int invoke(LuaState luaState) {
      final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
      final String ressource = luaState.checkString(1);
      final Scoreflex.RequestParams scoreflexParams  = new Scoreflex.RequestParams();
      try {
        Map params = luaState.checkJavaObject(2, Map.class);
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry)it.next();
          scoreflexParams.put(pairs.getKey().toString() , pairs.getValue().toString());
        }
      } catch (Exception e) {

      }

      boolean fullScreen = false;
      try {
        fullScreen = luaState.checkBoolean(3);
      } catch (Exception e) {

      }

      final boolean forceFullscreen = fullScreen;
      try {
       mainActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            View view = null;
            if (forceFullscreen) {
              view = Scoreflex.showFullScreenView(mainActivity, ressource, scoreflexParams);
            } else {
              view = Scoreflex.showPanelView(mainActivity, ressource, scoreflexParams, Scoreflex.getDefaultGravity());
            }
            view.requestFocus();
            view.requestFocusFromTouch();

            return;
          }
        });
      } catch (Exception e) {

      }
      return 0;
    }
  }

  private class ScoreflexInitWrapper implements NamedJavaFunction {
    @Override
    public String getName() {
      return "initialize";
    }
    /**
     * Warning! This method is not called on the main UI thread.
     */
    @Override
    public int invoke(final LuaState luaState) {
      // HashMap<String, String> params = new HashMap<String, String>();
      // params.put("code", "200007");
      // params.put("challengeInstanceId", "0180kv43fag12ig1");
      // params.put("data", "{\"challengeInstanceId\":\"0180kv43fag12ig1\",\"challengeConfigId\":\"bestScore\"}");

      // CoronaHelperFunction.sendRuntimeEvent(luaState, "scoreflexWebviewCallback", params);
      final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
      final String clientId = luaState.checkString(1);
      final String clientSecret = luaState.checkString(2);
      boolean isSandbox = false;
      try
      {
        isSandbox = luaState.checkBoolean(3);
      } catch (Exception ex) {

      }

      final boolean sandboxFinal = isSandbox;
      mainActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Scoreflex.initialize(mainActivity, clientId, clientSecret, sandboxFinal);
          return;
        }

      });


      return 0;
      // return show(L);
    }
  }
  private class ScoreflexRegisterPushWrapper implements NamedJavaFunction {
    @Override
    public String getName() {
      return "registerForPushNotification";
    }
    /**
     * Warning! This method is not called on the main UI thread.
     */
    @Override
    public int invoke(final LuaState luaState) {
      final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
      mainActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
        	Scoreflex.registerForPushNotification("191777458062", mainActivity);
          return;
        }

      });


      return 0;
      // return show(L);
    }
  }

  private class ScoreflexCheckNotificationWrapper implements NamedJavaFunction {
  	 @Override
     public String getName() {
       return "checkNotification";
     }
     /**
      * Warning! This method is not called on the main UI thread.
      */
     @Override
     public int invoke(final LuaState luaState) {
       final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
       mainActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
        	 ((MainActivity)mainActivity).checkNotification();
           return;
         }

       });


       return 0;
       // return show(L);
     }
  }

  private class ScoreflexPreloadWrapper implements NamedJavaFunction {
 	 @Override
   public String getName() {
     return "preload";
   }
   /**
    * Warning! This method is not called on the main UI thread.
    */
   @Override
   public int invoke(final LuaState luaState) {
  	 final String resource = luaState.checkString(1);
     final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
     mainActivity.runOnUiThread(new Runnable() {
       @Override
       public void run() {
      	 Scoreflex.preloadResource(mainActivity, resource);
         return;
       }

     });


     return 0;
     // return show(L);
   }
  }

  private class ScoreflexDisposePreloadedResourceWrapper implements NamedJavaFunction {
  	 @Override
     public String getName() {
       return "freePreloadedResource";
     }
     /**
      * Warning! This method is not called on the main UI thread.
      */
     @Override
     public int invoke(final LuaState luaState) {
    	 String resource = null;
    	 try {
    		 resource = luaState.checkString(1);
    	 } catch (Exception e){

     	 }
    	 final String finalResource = resource;
       final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
       mainActivity.runOnUiThread(new Runnable() {
         @Override
         public void run() {
        	 Scoreflex.freePreloadedResources(finalResource);
           return;
         }

       });


       return 0;
       // return show(L);
     }
  }

  private class SubmitScoreAndShowRankboxWrapper implements NamedJavaFunction {

		@Override
		public int invoke(LuaState luaState) {
			  final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
	      final String leaderboardId = luaState.checkString(1);
	      final Scoreflex.RequestParams scoreflexParams  = new Scoreflex.RequestParams();
	      try {
	        Map params = luaState.checkJavaObject(2, Map.class);
	        Iterator it = params.entrySet().iterator();
	        while (it.hasNext()) {
	          Map.Entry pairs = (Map.Entry)it.next();
	          scoreflexParams.put(pairs.getKey().toString() , pairs.getValue().toString());
	        }
	      } catch (Exception e) {

	      }
	      final int gravity = luaState.checkInteger(3) == 1 ? Gravity.TOP:Gravity.BOTTOM;

	      mainActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						long score = Long.parseLong(scoreflexParams.getParamValue("score"));
						fRankbox = Scoreflex.submitScoreAndShowRanksPanel(mainActivity, leaderboardId, score, scoreflexParams, gravity);
					}

	      });

	      return 0;
		}

		@Override
		public String getName() {
			return "submitScoreAndShowRankbox";
		}

  }
  private class  HideRankboxWrapper implements NamedJavaFunction {

		@Override
		public int invoke(LuaState luaState) {
			if (fRankbox != null) {
				final Activity mainActivity = CoronaEnvironment.getCoronaActivity();
				mainActivity.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						((ScoreflexView)fRankbox).close();
						fRankbox = null;
					}

				});

			}
			return 0;
		}

		@Override
		public String getName() {
			return "hideRankbox";
		}

  }
}


