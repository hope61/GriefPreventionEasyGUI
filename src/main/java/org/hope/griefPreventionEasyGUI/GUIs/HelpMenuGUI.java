package org.hope.griefPreventionEasyGUI.GUIs;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import org.bukkit.configuration.file.FileConfiguration;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HelpMenuGUI {
    private final GriefPreventionEasyGUI plugin;
    private final Player player;
    private final Window window;

    public HelpMenuGUI(GriefPreventionEasyGUI plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        FileConfiguration lang = plugin.getLang();
        FileConfiguration config = plugin.getConfig();

        int remainingBlocks = GriefPrevention.instance.dataStore
                .getPlayerData(player.getUniqueId()).getRemainingClaimBlocks();

        List<Item> row2Items = new ArrayList<>();
        List<Item> row3Items = new ArrayList<>();

        // Help/Guide — always shown
        Item helpItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.BOOK)
                        .setName(text(lang.getString("gui.help.name", "§bGriefPrevention Help")))
                        .setLore(lore(lang.getStringList("gui.help.lore"))))
                .build();

        // AutoClaim
        if (config.getBoolean("autoclaim-enabled", true)) {
            row2Items.add(Item.builder()
                    .setItemProvider(new ItemBuilder(Material.ENCHANTED_BOOK)
                            .setName(text(lang.getString("gui.autoclaim.name", "§a§lAuto Claim")))
                            .setLore(lang.getStringList("gui.autoclaim.lore").stream()
                                    .map(l -> text(l.replace("%blocks%", String.valueOf(remainingBlocks))))
                                    .toList()))
                    .addClickHandler(click -> new AutoClaimGUI(plugin, player).getWindow().open())
                    .build());
        }

        // Give Shovel
        if (config.getBoolean("shovel-enabled", true)) {
            long cooldownMs = config.getLong("shovel-cooldown-seconds", 3600) * 1000L;
            long lastUsed = plugin.getCooldowns().getLong("shovel." + player.getUniqueId(), 0);
            long cooldownRemaining = cooldownMs - (System.currentTimeMillis() - lastUsed);
            String shovelStatus;
            if (cooldownMs <= 0 || cooldownRemaining <= 0) {
                shovelStatus = lang.getString("gui.giveshovel.status_ready", "§aReady!");
            } else {
                shovelStatus = lang.getString("gui.giveshovel.status_cooldown", "§cCooldown: %time%")
                        .replace("%time%", formatTime(cooldownRemaining));
            }

            row2Items.add(Item.builder()
                    .setItemProvider(new ItemBuilder(Material.GOLDEN_SHOVEL)
                            .setName(text(lang.getString("gui.giveshovel.name", "§6§lGet Golden Shovel")))
                            .setLore(lang.getStringList("gui.giveshovel.lore").stream()
                                    .map(l -> text(l.replace("%status%", shovelStatus)))
                                    .toList()))
                    .addClickHandler(click -> giveShovel())
                    .build());
        }

        // Visualizer toggle
        if (plugin.isGlobalVisualizerEnabled()) {
            boolean vizEnabled = plugin.isVisualizerEnabled(player);
            String currentStatus = vizEnabled
                    ? lang.getString("gui.visualizer.status_on", "§aON")
                    : lang.getString("gui.visualizer.status_off", "§cOFF");

            row2Items.add(Item.builder()
                    .setItemProvider(new ItemBuilder(vizEnabled ? Material.GLOWSTONE : Material.REDSTONE)
                            .setName(text(lang.getString("gui.visualizer.name", "§bBoundary Visualizer")))
                            .setLore(lang.getStringList("gui.visualizer.lore").stream()
                                    .map(l -> text(l.replace("%status%", currentStatus))).toList()))
                    .addClickHandler(click -> {
                        plugin.setVisualizerEnabled(player, !vizEnabled);
                        new HelpMenuGUI(plugin, player).getWindow().open();
                    })
                    .build());
        }

        // My Claims
        row3Items.add(Item.builder()
                .setItemProvider(new ItemBuilder(Material.CHEST)
                        .setName(text(lang.getString("gui.myclaims.name", "§e§lMy Claims")))
                        .setLore(lore(lang.getStringList("gui.myclaims.lore"))))
                .addClickHandler(click -> new ClaimListGUI(plugin, player).getWindow().open())
                .build());

        // Claim Info
        row3Items.add(Item.builder()
                .setItemProvider(new ItemBuilder(Material.MAP)
                        .setName(text(lang.getString("gui.claiminfo_button.name", "§b§lClaim Info")))
                        .setLore(lore(lang.getStringList("gui.claiminfo_button.lore"))))
                .addClickHandler(click -> {
                    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
                    if (claim == null || claim.isAdminClaim()) {
                        player.sendMessage(lang.getString("gui.messages.no_claim_here", "§cNo claim here!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                        return;
                    }
                    new ClaimInfoGUI(plugin, player, claim, Origin.MAIN_MENU).getWindow().open();
                })
                .build());

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        // Dynamic layout — row2 uses A/B/D, row3 uses a/b/c
        char[] row2Chars = {'A', 'B', 'D', 'E', 'F'};
        char[] row3Chars = {'a', 'b', 'c', 'd', 'e'};

        Gui.Builder<?, ?> builder = Gui.builder()
                .setStructure(
                        "# # # # C # # # #",
                        centeredRow(row2Items.size(), row2Chars),
                        centeredRow(row3Items.size(), row3Chars),
                        "# # # # # # # # #"
                )
                .addIngredient('#', filler)
                .addIngredient('C', helpItem);

        for (int i = 0; i < row2Items.size(); i++) builder.addIngredient(row2Chars[i], row2Items.get(i));
        for (int i = 0; i < row3Items.size(); i++) builder.addIngredient(row3Chars[i], row3Items.get(i));

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.title", "GriefPrevention GUI")))
                .setUpperGui(builder.build())
                .setViewer(player)
                .build();
    }

    public Window getWindow() {
        return window;
    }

    private String centeredRow(int count, char[] chars) {
        return switch (count) {
            case 1 -> "# # # # " + chars[0] + " # # # #";
            case 2 -> "# # # " + chars[0] + " # " + chars[1] + " # # #";
            case 3 -> "# # " + chars[0] + " # " + chars[1] + " # " + chars[2] + " # #";
            case 4 -> "# " + chars[0] + " # " + chars[1] + " # " + chars[2] + " # " + chars[3] + " #";
            case 5 -> chars[0] + " # " + chars[1] + " # " + chars[2] + " # " + chars[3] + " # " + chars[4];
            default -> "# # # # # # # # #";
        };
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }

    private List<Component> lore(List<String> lines) {
        return lines.stream().map(this::text).toList();
    }

    private String formatTime(long ms) {
        long minutes = ms / 60_000;
        long seconds = (ms / 1000) % 60;
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }

    private void giveShovel() {
        FileConfiguration lang = plugin.getLang();
        FileConfiguration cooldowns = plugin.getCooldowns();

        long cooldownMs = plugin.getConfig().getLong("shovel-cooldown-seconds", 3600) * 1000L;
        long lastUsed = cooldowns.getLong("shovel." + player.getUniqueId(), 0);
        long now = System.currentTimeMillis();

        if (cooldownMs > 0 && now - lastUsed < cooldownMs) {
            long remaining = cooldownMs - (now - lastUsed);
            String msg = lang.getString("gui.messages.cooldown", "§cWait %time% before getting another shovel!")
                    .replace("%time%", formatTime(remaining));
            player.sendMessage(text(msg));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(text(lang.getString("gui.messages.inventory_full", "§cInventory is full!")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        player.getInventory().addItem(new ItemStack(Material.GOLDEN_SHOVEL));
        cooldowns.set("shovel." + player.getUniqueId(), now);
        plugin.saveCooldownsConfig();
        player.sendMessage(text(lang.getString("gui.messages.received_shovel", "§aYou received a Golden Shovel!")));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        player.closeInventory();
    }
}
