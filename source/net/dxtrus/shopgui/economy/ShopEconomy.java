package net.dxtrus.shopgui.economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.dxtrus.shopgui.ShopGUI;
import net.milkbowl.vault.economy.Economy;

public class ShopEconomy {
	
	public static double getMoney(Player p) {
		Economy econ = ShopGUI.getInstance().getEconomy();
		return econ.getBalance(Bukkit.getOfflinePlayer(p.getUniqueId()));
	}
	
	public static boolean hasEnough(Player p, double amount) {
		return getMoney(p) >= amount;
	}
	
	public static void charge(Player p, double amount) {
		Economy econ = ShopGUI.getInstance().getEconomy();
		econ.withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), amount);
	}

	public static void add(Player p, double amount) {
		Economy econ = ShopGUI.getInstance().getEconomy();
		econ.depositPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), amount);
	}
	
}
