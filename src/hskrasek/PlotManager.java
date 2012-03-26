package hskrasek;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.configuration.file.YamlConfiguration;

import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsConfig;
import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PlotManager 
{
	InfiniteClaims plugin;
	private File plotsFile;
	private static YamlConfiguration plots = new YamlConfiguration();
	
	public PlotManager(InfiniteClaims instance)
	{
		plugin = instance;
		plotsFile = new File(plugin.getDataFolder() + "plots.yml");
	}
	
//	public boolean updatePlots(String plotName, Player plotOwner, Location plotEntrance, Player[] members)
//	{
//		//If enough data wasnt provided, return false.
//		if(plotName == null || plotOwner == null || plotEntrance == null)
//		{
//			return false;
//		}
////		plots.
//	}
	
	public void loadRegionsIntoPlots(Player player)
	{
//		List<World> worlds = plugin.getServer().getWorlds();
		WorldGuardPlugin wg = plugin.getWorldGuard();
		
//		for(int i = 0; i < worlds.size(); i++)
//		{
//			ChunkGenerator cg = worlds.get(i).getGenerator();
			ChunkGenerator cg = player.getWorld().getGenerator();
			if(cg instanceof InfinitePlotsGenerator)
			{
				Map<String, ProtectedRegion> regions = wg.getRegionManager(player.getWorld()).getRegions();
				for(Map.Entry<String, ProtectedRegion> plot : regions.entrySet())
				{
					if(plot.getKey().equalsIgnoreCase("__global__"))
					{
						continue;
					}
					plots.set("plots", plot.getKey());
					player.sendMessage(plot.getKey());
					plots.set("plots."+plot.getKey() + ".owner", plot.getValue().getOwners());
					plots.set("plots."+plot.getKey() + ".members", plot.getValue().getMembers());
//					plots.set("plots."+plot.getKey() + ".world", worlds.get(i));
//					plots.set("plots."+plot.getKey() + ".home", plot.getValue().getMinimumPoint());
//					int x = plot.getValue().getMinimumPoint().getBlockX();
//					int y = plot.getValue().getMinimumPoint().getBlockY();
//					int z = plot.getValue().getMinimumPoint().getBlockZ();
//					int plotSize = ((InfinitePlotsGenerator)cg).getPlotSize();
//					Location topRight = new Location(worlds.get(i),(double)x, (double)y, (double)z + (plotSize -1));
//					Location topLeft = new Location(worlds.get(i),(double)x + (plotSize - 1), (double)y, (double)z + (plotSize -1));
//					Location entrance = new Location(worlds.get(i), topRight.getX() - (plotSize / 2), topRight.getY(), topRight.getZ() + 1);
				}
			}
			
			try {
				plots.save(plotsFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		}
		

		
	}
}