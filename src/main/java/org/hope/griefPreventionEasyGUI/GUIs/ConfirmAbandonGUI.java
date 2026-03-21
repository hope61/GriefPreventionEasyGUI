package org.hope.griefPreventionEasyGUI.GUIs;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import org.bukkit.configuration.file.FileConfiguration;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;

import java.util.List;

public class ConfirmAbandonGUI {
    private final Window window;
    private boolean confirmed = false;

    public ConfirmAbandonGUI(GriefPreventionEasyGUI plugin, Player player, Claim claim,
                             ConfirmAbandonOrigin abandonOrigin, Origin claimInfoOrigin) {
        FileConfiguration lang = plugin.getLang();

        Item confirmItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.LIME_DYE)
                        .setName(text(lang.getString("gui.confirm.confirm_name", "§a§lCONFIRM")))
                        .setLore(lore(lang.getStringList("gui.confirm.confirm_lore"))))
                .addClickHandler(click -> {
                    if (confirmed) return;
                    confirmed = true;
                    player.closeInventory();
                    if (abandonOrigin == ConfirmAbandonOrigin.CLAIM_INFO) {
                        // Remove stored teleport location
                        plugin.getCooldowns().set("claim-tp." + claim.getID(), null);
                        plugin.saveCooldownsConfig();

                        GriefPrevention.instance.dataStore.deleteClaim(claim);
                        player.sendMessage(text(lang.getString("gui.messages.claim_deleted",
                                "§aClaim deleted successfully!")));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                    } else {
                        player.performCommand("abandonclaim");
                    }
                })
                .build();

        Item cancelItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.RED_DYE)
                        .setName(text(lang.getString("gui.confirm.cancel_name", "§c§lCANCEL")))
                        .setLore(lore(lang.getStringList("gui.confirm.cancel_lore"))))
                .addClickHandler(click -> {
                    if (abandonOrigin == ConfirmAbandonOrigin.CLAIM_INFO) {
                        new ClaimInfoGUI(plugin, player, claim, claimInfoOrigin).getWindow().open();
                    } else {
                        new HelpMenuGUI(plugin, player).getWindow().open();
                    }
                })
                .build();

        Item warningItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.TNT)
                        .setName(text(lang.getString("gui.confirm.warning_name", "§e§lAre you sure?")))
                        .setLore(lore(lang.getStringList("gui.confirm.warning_lore"))))
                .build();

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        Gui gui = Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # n # w # y # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', filler)
                .addIngredient('n', cancelItem)
                .addIngredient('w', warningItem)
                .addIngredient('y', confirmItem)
                .build();

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.confirm.title", "§cConfirm Deletion?")))
                .setUpperGui(gui)
                .setViewer(player)
                .build();
    }

    public Window getWindow() {
        return window;
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }

    private List<Component> lore(List<String> lines) {
        return lines.stream().map(this::text).toList();
    }
}
