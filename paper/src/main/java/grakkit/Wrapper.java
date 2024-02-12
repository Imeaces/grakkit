package grakkit;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import org.graalvm.polyglot.Value;

public class Wrapper extends Command implements PluginIdentifiableCommand {

   public Plugin getPlugin(){
      return Main.pluginInstance;
   }

   /** The executor to use for this command. */
   public Value executor;

   /** The tab-completer to use for this command. */
   public Value tabCompleter;

   public SyncCallHelper syncCallHelper = null;

   /** Creates a custom command with the given options. */
   public Wrapper (String name, String[] aliases) {
      super(name, "", "", Arrays.asList(aliases));
   }

   @Override
   public boolean execute (CommandSender sender, String label, String[] args) {
      try {
         if (syncCallHelper != null){
            syncCallHelper.call(() -> {
               this.executor.executeVoid(sender, label, args);
            });
         } else {
            this.executor.executeVoid(sender, label, args);
         }
      } catch (Throwable error) {
         // do nothing
      }
      return true;
   }

   /** Sets this wrapper's command options. */
   public void options (String permission, String message, Value executor, Value tabCompleter) {
      this.executor = executor;
      this.tabCompleter = tabCompleter;
      this.setPermission(permission);
      this.setPermissionMessage(message);
   }

   /**
    * this variable aiming to receive return value from
    * sync tab complete inside the lambda Runnable
    *
    * you known, Java isn't JavaScript
    */
   private Value tabCompleteInput;

   @Override
   public ArrayList<String> tabComplete (CommandSender sender, String alias, String[] args) {
      ArrayList<String> output = new ArrayList<>();
      try {
         tabCompleteInput = null;
         if (syncCallHelper != null){
            syncCallHelper.call(() -> {
               tabCompleteInput = this.tabCompleter.execute(sender, alias, args);
            });
         } else {
            tabCompleteInput = this.tabCompleter.execute(sender, alias, args);
         }
         Value input = tabCompleteInput;
         for (long index = 0; index < input.getArraySize(); index++) output.add(input.getArrayElement(index).toString());
      } catch (Throwable error) {
         // do nothing
      }
      return output;
   }
}
