package eu.carrade.amaury.BelovedBlocks.dependencies;

import eu.carrade.amaury.BelovedBlocks.*;
import me.botsko.prism.*;
import me.botsko.prism.actionlibs.*;
import me.botsko.prism.actions.*;
import me.botsko.prism.exceptions.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;


public class PrismDependency
{
	private boolean enabled = false;
	private Prism prism = null;

	public PrismDependency(BelovedBlocks p)
	{
		// We try to load the plugin
		Plugin prismTest = Bukkit.getServer().getPluginManager().getPlugin("Prism");
		if (prismTest == null || !prismTest.isEnabled())
		{
			return; // cannot load
		}

		prism = (Prism) prismTest;

		enabled = p.getConfig().getBoolean("logs.prism");

		// Our actions are registered
		try
		{
			for (PrismActionType action : PrismActionType.values())
			{
				Prism.getActionRegistry().registerCustomAction(p, action.getAction());
			}
		}
		catch (InvalidActionException e)
		{
			p.getLogger().warning("Prism is installed, but WE CANNOT REGISTER OUR ACTIONS.");
			p.getLogger().info("Please check if BelovedBlock is allowed to access the Prism API in the Prism's configuration file located at plugins/Prism/config.yml .");
			p.getLogger().info("The stack trace is displayed below.");

			e.printStackTrace();

			prism = null;
		}
	}

	public boolean isEnabled()
	{
		return enabled && prism != null;
	}

	/**
	 * Registers a block change in Prism.
	 *
	 * @param player     The player who changed the block.
	 * @param before     The BlockState before the change.
	 * @param after      The BlockState after the change.
	 * @param actionType The action.
	 */
	public void registerBlockChange(Player player, BlockState before, BlockState after, PrismActionType actionType)
	{
		if (!isEnabled()) return;

		BlockChangeAction action = new BlockChangeAction();
		action.setActionType(actionType.getAction().getName());
		action.setPlayerName(player);
		action.setBlock(after);
		action.setBlockId(after.getTypeId());
		action.setBlockSubId(after.getRawData());
		action.setOldBlockId(before.getTypeId());
		action.setOldBlockSubId(before.getRawData());

		RecordingQueue.addToQueue(action);
	}

	/**
	 * Our registered Prism actions.
	 */
	public enum PrismActionType
	{
		SMOOTH_BLOCK(new ActionType("bb-smooth-block", false, true, true, "BBHandler", "smoothed")),
		CARVE_BLOCK(new ActionType("bb-carve-block", false, true, true, "BBHandler", "carved")),
		MOVED_BARK(new ActionType("bb-moved-bark", false, true, true, "BBHandler", "moved the bark of"));

		private final ActionType action;

		PrismActionType(ActionType action)
		{
			this.action = action;
		}

		public ActionType getAction()
		{
			return action;
		}
	}
}