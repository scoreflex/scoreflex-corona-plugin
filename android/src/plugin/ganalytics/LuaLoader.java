package plugin.ganalytics;

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
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.Tracker;

public class LuaLoader implements JavaFunction, CoronaRuntimeListener {
  private CoronaActivity fParentActivity;

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
  public int invoke(LuaState L) {

    GAServiceManager.getInstance().setDispatchPeriod(30);
    // Add functions to library
    NamedJavaFunction[] luaFunctions = new NamedJavaFunction[] {
      new AnalyticsSendViewWrapper(),
      new AnalyticsSendEventWrapper()
    };

    

    String libName = L.toString( 1 );
    L.register(libName, luaFunctions);

    return 1;
  }

  // CoronaRuntimeListener
  @Override
  public void onLoaded(CoronaRuntime runtime) {

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
  
  private class AnalyticsSendViewWrapper implements NamedJavaFunction {

    @Override
    public String getName() {
      return "sendView";
    }
    @Override
    public int invoke(LuaState luaState) {
      String trackingId = luaState.checkString(1);
      String viewName = luaState.checkString(2);

      GoogleAnalytics gaInstance = GoogleAnalytics.getInstance(LuaLoader.this.fParentActivity);
      gaInstance.setDebug(true);
      Tracker gaTracker = gaInstance.getTracker(trackingId);

      gaTracker.sendView(viewName);
      return 0;
    }
  }
  private class AnalyticsSendEventWrapper implements NamedJavaFunction {

    @Override
    public String getName() {
      return "sendEvent";
    }
    @Override
    public int invoke(LuaState luaState) {
      String trackingId = luaState.checkString(1);
      String category = luaState.checkString(2);
      String action = luaState.checkString(3);
      String label = luaState.checkString(4);
      Long value = new Long((long)luaState.checkNumber(5));

      GoogleAnalytics gaInstance = GoogleAnalytics.getInstance(LuaLoader.this.fParentActivity);
      gaInstance.setDebug(true);
      Tracker gaTracker = gaInstance.getTracker(trackingId);
      gaTracker.trackEvent(
            category,
            action,
            label,
            value);
      return 0;
    }
  }
}