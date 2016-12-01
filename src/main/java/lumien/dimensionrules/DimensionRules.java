package lumien.dimensionrules;

import lumien.dimensionrules.commands.CommandGameRuleD;
import lumien.dimensionrules.lib.Reference;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, acceptableRemoteVersions = "*")
public class DimensionRules
{
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandGameRuleD());
	}
}
