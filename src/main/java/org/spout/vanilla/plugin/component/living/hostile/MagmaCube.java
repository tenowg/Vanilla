/*
 * This file is part of Vanilla.
 *
 * Copyright (c) 2011-2012, Spout LLC <http://www.spout.org/>
 * Vanilla is licensed under the Spout License Version 1.
 *
 * Vanilla is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Vanilla is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.vanilla.plugin.component.living.hostile;

import org.spout.api.inventory.ItemStack;

import org.spout.vanilla.api.component.Hostile;

import org.spout.vanilla.plugin.VanillaPlugin;
import org.spout.vanilla.plugin.component.living.Living;
import org.spout.vanilla.plugin.component.misc.DropComponent;
import org.spout.vanilla.plugin.component.misc.HealthComponent;
import org.spout.vanilla.plugin.data.VanillaData;
import org.spout.vanilla.plugin.material.VanillaMaterials;
import org.spout.vanilla.plugin.protocol.entity.creature.MagmaCubeEntityProtocol;

/**
 * A component that identifies the entity as a MagmaCube.
 */
public class MagmaCube extends Living implements Hostile {
	@Override
	public void onAttached() {
		super.onAttached();
		getOwner().getNetwork().setEntityProtocol(VanillaPlugin.VANILLA_PROTOCOL_ID, new MagmaCubeEntityProtocol());
		getOwner().add(DropComponent.class).addDrop(new ItemStack(VanillaMaterials.MAGMA_CREAM, getRandom().nextInt(1)));
		if (getAttachedCount() == 1) {
			int spawnHealth = 1;
			if (getSize() == 2) {
				spawnHealth = 4;
			} else if (getSize() == 4) {
				spawnHealth = 16;
			}
			getOwner().add(HealthComponent.class).setSpawnHealth(spawnHealth);
			
		}
	}

	public byte getSize() {
		return getOwner().getData().get(VanillaData.SLIME_SIZE);
	}

	public void setSize(byte size) {
		if (size >= 0 && size <= 4 && size != 3) {
			getOwner().getData().put(VanillaData.SLIME_SIZE, size);
		}
	}
}
