package net.dxtrus.shopgui.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dxtrus.shopgui.ShopGUI;

public class ShopInventory implements InventoryHolder {

	private final Inventory inventory;
	private final Player player;
	private final String name;
	private final Map<Integer, String> shops = new HashMap<>();

	private int currentPage = 0;

	public ShopInventory(Player player, String name, boolean isCategoryList) {
		this.player = player;
		this.name = name;

		inventory = Bukkit.createInventory(this, 54, ShopGUI.tlf("inventory.name"));
		populateInventory();
	}

	private void populateInventory() {
		ShopGUI shopGui = ShopGUI.getInstance();
		populateCategories(shopGui);
	}

	private void populateCategories(ShopGUI shopGui) {

		for (String shopName : shopGui.getShops()) {

			String itemName = shopGui.getShopsConfig().getString("shops." + shopName + ".item-name");
			int amount = shopGui.getShopsConfig().getInt("shops." + shopName + ".amount");
			int inventorySlot = shopGui.getShopsConfig().getInt("shops." + shopName + ".inventory-slot");
			short data = (short) shopGui.getShopsConfig().getInt("shops." + shopName + ".data");
			Material material = Material.valueOf(shopGui.getShopsConfig().getString("shops." + shopName + ".material"));
			List<String> lore = new ArrayList<String>();

			for (String s : shopGui.getShopsConfig().getStringList("shops." + shopName + ".lore")) {
				lore.add(ShopGUI.tl(s));
			}

			ItemStack is = new ItemStack(material, amount, data);
			ItemMeta im = is.getItemMeta();

			im.setDisplayName(ShopGUI.tl(itemName));
			im.setLore(lore);
			is.setItemMeta(im);

			inventory.setItem(inventorySlot, is);
			shops.put(inventorySlot, shopName);

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

	}

	public void show() {
		player.openInventory(this.inventory);
	}

	public boolean hasNextPage() {
		return shops.size() > 27 && (shops.size() / (currentPage == 0 ? 1 : currentPage) > 0);
	}

	public boolean hasPrevPage() {
		return shops.size() > 27 && currentPage > 0;
	}

	public Player getPlayer() {
		return player;
	}

	public String getName() {
		return name;
	}

	public Map<Integer, String> getShops() {
		return Collections.unmodifiableMap(shops);
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

}
