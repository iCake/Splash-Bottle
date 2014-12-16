package co.kiwidev.splashbottles;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Suggested on Reddit found at: http://www.reddit.com/r/Minecraft/comments/2pf9fo/suggestion_splash_water_bottles/
 *
 * @author CoderCake
 *
 */
public class SplashBottles extends JavaPlugin implements Listener {

    private double MAX_DISTANCE = 2.5;
    private boolean RECIPE = false;

    private final ItemStack SINGLE_POTION = setName(new ItemStack(Material.POTION, 1, (short)16384), "&rSplash Water Bottle");

    public void onEnable() {
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        MAX_DISTANCE = config.getDouble("radius", 2.5);
        RECIPE = config.getBoolean("recipe", false);

        if(MAX_DISTANCE > 10) {
            MAX_DISTANCE = 10;
        }

        if(RECIPE) {
            ShapelessRecipe recipe = new ShapelessRecipe(setName(new ItemStack(Material.POTION, 3, (short)16384), "&rSplash Water Bottle"));
            recipe.addIngredient(Material.POTION);
            recipe.addIngredient(Material.POTION);
            recipe.addIngredient(Material.POTION);
            recipe.addIngredient(Material.SULPHUR);
            Bukkit.addRecipe(recipe);
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if(isSplashWaterBottle(e.getPotion().getItem())) {
            Location loc = e.getPotion().getLocation();
            Location foundFire = null;

            int minX = loc.getBlockX() - 3, maxX = loc.getBlockX() + 3;
            int minY = loc.getBlockY() - 1, maxY = loc.getBlockY() + 1;
            int minZ = loc.getBlockZ() - 3, maxZ = loc.getBlockZ() + 3;

            for(int x = minX; x < maxX; x++) {
                for(int y = minY; y < maxY; y++) {
                    for(int z = minZ; z < maxZ; z++) {
                        Block block = loc.getWorld().getBlockAt(x, y, z);

                        if(block.getType() == Material.FIRE && block.getLocation().distanceSquared(loc) <= MAX_DISTANCE) {
                            block.setType(Material.AIR);
                            foundFire = block.getLocation();
                        }
                    }
                }
            }

            if(foundFire != null) {
                foundFire.getWorld().playSound(foundFire, Sound.FIZZ, 1f, 1f);
            }

            for(Entity ent : e.getPotion().getNearbyEntities(MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE)) {
                if(ent instanceof LivingEntity) {
                    ((LivingEntity)ent).setFireTicks(0);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getInventory() instanceof BrewerInventory) {
            BrewerInventory inv = (BrewerInventory)e.getInventory();

            if(inv.getIngredient() != null && inv.getIngredient().getType() == Material.SULPHUR) {
                ItemStack[] contents = inv.getContents();
                boolean same = true;

                for(int i = 0; i < 3; i++) {
                    if(contents[i] == null || contents[i].getType() == Material.AIR) {
                        continue;
                    }

                    if(contents[i].getType() == Material.POTION && contents[i].getDurability() != 0) {
                        same = false;
                    }
                }

                if(same) {
                    for(int i = 0; i < 3; i++) {
                        if(contents[i] == null || contents[i].getType() == Material.AIR) {
                            continue;
                        }

                        contents[i] = SINGLE_POTION;
                    }

                    inv.setContents(contents);

                    ItemStack ing = inv.getIngredient();

                    if(ing.getAmount() - 1 >= 1) {
                        ing.setAmount(ing.getAmount() - 1);
                    } else {
                        ing = null;
                    }

                    inv.setIngredient(ing);
                }
            }
        }
    }

    private ItemStack setName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return item;
    }

    private boolean isSplashWaterBottle(ItemStack item) {
        if(item == null) {
            return false;
        }

        if(item.getType() == Material.POTION && item.getDurability() == 16384 && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if(meta.hasDisplayName() && ChatColor.stripColor(meta.getDisplayName()).equals("Splash Water Bottle")) {
                return true;
            }
        }

        return false;
    }

}
