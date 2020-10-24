package net.dxtrus.shopgui.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import net.dxtrus.shopgui.ShopGUI;
import net.dxtrus.shopgui.gui.ItemShopInventory;
import net.dxtrus.shopgui.gui.PurchaseInventory;
import net.dxtrus.shopgui.gui.ShopInventory;

public class InventoryListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getClickedInventory() == null)
			return;
		if (e.getAction() == null)
			return;
		if (e.getSlotType() == null)
			return;

		if (e.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof ShopInventory
				|| e.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof ItemShopInventory
				|| e.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof PurchaseInventory)
			e.setCancelled(true);

		if (!(e.getClickedInventory().getHolder() instanceof ShopInventory
				|| (e.getClickedInventory().getHolder() instanceof ItemShopInventory || e.getWhoClicked()
						.getOpenInventory().getTopInventory().getHolder() instanceof PurchaseInventory)))
			return;

		e.setCancelled(true);

		if (!(e.getWhoClicked() instanceof Player))
			return;

		InventoryHolder shop = e.getClickedInventory().getHolder();

		if (shop instanceof ShopInventory) {
			ShopInventory inv = (ShopInventory) shop;

			if (!inv.getShops().containsKey(e.getSlot()))
				return;

			String shopName = inv.getShops().get(e.getSlot());
			ItemShopInventory isi = new ItemShopInventory((Player) e.getWhoClicked(), shopName);

			if (!e.getWhoClicked().hasPermission(ShopGUI.getInstance().getConfig()
					.getString("permission-based-shops.permission").replace("{shopname}", isi.getName()))) {
				if (ShopGUI.getInstance().getConfig().getBoolean("permission-based-shops.enabled")) {
					e.getWhoClicked().sendMessage(ShopGUI.tlf("shops.no-permission"));
					return;
				}
			}

			e.getWhoClicked().closeInventory();
			isi.show();
			return;
		}
		if (shop instanceof ItemShopInventory) {
			ItemShopInventory inv = (ItemShopInventory) shop;

			if (e.getSlot() == 39 && e.getCurrentItem().getType().equals(Material.PAPER)) {
				if (inv.hasPrevPage())
					inv.prevPage();

				return;
			}
			
			if (e.getSlot() == 40) {
				e.getWhoClicked().closeInventory();
				new ShopInventory((Player)e.getWhoClicked(), "main", true).show();
				
				return;
			}

			if (e.getSlot() == 41 && e.getCurrentItem().getType().equals(Material.PAPER)) {
				if (inv.hasNextPage())
					inv.nextPage();

				return;
			}

			int page = inv.getPage();

			int slot = e.getSlot();
			if (page > 0) {
				slot = 27 + e.getSlot() * page;
			}

			if (!inv.getItems().containsKey(slot))
				return;

			PurchaseInventory purchaseInventory = new PurchaseInventory((Player) e.getWhoClicked(),
					inv.getItems().get(e.getSlot()), inv.getName());
			e.getWhoClicked().closeInventory();
			purchaseInventory.show();
			return;
		}

		if (shop instanceof PurchaseInventory) {

			PurchaseInventory purchaseInventory = (PurchaseInventory) shop;

			// BUY

			if (e.getSlot() == 37
					&& purchaseInventory.getInventory().getItem(37).getType().equals(Material.EMERALD_BLOCK)) {

				purchaseInventory.buy(1);

				return;
			}

			if (e.getSlot() == 38) {

				purchaseInventory.buy(purchaseInventory.getItem().getItem().getAmount());

				return;
			}

			if (e.getSlot() == 39) {
				purchaseInventory.buy(64);
				return;
			}
			
			if (e.getSlot() == 40) {
				purchaseInventory.backToShop();
				return;
			}

			// SELL

			if (e.getSlot() == 41) {
				purchaseInventory.sell(64);
				return;
			}

			if (e.getSlot() == 42) {
				purchaseInventory.sell(purchaseInventory.getItem().getItem().getAmount());
				return;
			}

			if (e.getSlot() == 43
					&& purchaseInventory.getInventory().getItem(43).getType().equals(Material.REDSTONE_BLOCK)) {
				purchaseInventory.sell(1);
				return;
			}

		}

	}

}
