package plugin.facebook;

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
import com.scoreflex.Scoreflex;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import com.chartboost.sdk.*;
import android.widget.FrameLayout;
import com.google.ads.*;
import com.mycompany.app.*;
import com.facebook.*;
import android.content.Intent;
import com.facebook.widget.*;

public class LuaLoader implements JavaFunction, CoronaRuntimeListener {
//  private CoronaActivity fParentActivity;
  // This corresponds to the event name, e.g. [Lua] event.name
  // private static final String EVENT_NAME = "pluginlibraryevent";

  /**
   * Creates a new object for displaying banner ads on the CoronaActivity
   */
  public LuaLoader() {

  }

  /**
   * Warning! This method is not called on the main UI thread.
   */
  @Override
  public int invoke(LuaState L) {
    // Add functions to library
    NamedJavaFunction[] luaFunctions = new NamedJavaFunction[] {
      new FacebookShareWrapper(),
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
  private class FacebookShareWrapper implements NamedJavaFunction {

    @Override
    public String getName() {
      return "share";
    }

    @Override
    public int invoke(LuaState luaState) {
      MainActivity activity = (MainActivity)CoronaEnvironment.getCoronaActivity();
      String url = luaState.checkString(1);
      String name = luaState.checkString(2);
      String caption = luaState.checkString(3);
      String description = luaState.checkString(4);

   	 	final Intent intent=new Intent(android.content.Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      intent.putExtra(Intent.EXTRA_SUBJECT, caption);
      intent.putExtra(Intent.EXTRA_TEXT, description + " :" + url);
      com.ansca.corona.CoronaEnvironment.getCoronaActivity().runOnUiThread(new Runnable() {

        @Override
        public void run() {
          com.ansca.corona.CoronaEnvironment.getCoronaActivity().startActivity(Intent.createChooser(intent, "How do you want to share?"));
        }
      });
      return 0;
    }
  }
}