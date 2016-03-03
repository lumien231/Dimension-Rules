package lumien.dimensionrules.lib;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

public class RuleData extends WorldSavedData
{
	public static final String ID = "DimensionRuleData";

	GameRules gameRulesInstance = new GameRules();

	boolean toBeRemoved;

	public RuleData(String name)
	{
		super(name);
	}

	public RuleData()
	{
		this(ID);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagCompound ruleCompound = nbt.getCompoundTag("dimensionRuleData");

		if (ruleCompound != null)
		{
			gameRulesInstance.readFromNBT(ruleCompound);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagCompound ruleCompound = gameRulesInstance.writeToNBT();
		nbt.setTag("dimensionRuleData", ruleCompound);
	}

	public boolean toBeRemoved()
	{
		return toBeRemoved;
	}

	@Override
	public boolean isDirty()
	{
		return !toBeRemoved;
	}

	public GameRules getGameRules()
	{
		return gameRulesInstance;
	}

	public static RuleData getFromWorld(World worldObj)
	{
		RuleData data = (RuleData) worldObj.getPerWorldStorage().loadData(RuleData.class, ID);
		if (data == null)
		{
			return null;
		}

		return data.toBeRemoved() ? null : data;
	}

	public static RuleData getOrCreateFromWorld(World worldObj)
	{
		RuleData data = (RuleData) worldObj.getPerWorldStorage().loadData(RuleData.class, ID);

		if (data == null || data.toBeRemoved())
		{
			data = new RuleData();
			data.readFromNBT(DimensionManager.getWorld(0).getWorldInfo().getGameRulesInstance().writeToNBT());
			worldObj.getPerWorldStorage().setData(ID, data);
		}

		return data;
	}

	public void remove()
	{
		this.toBeRemoved = true;
	}
}
