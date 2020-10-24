package net.dxtrus.shopgui.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.dxtrus.shopgui.ShopGUI;
import net.dxtrus.shopgui.api.events.ShopPurchaseItemEvent;
import net.dxtrus.shopgui.api.events.ShopSellItemEvent;
import net.dxtrus.shopgui.economy.ShopEconomy;

public class PurchaseInventory implements InventoryHolder {

	private final Inventory inventory;
	private final Player player;
	private final ShopItem item;
	private final String previousShopName;
	
	
	private double pricePerUnit = 0d;
	private double sellPrice = 0d;

	public PurchaseInventory(Player player, ShopItem item, String previousShopName) {
		this.player = player;
		this.item = item;
		this.previousShopName = previousShopName;
		
		this.inventory = Bukkit.createInventory(this, 54, ShopGUI.tlf("inventory.name"));
		this.pricePerUnit = item.getPrice() / item.getItem().getAmount();
		this.sellPrice = item.getSellPrice();

		populate(ShopGUI.getInstance());
	}

	private void populate(ShopGUI shopGui) {
		inventory.clear();

		inventory.setItem(13, item.getItem());

		int amount = item.getItem().getAmount();

		if (amount != 1) {
			ItemStack buy1 = new ItemStack(Material.EMERALD_BLOCK, 1);
			ItemMeta buy1Meta = buy1.getItemMeta();
			buy1Meta.setDisplayName(ShopGUI.tl("&aBuy 1"));
			buy1Meta.setLore(Lists.newArrayList(ShopGUI.tl("&aCosts: &c" + pricePerUnit)));
			buy1.setItemMeta(buy1Meta);

			inventory.setItem(37, buy1);
		}

		ItemStack buyAmount = new ItemStack(Material.EMERALD_BLOCK, amount);
		ItemMeta buyAmountMeta = buyAmount.getItemMeta();
		buyAmountMeta.setDisplayName(ShopGUI.tl("&aBuy " + amount));
		buyAmountMeta.setLore(Lists.newArrayList(ShopGUI.tl("&aCosts: &c" + item.getPrice())));
		buyAmount.setItemMeta(buyAmountMeta);

		ItemStack buy64 = new ItemStack(Material.EMERALD_BLOCK, 64);
		ItemMeta buy64Meta = buy64.getItemMeta();
		buy64Meta.setDisplayName(ShopGUI.tl("&aBuy 64"));
		buy64Meta.setLore(Lists.newArrayList(ShopGUI.tl("&aCosts: &c" + pricePerUnit * 64)));
		buy64.setItemMeta(buy64Meta);

		inventory.setItem(38, buyAmount);
		inventory.setItem(39, buy64);

		if (amount != 1) {
			ItemStack sell1 = new ItemStack(Material.REDSTONE_BLOCK, 1);
			ItemMeta sell1Meta = sell1.getItemMeta();
			sell1Meta.setDisplayName(ShopGUI.tl("&aSell 1"));
			sell1Meta.setLore(Lists.newArrayList(ShopGUI.tl("&aPrice: &c" + sellPrice / amount)));
			sell1.setItemMeta(sell1Meta);

			inventory.setItem(43, sell1);
		}

		ItemStack sellAmount = new ItemStack(Material.REDSTONE_BLOCK, amount);
		ItemMeta sellAmountMeta = sellAmount.getItemMeta();
		sellAmountMeta.setDisplayName(ShopGUI.tl("&aSell " + amount));
		sellAmountMeta.setLore(Lists.newArrayList(ShopGUI.tl("&aPrice: &c" + sellPrice)));
		sellAmount.setItemMeta(sellAmountMeta);

		ItemStack sell64 = new ItemStack(Material.REDSTONE_BLOCK, 64);
		ItemMeta sell64Meta = sell64.getItemMeta();
		sell64Meta.setDisplayName(ShopGUI.tl("&aSell 64"));
		sell64Meta.setLore(Lists.newArrayList(ShopGUI.tl("&aPrice: &c" + (sellPrice / amount) * 64)));
		sell64.setItemMeta(sell64Meta);

		ItemStack goBack = new ItemStack(Material.BARRIER, 1);
		ItemMeta goBackMeta = goBack.getItemMeta();
		
		goBackMeta.setDisplayName(ShopGUI.tl("&eBack to Shop."));
		goBack.setItemMeta(goBackMeta);
		
		inventory.setItem(38, buyAmount);
		inventory.setItem(39, buy64);

		inventory.setItem(40, goBack);
		
		inventory.setItem(41, sell64);
		inventory.setItem(42, sellAmount);

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

	public void buy(int amount) {
		processBuy(amount);
	}

	public void sell(int amount) {
		double price = amount == item.getItem().getAmount() ? item.getSellPrice()
				: amount == 64 ? (item.getSellPrice() / item.getItem().getAmount()) * 64
						: item.getSellPrice() / item.getItem().getAmount();

		int amountOfItem = 0;
		for (ItemStack i : player.getInventory().getContents()) {
			if (i == null)
				continue;
			if (i.isSimilar(item.getItem()))
				amountOfItem += i.getAmount();
		}

		if (amountOfItem < amount) {
			player.sendMessage(ShopGUI.tlf("shops.sell.not-enough-of-item", amount, item.getItem().getType().name()));
			return;
		}

		ItemStack selling = new ItemStack(item.getItem());
		selling.setAmount(amount);
		player.getInventory().removeItem(selling);
		ShopEconomy.add(player, price);

		player.sendMessage(ShopGUI.tlf("shops.sell.sold", amount, item.getItem().getType().name(), price));
		onSellItem(player, item, amount);
	}

	private final void processBuy(int amount) {
		double price = amount == item.getItem().getAmount() ? item.getPrice()
				: amount == 64 ? (item.getPrice() / item.getItem().getAmount()) * 64
						: item.getPrice() / item.getItem().getAmount();

		if (!ShopEconomy.hasEnough(player, price)) {
			player.sendMessage(ShopGUI.tlf("shops.buy.not-enough-money"));
			player.closeInventory();
			return;
		}

		if (player.getInventory().firstEmpty() == -1) {
			player.sendMessage(ShopGUI.tlf("shops.buy.not-enough-space"));
			player.closeInventory();
			return;
		}

		ItemStack buying = new ItemStack(item.getItem());
		buying.setAmount(amount);

		ShopEconomy.charge(player, price);
		player.getInventory().addItem(buying);
		player.sendMessage(ShopGUI.tlf("shops.buy.purchased", amount, item.getItem().getType().name(), price));

		onPurchaseItem(player, item, amount);
	}

	private void onPurchaseItem(Player p, ShopItem item, int amount) {
		Bukkit.getPluginManager().callEvent(new ShopPurchaseItemEvent(p, item, amount));
	}

	private void onSellItem(Player p, ShopItem item, int amount) {
		Bukkit.getPluginManager().callEvent(new ShopSellItemEvent(p, item, amount));
	}
	
	public void backToShop() {
		player.closeInventory();
		new ItemShopInventory(player, previousShopName).show();
	}

	public Player getPlayer() {
		return player;
	}

	public ShopItem getItem() {
		return item;
	}

	public double getPricePerUnit() {
		return pricePerUnit;
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

}
