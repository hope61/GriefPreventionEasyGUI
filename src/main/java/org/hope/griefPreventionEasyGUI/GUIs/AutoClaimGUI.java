package org.hope.griefPreventionEasyGUI.GUIs;

import me.ryanhamshire.GriefPrevention.CreateClaimResult;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;
import org.hope.griefPreventionEasyGUI.listeners.ChatInputListener;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class AutoClaimGUI {
    private final GriefPreventionEasyGUI plugin;
    private final Player player;
    private final Window window;

    public AutoClaimGUI(GriefPreventionEasyGUI plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        FileConfiguration lang = plugin.getLang();
        FileConfiguration config = plugin.getConfig();

        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        int remaining = playerData.getRemainingClaimBlocks();

        int smallSize = config.getInt("autoclaim-sizes.small", 10);
        int mediumSize = config.getInt("autoclaim-sizes.medium", 20);
        int largeSize = config.getInt("autoclaim-sizes.large", 30);
        int minSize = GriefPrevention.instance.config_claims_minWidth;

        // Small
        Item smallItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ENCHANTED_BOOK)
                        .setName(text(lang.getString("gui.autoclaim_gui.small_name", "§a§lSmall Claim")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§eSize: §f" + smallSize + "×" + smallSize),
                                text("§eBlocks needed: §f" + (smallSize * smallSize)),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> createClaim(smallSize))
                .build();

        // Medium
        Item mediumItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ENCHANTED_BOOK)
                        .setName(text(lang.getString("gui.autoclaim_gui.medium_name", "§e§lMedium Claim")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§eSize: §f" + mediumSize + "×" + mediumSize),
                                text("§eBlocks needed: §f" + (mediumSize * mediumSize)),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> createClaim(mediumSize))
                .build();

        // Large
        Item largeItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ENCHANTED_BOOK)
                        .setName(text(lang.getString("gui.autoclaim_gui.large_name", "§6§lLarge Claim")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§eSize: §f" + largeSize + "×" + largeSize),
                                text("§eBlocks needed: §f" + (largeSize * largeSize)),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> createClaim(largeSize))
                .build();

        // All Blocks
        int allBlocksSide = (int) Math.floor(Math.sqrt(remaining));
        Item allBlocksItem;
        if (allBlocksSide < minSize) {
            allBlocksItem = Item.builder()
                    .setItemProvider(new ItemBuilder(Material.BARRIER)
                            .setName(text(lang.getString("gui.autoclaim_gui.allblocks_disabled", "§c§lNot enough claim blocks!")))
                            .setLore(List.of(text("§7Need at least " + ((long) minSize * minSize) + " blocks."))))
                    .build();
        } else {
            allBlocksItem = Item.builder()
                    .setItemProvider(new ItemBuilder(Material.ENCHANTED_BOOK)
                            .setName(text(lang.getString("gui.autoclaim_gui.allblocks_name", "§c§lAll Blocks")))
                            .setLore(List.of(
                                    text("§8§m----------------------------"),
                                    text("§eSize: §f" + allBlocksSide + "×" + allBlocksSide),
                                    text("§eBlocks used: §f" + ((long) allBlocksSide * allBlocksSide)),
                                    text("§8§m----------------------------"))))
                    .addClickHandler(click -> createClaim(allBlocksSide))
                    .build();
        }

        // Custom Size
        Item customItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ENCHANTED_BOOK)
                        .setName(text(lang.getString("gui.autoclaim_gui.custom_name", "§b§lCustom Size")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§7Type the size in chat."),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> startCustomInput())
                .build();

        // Back
        Item backItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.back", "§7Back"))))
                .addClickHandler(click -> new HelpMenuGUI(plugin, player).getWindow().open())
                .build();

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        Gui gui = Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # s # m # l # #",
                        "# # # a # c # # #",
                        "# # # # B # # # #"
                )
                .addIngredient('#', filler)
                .addIngredient('s', smallItem)
                .addIngredient('m', mediumItem)
                .addIngredient('l', largeItem)
                .addIngredient('a', allBlocksItem)
                .addIngredient('c', customItem)
                .addIngredient('B', backItem)
                .build();

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.autoclaim_gui.title", "§6§lClaim Size")))
                .setUpperGui(gui)
                .setViewer(player)
                .build();
    }

    public Window getWindow() {
        return window;
    }

    private void createClaim(int side) {
        FileConfiguration lang = plugin.getLang();
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        int remaining = playerData.getRemainingClaimBlocks();

        long needed = (long) side * side;
        if (remaining < needed) {
            player.sendMessage(text(lang.getString("gui.messages.not_enough_blocks_size",
                    "§cNot enough claim blocks for this size! Needed: %needed%, Available: %available%")
                    .replace("%needed%", String.valueOf(needed))
                    .replace("%available%", String.valueOf(remaining))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        Location loc = player.getLocation();
        int x1 = loc.getBlockX() - side / 2;
        int z1 = loc.getBlockZ() - side / 2;

        CreateClaimResult result = GriefPrevention.instance.dataStore.createClaim(
                loc.getWorld(),
                x1, x1 + side - 1,
                loc.getWorld().getMinHeight(), loc.getWorld().getMaxHeight(),
                z1, z1 + side - 1,
                player.getUniqueId(), null, null, player
        );

        if (!result.succeeded) {
            player.sendMessage(text(lang.getString("gui.messages.failed",
                    "§cFailed to create auto claim. Check if it overlaps another claim or if you have enough blocks.")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        } else {
            // Store the player's location as the teleport point for this claim
            if (result.claim == null) return;
            String claimId = String.valueOf(result.claim.getID());
            plugin.getCooldowns().set("claim-tp." + claimId + ".world", loc.getWorld().getName());
            plugin.getCooldowns().set("claim-tp." + claimId + ".x", loc.getX());
            plugin.getCooldowns().set("claim-tp." + claimId + ".y", loc.getY());
            plugin.getCooldowns().set("claim-tp." + claimId + ".z", loc.getZ());
            plugin.saveCooldownsConfig();

            String msg = lang.getString("gui.messages.success", "§aSuccessfully created a %width%x%length% claim (%total% blocks)!")
                    .replace("%width%", String.valueOf(side))
                    .replace("%length%", String.valueOf(side))
                    .replace("%side%", String.valueOf(side))
                    .replace("%total%", String.valueOf((long) side * side));
            player.sendMessage(text(msg));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.closeInventory();
        }
    }

    private void startCustomInput() {
        FileConfiguration lang = plugin.getLang();
        player.closeInventory();

        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        int remaining = playerData.getRemainingClaimBlocks();
        int minSize = GriefPrevention.instance.config_claims_minWidth;

        player.sendMessage(text(lang.getString("gui.autoclaim_gui.custom_prompt",
                "§eType the side length of your claim (in blocks):")));

        new ChatInputListener(plugin, player,
                input -> {
                    try {
                        int size = Integer.parseInt(input);
                        return size >= minSize && (long) size * size <= remaining;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                input -> createClaim(Integer.parseInt(input)),
                lang.getString("gui.autoclaim_gui.custom_error",
                        "§cInvalid size! Minimum: %min%, Max blocks: %max%. Try again.")
                        .replace("%min%", String.valueOf(minSize))
                        .replace("%max%", String.valueOf(remaining)),
                lang.getString("gui.messages.input_cancelled", "§cInput cancelled."),
                plugin.getConfig().getInt("chat-input-timeout-seconds", 60) * 20L
        );
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }
}
