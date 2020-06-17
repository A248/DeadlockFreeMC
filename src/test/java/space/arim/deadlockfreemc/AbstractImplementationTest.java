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

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AbstractImplementationTest {

	private ScheduledExecutorService scheduler;
	private DeadlockFree df;
	
	@BeforeEach
	public void setup() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		df = new TestImpl(scheduler);
	}
	
	private CompletableFuture<?> getDeadlockableFuture(Consumer<CompletableFuture<?>> inProgressFutureAcceptor) {
		assertFalse(df.isPrimaryThread());
		return CompletableFuture.runAsync(() -> {
			// on main thread
			assertTrue(df.isPrimaryThread());

			inProgressFutureAcceptor.accept(CompletableFuture.runAsync(() -> {
				assertFalse(df.isPrimaryThread());
				try {
					TimeUnit.MILLISECONDS.sleep(50L);
				} catch (InterruptedException ex) {
					fail(ex);
				}
			}));
		}, scheduler);
	}
	
	@Test
	public void testDeadlockWithoutDeadlockFreeMC() {
		/*
		 * A proof of concept why deadlocks happen WITHOUT DeadlockFreeMC
		 */
		getDeadlockableFuture((inProgressFuture) -> {
			try {
				inProgressFuture = inProgressFuture.thenRunAsync(() -> {
					// do something on main thread
					assertTrue(df.isPrimaryThread());

				}, scheduler);
				// We can use a longer timeout, the deadlock still happens
				inProgressFuture.get(5L, TimeUnit.SECONDS);
				fail("There is no deadlock!");
			} catch (InterruptedException | ExecutionException ex) {
				fail(ex);

			} catch (TimeoutException expected) {
				
			}
		}).join();
	}
	
	@Test
	public void testNoDeadlockWithDeadlockFreeMC() {		
		/*
		 * Ensure no deadlocks happen when we do everything properly
		 */
		try {
			getDeadlockableFuture((inProgressFuture) -> {
				inProgressFuture = inProgressFuture.thenRunAsync(() -> {
					assertTrue(df.isPrimaryThread());
				}, df);
				df.join(inProgressFuture);
			}).get(5L, TimeUnit.SECONDS);
			// If not timed out, we succeeded
		} catch (InterruptedException | ExecutionException | TimeoutException ex) {
			fail(ex);
		}
	}
	
	@AfterEach
	public void tearDown() {
		scheduler.shutdown();
		try {
			assertTrue(scheduler.awaitTermination(10L, TimeUnit.SECONDS), "Testing scheduler must not timeout on termination");
		} catch (InterruptedException ex) {
			fail(ex);
		}
	}
	
}
