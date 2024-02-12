package grakkit;

import java.util.function.*;

public class SyncCallUtil {
   protected static Thread mainThread = null;
   public static boolean currentThreadIsMainThread(){
      return Thread.currentThread().equals(mainThread);
   }
   public BiConsumer<Object, Object> createSyncBiConsumer(BiConsumer<Object, Object> fn){
      return (a, b) -> {
         call(() -> {
            fn(a, b);
         });
      };
   }
   public Consumer<Object> createSyncConsumer(Consumer<Object> fn){
      return (value) -> {
         call(() -> {
            fn(value);
         });
      };
   }
   public Runnable createSyncRunnable(Runnable fn){
      return () -> {
         call(fn);
      }
   }
   private static synchronized call(Runnable){
      fn.run();
   }
}