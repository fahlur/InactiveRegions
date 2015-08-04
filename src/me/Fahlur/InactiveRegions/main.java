package me.Fahlur.InactiveRegions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class main extends JavaPlugin  {
	
	Plugin plugin = this;
	public static Essentials ess3 = null;
	public static WorldGuardPlugin wg = null;
	
	@Override
	public void onEnable(){
		loadConfiguration();
		ess3 = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
		wg = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
	}
	
	public void onDisable(){
		Bukkit.getLogger().warning("[InactiveRegions] Disabled!");
	}
	
	public void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		this.saveConfig();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Bukkit.getLogger().severe("[InactiveRegions]: That command can only be done as a player!");
			return true;
		}
		
		Player player = (Player) sender;
		
		if(!player.hasPermission("InactiveRegionsList")){
			player.sendMessage(ChatColor.DARK_RED + "You do not have permission to do this!");
			return true;
		}
	
		if(cmd.getName().equalsIgnoreCase("oldregions")){
			
			int i = 1;
			RegionManager worldGuard = wg.getRegionManager(player.getWorld());
			Map<String, ProtectedRegion> regionList = worldGuard.getRegions();
			List<String> oNames = new ArrayList<String>();
			
			for(Entry<String, ProtectedRegion> list : regionList.entrySet()){
				String newList = list.getValue().getId();
				if (!newList.equalsIgnoreCase("__global__")){
					Set<String> owners = list.getValue().getOwners().getPlayers();
					int getCount = owners.size();
				
					if(!owners.contains("server")){
						if(getCount == 1){
							for(String o : owners){
								//long maxInactiveDays = 7776000000L;
								long maxInactiveDays = 60000;
								long lastOnline = ess3.getUser(o).getLastLogout();	
								long difference = lastOnline+maxInactiveDays;
								if(System.currentTimeMillis() > difference){
									oNames.add(newList);
								}					
							}
						}
					}
					
					
					
				}
			}
			
			player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
			player.sendMessage(ChatColor.RED + " Expired Regions List (90+ days with inactive owners)");
			player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
			
			if(oNames.size() != 0){
				for(String name : oNames){
					player.sendMessage(i+". " + ChatColor.YELLOW + name);
					i++;
				}
			}else{
				player.sendMessage(ChatColor.LIGHT_PURPLE+"There are currently no inactive regions!");
			}
			return true;	
		}
		return true;
	}
	
}
