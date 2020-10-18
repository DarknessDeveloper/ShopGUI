package net.dxtrus.shopgui.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import net.dxtrus.shopgui.gui.ShopItem;

public class ShopPurchaseItemEvent extends PlayerEvent {

	public static HandlerList handlers = new HandlerList();

	private final ShopItem item;
	private final int amount;

	public ShopPurchaseItemEvent(Player who, ShopItem item, int amount) {
		super(who);
		this.item = item;
		this.amount = amount;
	}

	public ShopItem getItem() {
		return item;
	}
	
	public int getAmount() {
		return amount;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
