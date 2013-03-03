/*    
Craftbook - CBX Scheduler.java
Copyright (C) 2013 Stefan Steinheimer <nosefish@gmx.net>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles threading in CraftBook-Extra.
 * 
 * Make sure to handle <i>all</i> exceptions inside your Runnables, or you'll crash
 * the server. Also check for RejectedExecutionException when submitting tasks.
 * 
 * If you access anything in the world in a runnable submitted here,
 * make sure that the chunk is actually still loaded, even when using the server queue!
 * 
 * 
 * @author Stefan Steinheimer (nosefish)
 * 
 */
public class CBXScheduler implements ScheduledExecutorService {
	// the CBX thread pool where most of the asynchronous work happens
	private final ExecutorService pool;
	
	// These threads just play the waiting game and submit their tasks to the
	// main pool when the time comes
	private final ScheduledExecutorService waitPool;

	public CBXScheduler() {
		final int hardwareThreads = Runtime.getRuntime().availableProcessors();
		int poolThreadCount = hardwareThreads / 2;
		if (poolThreadCount < 2) {
			poolThreadCount = 2;
		} else if (poolThreadCount > 4) {
			poolThreadCount = 4;
		}
		pool = Executors.newFixedThreadPool(poolThreadCount);
		waitPool = Executors.newScheduledThreadPool(2);
	}

	/**
	 * Runs the Runnable in the main server thread. Use this if you modify the
	 * world in your code. Running processor intensive tasks here will cause
	 * server lag.
	 * 
	 * @param command
	 *            the Runnable to run
	 */
	public void executeInServer(Runnable command) {
		etc.getServer().addToServerQueue(command);
	}

	/**
	 * "Stolen" from 14mRh4x0R's version of WorldEdit.
	 * 
	 * @param task
	 * @param delay in milliseconds
	 * @param period in milliseconds
	 * 
	 * @return
	 */
	public int scheduleInServer(Runnable task, long delay, long period) {
		etc.getServer().addToServerQueue(wrapPeriodicServerTask(task, period), delay);
		return 0;
	}

	/**
	 * "Stolen" from 14mRh4x0R's version of WorldEdit.
	 * 
	 * @param task
	 * @param period in milliseconds
	 * @return
	 */
	private Runnable wrapPeriodicServerTask(final Runnable task,
			final long period) {
		return new Runnable() {
			@Override
			public void run() {
				if (period > 0) {
					etc.getServer().addToServerQueue(
							wrapPeriodicServerTask(task, period), period);
				}
				task.run();
				if (period < 0) {
					etc.getServer().addToServerQueue(
							wrapPeriodicServerTask(task, period), -period);
				}
			}
		};
	}

	// -----------------------------------
	// ----- ExecutorService methods -----
	// -----------------------------------
	/**
	 * Runs the Runnable in a thread of the CBX thread pool. Do not modify
	 * the world in any way here, or your code will cause
	 * ConcurrentModificationExceptions and crash the server!
	 * 
	 * Don't forget to handle exceptions in your Runnables!
	 * 
	 * @param command
	 *            the Runnable to run
	 */
	@Override
	public void execute(Runnable command) {
			pool.execute(command);
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		boolean waitPoolDone = waitPool.awaitTermination(timeout, unit);
		boolean mainPoolDone = pool.awaitTermination(timeout, unit);
		return waitPoolDone && mainPoolDone;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return pool.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return pool.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return pool.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return pool.invokeAny(tasks, timeout, unit);
	}

	@Override
	public boolean isShutdown() {
		return pool.isShutdown() && waitPool.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return pool.isTerminated() && waitPool.isTerminated();
	}

	@Override
	public void shutdown() {
		waitPool.shutdown();
		pool.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		List <Runnable> outList=new ArrayList<Runnable>();
		outList.addAll(waitPool.shutdownNow());
		outList.addAll(pool.shutdownNow());
		return outList;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return pool.submit(task);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return pool.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return pool.submit(task, result);
	}

	// --------------------------------------------
	// ----- ScheduledExecutorService methods -----
	// --------------------------------------------
	@Override
	public ScheduledFuture<?> schedule(Runnable arg0, long arg1, TimeUnit arg2) {
		return waitPool.schedule(new WrappedCBXRunnable(arg0), arg1, arg2);
	}

	// TODO: test
	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> arg0, long arg1,
			TimeUnit arg2) {
		return waitPool.schedule(new WrappedCBXCallable<V>(arg0), arg1, arg2);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable arg0, long arg1,
			long arg2, TimeUnit arg3) {
		return waitPool.scheduleAtFixedRate(new WrappedCBXRunnable(arg0), arg1,
				arg2, arg3);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable arg0, long arg1,
			long arg2, TimeUnit arg3) {
		return waitPool.scheduleWithFixedDelay(new WrappedCBXRunnable(arg0),
				arg1, arg2, arg3);
	}

	// ---------------------------
	// ----- Private classes -----
	// ---------------------------

	// Wraps a Runnable to be executed in the CBX thread pool
	private class WrappedCBXRunnable implements Runnable {
		private Runnable r;

		public WrappedCBXRunnable(Runnable r) {
			this.r = r;
		}

		public void run() {
			execute(r);
		}
	}
	
	// Wraps a Callable to be executed in the CBX thread pool
	// TODO: test
	private class WrappedCBXCallable<V> implements Callable<V> {
		Callable<V> callMe;
		
		public WrappedCBXCallable(Callable<V> arg0){
			this.callMe=arg0;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public V call() throws Exception {
			return (V) submit(callMe);
		}
		
	}

	// Wraps a runnable to be executed in the server thread
	@SuppressWarnings("unused")
	private class WrappedServerRunable implements Runnable {
		private Runnable r;

		public WrappedServerRunable(Runnable r) {
			this.r = r;
		}

		public void run() {
			executeInServer(r);
		}
	}
}
