package com.fichaenddanj;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.FileInputStream;

public class FichaEndDanj extends JavaPlugin {

    private File itemsFile;
    private FileConfiguration itemsConfig;

    private File configFile;
    private FileConfiguration config;

    private File databaseFile;
    private FileConfiguration databaseConfig;

    private Random random = new Random();

    @Override
    public void onEnable() {
        loadConfigurations();
        getCommand("danjend").setExecutor(new CommandHandler(this));
    }

    private void loadConfigurations() {
        configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        itemsFile = new File(getDataFolder(), "items.yml");
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        databaseFile = new File(getDataFolder(), "database.yml");
        databaseConfig = YamlConfiguration.loadConfiguration(databaseFile);

        saveDefaultConfig();
    }

    public void spawnStructure(String structure, Player player, int amount) {
        org.bukkit.World bukkitWorld = Bukkit.getWorld(config.getString("world", "game_end"));
        int xRadius = config.getInt("diapason.x", 3000);
        int zRadius = config.getInt("diapason.z", 3000);
        int yCoord = config.getInt("coords." + structure, 75);

        for (int i = 0; i < amount; i++) {
            int x = random.nextInt(xRadius * 2) - xRadius;
            int z = random.nextInt(zRadius * 2) - zRadius;
            Location loc = new Location(bukkitWorld, x, yCoord, z);

            // Очистка области для спавна схематики
            clearArea(loc, structure);

            // Спавн схематики
            spawnSchematic(structure, loc);

            // Запись координат в базу данных
            databaseConfig.set("coords.schem.location", loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ());
            try {
                databaseConfig.save(databaseFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Сообщение игроку
            String message = config.getString("spawn.text", "Схематика была заспавнена на координатах: {x} {y} {z}")
                    .replace("{x}", String.valueOf(x))
                    .replace("{y}", String.valueOf(yCoord))
                    .replace("{z}", String.valueOf(z));
            player.sendMessage(message);
        }
    }

    private void clearArea(Location loc, String structure) {
        // Логика для очистки области перед спавном
    }

    private void spawnSchematic(String structure, Location loc) {
        File schematicFile = new File(getDataFolder(), "schematics/" + structure + ".schem");
        if (!schematicFile.exists()) {
            getLogger().warning("Схематик файл не найден: " + schematicFile.getPath());
            return;
        }
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession((World) BukkitAdapter.adapt(loc.getWorld()), -1)) {
            Clipboard clipboard;
            try (FileInputStream fis = new FileInputStream(schematicFile)) {
                clipboard = ClipboardFormats.findByFile(schematicFile).getReader(fis).read();
            }
            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            Operation operation = clipboardHolder.createPaste(editSession)
                    .to(BukkitAdapter.asBlockVector(loc))
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
        } catch (IOException e) {
            getLogger().severe("Ошибка при спавне схематики: " + e.getMessage());
        }
    }

    public void addItemToConfig(ItemStack item, int chance) {
        String material = item.getType().toString();
        itemsConfig.set("items.material", material);
        itemsConfig.set("items.chance", chance);
        try {
            itemsConfig.save(itemsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
