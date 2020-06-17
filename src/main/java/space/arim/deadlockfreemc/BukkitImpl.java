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

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Implementation on Bukkit, Spigot, or Paper. If Paper is detected,
 * uses the ServerTickStartEvent. Else uses a repeating task with a 1-tick period.
 * 
 * @author A248
 *
 */
class BukkitImpl extends AbstractImplementation {
	
	BukkitImpl(Plugin pluginToUse) {
		try {
			Class.forName("com.destroystokyo.paper.event.server.ServerTickStartEvent");
			Bukkit.getPluginManager().registerEvents(new Listener() {
				@SuppressWarnings("unused")
				@EventHandler
				public void doTick(com.destroystokyo.paper.event.server.ServerTickStartEvent evt) {
					unleash();
				}
			}, pluginToUse);

		} catch (ClassNotFoundException tickStartEventUnsupported) {
			Bukkit.getScheduler().runTaskTimer(pluginToUse, this::unleash, 0L, 1L);
		}
	}
	
	@Override
	public boolean isPrimaryThread() {
		return Bukkit.isPrimaryThread();
	}

}
