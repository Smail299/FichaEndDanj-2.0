package com.fichaenddanj;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandHandler implements CommandExecutor {
    
    private FichaEndDanj plugin;
    
    public CommandHandler(FichaEndDanj(FichaEndDanj plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("Usage: /danjend [additems|spawn]");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("additems")) {
            if (args.length < 3) {
                player.sendMessage("Usage: /danjend additems <chance>");
                return true;
            }
            
            int chance;
            try {
                chance = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid chance value.");
                return true;
            }
            
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            plugin.addItemToConfig(itemInHand, chance);
            player.sendMessage("Item added with chance " + chance + "%.");
            
        } else if (args[0].equalsIgnoreCase("spawn")) {
            if (args.length < 3) {
                player.sendMessage("Usage: /danjend spawn <structure> <amount>");
                return true;
            }
            
            String structure = args[1];
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid amount value.");
                return true;
            }
            
            plugin.spawnStructure(structure, player, amount);
        }
        
        return true;
    }
}
