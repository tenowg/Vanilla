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
package org.spout.vanilla.plugin.inventory.entity;

import org.spout.api.entity.Entity;
import org.spout.api.inventory.ItemStack;

import org.spout.vanilla.api.event.entity.EntityEquipmentEvent;
import org.spout.vanilla.api.inventory.entity.ArmorInventory;

import org.spout.vanilla.plugin.protocol.msg.entity.EntityEquipmentMessage;

public class EntityArmorInventory extends ArmorInventory {
	@Override
	public void updateSlot(int i, ItemStack item, Entity entity) {
		final int equip;
		switch (i) {
			case BOOT_SLOT:
				equip = EntityEquipmentMessage.BOOTS_SLOT;
				break;
			case CHEST_PLATE_SLOT:
				equip = EntityEquipmentMessage.CHESTPLATE_SLOT;
				break;
			case HELMET_SLOT:
				equip = EntityEquipmentMessage.HELMET_SLOT;
				break;
			case LEGGINGS_SLOT:
				equip = EntityEquipmentMessage.LEGGINGS_SLOT;
				break;
			default:
				return;
		}
		entity.getNetwork().callProtocolEvent(new EntityEquipmentEvent(entity, equip, item), true);
	}
}
