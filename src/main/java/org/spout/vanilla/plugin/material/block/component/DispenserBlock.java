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
package org.spout.vanilla.plugin.material.block.component;

import java.util.Random;

import org.spout.api.component.type.EntityComponent;
import org.spout.api.event.Cause;
import org.spout.api.geo.LoadOption;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.geo.discrete.Point;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.BlockMaterial;
import org.spout.api.material.Material;
import org.spout.api.material.block.BlockFace;
import org.spout.api.material.block.BlockFaces;
import org.spout.api.math.GenericMath;
import org.spout.api.math.Vector3;

import org.spout.vanilla.api.data.MoveReaction;
import org.spout.vanilla.api.data.effect.Effect;
import org.spout.vanilla.api.material.block.Directional;
import org.spout.vanilla.api.material.block.redstone.RedstoneTarget;

import org.spout.vanilla.plugin.component.substance.material.Dispenser;
import org.spout.vanilla.plugin.component.substance.object.Item;
import org.spout.vanilla.plugin.component.substance.object.projectile.Arrow;
import org.spout.vanilla.plugin.data.effect.store.GeneralEffects;
import org.spout.vanilla.plugin.material.VanillaMaterials;
import org.spout.vanilla.plugin.material.item.misc.SpawnEgg;
import org.spout.vanilla.plugin.material.item.potion.Potion;
import org.spout.vanilla.plugin.resources.VanillaMaterialModels;
import org.spout.vanilla.plugin.util.PlayerUtil;
import org.spout.vanilla.plugin.util.RedstoneUtil;

public class DispenserBlock extends ComponentMaterial implements Directional, RedstoneTarget {
	public DispenserBlock(String name, int id) {
		super(name, id, Dispenser.class, VanillaMaterialModels.DISPENSER);
		this.setHardness(3.5F).setResistance(5.8F);
	}

	@Override
	public boolean hasPhysics() {
		return true;
	}

	@Override
	public void onUpdate(BlockMaterial oldMaterial, Block block) {
		super.onUpdate(oldMaterial, block);
		Dispenser dispenser = (Dispenser) block.getComponent();
		dispenser.setPowered(this.isReceivingPower(block));
	}

	@Override
	public MoveReaction getMoveReaction(Block block) {
		return MoveReaction.DENY;
	}

	@Override
	public BlockFace getFacing(Block block) {
		return BlockFaces.EWNS.get(block.getData() - 2);
	}

	/**
	 * Shoots an item from this Dispenser
	 * @param block of the Dispenser
	 * @param item to shoot
	 */
	public boolean shootItem(Block block, ItemStack item) {
		if (item == null) {
			GeneralEffects.RANDOM_CLICK2.playGlobal(block.getPosition());
			return false;
		}
		final Random rand = GenericMath.getRandom();
		Vector3 direction = this.getFacing(block).getOffset();

		// Calculate position to shoot from
		Point position = block.getPosition().add(direction.multiply(0.6));
		EntityComponent toLaunch = null;

		// Calculate shooting velocity using facing direction
		Vector3 velocity = direction.multiply(rand.nextDouble() * 0.1 + 0.2);
		// Set velocity y to above (0.2)
		velocity = velocity.multiply(1.0, 0.0, 1.0).add(0.0, 0.2, 0.0);
		velocity = velocity.add(0.045 * rand.nextGaussian(), 0.045 * rand.nextGaussian(), 0.045 * rand.nextGaussian());

		Effect shootEffect;
		Material material = item.getMaterial();
		//TODO: Implement the following 'special' shoot cases:
		// - eggs, arrows, fireballs and snowballs
		// - potions, exp. bottles and monster eggs

		if (material.equals(VanillaMaterials.ARROW)) {
			shootEffect = GeneralEffects.RANDOM_BOW;
			toLaunch = new Arrow();
		} else if (material.equals(VanillaMaterials.EGG)) {
			shootEffect = GeneralEffects.RANDOM_BOW;
			//TODO: Spawn
		} else if (material.equals(VanillaMaterials.SNOWBALL)) {
			shootEffect = GeneralEffects.RANDOM_BOW;
			//TODO: Spawn
		} else if (material instanceof Potion && ((Potion) material).isSplash()) {
			shootEffect = GeneralEffects.RANDOM_BOW;
			//TODO: Spawn
		} else if (material.equals(VanillaMaterials.BOTTLE_O_ENCHANTING)) {
			shootEffect = GeneralEffects.RANDOM_BOW;
			//TODO: Spawn
		} else if (material instanceof SpawnEgg) {
			shootEffect = GeneralEffects.RANDOM_BOW;
			//TODO: Spawn
		} else if (material.equals(VanillaMaterials.FIRE_CHARGE)) {
			shootEffect = GeneralEffects.SHOOT_FIREBALL;
			//TODO: Spawn
		} else {
			shootEffect = GeneralEffects.RANDOM_CLICK1;
			position = position.subtract(0.0, 0.3, 0.0);
			toLaunch = new Item();
			((Item) toLaunch).setItemStack(item);
		}

		if (toLaunch != null) {
			block.getWorld().createAndSpawnEntity(position, toLaunch.getClass(), LoadOption.NO_LOAD);
		}
		shootEffect.playGlobal(block.getPosition());
		GeneralEffects.SMOKE.playGlobal(block.getPosition(), direction);
		return true;
	}

	@Override
	public void setFacing(Block block, BlockFace facing) {
		block.setData((short) (BlockFaces.EWNS.indexOf(facing, 0) + 2));
	}

	@Override
	public boolean isPlacementSuppressed() {
		return true;
	}

	@Override
	public void onPlacement(Block block, short data, BlockFace against, Vector3 clickedPos, boolean isClickedBlock, Cause<?> cause) {
		super.onPlacement(block, data, against, clickedPos, isClickedBlock, cause);
		this.setFacing(block, PlayerUtil.getFacing(cause).getOpposite());
	}

	@Override
	public boolean isReceivingPower(Block block) {
		return RedstoneUtil.isReceivingPower(block);
	}

	@Override
	public boolean canSupport(BlockMaterial material, BlockFace face) {
		if (material == VanillaMaterials.VINES
				&& face != BlockFace.TOP && face != BlockFace.BOTTOM) {
			return true;
		}
		return super.canSupport(material, face);
	}
}
