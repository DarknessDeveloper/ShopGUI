package net.dxtrus.shopgui.commands;

import java.io.File;
import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dxtrus.shopgui.ShopGUI;
import net.dxtrus.shopgui.gui.ShopInventory;

public class CommandShopGUI implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {

		if (!cmd.getName().equalsIgnoreCase("shopgui"))
			return false;

		if (lbl.equalsIgnoreCase("shop")) {
			if (!(sender instanceof Player))
				return true;

			ShopInventory shopInv = new ShopInventory((Player) sender, "categories", true);
			shopInv.show();

			return true;
		}

		if (args.length < 1) {
			sender.sendMessage(ShopGUI.tlf("commands.shopgui.no-args"));
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("shopgui.admin.reload")) {
				sender.sendMessage(ShopGUI.tlf("commands.no-permission"));
				return true;
			}

			ShopGUI.getInstance().reloadConfigs();
			sender.sendMessage(ShopGUI.tlf("commands.shopgui.reload.success"));
			
			return true;
		}

		if (args[0].equalsIgnoreCase("serialize")) {
			if (!sender.hasPermission("shopgui.admin.serialize")) {
				sender.sendMessage(ShopGUI.tlf("commands.no-permission"));
				return true;
			}

			if (!(sender instanceof Player))
				return true;

			if (args.length < 2) {
				sender.sendMessage(ShopGUI.tlf("commands.insufficient-args"));
				return true;
			}

			String itemName = args[1].toLowerCase();
			File file = new File(ShopGUI.getInstance().getSerializedItemsDirectory(), itemName + ".yml");
			ItemStack item = ((Player) sender).getInventory().getItemInMainHand();

			
			
			if (item == null || !item.getType().isItem()) {
				sender.sendMessage(ShopGUI.tlf("commands.shopgui.serialize.no-item"));
				return true;
			}
			
			if (file.exists()) {
				sender.sendMessage(ShopGUI.tlf("commands.shopgui.serialize.item-already-exists"));
				return true;
			}

			try {
				file.createNewFile();
				FileConfiguration config = YamlConfiguration.loadConfiguration(file);

				config.set("name", itemName);
				config.set("item", item.serialize());
				config.save(file);
				ShopGUI.getInstance().addSerializedItem(itemName, item);

				sender.sendMessage(ShopGUI.tlf("commands.shopgui.serialize.success", itemName));
				return true;

			} catch (IOException e) {
				e.printStackTrace();
				sender.sendMessage(ShopGUI.tlf("commands.shopgui.serialize.error"));
			}

			return true;
		}

		sender.sendMessage(ShopGUI.tlf("commands.invalid-args"));
		return true;
	}

}
