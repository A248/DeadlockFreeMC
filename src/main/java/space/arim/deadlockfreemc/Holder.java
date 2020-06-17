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

public class Holder {

	static final DeadlockFree INST;
	
	static {
		DeadlockFree inst = null;
		try {
			Class.forName("org.bukkit.Bukkit");
			inst = new BukkitImpl(org.bukkit.Bukkit.getPluginManager().getPlugins()[0]);
		} catch (ClassNotFoundException ex) {

		}
		if (inst == null) {
			try {
				Class.forName("org.spongepowered.api.Sponge");
				inst = new SpongeImpl(org.spongepowered.api.Sponge.getPluginManager().getPlugins().iterator().next());
			} catch (ClassNotFoundException ex) {

			}
		}
		if (inst == null) {
			throw new ExceptionInInitializerError();
		}
		INST = inst;
	}
	
}
