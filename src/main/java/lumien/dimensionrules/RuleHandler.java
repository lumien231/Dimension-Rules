package lumien.dimensionrules;

import lumien.dimensionrules.lib.RuleData;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraftforge.common.DimensionManager;

public class RuleHandler
{
	public static GameRules getGameRuleInstance(DerivedWorldInfo worldInfo)
	{
		World worldObj = null;

		for (World world : DimensionManager.getWorlds())
		{
			if (world.getWorldInfo() == worldInfo)
			{
				worldObj = world;
				break;
			}
		}

		if (worldObj != null)
		{
			return getGameRuleInstance(worldObj);
		}

		return null;
	}
	
	public static GameRules getGameRuleInstance(World worldObj)
	{
		RuleData ruleData = RuleData.getFromWorld(worldObj);
		if (ruleData != null)
		{
			return ruleData.getGameRules();
		}
		
		return null;
	}
}
