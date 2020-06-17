/* 
 * DeadlockFreeMC
 * Copyright © 2020 Anand Beh <https://www.arim.space>
 * 
 * DeadlockFreeMC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DeadlockFreeMC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DeadlockFreeMC. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU General Public License.
 */
package space.arim.deadlockfreemc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides the ability to await completion of futures using synchronous executors on the main thread
 * <i>without</i> dead-locking the main thread. <br>
 * Before this, the following would cause a deadlock if {@code join} is called from the main thread.: <br>
 * <br>
 * <pre>
 * {@code
 * Executor synchronousExecutor = (cmd) -> Bukkit.getScheduler().runTask(plugin, cmd);
 * CompletableFuture<?> future = CompletableFuture.runAsync(() -> doSomethingOnMainThread(), synchronousExecutor);
 * future.join();
 * }
 * </pre>
 * The main thread will wait on {@code join}, but the future will never complete,
 * because, in order to complete, the main thread must also run the scheduler tasks. <br>
 * <br>
 * <b>Solution</b> <br>
 * Instead, use this interface, first getting an instance via {@link #getInstance()}. Create a {@code Future}/{@code CompletableFuture}
 * as usual. The future must use this {@code Executor} if running synchronously and NOT the platform-specific scheduler.
 * Then, use {@link #join(CompletableFuture)} instead of <code>future.join()</code>. <br>
 * <br>
 * The example code thus becomes: <br>
 * <pre>
 * {@code
 * DeadlockFree deadlockFreeExecutor = DeadlockFree.get();
 * CompletableFuture<?> future = CompletableFuture.runAsync(() -> doSomethingOnMainThread(), deadlockFreeExecutor);
 * deadlockFreeExecutor.join(future);
 * }
 * </pre>
 * 
 * @author A248
 *
 */
public interface DeadlockFree extends Executor {

	/**
	 * Gets the instance. The environment (Bukkit/Spigot/Paper or Sponge)
	 * is automatically detected.
	 * 
	 * @return the instance
	 */
	static DeadlockFree getInstance() {
		return Holder.INST;
	}
	
	/**
	 * Executes a Runnable on the main thread. <br>
	 * <br>
	 * If the caller is in the main thread, the command is immediately executed. <br>
	 * Otherwise, the command is scheduled using the platform-specific scheduler,
	 * i.e. {@code BukkitScheduler} or {@code SpongeExecutorService}. <br>
	 * <br>
	 * Note that there is no specific requirement when, in the server tick loop,
	 * the command must run. The only stipulation is that the command execute
	 * not more than 1 tick (50 ms) since the call to this method.
	 * 
	 */
	@Override
	void execute(Runnable command);
	
	/**
	 * Whether current thread is the main server thread. <br>
	 * This is used internally and is also provided for convenience.
	 * 
	 * @return true if on the main thread, false otherwise
	 */
	boolean isPrimaryThread();
	
	/**
	 * Safely awaits completion of a {@link CompletableFuture}. Identical in function to
	 * <code>future.join</code> except that no deadlock will occur if DeadlockFree
	 * is used as the executor.
	 * 
	 * @param <T> the type of the future's result
	 * @param future the future to await completion of
	 * @return the same result as <code>future.join</code> would return
	 */
	<T> T join(CompletableFuture<T> future);
	
	/**
	 * Safely awaits completion of a {@link Future}. Identical in function to
	 * <code>future.get()</code> except that no deadlock will occur if DeadlockFree
	 * is used as the executor.
	 * 
	 * @param <T> the type of the future's result
	 * @param future the future to await completion of
	 * @return the same result as <code>future.get()</code> would return
	 * @throws CancellationException if the future's computation was cancelled
	 * @throws InterruptedException if interrupted while waiting
	 * @throws ExecutionException if the future's computation threw an exception
	 */
	<T> T get(Future<T> future) throws InterruptedException, ExecutionException;
	
	/**
	 * Safely awaits completion of a {@link Future}. Identical in function to
	 * <code>future.get(timeout, unit)</code> except that no deadlock will occur if DeadlockFree
	 * is used as the executor.
	 * 
	 * @param <T> the type of the future's result
	 * @param future the future to await completion of
	 * @param timeout per <code>future.get(long, TimeUnit)</code>
	 * @return the same result as <code>future.get(timeout, unit)</code> would return
	 * @throws CancellationException if the future's computation was cancelled
	 * @throws InterruptedException if interrupted while waiting
	 * @throws ExecutionException if the future's computation threw an exception
	 * @throws TimeoutException if the timeout elapsed while waiting
	 */
	<T> T get(Future<T> future, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException;
	
}
