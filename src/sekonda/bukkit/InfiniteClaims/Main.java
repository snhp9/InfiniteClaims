package sekonda.bukkit.InfiniteClaims;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
//import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	//private static final Listener CommandListener = null;
	
	Logger log = Logger.getLogger("Minecraft");
	@Override
	public void onEnable(){
		
		/* Pulling Config info to go here */
		
		getServer().getPluginManager().registerEvents(this, this);
		
		//send to console
		logger("Has been enabled.","normal");
	}

	@Override
	public void onDisable(){
		//Reload and Save config before shutdown
		reloadConfig();
		saveConfig();
				
		//send to console
		logger("has been disabled.","normal");
	}	
	
	//Check player commands
	@EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
    	
		//check for myplot / plothome
		if(cmd.getName().equalsIgnoreCase("myplot") || cmd.getName().equalsIgnoreCase("plothome")){ 
    		sender.sendMessage("You issues the "+ChatColor.RED + "myplot/plothome" + ChatColor.WHITE + " command correctly.");
    		return true;
    	} //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
    	if(cmd.getName().equalsIgnoreCase("clearplot")) {
    		sender.sendMessage("You issues the "+ChatColor.RED + "clearplot" + ChatColor.WHITE + "command correctly.");
    		return true;
    	}
    	if(cmd.getName().equalsIgnoreCase("getplot")) {
    		sender.sendMessage("You issues the "+ChatColor.RED + "getplot" + ChatColor.WHITE + "command correctly.");
    		return true;
    	}
		
		return false; 
    }
	

	//Logger Method
	public void logger(String msg, String type) {
		
		//Pull config for extendedLogs
		FileConfiguration config = this.getConfig();
		final boolean extendedLog = config.getBoolean("extendedLog");
		//Create Constant Prefix
		PluginDescriptionFile pdf = this.getDescription();
		final String pluginPrefix = "[" + pdf.getName() + "] ";
		
		//Checks for extendedLogging
		if(type == "extended" && extendedLog == true) {
			this.log.info(pluginPrefix + msg); 
		} else if (type == "normal"){
			this.log.info(pluginPrefix + msg);
		}
	}
}