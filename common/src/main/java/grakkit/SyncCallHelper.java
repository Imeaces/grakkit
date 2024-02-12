package grakkit;

import java.util.function.*;

public class SyncCallHelper {
   protected static Thread mainThread = null;
   public static boolean currentThreadIsMainThread(){
      return Thread.currentThread().equals(mainThread);
   }
   public BiConsumer<Object, Object> createSyncBiConsumer(BiConsumer<Object, Object> fn){
      return (a, b) -> {
         call(() -> {
            fn.accept(a, b);
         });
      };
   }
   public Consumer<Object> createSyncConsumer(Consumer<Object> fn){
      return (value) -> {
         call(() -> {
            fn.accept(value);
         });
      };
   }
   public Runnable createSyncRunnable(Runnable fn){
      return () -> {
         call(fn);
      };
   }
   protected synchronized void call(Runnable fn){
      fn.run();
   }
}