package me.Fahlur.InactiveRegions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class main extends JavaPlugin  {
	
	/*
	 * ~~~ Developer Notes ~~~
	 * + Need to make lists by command page index, say 5 items per page?, maybe 10?
	 * + Need to make multi-owner checks a thing, make sure if any owner is active at all
	 * + Need to make invalid owner command, report ALL invalid owners, no owner on main regions, 
	 *   or invalid minecraft name, maybe a player never been on server name as well!
	 * + Whitelist, including timed whitlist
	 * 
	 */
	
	Plugin plugin = this;
	public static Essentials ess3 = null;
	public static WorldGuardPlugin wg = null;
	PluginDescriptionFile pdfFile = this.getDescription();
	List<String> oNames = new ArrayList<String>();
	List<String> invalidNames = new ArrayList<String>();
	
	@Override
	public void onEnable(){
		loadConfiguration();
		ess3 = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
		wg = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
	}
	
	public void onDisable(){
		Bukkit.getLogger().info("[InactiveRegions] Disabled!");
	}
	
	public void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		this.saveConfig();
	}
	
	public boolean isUUID(String string) {
	    try {
	        UUID.fromString(string);
	        return true;
	    } catch (Exception ex) {
	        return false;
	    }
	}
	
	@SuppressWarnings("deprecation")
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
		
		
		if(cmd.getName().equalsIgnoreCase("inactiveregions")){
			player.sendMessage(ChatColor.GOLD + pdfFile.getName()  + " " + ChatColor.WHITE + pdfFile.getVersion());
			player.sendMessage(ChatColor.GOLD + "Author(s): " + ChatColor.WHITE + pdfFile.getAuthors());
			player.sendMessage(ChatColor.GOLD + "Description: " + ChatColor.WHITE + pdfFile.getDescription());
		}
	
		if(cmd.getName().equalsIgnoreCase("invalidregions")){
			
			int i = 0;
			RegionManager worldGuard = wg.getRegionManager(player.getWorld());
			Map<String, ProtectedRegion> regionList = worldGuard.getRegions();
			
			
			for(Entry<String, ProtectedRegion> list : regionList.entrySet()){
				String newList = list.getValue().getId();
				if (!newList.equalsIgnoreCase("__global__")){
	
					String owners = list.getValue().getOwners().toPlayersString().trim().replace("uuid:", "");
					ProtectedRegion isChild = list.getValue().getParent();
					
					if(!owners.contains("timsandtoms") || !owners.contains("server") || owners == null || owners.isEmpty() || isChild == null){
					
						player.sendMessage("[InaciveRegions] It appears theres a region with a invalid owner name! -> Region Name: "+newList);
						i++;
						
					}
					
					
				}
			}
			
			player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
			player.sendMessage(ChatColor.RED + "[ " + ChatColor.WHITE + i + ChatColor.RED + " ]" + ChatColor.RED +" reported invalid owner(s)");
			player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
			invalidNames.clear();
			return true;
			
			
		}
		
		
		
		if(cmd.getName().equalsIgnoreCase("oldregions")){
			
			int i = 1;
			RegionManager worldGuard = wg.getRegionManager(player.getWorld());
			Map<String, ProtectedRegion> regionList = worldGuard.getRegions();
			
			
			for(Entry<String, ProtectedRegion> list : regionList.entrySet()){
				String newList = list.getValue().getId();
				if (!newList.equalsIgnoreCase("__global__")){
	
					String owners = list.getValue().getOwners().toPlayersString().trim().replace("uuid:", "");
					ProtectedRegion isChild = list.getValue().getParent();
					
					
				
					
					if(!owners.contains("server") && owners != null && !owners.isEmpty() && isChild == null){
					
							if(!owners.contains(",")){
								if(!isUUID(owners)){
									owners = list.getValue().getOwners().toPlayersString().trim().replace("name:", "");
									owners = Bukkit.getServer().getOfflinePlayer(owners).getUniqueId().toString();
								}


									
							
									long maxInactiveDays = 7776000000L; //7776000000L
									OfflinePlayer getPlayer = Bukkit.getServer().getOfflinePlayer(UUID.fromString(owners));
									if(ess3.getUser(getPlayer) != null){
										
									
										long lastLogout = ess3.getUser(getPlayer).getLastLogout();	
										long lastLogin = ess3.getUser(getPlayer).getLastLogin();	
										if(lastLogout > lastLogin){
											long difference = lastLogout+maxInactiveDays;
											if(System.currentTimeMillis() > difference){
												oNames.add(newList);
											}					
										
										}
									}
								
								
							}
						
					}
					
					
				}
			}
			
			player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
			player.sendMessage(ChatColor.RED + " Expired Region/region s List (90+ days with inactive owners)");
			player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
			
			if(oNames.size() != 0){
				for(String name : oNames){
					player.sendMessage(i+". " + ChatColor.YELLOW + name);
					i++;
				}
			}else{
				player.sendMessage(ChatColor.LIGHT_PURPLE+"There are currently no inactive regions!");
			}
			oNames.clear();
			return true;	
		}
		return true;
	}
	
}
