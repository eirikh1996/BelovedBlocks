/**
 * Plugin BelovedBlocks Copyright (C) 2014-2015 Amaury Carrade & Florian Cassayre
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.BelovedBlocks.blocks;

import eu.carrade.amaury.BelovedBlocks.BBConfig;
import eu.carrade.amaury.BelovedBlocks.BelovedBlocks;
import eu.carrade.amaury.BelovedBlocks.utils.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;
import java.util.UUID;


public abstract class BelovedBlock
{
	/**
	 * Used to identify the block when used. Display name of the item visible to
	 * any player.
	 */
	protected String displayName;

	/**
	 * The internal name this block.
	 *
	 * Used by the /bb give command, and for the permissions:
	 *  - usage permission (place, remove): belovedblocks.blocks.{internalName}.use (default true);
	 *  - craft permission (craft, uncraft): belovedblocks.blocks.{internalName}.craft (default true);
	 *  - give permission: belovedblocks.blocks.{internalName}.give.self & .others (both default op).
	 */
	protected String internalName;

	/**
	 * Can this block be crafted or uncrafted, regardless to the permissions?
	 */
	protected Boolean isCraftable;
	protected Boolean isUncraftable;

	/**
	 * If true the item will be glowing.
	 */
	protected Boolean glowOnItem;

	/**
	 * The constructed ItemStack representing this block in the players' inventories.
	 */
	private ItemStack constructedItemStack = null;


	public BelovedBlock(final String displayName, final String internalName, final boolean isCraftable, final boolean isUncraftable, final boolean glowOnItem)
	{
		this.displayName = ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', displayName);

		this.internalName  = internalName;
		this.isCraftable   = isCraftable;
		this.isUncraftable = isUncraftable;
		this.glowOnItem    = glowOnItem;

		// Crafting recipes registration
		Bukkit.getScheduler().runTaskLater(BelovedBlocks.get(), new Runnable()
		{
			@Override
			public void run()
			{
				if (isCraftable)
				{
					registerRecipes(getCraftingRecipes());
				}


				ItemStack ingredient = getIngredient();
				ItemStack representation = getItem();

				if (isUncraftable && getMatterRatio() != null && ingredient != null)
				{
					ingredient = ingredient.clone();
					ingredient.setAmount(getMatterRatio());

					ShapelessRecipe recipe = new ShapelessRecipe(ingredient);
					recipe.addIngredient(representation.getData());

					BelovedBlocks.get().getServer().addRecipe(recipe);
				}
			}
		}, 1l);
	}

	public BelovedBlock(String displayName, Boolean isCraftable, Boolean glowOnItem)
	{
		this(displayName, displayName.replace(" ", ""), isCraftable, isCraftable, glowOnItem);
	}
        
        public BelovedBlock(BBConfig.ItemSection config)
        {
            this(config.NAME.get(), config.CRAFTABLE.get(), config.GLOW.get());
        }
        
	/* **  Abstract methods to override  ** */

	/**
	 * Returns the item used by the players to place this block.
	 *
	 * Don't set the display name and the glow here.
	 *
	 * @return The item.
	 */
	public abstract ItemStack getItem();

	/**
	 * The recipes used to craft this block.
	 *
	 * Returning {@code null} disables the craft.
	 *
	 * @return The recipes.
	 */
	public abstract Set<Recipe> getCraftingRecipes();

	/**
	 * Returns the ratio between the amount of matter of the ingredient and of the result
	 * of the crafting recipe.
	 *
	 * As example, a smooth double-stone-slab is crafted with four slabs in a square and this
	 * gives two double-slabs, so the ratio is 2.
	 *
	 * This is used to determine the reversed craft balance, and it will be used as the amount
	 * of matter of the original block given against one transformed block. So, this needs to
	 * be an integer.
	 *
	 * Returning {@code null} disables the reversed craft.
	 *
	 * @return The recipes.
	 */
	public abstract Integer getMatterRatio();

	/**
	 * Returns this block's main ingredient, given back in the reversed crafting recipe.
	 *
	 * @return The ingredient.
	 */
	public abstract ItemStack getIngredient();

	/**
	 * Returns the block to place in the world.
	 *
	 * @return The block.
	 */
	public abstract SimpleBlock getPlacedBlock();

	/**
	 * In the default implementation of {@link #onBlockPlace(Block)}, controls wherever the
	 * placed block is updated and the Minecraft's physics applied. Override this to disable
	 * this update, if needed.
	 *
	 * @return {@code true} if the physics have to be applied.
	 */
	public boolean applyPhysics()
	{
		return true;
	}

	/**
	 * Executed when this block is placed, if the placement is allowed.
	 *
	 * Override this if needed. Default behavior: change the block to the one returned
	 * by {@link #getPlacedBlock()}.
	 *
	 * @param placedBlock The placed block. Use this to change the placed block.
	 */
	public void onBlockPlace(Block placedBlock)
	{
		SimpleBlock blockToPlace = getPlacedBlock();
		BlockState state = placedBlock.getState();

		state.setType(blockToPlace.getType());
		state.setRawData(blockToPlace.getDataValue());

		state.update(true, applyPhysics());
	}


	/* **  Item constructors  ** */

	public ItemStack constructItem(int amount)
	{
		if (constructedItemStack != null)
			return getConstructedItemStack(amount);


		constructedItemStack = getItem().clone();

		ItemMeta meta = constructedItemStack.getItemMeta();
		meta.setDisplayName(getDisplayName());
		constructedItemStack.setItemMeta(meta);

		if (getGlowOnItem())
			GlowEffect.addGlow(constructedItemStack);


		return getConstructedItemStack(amount);
	}

	private ItemStack getConstructedItemStack(int amount)
	{
		if (constructedItemStack == null)
			return constructItem(amount);

		constructedItemStack.setAmount(amount);
		return constructedItemStack.clone();
	}


	/* **  Permissions-related helper methods  ** */

	/**
	 * Checks if the given player can use this block (place or break it).
	 *
	 * @param playerUUID The player.
	 *
	 * @return {@code true} if allowed.
	 */
	public Boolean canUse(UUID playerUUID)
	{
		return isAllowed(playerUUID, "use");
	}

	/**
	 * Checks if the given player can give this block using the {@code /bb give} command
	 * to the {@code givenTo} player.
	 *
	 * @param playerUUID The player.
	 * @param givenTo The receiver of the block.
	 *
	 * @return {@code true} if allowed.
	 */
	public Boolean canGive(UUID playerUUID, UUID givenTo)
	{
		return canGive(playerUUID, playerUUID.equals(givenTo));
	}

	/**
	 * Checks if the given player can give this block using the {@code /bb give}.
	 *
	 * @param playerUUID The player.
	 * @param self {@code true} if the receiver is the same player as the giver.
	 *
	 * @return {@code true} if allowed.
	 */
	public Boolean canGive(UUID playerUUID, boolean self)
	{
		return isAllowed(playerUUID, "give." + (self ? "self" : "others"));
	}

	/**
	 * Checks if the given player can craft this block (or uncraft it).
	 *
	 * @param playerUUID The player.
	 *
	 * @return {@code true} if allowed.
	 */
	public Boolean canCraft(UUID playerUUID)
	{
		return isAllowed(playerUUID, "craft");
	}

	/**
	 * Checks if the given player is granted the permission {@code subPermissionNode} associated
	 * with this block, i.e. the permission {@code belovedblocks.blocks.internalName.subPermissionNode}.
	 *
	 * @param playerUUID The player to check.
	 * @param subPermissionNode The sub permission node.
	 *
	 * @return {@code True} if the permission is granted.
	 */
	private Boolean isAllowed(UUID playerUUID, String subPermissionNode)
	{
		Player player = BelovedBlocks.get().getServer().getPlayer(playerUUID);
		return player != null && player.hasPermission("belovedblocks.blocks." + getInternalName() + "." + subPermissionNode);
	}


	/* **  Accessors  ** */

	public String getDisplayName()
	{
		return displayName;
	}

	protected BelovedBlock setDisplayName(String displayName)
	{
		this.displayName = displayName;
		return this;
	}

	public String getInternalName()
	{
		return internalName;
	}

	protected BelovedBlock setInternalName(String internalName)
	{
		this.internalName = internalName;
		return this;
	}

	public boolean isCraftable()
	{
		return isCraftable;
	}

	public boolean isUncraftable()
	{
		return isUncraftable;
	}

	public boolean getGlowOnItem()
	{
		return glowOnItem;
	}

	protected BelovedBlock setGlowOnItem(Boolean glowOnItem)
	{
		this.glowOnItem = glowOnItem;
		return this;
	}

	protected BelovedBlock setCraftable(Boolean isCraftable)
	{
		this.isCraftable = isCraftable;
		return this;
	}

	protected BelovedBlock setUncraftable(Boolean isUncraftable)
	{
		this.isUncraftable = isUncraftable;
		return this;
	}


	/* **  Utilities  ** */

	/**
	 * Register the given recipes.
	 *
	 * Accepts any {@code null} value, either in the {@code Set} or the {@code Set} itself.
	 *
	 * @param recipes The recipes to register.
	 */
	private void registerRecipes(Set<Recipe> recipes)
	{
		if (recipes == null) return;

		for (Recipe recipe : recipes)
		{
			if (recipe != null)
				BelovedBlocks.get().getServer().addRecipe(recipe);
		}
	}

	protected void setDisplayNameFromConfig(String key)
	{
		setDisplayName(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', BelovedBlocks.get().getConfig().getString(key)));
	}


	/* **  Comparison methods  ** */

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof BelovedBlock)) return false;

		BelovedBlock that = (BelovedBlock) o;

		return !(internalName != null ? !internalName.equals(that.internalName) : that.internalName != null);
	}

	@Override
	public int hashCode()
	{
		return internalName != null ? internalName.hashCode() : 0;
	}
}
