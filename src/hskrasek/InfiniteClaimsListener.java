package hskrasek;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.Wool;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import uk.co.jacekk.bukkit.infiniteplots.InfinitePlotsGenerator;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldguard.domains.*;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

public class InfiniteClaimsListener implements Listener
{
	InfiniteClaims plugin;
	Logger logger = Logger.getLogger("Minecraft");
	public InfiniteClaimsListener(InfiniteClaims instance)
	{
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent changedWorld)
	{
		Player p = changedWorld.getPlayer();
		LocalPlayer lp = plugin.getWorldGuard().wrapPlayer(p);
		World w = p.getWorld();
		ChunkGenerator cg = w.getGenerator();
		if(cg instanceof InfinitePlotsGenerator != false)
		{
			int plotSize = ((InfinitePlotsGenerator)cg).getPlotSize();
//			int y = plugin.plotHeight + 3;
			int y = plugin.plotHeight;
			int walkwaySize = 7; // width of walkway between plots, this is not configurable in InfinitePlots so I'm not going to make it configurable in this plugin.
			
			Location startLoc = new Location(w, plugin.roadOffsetX, y, plugin.roadOffsetZ);
			plugin.log.info("Starting Location: " + startLoc.toString());
			WorldGuardPlugin wgp = plugin.getWorldGuard();
			WorldEditPlugin wep = plugin.getWorldEdit();
			String pluginPrefix = ChatColor.WHITE + "[" + ChatColor.RED + "Plot Manager" + ChatColor.WHITE + "] ";
			
			if(wgp == null || wep == null)
			{
				p.sendMessage(pluginPrefix + "WorldEdit and/or WorldGuard are missing. Please notify an admin.");
			}
			else
			{
				RegionManager rm = wgp.getRegionManager(w);
				int playerRegionCount = rm.getRegionCountOfPlayer(lp);
				Location workingLocation = startLoc; // workingLocation will be used for searching for an empty plot
				
				if(playerRegionCount == 0)
				{
					int regionSpacing = plotSize + walkwaySize;
					int failedAttemptCount = 0;
					boolean owned = true;
					
					Map<String, ProtectedRegion> regions = rm.getRegions();
					Set<String> keySet = regions.keySet();
					Object[] keys = keySet.toArray();
					int failedAttemptMaxCount = keys.length + 1; // finding an owned region counts as a failed attempt, so it's possible to validly have that many failures

					p.sendMessage(pluginPrefix + "Hi " + p.getName() + ".  You don't seem to have a plot. Let me fix that for you!");
					p.sendMessage(pluginPrefix + "Size for plots in this world: " + plotSize);
					
					while(owned && failedAttemptCount < failedAttemptMaxCount)
					{
						// this block will execute until the owned flag is set to false or until failedAttemptCount reaches the max
						
						owned = false; // ensures the loop will only execute once if no plots are owned.
						Random rnd = new Random();
						int plotDir = rnd.nextInt(8);
						List<Location> checkedLocations = new ArrayList<Location>();
						
						if(plotDir == 0)
						{
							// one plot to the right of current workingLocation
							workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ());
						}
						else if(plotDir == 1)
						{
							// one plot to the right and up of current workingLocation							
							workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() + regionSpacing);		
						}
						else if(plotDir == 2)
						{
							// one plot up of current workingLocation													
							workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + regionSpacing);		
						}
						else if(plotDir == 3)
						{
							// one plot to the left and up of current workingLocation													
							workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() + regionSpacing);					
						}
						else if(plotDir == 4)
						{
							// one plot to the left of current workingLocation													
							workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ());					
						}
						else if(plotDir == 5)
						{
							// one plot to the left and down of current workingLocation													
							workingLocation = new Location(w, workingLocation.getX() - regionSpacing, y, workingLocation.getZ() - regionSpacing);					
						}
						else if(plotDir == 6)
						{
							// one plot down of current workingLocation													
							workingLocation = new Location(w, workingLocation.getX(), y, workingLocation.getZ() - regionSpacing);					
						}
						else if(plotDir == 7)
						{
							// one plot to the right and down of current workingLocation							
							workingLocation = new Location(w, workingLocation.getX() + regionSpacing, y, workingLocation.getZ() - regionSpacing);					
						}

						if(!checkedLocations.contains(workingLocation))
						{
							// only check the region if it hasn't already been checked, otherwise it will falsely update the failedAttemptCount
							checkedLocations.add(workingLocation);

							for (Object key : keys)
							{
								ProtectedRegion pr = regions.get(key);	
								owned = pr.contains((int)workingLocation.getX(), (int)workingLocation.getY(), (int)workingLocation.getZ());

								if(owned)
								{
									// if the ProtectedRegion contains the coord's of the workingLocation, then 
									// it's owned and we need to reset workingLocation to a new spot
									failedAttemptCount++;
									break;
								}							
							}							
						}					
					}
					
					if(failedAttemptCount < failedAttemptMaxCount)
					{
						Location bottomRight = workingLocation; // not really needed, I did it just for clarity
						bottomRight.getBlock().setType(Material.WOOL);
						bottomRight.getBlock().setData(new Wool(DyeColor.RED).getData());
		                Location bottomLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ());
		                bottomLeft.getBlock().setType(Material.WOOL);
						bottomLeft.getBlock().setData(new Wool(DyeColor.BLUE).getData());
		                Location topRight = new Location(w, workingLocation.getX(), y, workingLocation.getZ() + (plotSize - 1));
		                topRight.getBlock().setType(Material.WOOL);
						topRight.getBlock().setData(new Wool(DyeColor.GREEN).getData());
		                Location topLeft = new Location(w, workingLocation.getX() + (plotSize - 1), y, workingLocation.getZ() + (plotSize - 1));
		                topLeft.getBlock().setType(Material.WOOL);
						topLeft.getBlock().setData(new Wool(DyeColor.YELLOW).getData());
						
		                plugin.log.info("Bottom Right: "+ bottomRight.toString());
		                plugin.log.info("Top Left: " + topLeft.toString());
		               
						CuboidSelection plot = new CuboidSelection(w, bottomRight, topLeft);
						p.sendMessage("Selection Max: " + plot.getMaximumPoint() + " - Selection Min: " + plot.getMinimumPoint());
						plugin.log.info("Selection Max: " + plot.getMaximumPoint() + " - Selection Min: " + plot.getMinimumPoint());
						Region tempRegion = null;
						try {
							tempRegion = plot.getRegionSelector().getRegion();
							tempRegion.expand(new Vector(0, w.getMaxHeight(), 0), new Vector(0, (-(plugin.plotHeight)+1), 0));
						} 
						catch (IncompleteRegionException e) 
						{
							p.sendMessage(e.getMessage());
						}
						catch(RegionOperationException e)
						{
							p.sendMessage(e.getMessage());
						}
						String plotName = "Plot" + p.getName() + failedAttemptCount; // failedAttemptCount is appended at the end for uniqueness
						p.sendMessage(pluginPrefix + "I've found a plot for you! Naming it: " + plotName);
						
						BlockVector minPoint = tempRegion.getMinimumPoint().toBlockVector();
						BlockVector maxPoint = tempRegion.getMaximumPoint().toBlockVector();
						ProtectedRegion playersPlot = new ProtectedCuboidRegion(plotName, minPoint, maxPoint);
						DefaultDomain owner = new DefaultDomain();
						owner.addPlayer(lp);
						playersPlot.setOwners(owner);
						
						RegionManager mgr = wgp.getGlobalRegionManager().get(w) ;
						mgr.addRegion(playersPlot);
						try 
						{
							mgr.save();
						} catch (ProtectionDatabaseException e) 
						{
							e.getMessage();
						}
						
						if(plugin.signPlacementMethod == 1)
						{
							Location entranceLocation = new Location(w, bottomRight.getX() + (plotSize / 2), y + 3, bottomRight.getZ() + (plotSize - 1));
			                Block entranceBlock = entranceLocation.getBlock();
			                placeSign(plugin.ownerSignPrefix, plotName, entranceBlock, BlockFace.WEST);
						}
						else if(plugin.signPlacementMethod == 2)
						{
							/**
							 * Change this method, to center of Entrance...maybe, or just get rid of it.
							 */
							Location centerLocation = new Location(w, bottomRight.getX() + (plotSize / 2), y, bottomRight.getZ() + (plotSize / 2));						
			                Block centerBlock = centerLocation.getBlock();
			                placeSign(plugin.ownerSignPrefix, plotName, centerBlock, BlockFace.WEST);
						}
						else if(plugin.signPlacementMethod == 0)
						{
							// creates a sign for the bottom right corner
							Location bottomRightTest = new Location(w, bottomRight.getX() - 1, bottomRight.getY() + 3, bottomRight.getZ() -1);
			                Block brBlock = bottomRightTest.getBlock();
			                placeSign(plugin.ownerSignPrefix, plotName, brBlock, BlockFace.NORTH_WEST);
			                
							// creates a sign for the bottom left corner
			                Location bottomLeftTest = new Location(w, bottomLeft.getX() + 1, bottomLeft.getY() + 3, bottomLeft.getZ() - 1);
			                Block blBlock = bottomLeftTest.getBlock();
			                placeSign(plugin.ownerSignPrefix, plotName, blBlock, BlockFace.NORTH_EAST);

							// creates a sign for the top right corner
			                Location topRightSign = new Location(w, topRight.getX() - 1, topRight.getY() + 3, topRight.getZ() + 1);
			                Block trBlock = topRightSign.getBlock();
			                placeSign(plugin.ownerSignPrefix, plotName, trBlock, BlockFace.SOUTH_WEST);
			                
			                // creates a sign for the top left corner
			                Location topLeftSign = new Location(w, topLeft.getX() + 1, topLeft.getY() + 3, topLeft.getZ() + 1);
			                Block tlBlock = topLeftSign.getBlock();
			                placeSign(plugin.ownerSignPrefix, plotName, tlBlock, BlockFace.SOUTH_EAST);                
						}
		                
		                // teleports player to their plot
						p.teleport(new Location(w, bottomRight.getX() + (plotSize / 2), y, bottomRight.getZ() + (plotSize / 2)));
						p.sendMessage(pluginPrefix + "Teleporting you to your plot.");
						//homes enabled?
						if(plugin.enableHome = true) { 
							if(plugin.setHome != "" || plugin.goHome != "")	{
								// attempts to issue a command to set the users home location, if defined
								p.performCommand(plugin.setHome);
								p.sendMessage(pluginPrefix + "Your home has been set to this plot. If you need to return, use /" + ChatColor.RED + plugin.goHome);
							} else { //Sends them a message if it can't setHome
								p.sendMessage(pluginPrefix + "We could not set this plot as your home. Please notify an admin.");
							}
						}
					}
					else
					{
						p.sendMessage(pluginPrefix + "Unable to find an unclaimed location.  Please exit the world and try again.  If this continues, please notify an admin.");
					}			
				}
			}		
		}
	}
	
	public void placeSign(String plotOwnerPrefix, String plotOwner, Block theBlock, BlockFace facingDirection)
	{
		logger.info("Recieved block: " + theBlock + "Direction: " + facingDirection);
		theBlock.setType(Material.SIGN_POST);
		Sign theSign = (Sign)theBlock.getState();
		theSign.setLine(1, plotOwnerPrefix);
		theSign.setLine(2, plotOwner);
		
		byte ne = 0xA; // North East
		byte se = 0xE; // South East
		byte sw = 0x2; // South West
		byte nw = 0x6; // North West
		byte w = 0x4;
		
		if(facingDirection == BlockFace.SOUTH_WEST)
		{
			theSign.setRawData((byte) sw);
		}
		if(facingDirection == BlockFace.SOUTH_EAST)
		{
			theSign.setRawData((byte) se);
		}
		if(facingDirection == BlockFace.NORTH_EAST)
		{
			theSign.setRawData((byte) ne);
		}
		if(facingDirection == BlockFace.NORTH_WEST)
		{
			theSign.setRawData((byte) nw);
		}
		if(facingDirection == BlockFace.WEST)
		{
			theSign.setRawData((byte) w);
		}
		theSign.update();
	}
}


