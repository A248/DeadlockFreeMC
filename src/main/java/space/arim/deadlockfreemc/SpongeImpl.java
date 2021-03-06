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

import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * Implementation on Sponge. Uses a 
 * 
 * @author A248
 *
 */
class SpongeImpl extends AbstractImplementation {
	
	SpongeImpl(PluginContainer plugin) {
		Sponge.getScheduler().createSyncExecutor(plugin.getInstance().get()).scheduleAtFixedRate(this::unleash, 0L, 50L,
				TimeUnit.MILLISECONDS);
	}
	
	@Override
	public boolean isPrimaryThread() {
		return Sponge.getServer().isMainThread();
	}

}
