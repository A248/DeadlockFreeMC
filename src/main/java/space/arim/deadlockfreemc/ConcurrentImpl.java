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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A blank implementation for servers which don't have a main thread.
 * Useful on BungeeCord / Velocity to fulfill the interface implementation.
 * 
 * @author A248
 *
 */
class ConcurrentImpl implements DeadlockFree {

	@Override
	public void execute(Runnable command) {
		// What else is there to do?
		command.run();
	}

	@Override
	public boolean isPrimaryThread() {
		return false;
	}

	@Override
	public <T> T join(CompletableFuture<T> future) {
		return future.join();
	}

	@Override
	public <T> T get(Future<T> future) throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public <T> T get(Future<T> future, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

}
