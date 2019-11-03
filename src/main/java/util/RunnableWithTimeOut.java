/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 *  This code was taken from this page: http://stackoverflow.com/questions/5715235/java-set-timeout-on-a-certain-block-of-code
 *  From an answer from Neeme Praks
 * 
 */
public class RunnableWithTimeOut {

  public static void runWithTimeout(final Runnable runnable, long timeout, TimeUnit timeUnit) throws Exception {
    runWithTimeout(new Callable<Object>() {
      public Object call() throws Exception {
        runnable.run();
        return null;
      }
    }, timeout, timeUnit);
  }

  public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) throws Exception {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Future<T> future = executor.submit(callable);
    executor.shutdown(); // This does not cancel the already-scheduled task.
    try {
      return future.get(timeout, timeUnit);
    }
    catch (TimeoutException e) {
      //remove this if you do not want to cancel the job in progress
      //or set the argument to 'false' if you do not want to interrupt the thread
      future.cancel(true);
      throw e;
    }
    catch (ExecutionException e) {
      //unwrap the root cause
      Throwable t = e.getCause();
      if (t instanceof Error) {
        throw (Error) t;
      } else if (t instanceof Exception) {
        throw (Exception) e;
      } else {
        throw new IllegalStateException(t);
      }
    }
  }
  }