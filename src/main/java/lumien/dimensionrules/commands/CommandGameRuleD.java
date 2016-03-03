package lumien.dimensionrules.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import lumien.dimensionrules.RuleHandler;
import lumien.dimensionrules.lib.RuleData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class CommandGameRuleD extends CommandBase
{
	public String getCommandName()
	{
		return "gameruled";
	}

	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	public String getCommandUsage(ICommandSender sender)
	{
		return "/gameruled [dimension] <get|list|set|reset> [gamerule] [value]";
	}

	public void processCommand(ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length == 0)
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}

		GameRules gamerules;

		int dimension = sender.getEntityWorld().provider.getDimensionId();

		int a = 0;

		try
		{
			dimension = Integer.parseInt(args[0]);
			a += 1;
		}
		catch (NumberFormatException e)
		{

		}

		if (args.length <= a)
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}

		String action = args[a];

		WorldServer worldObj = DimensionManager.getWorld(dimension);
		
		if (worldObj != null)
		{
			if (action.equals("list"))
			{
				gamerules = worldObj.getGameRules();
				String[] rules = gamerules.getRules();
				StringBuilder builder = new StringBuilder();
				builder.append("Listing Gamerules for dimension " + dimension + " (Custom: " + (RuleHandler.getGameRuleInstance(worldObj) != null ? (EnumChatFormatting.DARK_GREEN.toString() + "Yes") : (EnumChatFormatting.DARK_RED.toString() + "No")) + EnumChatFormatting.RESET.toString() + ")\n");

				for (int i = 0; i < rules.length; i++)
				{
					String rule = rules[i];
					if (gamerules.areSameType(rule, GameRules.ValueType.BOOLEAN_VALUE))
					{
						boolean value = gamerules.getBoolean(rule);
						builder.append(rule + ": " + (value ? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.DARK_RED) + gamerules.getBoolean(rule));
					}
					else
					{
						builder.append(rule + ": " + gamerules.getString(rule));
					}

					if ((i + 1) < rules.length)
					{
						builder.append("\n");
					}
				}

				sender.addChatMessage(new ChatComponentText(builder.toString()));
			}
			else if (action.equals("get"))
			{
				gamerules = worldObj.getGameRules();

				if (args.length <= a + 1)
				{
					throw new WrongUsageException(getCommandUsage(sender));
				}

				String gameRule = args[a + 1];
				sender.addChatMessage((new ChatComponentText("Getting " + gameRule + " for dimension " + dimension + " (Custom: " + (RuleHandler.getGameRuleInstance(worldObj) != null ? (EnumChatFormatting.DARK_GREEN.toString() + "Yes") : (EnumChatFormatting.DARK_RED.toString() + "No")) + EnumChatFormatting.RESET.toString() + ")")));

				if (!gamerules.hasRule(gameRule))
				{
					throw new CommandException("commands.gamerule.norule", new Object[] { gameRule });
				}

				String value = gamerules.getString(gameRule);
				sender.addChatMessage((new ChatComponentText(gameRule)).appendText(" = ").appendText(value));
				sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, gamerules.getInt(gameRule));
			}
			else if (action.equals("set"))
			{
				if (args.length <= a + 2)
				{
					throw new WrongUsageException(getCommandUsage(sender));
				}
				
				if (worldObj.provider.getDimensionId()==0)
				{
					throw new CommandException("Use /gamerule to set the \"default\" gamerules");
				}

				String gameRule = args[a + 1];
				String value = args[a + 2];
				sender.addChatMessage((new ChatComponentText("Setting " + gameRule + " to " + value + " in dimension " + dimension)));

				gamerules = RuleData.getOrCreateFromWorld(worldObj).getGameRules();
				if (gamerules.areSameType(gameRule, GameRules.ValueType.BOOLEAN_VALUE) && !"true".equals(value) && !"false".equals(value))
				{
					throw new CommandException("commands.generic.boolean.invalid", new Object[] { value });
				}

				gamerules.setOrCreateGameRule(gameRule, value);
				func_175773_a(gamerules, gameRule);
				notifyOperators(sender, this, "commands.gamerule.success", new Object[0]);
			}
			else if (action.equals("reset"))
			{
				boolean hasDefaultRules = RuleData.getFromWorld(worldObj) == null || RuleData.getFromWorld(worldObj).toBeRemoved();
				
				if (hasDefaultRules)
				{
					sender.addChatMessage(new ChatComponentText("Dimension "+worldObj.provider.getDimensionId()+" already has default gamerules"));
				}
				else
				{
					sender.addChatMessage(new ChatComponentText("Resetting Dimension "+worldObj.provider.getDimensionId()+" to default gamerules"));
					
					RuleData ruleData = RuleData.getFromWorld(worldObj);
					ruleData.remove();
					
					try
		            {
		                File file1 = new File(worldObj.getChunkSaveLocation(),"data/"+ruleData.mapName+".dat");

		                if (file1.exists())
		                {
		                    file1.delete();
		                }
		            }
		            catch (Exception exception)
		            {
		                exception.printStackTrace();
		            }
				}
			}
		}
		else
		{
			throw new CommandException("Couldn't find world for dimension %s", new Object[] { dimension });
		}
	}

	public static void func_175773_a(GameRules p_175773_0_, String p_175773_1_)
	{
		if ("reducedDebugInfo".equals(p_175773_1_))
		{
			byte b0 = (byte) (p_175773_0_.getBoolean(p_175773_1_) ? 22 : 23);

			for (EntityPlayerMP entityplayermp : MinecraftServer.getServer().getConfigurationManager().getPlayerList())
			{
				entityplayermp.playerNetServerHandler.sendPacket(new S19PacketEntityStatus(entityplayermp, b0));
			}
		}
	}

	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, "get", "list", "set", "reset");
		}
		else
		{
			boolean dimensionSpecified = false;

			World worldObj = sender.getEntityWorld();

			try
			{
				worldObj = DimensionManager.getWorld(Integer.parseInt(args[0]));
				dimensionSpecified = true;
			}
			catch (NumberFormatException e)
			{

			}

			if (worldObj != null)
			{
				if (dimensionSpecified && args.length == 2)
				{
					return getListOfStringsMatchingLastWord(args, "get", "list", "set", "reset");
				}

				GameRules gamerules = worldObj.getGameRules();

				if (args.length == 2 || (args.length == 3 && dimensionSpecified))
				{
					String action = args[(dimensionSpecified ? 1 : 0)];

					if (action.equals("get") || action.equals("set"))
					{
						return getListOfStringsMatchingLastWord(args, gamerules.getRules());
					}
				}
			}

			return null;
		}
	}
}