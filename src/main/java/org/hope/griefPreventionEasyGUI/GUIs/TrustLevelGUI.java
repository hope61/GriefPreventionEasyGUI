package org.hope.griefPreventionEasyGUI.GUIs;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.List;
import java.util.UUID;

public class TrustLevelGUI {
    private final GriefPreventionEasyGUI plugin;
    private final Player player;
    private final Claim claim;
    private final String targetName;
    private final Origin origin;
    private final Window window;

    public TrustLevelGUI(GriefPreventionEasyGUI plugin, Player player, Claim claim, String targetName, Origin origin) {
        this.plugin = plugin;
        this.player = player;
        this.claim = claim;
        this.targetName = targetName;
        this.origin = origin;

        FileConfiguration lang = plugin.getLang();

        Item containerItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.CHEST)
                        .setName(text(lang.getString("gui.trustlevel.container_name", "§6§lContainer Access")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§7Access to chests, furnaces, etc."),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> grantTrust(ClaimPermission.Inventory,
                        lang.getString("gui.trustlevel.container_label", "container access")))
                .build();

        Item buildItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.IRON_PICKAXE)
                        .setName(text(lang.getString("gui.trustlevel.build_name", "§e§lBuild Access")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§7Place and break blocks."),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> grantTrust(ClaimPermission.Build,
                        lang.getString("gui.trustlevel.build_label", "build access")))
                .build();

        Item fullItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.NETHER_STAR)
                        .setName(text(lang.getString("gui.trustlevel.full_name", "§c§lFull Access")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§7Full claim management."),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> grantTrust(ClaimPermission.Manage,
                        lang.getString("gui.trustlevel.full_label", "full access")))
                .build();

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        Gui gui = Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # c # b # f # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', filler)
                .addIngredient('c', containerItem)
                .addIngredient('b', buildItem)
                .addIngredient('f', fullItem)
                .build();

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.trustlevel.title", "§6§lTrust Level for %player%")
                        .replace("%player%", targetName)))
                .setUpperGui(gui)
                .setViewer(player)
                .build();
    }

    public Window getWindow() {
        return window;
    }

    private void grantTrust(ClaimPermission permission, String levelLabel) {
        FileConfiguration lang = plugin.getLang();

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = target.getUniqueId();

        claim.setPermission(targetUUID.toString(), permission);
        GriefPrevention.instance.dataStore.saveClaim(claim);

        player.sendMessage(text(lang.getString("gui.messages.trust_granted",
                        "§aGranted %level% to §f%player%§a!")
                .replace("%level%", levelLabel)
                .replace("%player%", targetName)));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);

        new TrustGUI(plugin, player, claim, origin).getWindow().open();
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }
}
