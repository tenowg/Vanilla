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
package org.spout.vanilla.plugin.protocol.handler.entity;

import org.spout.api.entity.Entity;
import org.spout.api.entity.Player;
import org.spout.api.event.player.PlayerInteractEvent.Action;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.Material;
import org.spout.api.protocol.MessageHandler;
import org.spout.api.protocol.Session;

import org.spout.vanilla.api.data.effect.StatusEffect;
import org.spout.vanilla.api.event.cause.DamageCause.DamageType;
import org.spout.vanilla.api.event.cause.PlayerDamageCause;
import org.spout.vanilla.api.inventory.Slot;
import org.spout.vanilla.api.material.VanillaMaterial;

import org.spout.vanilla.plugin.component.living.Living;
import org.spout.vanilla.plugin.component.living.neutral.Human;
import org.spout.vanilla.plugin.component.misc.EffectsComponent;
import org.spout.vanilla.plugin.configuration.VanillaConfiguration;
import org.spout.vanilla.plugin.material.VanillaMaterials;
import org.spout.vanilla.plugin.material.item.tool.Tool;
import org.spout.vanilla.plugin.protocol.msg.entity.EntityInteractMessage;
import org.spout.vanilla.plugin.util.PlayerUtil;

public class EntityInteractHandler extends MessageHandler<EntityInteractMessage> {
	@Override
	public void handleServer(Session session, EntityInteractMessage message) {
		if (!session.hasPlayer()) {
			return;
		}

		Player playerEnt = session.getPlayer();
		Human player = playerEnt.get(Human.class);
		Entity clickedEntity = playerEnt.getWorld().getEntity(message.getTarget());
		if (clickedEntity == null || player == null) {
			return;
		}
		Slot held = PlayerUtil.getHeldSlot(playerEnt);
		if (held == null) {
			return;
		}
		ItemStack holding = held.get();
		Material holdingMat = holding == null ? VanillaMaterials.AIR : holding.getMaterial();
		if (holdingMat == null) {
			holdingMat = VanillaMaterials.AIR;
		}
		if (message.isPunching()) {
			holdingMat.onInteract(playerEnt, clickedEntity, Action.LEFT_CLICK);
			clickedEntity.interact(Action.LEFT_CLICK, playerEnt);

			if (clickedEntity.has(Human.class) && !VanillaConfiguration.PLAYER_PVP_ENABLED.getBoolean()) {
				return;
			}

			Living clicked = clickedEntity.get(Living.class);
			if (clicked != null) {
				//TODO: Reimplement exhaustion values

				int damage = 1;
				if (holding != null && holdingMat instanceof VanillaMaterial) {
					damage = ((VanillaMaterial) holdingMat).getDamage();
					if (holdingMat instanceof Tool) {
						// This is a bit of a hack due to the way Tool hierarchy is now (Only Swords can have a damage modifier, but Sword must be an interface and therefore is not able to contain getDamageModifier without code duplication)
						damage += ((Tool) holdingMat).getDamageBonus(clickedEntity, holding);
						//						player.getInventory().getQuickbar().getCurrentSlotInventory().addData(1); TODO: Reimplement durability change
					}
				}

				//Potion modification
				if (holdingMat.equals(VanillaMaterials.AIR)) {
					EffectsComponent effect = playerEnt.add(EffectsComponent.class);
					if (effect.containsEffect(StatusEffect.STRENGTH)) {
						damage += 3;
					}
					if (effect.containsEffect(StatusEffect.WEAKNESS)) {
						damage -= 2;
					}
				}
				//END Potion modification

				// Damage the clicked entity
				if (damage > 0 && !PlayerUtil.isCreativePlayer(clickedEntity) && !clicked.getHealth().isDead()) {
					clicked.getHealth().damage(damage, new PlayerDamageCause(playerEnt, DamageType.ATTACK), damage > 0);
				}
			}
		} else {
			holdingMat.onInteract(playerEnt, clickedEntity, Action.RIGHT_CLICK);
			clickedEntity.interact(Action.RIGHT_CLICK, playerEnt);
		}
	}
}
