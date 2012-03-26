package hskrasek;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class InfiniteClaims extends JavaPlugin
{
	Logger log = Logger.getLogger("Minecraft");	
	InfiniteClaims plugin; 
	
	public InfiniteClaimsListener playerListener = null;
	public int roadOffsetX = 4;
	public int roadOffsetZ = 4;
	public int plotHeight = 0;
	public String ownerSignPrefix = "";
	public int signPlacementMethod = 0;
	public boolean enableHome = false; 
	public String setHome = "sethome";
	public String goHome = "home";
	
	File configFile;
	File plotsFile;
	
	@Override
	public void onDisable()
	{
		this.reloadConfig();
		//Saves the config
		this.saveConfig();
	}
	
	@Override
	public void onEnable()
	{
		FileConfiguration config = this.getConfig();
		configFile = new File(getDataFolder() + "config.yml");
		try {
			//Extended Logs
			if(!config.contains("extendedLog")) {
				config.set("extendedLog", false);
			}
			//Plot Config
			if(!config.contains("plots.height")) {
				config.set("plots.height", 20);
			}
			//Sign Config
			if(!config.contains("signs.enabled")) {
				config.set("signs.enabled", false);
			}
			if(!config.contains("signs.placement")) {
				config.set("signs.placement", 0);
			}
			if(!config.contains("signs.prefix")) {
				config.set("signs.prefix", "Plot Owner:");
			}
			//Sign Config
			if(!config.contains("homes.enabled")) {
				config.set("homes.enabled", true);
			}
			if(!config.contains("signs.sethome")) {
				config.set("home.sethome", "sethome");
			}
			if(!config.contains("home.gohome")) {
				config.set("home.gohome", "home");
			}
			//Save Config
			saveConfig();
		} catch(Exception e1){
			e1.printStackTrace();
		}

		plotHeight = config.getInt("plots.height");
		ownerSignPrefix = config.getString("signs.prefix");
		signPlacementMethod = config.getInt("signs.placement");
		//home related
		enableHome = config.getBoolean("homes.enabled");
		setHome = config.getString("homes.sethome");
		goHome = config.getString("homes.gohome");
		
		playerListener = new InfiniteClaimsListener(this);		
		getServer().getPluginManager().registerEvents(playerListener, this);
		
		if(!(new File(getDataFolder() + "plots.yml").exists()))
		{
			
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if(cmd.getName().equalsIgnoreCase("plotfile"))
		{
			player.sendMessage(ChatColor.RED + "Starting file population...");
			PlotManager pm = new PlotManager(this);
			pm.loadRegionsIntoPlots(player);
			player.sendMessage(ChatColor.RED + "Finished!");
		}
		return false;
	}
	
	public WorldGuardPlugin getWorldGuard()
	{
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	    return (WorldGuardPlugin) plugin;
	}
	
	public WorldEditPlugin getWorldEdit()
	{
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
		
		//WorldEdit may not be loaded
		if(plugin == null || !(plugin instanceof WorldEditPlugin)) {
			return null;
		}
		return (WorldEditPlugin)plugin;

	}
}