package net.dxtrus.shopgui.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.dxtrus.shopgui.ShopGUI;

public class ItemShopInventory implements InventoryHolder {

	private final Inventory inventory;
	private final Player player;
	private final String name;

	private boolean isValid = false;
	private Map<Integer, ShopItem> items = new HashMap<>();
	private int keySize = 0;

	private int currentPage = 0;

	public ItemShopInventory(Player player, String name) {
		this.player = player;
		this.name = name;

		inventory = Bukkit.createInventory(this, 54, ShopGUI.tlf("inventory.name"));
		isValid = ShopGUI.getInstance().getShopConfigs().containsKey(name.toLowerCase());

		if (isValid)
			populateInventory();
	}

	private void populateInventory() {
		ShopGUI shopGui = ShopGUI.getInstance();

		
		populateItemShop(shopGui, currentPage);

	}

	private void populateItemShop(ShopGUI shopGui, int page) {

		inventory.clear();

		FileConfiguration shopConfig = shopGui.getShopConfigs().get(name.toLowerCase());

		Set<String> itemKeys = shopConfig.getConfigurationSection("items").getKeys(false);
		List<String> itemKeys1 = Lists.newArrayList(itemKeys);
		
		keySize = itemKeys1.size();
		for (int i = currentPage * 27; i < 27 * (currentPage + 1); i++) {
			if (itemKeys1.size() <= i)
				break;

			String key = itemKeys1.get(i);

			String customItem = shopConfig.getString("items." + key + ".custom-item");
			if (customItem != null) {
				if (!shopGui.getSerializedItems().containsKey(customItem.toLowerCase())) {
					shopGui.getLogger().info("[Shop: " + name + "] Invalid custom item: " + customItem);
				}

				ItemStack item = shopGui.getSerializedItems().get(customItem.toLowerCase());
				double price = shopConfig.getDouble("items." + key + ".price");

				ShopItem shopItem = new ShopItem(price, shopConfig.getDouble("items." + key + ".sell-price"), item);
				inventory.setItem(currentPage == 0 ? i : i - (27 * currentPage), item);

				items.put(i, shopItem);
				//shopGui.getLogger().info("[Shop: " + name + "] Using custom item: " + customItem + " in slot " + i);

				continue;
			}

			Material material = Material.valueOf(shopConfig.getString("items." + key + ".item.type"));
			int amount = shopConfig.getInt("items." + key + ".item.amount", 1);
			short data = (short) shopConfig.getInt("items." + key + ".item.damage", 0);

			ItemStack item = new ItemStack(material, amount, data);

			ItemMeta itemMeta = (ItemMeta) shopConfig.get("items." + key + ".item.meta");
			item.setItemMeta(itemMeta);

			double price = shopConfig.getDouble("items." + key + ".price");

			ShopItem shopItem = new ShopItem(price, shopConfig.getDouble("items." + key + ".sell-price"), item);
			inventory.setItem(currentPage == 0 ? i : i - (27 * currentPage), item);

			items.put(i, shopItem);

		}

		

		if (shopGui.getConfig().getBoolean("shop-fill-empty-slots")) {
			ItemStack noItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
			ItemMeta niMeta = noItem.getItemMeta();

			niMeta.setDisplayName(" ");
			noItem.setItemMeta(niMeta);

			for (int i = 0; i < inventory.getSize(); i++) {
				if (inventory.getItem(i) == null) {
					inventory.setItem(i, noItem);
				}
			}

		}

		if (hasNextPage()) {
			ItemStack nextPage = new ItemStack(Material.PAPER, 1);
			ItemMeta meta = nextPage.getItemMeta();
			meta.setDisplayName(ShopGUI.tlf("items.next-page.name"));
			nextPage.setItemMeta(meta);

			inventory.setItem(41, nextPage);
		}

		if (hasPrevPage()) {
			ItemStack prevPage = new ItemStack(Material.PAPER, 1);
			ItemMeta meta = prevPage.getItemMeta();
			meta.setDisplayName(ShopGUI.tlf("items.prev-page.name"));
			prevPage.setItemMeta(meta);

			inventory.setItem(39, prevPage);
		}
		
		ItemStack goBack = new ItemStack(Material.BARRIER, 1);
		ItemMeta goBackMeta = goBack.getItemMeta();
		
		goBackMeta.setDisplayName(ShopGUI.tl("&eBack to Shop List."));
		goBack.setItemMeta(goBackMeta);
		
		inventory.setItem(40,  goBack);
		
	}

	public void show() {
		if (isValid)
			player.openInventory(this.inventory);
		else
			player.sendMessage(ShopGUI.tlf("shops.invalid-shop"));
	}

	public void prevPage() {
		currentPage -= 1;
		populateInventory();
	}

	public void nextPage() {
		currentPage += 1;
		populateInventory();
	}

	public boolean hasNextPage() {
		return keySize > 27 && (keySize/27) > currentPage;
	}

	public boolean hasPrevPage() {
		return items.size() > 27 && currentPage > 0;
	}

	public Player getPlayer() {
		return player;
	}

	public String getName() {
		return name;
	}

	public int getPage() {
		return currentPage;
	}
	
	public int getKeySize() {
		return keySize;
	}
	

	public Map<Integer, ShopItem> getItems() {
		return Collections.unmodifiableMap(items);
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

}
