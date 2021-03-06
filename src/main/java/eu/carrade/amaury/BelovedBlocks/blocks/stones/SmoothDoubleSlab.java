/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.carrade.amaury.BelovedBlocks.blocks.stones;

import eu.carrade.amaury.BelovedBlocks.BBConfig;
import eu.carrade.amaury.BelovedBlocks.blocks.BelovedBlock;
import fr.zcraft.zlib.tools.items.CraftingRecipes;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.MaterialData;

abstract public class SmoothDoubleSlab extends BelovedBlock
{
    private final short itemData;
    
    public SmoothDoubleSlab(String internalName, Material itemMaterial, int itemData, BBConfig.BlockSection config)
    {
        this(internalName, itemMaterial, (short) itemData, config);
    }
    
    public SmoothDoubleSlab(String internalName, Material itemMaterial, short itemData, BBConfig.BlockSection config)
    {
        super(internalName, new MaterialData(itemMaterial), config);
        this.itemData = itemData;
    }

    @Override
    public Iterable<Recipe> getCraftingRecipes()
    {
        return CraftingRecipes.get2x2Recipes(getIngredient().getData(), makeItem(2));
    }

    @Override
    public int getMatterRatio()
    {
        return 2;
    }
    
    @Override
    public ItemStackBuilder getItemBuilder()
    {
        return super.getItemBuilder().data(itemData);
    }
}
