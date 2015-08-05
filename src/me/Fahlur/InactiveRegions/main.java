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
	
	Plugin plugin = this;
	public static Essentials ess3 = null;
	public static WorldGuardPlugin wg = null;
	PluginDescriptionFile pdfFile = this.getDescription();
	List<String> oNames = new ArrayList<String>();
	List<String> invalidNames = new ArrayList<String>();
	List<String> page = new ArrayList<String>();
	
	static final int PAGELENGTH = 10;
    
    
    public List<String> getPage(List<String> l, int pagenr)
    {
        
     
        
        int listart = (pagenr - 1) * PAGELENGTH;
        int liend  = listart + PAGELENGTH;
     
        for(int i=listart ; i<liend ;i++)
        {
            if(i < l.size())
            {
                page.add(l.get(i));
            }
            else
            {
                break;
            }
        }
     
        return page;
    }
	
    
    public void displayList(Player player, List<String> l)
    {
        for(int i=0 ; i<l.size() ; i++)
        {
            player.sendMessage(l.get(i));
        }
    }
    
    
	
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
			
			
			RegionManager worldGuard = wg.getRegionManager(player.getWorld());
			Map<String, ProtectedRegion> regionList = worldGuard.getRegions();
			
			
			for(Entry<String, ProtectedRegion> list : regionList.entrySet()){
				String newList = list.getValue().getId();
				if (!newList.equalsIgnoreCase("__global__")){
	
					String owners = list.getValue().getOwners().toPlayersString().trim().replace("uuid:", "");
					ProtectedRegion isChild = list.getValue().getParent();
					
					if(!owners.contains("server") && owners.isEmpty() && isChild != null){
					
						invalidNames.add(newList);
						
					}
					
					
				}
			}
			
			
			
			int pageNum = 1;
			if (args.length == 1 && args[0].matches("[-+]?\\d+(\\.\\d+)?")==true){
				pageNum = Integer.parseInt(args[0]);
			}

				if (pageNum == 0){
					pageNum = 1;
				}
				
				
				
				player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
				player.sendMessage(ChatColor.RED + "In-Valid Owner Regions List :: Page "+pageNum+" / " + ((invalidNames.size()/10)+1));
				player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
				
				displayList(player, getPage(invalidNames, pageNum));
				page.clear();
				if (invalidNames.size() == 0){
					player.sendMessage(ChatColor.GRAY + "Currently No Invalid Region Owner Names!");
				}
				player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
				player.sendMessage(ChatColor.RED + "[ " + ChatColor.WHITE + invalidNames.size() + ChatColor.RED + " ]" + ChatColor.RED +" reported invalid owner(s)");
				player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");

				invalidNames.clear();
				return true;
				
			
			
			
			
		}
		
		
		
		if(cmd.getName().equalsIgnoreCase("oldregions")){
			
			
			RegionManager worldGuard = wg.getRegionManager(player.getWorld());
			Map<String, ProtectedRegion> regionList = worldGuard.getRegions();
			
			
			for(Entry<String, ProtectedRegion> list : regionList.entrySet()){
				String newList = list.getValue().getId();
				if (!newList.equalsIgnoreCase("__global__")){
	
					String owners = list.getValue().getOwners().toPlayersString().trim().replace("uuid:", "");
					ProtectedRegion isChild = list.getValue().getParent();
					
					
				
					
					if(owners != null && !owners.isEmpty() && isChild == null){
					
							if(!owners.contains(",")){
								if(!isUUID(owners)){
									owners = list.getValue().getOwners().toPlayersString().trim().replace("name:", "");
									owners = Bukkit.getServer().getOfflinePlayer(owners).getUniqueId().toString();
								}

									long maxInactiveDays = 0; //7776000000L
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
			
			
			
			
			
			
			
			int pageNum = 1;
			if (args.length == 1 && args[0].matches("[-+]?\\d+(\\.\\d+)?")==true){
				pageNum = Integer.parseInt(args[0]);
			}

				if (pageNum == 0){
					pageNum = 1;
				}
				
				player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
				player.sendMessage(ChatColor.RED + " In-Active Owner Region(s) List :: Page "+pageNum+" / " + ((oNames.size()/10)+1));
				player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
				
				
				displayList(player, getPage(oNames, pageNum));
				page.clear();
				if (oNames.size() == 0){
					player.sendMessage(ChatColor.GRAY+"There are currently no inactive regions!");
				}
				player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
				player.sendMessage(ChatColor.RED + "[ " + ChatColor.WHITE + oNames.size() + ChatColor.RED + " ]" + ChatColor.RED +" reported in-active owner(s)");
				player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");

			oNames.clear();
			
			
			
			
			
			
			return true;	
		}
		return true;
	}
	
}
