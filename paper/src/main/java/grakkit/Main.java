package grakkit;

import java.lang.reflect.Field;
import java.sql.DriverManager;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.bukkit.command.CommandMap;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import org.graalvm.polyglot.Value;

public class Main extends JavaPlugin {

   protected static Plugin pluginInstance = null;

   /** A list of all registered commands. */
   public static HashMap<String, Wrapper> commands = new HashMap<>();

   /** The internal command map used to register commands. */
   public static CommandMap registry;

   /** Internal consumer for onDisable */
   private static final List<Consumer<Void>> onDisableCallbacks = new LinkedList<>(); 

   @Override
   public void onLoad() {
      // Black magic. This fixes a bug, as something is breaking SQL Integration for other plugins. 
      // See https://github.com/grakkit/grakkit/issues/14.
      DriverManager.getDrivers();
      Grakkit.patch(new Loader(this.getClassLoader())); // CORE - patch class loader with GraalJS
      try {
         Field internal = this.getServer().getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         registry = (CommandMap) internal.get(this.getServer());
      } catch (Throwable error) {
         error.printStackTrace();
      }
      
      pluginInstance = this;

      SyncCallHelper.mainThread = Thread.currentThread();
   }

   @Override
   public void onEnable() {
      try {
         this.getServer().getScheduler().runTaskTimer(this, Grakkit::tick, 0, 1); // CORE - run task loop
      } catch (Throwable error) {
         // none
      }
      Grakkit.init(this.getDataFolder().getPath()); // CORE - initialize
   }

   @Override
   public void onDisable() {
      if (onDisableCallbacks.size() > 0) {
         for (Consumer<Void> fn : onDisableCallbacks){
            try {
               fn.accept(null);
            } catch (Throwable e){
               e.printStackTrace();
            }
         }
         onDisableCallbacks.clear();
      }

      Grakkit.close(); // CORE - close before exit
      commands.values().forEach(command -> {
         command.executor = Value.asValue((Runnable) () -> {});
         command.tabCompleter = Value.asValue((Runnable) () -> {});
      });
      commands.clear();
      pluginInstance = null;
   }

   /** Registers a custom command to the server with the given options. */
   public void register (String namespace, String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      String key = namespace + ":" + name;
      Wrapper command;
      if (commands.containsKey(key)) {
         command = commands.get(key);
      } else {
         command = new Wrapper(name, aliases);
         registry.register(namespace, command);
         commands.put(key, command);
      }
      command.options(permission, message, executor, tabCompleter);
   }

   /**
    * Allow developers to pass in a callback to the `onDisable` function.
    * @param fn
    */
   public void registerOnDisable(Consumer<Void> fn) {
      onDisableCallbacks.add(fn);
   }
}
