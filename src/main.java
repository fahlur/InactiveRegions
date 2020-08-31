package me.Fahlur.InactiveRegions;

import java.io.File;
import java.io.IOException;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class main extends JavaPlugin {

	Plugin plugin = this;
	public static Essentials ess3 = null;
	public static WorldGuardPlugin wg = null;
	PluginDescriptionFile pdfFile = this.getDescription();
	List < String > oNames = new ArrayList < String > ();
	List < String > Whitelisted = new ArrayList < String > ();
	List < String > invalidNames = new ArrayList < String > ();
	List < String > pages = new ArrayList < String > ();

	static final int PAGELENGTH = 10;

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public FileConfiguration WhiteList() {
		File folder = new File(getDataFolder(), "");
		folder.mkdir();
		File file = new File(folder, "whitelist.yml");
		FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
		return conf;
	}

	public List < String > getPage(List < String > l, int pagenr) {

		int listart = (pagenr - 1) * PAGELENGTH;
		int liend = listart + PAGELENGTH;

		for (int i = listart; i < liend; i++) {
			if (i < l.size()) {
				pages.add(l.get(i));
			} else {
				break;
			}
		}

		return pages;
	}

	public void displayList(Player player, List < String > l) {
		for (int i = 0; i < l.size(); i++) {
			player.sendMessage(l.get(i));
		}
	}

	@Override
	public void onEnable() {
		loadConfiguration();
		ess3 = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
		wg = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
	}

	public void onDisable() {
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

	/*
	 * Invalid Regions Function
	 */

	public void invalidRegions(Player player, String page) {

		RegionManager worldGuard = wg.getRegionManager(player.getWorld());
		Map < String, ProtectedRegion > regionList = worldGuard.getRegions();


		for (Entry < String, ProtectedRegion > list: regionList.entrySet()) {
			String newList = list.getValue().getId();
			if (!newList.equalsIgnoreCase("__global__")) {

				String owners = list.getValue().getOwners().toPlayersString().trim().replace("uuid:", "");
				ProtectedRegion isChild = list.getValue().getParent();

				if (!owners.contains("server") && owners.isEmpty() && isChild != null) {
					invalidNames.add(newList);
				}
			}
		}

		int pageNum = 1;
		if (isInt(page)) {
			pageNum = Integer.parseInt(page);
		}

		if (pageNum == 0) {
			pageNum = 1;
		}

		player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
		player.sendMessage(ChatColor.RED + "Invalid Owner Regions List :: Page " + pageNum + " / " + ((invalidNames.size() / 10) + 1));
		player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");

		displayList(player, getPage(invalidNames, pageNum));
		pages.clear();
		if (invalidNames.size() == 0) {
			player.sendMessage(ChatColor.GRAY + "Currently No Invalid Region Owner Names!");
		}
		player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
		player.sendMessage(ChatColor.RED + "[ " + ChatColor.WHITE + invalidNames.size() + ChatColor.RED + " ]" + ChatColor.RED + " reported invalid owner(s)");
		player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");

		invalidNames.clear();

	}


	/*
	 * Inactive Regions Function
	 */

	@SuppressWarnings("deprecation")
	public void inactiveRegions(Player player, String page) {
		RegionManager worldGuard = wg.getRegionManager(player.getWorld());
		Map < String, ProtectedRegion > regionList = worldGuard.getRegions();


		for (Entry < String, ProtectedRegion > list: regionList.entrySet()) {
			String newList = list.getValue().getId();
			if (!newList.equalsIgnoreCase("__global__")) {


				String owners = list.getValue().getOwners().toPlayersString().trim().replace("uuid:", "");

				ProtectedRegion isChild = list.getValue().getParent();

				if (owners != null && !owners.isEmpty() && isChild == null) {

					if (!owners.contains(",")) {
						if (!isUUID(owners)) {
							owners = list.getValue().getOwners().toPlayersString().trim().replace("name:", "");
							owners = Bukkit.getServer().getOfflinePlayer(owners).getUniqueId().toString();
						}

						long maxInactiveDays = 7776000000L;
						OfflinePlayer getPlayer = Bukkit.getServer().getOfflinePlayer(UUID.fromString(owners));
						if (ess3.getUser(getPlayer) != null) {


							long lastLogout = ess3.getUser(getPlayer).getLastLogout();
							long lastLogin = ess3.getUser(getPlayer).getLastLogin();

							for (String parent: WhiteList().getConfigurationSection("whitelist").getKeys(false)) {
								Whitelisted.add(parent);
							}
							if (!Whitelisted.contains(getPlayer.getName().toString())) {
								if (lastLogout > lastLogin) {
									long difference = lastLogout + maxInactiveDays;
									if (System.currentTimeMillis() > difference) {
										oNames.add(newList);
									}
								}
							}
							Whitelisted.clear();
						}
					}
				}
			}
		}

		int pageNum = 1;
		if (isInt(page)) {
			pageNum = Integer.parseInt(page);
		}

		if (pageNum == 0) {
			pageNum = 1;
		}

		player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
		player.sendMessage(ChatColor.RED + " In-Active Owner Region(s) List :: Page " + pageNum + " / " + ((oNames.size() / 10) + 1));
		player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");


		displayList(player, getPage(oNames, pageNum));
		pages.clear();
		if (oNames.size() == 0) {
			player.sendMessage(ChatColor.GRAY + "There are currently no inactive regions!");
		}
		player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");
		player.sendMessage(ChatColor.RED + "[ " + ChatColor.WHITE + oNames.size() + ChatColor.RED + " ]" + ChatColor.RED + " reported in-active owner(s)");
		player.sendMessage(ChatColor.DARK_GRAY + "------------------------------------------------");

		oNames.clear();

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Bukkit.getLogger().severe("[InactiveRegions]: That command can only be done as a player!");
			return true;
		}

		Player player = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("inactiveregions")) {

			// if no params give, return usage
			if (args.length == 0) {
				player.sendMessage(ChatColor.RED + "Usage " + ChatColor.GRAY + "/inactiveregions <version|list|whitelist>");
				return true;
			}


			/*
			 * Version Information Parameter
			 */
			if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
				player.sendMessage(ChatColor.GOLD + pdfFile.getName() + " " + ChatColor.WHITE + pdfFile.getVersion());
				player.sendMessage(ChatColor.GOLD + "Author(s): " + ChatColor.WHITE + pdfFile.getAuthors());
				player.sendMessage(ChatColor.GOLD + "Description: " + ChatColor.WHITE + pdfFile.getDescription());
				return true;
			}


			/*
			 * WhiteList
			 */

			if (args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("wl")) {
				if (!player.hasPermission("ia.Whitelist")) {
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission to do this!");
					return true;
				}

				if (args.length == 2) {
					if (args[1].equalsIgnoreCase("list")) {
						for (String parent: WhiteList().getConfigurationSection("whitelist").getKeys(false)) {
							Whitelisted.add(parent);
						}
						int itemCount = Whitelisted.size();
						player.sendMessage(ChatColor.GOLD + "Whitelisted (" + ChatColor.GRAY + itemCount + ChatColor.GOLD + "): " + ChatColor.GRAY + Whitelisted);

						Whitelisted.clear();

						return true;
					}
				}

				if (args.length >= 3) {

					if (args[1].equalsIgnoreCase("remove")) {
						for (String parent: WhiteList().getConfigurationSection("whitelist").getKeys(false)) {
							Whitelisted.add(parent);
						}
						FileConfiguration whitelisted = WhiteList();

						if (Whitelisted.contains(args[2].toString())) {
							whitelisted.set("whitelist." + args[2], null);
							try {
								whitelisted.save(getDataFolder() + File.separator + "whitelist.yml");
								player.sendMessage(ChatColor.GREEN + args[2] + ChatColor.RESET + " successfully un-whitelisted!");
							} catch (IOException e) {
								e.printStackTrace();
							}
							Whitelisted.clear();
							return true;
						}

						player.sendMessage("Player " + ChatColor.GREEN + args[2] + ChatColor.RESET + " was not in the whitelist! Remember this is case sensitive!");
						Whitelisted.clear();
						return true;
					}


					if (args[1].equalsIgnoreCase("add")) {
						for (String parent: WhiteList().getConfigurationSection("whitelist").getKeys(false)) {
							Whitelisted.add(parent);
						}
						FileConfiguration whitelisted = WhiteList();

						if (Whitelisted.contains(args[2].toString())) {
							player.sendMessage("User is already whitelisted!");
							Whitelisted.clear();
							return true;
						}
						int time = 0;
						whitelisted.createSection("whitelist." + args[2]);
						if (args.length == 4) {
							if (isInt(args[3])) {
								time = Integer.parseInt(args[3]);
							}
						}
						whitelisted.set("whitelist." + args[2] + ".time", time);
						try {
							whitelisted.save(getDataFolder() + File.separator + "whitelist.yml");
							player.sendMessage(ChatColor.GREEN + args[2] + ChatColor.RESET + " successfully whitelisted!");
						} catch (IOException e) {
							e.printStackTrace();
						}
						Whitelisted.clear();

						return true;
					}

					return true;
				}

				// If invalid param, return usage
				player.sendMessage(ChatColor.RED + "Usage " + ChatColor.GRAY + "/inactiveregions whitelist <add|remove|list>");
				return true;
			}


			/*
			 * List Param
			 */

			if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
				if (!player.hasPermission("ia.List")) {
					player.sendMessage(ChatColor.DARK_RED + "You do not have permission to do this!");
					return true;
				}
				if (args.length >= 2) {

					if (args[1].equalsIgnoreCase("old") || args[1].equalsIgnoreCase("o")) {
						String pageNumber = "1";
						if (args.length == 3) {
							pageNumber = args[2];
						}
						inactiveRegions(player, pageNumber);
						return true;
					}

					if (args[1].equalsIgnoreCase("invalid") || args[1].equalsIgnoreCase("i")) {
						String pageNumber = "1";
						if (args.length == 3) {
							pageNumber = args[2];
						}
						invalidRegions(player, pageNumber);
						return true;
					}

					return true;
				}

				player.sendMessage(ChatColor.RED + "Usage " + ChatColor.GRAY + "/inactiveregions list <old|invalid> <pageNum>");
				return true;
			}

			// If invalid param, return usage
			player.sendMessage(ChatColor.RED + "Usage " + ChatColor.GRAY + "/inactiveregions <version|list|whitelist>");

			return true;
		}

		return true;
	}

}
