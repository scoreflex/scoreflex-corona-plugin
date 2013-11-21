package plugin.adutils;

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

public class LuaLoader implements JavaFunction, CoronaRuntimeListener {
  private CoronaActivity fParentActivity;
  private Chartboost fChartboost;
  private AdView fAdMobView;

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
    // Add functions to library
    NamedJavaFunction[] luaFunctions = new NamedJavaFunction[] {
      new ChartboostInitWrapper(),
      new ShowChartboostInterstitial(), 
      new AdMobShowAdWrapper(), 
      new AdMobHideAdWrapper()
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
  private class ChartboostInitWrapper implements NamedJavaFunction {
   
    @Override
    public String getName() {
      return "startChartboostSession";
    }
    @Override
    public int invoke(LuaState luaState) {
      final String appId = luaState.checkString(1);
      final String appSignature = luaState.checkString(2);
      String cacheInterstitial = null;
      try
      {
        cacheInterstitial = luaState.checkString(3);
      } catch (Exception ex) {

      }
      final String cacheIntersticialFinal = cacheInterstitial;

      LuaLoader.this.fParentActivity.runOnUiThread(new Runnable() {

        @Override
        public void run() {
          LuaLoader.this.fChartboost = Chartboost.sharedChartboost();
          Chartboost chartboost = LuaLoader.this.fChartboost;

          chartboost.onCreate(LuaLoader.this.fParentActivity, appId, appSignature, null);
          chartboost.startSession();
          chartboost.onStart(LuaLoader.this.fParentActivity);
          if (cacheIntersticialFinal != null) {
            chartboost.cacheInterstitial(cacheIntersticialFinal);
          }
        }
      });
      return 0;
    }
  }  

  private class ShowChartboostInterstitial implements NamedJavaFunction {
    
    @Override
    public String getName() {
      return "showChartboostInterstitial";
    }
    @Override
    public int invoke(LuaState luaState) {
      final String interstitialName = luaState.checkString(1) ;
      LuaLoader.this.fParentActivity.runOnUiThread(new Runnable() {

        @Override
        public void run() {
          if (LuaLoader.this.fChartboost.hasCachedInterstitial(interstitialName)) {
            LuaLoader.this.fChartboost.showInterstitial(interstitialName);
          } 
        }
      });
      return 0;
    }
  }

  private class AdMobShowAdWrapper implements NamedJavaFunction {
    
    private final int AD_PLACEMENT_TOP = 1; 
    private final int AD_PLACEMENT_BOTTOM = 2; 


    @Override
    public String getName() {
      return "showAdMobAd";
    }
    @Override
    public int invoke(LuaState luaState) {
      final String campagnId = luaState.checkString(1);
      final int position = (int)luaState.checkNumber(2);
      LuaLoader.this.fParentActivity.runOnUiThread(new Runnable() {

        @Override
        public void run() {
          LuaLoader.this.fAdMobView = new AdView(LuaLoader.this.fParentActivity, AdSize.BANNER, campagnId);
          FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, android.view.Gravity.CENTER_HORIZONTAL);
          if (position == AD_PLACEMENT_BOTTOM) 
          {
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, android.view.Gravity.BOTTOM|android.view.Gravity.CENTER_HORIZONTAL);
          }
          LuaLoader.this.fAdMobView.setLayoutParams(params);      
          LuaLoader.this.fParentActivity.getOverlayView().addView(LuaLoader.this.fAdMobView);
          LuaLoader.this.fAdMobView.loadAd(new AdRequest());
        }
      });
      return 0;
    }
  }
  private class AdMobHideAdWrapper implements NamedJavaFunction {

    @Override
    public String getName() {
      return "hideAdMobAd";
    }
    @Override
    public int invoke(LuaState luaState) {
      LuaLoader.this.fParentActivity.runOnUiThread(new Runnable() {

        @Override
        public void run() {
          if (LuaLoader.this.fAdMobView != null) { 
            LuaLoader.this.fParentActivity.getOverlayView().removeView(LuaLoader.this.fAdMobView);
            LuaLoader.this.fAdMobView.destroy();
            LuaLoader.this.fAdMobView = null;
          }
        }
      });
      return 0;
    }
  }


}