package plugin.scoreflex;

import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import java.util.Map;
import com.naef.jnlua.LuaState;
import android.util.Log;
import plugin.scoreflex.*;

public class CoronaHelperFunction {

  public static void sendRuntimeEvent(LuaState luaState,String eventName, Map<String, String> parameters)
  {

    // com.ansca.corona.CoronaRuntimeTaskDispatcher dispatcher = new com.ansca.corona.CoronaRuntimeTaskDispatcher(luaState);
    // com.ansca.corona.CoronaRuntimeTask task = new com.ansca.corona.CoronaRuntimeTask() {
    //   @Override
    //   public void executeUsing(com.ansca.corona.CoronaRuntime runtime) {
    //     com.naef.jnlua.LuaState luastate = runtime.getLuaState();
        luaState.getGlobal("Runtime");
        luaState.getField(-1,"dispatchEvent");
        luaState.pushValue(-2);
        luaState.newTable();
        int idx = luaState.getTop();
        luaState.pushString(eventName);
        luaState.setField(-2,"name");
        if (parameters != null) {
          for (Map.Entry<String, String> entry : parameters.entrySet())
          {
            luaState.pushString(entry.getValue());
            luaState.setField(idx, entry.getKey());
          }
        }
        luaState.call(2, 0);
    //   }
    // };
    // dispatcher.send(task);
    luaState.pop(1);
    return;
  }

  public static void RunOnCoronaThread(CoronaRuntimeTaskDispatcher dispatcher, final int refId, final CoronaArgumentListener listener) {
    com.ansca.corona.CoronaRuntimeTask task = new com.ansca.corona.CoronaRuntimeTask() {
      @Override
      public void executeUsing(com.ansca.corona.CoronaRuntime runtime) {
        // *** We are now running on the Corona runtime thread. ***
        try {
          // Fetch the Corona runtime's Lua state.
          com.naef.jnlua.LuaState luaState = runtime.getLuaState();

          // Fetch the Lua function stored in the registry and push it to the top of the stack.
          luaState.rawGet(com.naef.jnlua.LuaState.REGISTRYINDEX, refId);

          // Remove the Lua function from the registry.
          luaState.unref(com.naef.jnlua.LuaState.REGISTRYINDEX, refId);

          // Call the Lua function that was just pushed to the top of the stack.
          // The 1st argument indicates the number of arguments that we are passing to the Lua function.
          // The 2nd argument indicates the number of return values to accept from the Lua function.
          // In this case, we are calling this Lua function without arguments and accepting no return values.
          // Note: If you want to call the Lua function with arguments, then you need to push each argument
          //       value to the luaState object's stack.
          int nbArgument = listener.pushArguments(luaState);
          luaState.call(nbArgument, 0);
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    };
    dispatcher.send(task);
  }

  public interface CoronaArgumentListener {
    public int pushArguments(com.naef.jnlua.LuaState luaState);
  }
}