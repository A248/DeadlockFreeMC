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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

abstract class AbstractImplementation implements DeadlockFree {

	private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	private volatile Thread mainThread;
	
	@Override
	public void execute(Runnable command) {
		if (isPrimaryThread()) {
			command.run();
		} else {
			tasks.offer(command);
			LockSupport.unpark(mainThread);
		}
	}

	@Override
	public <T> T join(CompletableFuture<T> future) {
		if (isPrimaryThread()) {
			if (mainThread == null) {
				mainThread = Thread.currentThread();
			}
			unleash();
			while (!future.isDone()) {
				LockSupport.park();
				unleash();
			}
		}
		return future.join();
	}
	
	@Override
	public <T> T get(Future<T> future) throws InterruptedException, ExecutionException {
		if (isPrimaryThread()) {
			if (mainThread == null) {
				mainThread = Thread.currentThread();
			}
			unleash();
			while (!future.isDone()) {
				LockSupport.park();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				unleash();
			}
		}
		return future.get();
	}
	
	@Override
	public <T> T get(Future<T> future, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		if (isPrimaryThread()) {
			long startTime = System.nanoTime();
			long nanosTimeout = TimeUnit.NANOSECONDS.convert(timeout, unit);
			if (mainThread == null) {
				mainThread = Thread.currentThread();
			}
			unleash();
			while (!future.isDone()) {
				LockSupport.park();
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				if (System.nanoTime() - startTime > nanosTimeout) {
					throw new TimeoutException();
				}
				unleash();
			}
			return future.get();

		} else {
			return future.get(timeout, unit);
		}
	}
	
	/**
	 * Runs all scheduled tasks. Should only be called on main thread.
	 * 
	 */
	void unleash() {
		Runnable toRun;
		while ((toRun = tasks.poll()) != null) {
			toRun.run();
		}
	}

}
