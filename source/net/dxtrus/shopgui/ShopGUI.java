package net.dxtrus.shopgui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.dxtrus.shopgui.commands.CommandShopGUI;
import net.dxtrus.shopgui.events.InventoryListener;
import net.dxtrus.shopgui.gui.ItemShopInventory;
import net.dxtrus.shopgui.gui.PurchaseInventory;
import net.dxtrus.shopgui.gui.ShopInventory;
import net.milkbowl.vault.economy.Economy;

public class ShopGUI extends JavaPlugin {

	private static ShopGUI instance;

	private FileConfiguration messageConfig;
	private FileConfiguration shopsCfg;
	private File msgFile;
	private File serializedItemDirectory;
	private Economy econ;

	private List<String> shops = new ArrayList<String>();
	private Map<String, FileConfiguration> shopConfigs = new HashMap<String, FileConfiguration>();
	private Map<String, ItemStack> serializedItems = new HashMap<>();

	public final void onEnable() {
		instance = this;

		setupConfigs(false);
		registerCommands();
		registerEvents();
		if (!setupEconomy()) {
			setEnabled(false);
			return;
		}
		lateEnable();
	}

	public final void onDisable() {

		for (Player p : Bukkit.getOnlinePlayers()) {
			Inventory openInv = p.getOpenInventory().getTopInventory();
			if (openInv == null)
				continue;

			if (openInv instanceof ShopInventory || openInv instanceof ItemShopInventory
					|| openInv instanceof PurchaseInventory)
				p.closeInventory();
		}

		try {
			messageConfig.save(msgFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	/**
	 * [Re]Load configuration files from the plugins directory.
	 * 
	 * @param isReload Is this function is being called due to "/shopgui reload"?
	 */
	private final void setupConfigs(boolean isReload) {
		if (!isReload)
			saveDefaultConfig();
		else {
			shopConfigs.clear();
			serializedItems.clear();
			getLogger().info("A config reload has been requested.");
		}

		String msgLoc = getConfig().getString("message-file");

		msgFile = new File(getDataFolder(), msgLoc);
		try {
			if (!msgFile.exists())
				msgFile.createNewFile();

			FileConfiguration defaults = getConfigDefaults("messages.yml");

			messageConfig = YamlConfiguration.loadConfiguration(msgFile);
			if (defaults != null) {
				messageConfig.setDefaults(defaults);
				messageConfig.options().copyDefaults(true);
				messageConfig.save(this.msgFile);
			}

			// Load Shops
			getLogger().info("Loading shop configs.");

			File SCD = new File(getDataFolder(), "shops/");

			SCD.mkdir();
			File[] files = SCD.listFiles();

			for (File file : files) {
				if (!file.isFile())
					continue;
				if (!file.getPath().toLowerCase().endsWith(".yml"))
					continue;

				FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(file);
				String shopName = shopConfig.getString("name");

				if (shopConfigs.containsKey(shopName)) {
					getLogger().warning("Ambiguous shop name " + shopName + ". Skipping " + file.getPath());
					continue;
				}

				shopConfigs.put(shopName.toLowerCase(), shopConfig);
			}

			File shopsConfig = new File(getDataFolder(), "shops.yml");

			FileConfiguration shopDefaults = getConfigDefaults("shops.yml");
			if (!shopsConfig.exists()) {
				shopsConfig.createNewFile();
			}

			shopsCfg = YamlConfiguration.loadConfiguration(shopsConfig);
			if (shopDefaults != null) {
				shopsCfg.setDefaults(shopDefaults);
				shopsCfg.options().copyDefaults(true);
				shopsCfg.save(shopsConfig);
			}

			shops.addAll(shopsCfg.getConfigurationSection("shops").getKeys(false));

			// Load Serialized Items
			serializedItemDirectory = new File(getDataFolder(), "custom-items/");
			serializedItemDirectory.mkdir();

			for (File siFile : serializedItemDirectory.listFiles()) {
				if (!siFile.isFile())
					continue;
				if (!siFile.getPath().toLowerCase().endsWith(".yml"))
					continue;

				FileConfiguration serializedItem = YamlConfiguration.loadConfiguration(siFile);

				Material material = Material.valueOf(serializedItem.getString("item.type"));
				// getLogger().info(material.name());
				int amount = serializedItem.getInt("item.amount", 1);
				short dmg = (short) serializedItem.getInt("item.damage", 0);

				ItemStack item = new ItemStack(material, amount, dmg);
				ItemMeta meta = (ItemMeta) serializedItem.get("item.meta", item.getItemMeta());
				item.setItemMeta(meta);

				this.serializedItems.put(serializedItem.getString("name").toLowerCase(), item);

			}

			getLogger().info("Finished loading shop configs.");

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private final void registerCommands() {
		getCommand("shopgui").setExecutor(new CommandShopGUI());
	}

	private final void registerEvents() {
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
	}

	private final void lateEnable() {
		// TODO: Implement any functionality required after everything else has loaded.
	}

	public void reloadConfigs() {
		reloadConfig();
		setupConfigs(true);
	}

	public void addSerializedItem(String itemName, ItemStack item) {
		if (serializedItems.containsKey(itemName))
			return;

		serializedItems.put(itemName, item);
	}

	public FileConfiguration getShopsConfig() {
		return shopsCfg;
	}

	public FileConfiguration getMessageConfig() {
		return messageConfig;
	}

	public File getMessageFile() {
		return this.msgFile;
	}

	public File getSerializedItemsDirectory() {
		return serializedItemDirectory;
	}

	public Map<String, FileConfiguration> getShopConfigs() {
		return shopConfigs;
	}

	public Map<String, ItemStack> getSerializedItems() {
		return serializedItems;
	}

	public List<String> getShops() {
		return shops;
	}

	public Economy getEconomy() {
		return econ;
	}

	public static ShopGUI getInstance() {
		return instance;
	}

	public static FileConfiguration getConfigDefaults(String resourceName) {
		try {
			InputStream is = instance.getResource(resourceName);
			FileConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(is));

			return defaults;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static String tl(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static String tlf(String msg) {
		return tl(instance.messageConfig.getString(msg));
	}

	public static String tlf(String msg, Object... replacements) {

		String newMsg = tlf(msg);

		for (int i = 0; i < replacements.length; i++) {
			if (!newMsg.contains("{" + i + "}"))
				continue;

			newMsg = newMsg.replace("{" + i + "}", String.valueOf(replacements[i]));

		}

		return newMsg;

	}

}
