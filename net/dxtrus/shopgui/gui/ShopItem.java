package net.dxtrus.shopgui.gui;

import org.bukkit.inventory.ItemStack;

public class ShopItem {

	private final double price;
	private final double sellPrice;
	
	private final ItemStack item;

	public ShopItem(double price, double sellPrice, ItemStack item) {
		this.price = price;
		this.sellPrice = sellPrice;
		this.item = item;
	}

	public double getPrice() {
		return price;
	}
	
	public double getSellPrice() {
		return sellPrice;
	}

	public ItemStack getItem() {
		return item;
	}
	
}
