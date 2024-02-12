package grakkit;

import org.bukkit.plugin.EventExecutor;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;

public class SyncEventExecutor implements EventExecutor {
   public SyncEventExecutor(EventExecutor executor, SyncCallHelper helper){
      this.executor = executor;
      this.helper = helper;
   }
   private EventExecutor executor;
   private SyncCallHelper helper;
   public void execute(Listener listener, Event event){
      helper.call(() -> {
         executor.execute(listener, event);
      });
   }
}
