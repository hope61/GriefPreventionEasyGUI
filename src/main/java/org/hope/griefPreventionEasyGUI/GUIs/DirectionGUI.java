package org.hope.griefPreventionEasyGUI.GUIs;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class DirectionGUI {
    private final Window window;

    public DirectionGUI(GriefPreventionEasyGUI plugin, Player player, Claim claim, Origin origin) {
        FileConfiguration lang = plugin.getLang();

        Location lesser = claim.getLesserBoundaryCorner();
        Location greater = claim.getGreaterBoundaryCorner();

        int remaining = GriefPrevention.instance.dataStore
                .getPlayerData(player.getUniqueId()).getRemainingClaimBlocks();

        int width = greater.getBlockX() - lesser.getBlockX() + 1;
        int length = greater.getBlockZ() - lesser.getBlockZ() + 1;

        Item northItem = createDirectionItem(lang, Direction.NORTH, width, length,
                plugin, player, claim, origin);
        Item southItem = createDirectionItem(lang, Direction.SOUTH, width, length,
                plugin, player, claim, origin);
        Item westItem = createDirectionItem(lang, Direction.WEST, width, length,
                plugin, player, claim, origin);
        Item eastItem = createDirectionItem(lang, Direction.EAST, width, length,
                plugin, player, claim, origin);

        Item infoItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.BOOK)
                        .setName(text(lang.getString("gui.resize.info_remaining", "§e§lAvailable Blocks")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§eAvailable: §f" + remaining + " blocks"),
                                text("§8§m----------------------------"))))
                .build();

        Item backItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.back", "§7Back"))))
                .addClickHandler(click -> new ClaimInfoGUI(plugin, player, claim, origin).getWindow().open())
                .build();

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        Gui gui = Gui.builder()
                .setStructure(
                        "# # # N # # # # #",
                        "# # W # E # i # #",
                        "# # # S # # # B #"
                )
                .addIngredient('#', filler)
                .addIngredient('N', northItem)
                .addIngredient('S', southItem)
                .addIngredient('W', westItem)
                .addIngredient('E', eastItem)
                .addIngredient('i', infoItem)
                .addIngredient('B', backItem)
                .build();

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.resize.title_direction", "§6§lChoose Direction")))
                .setUpperGui(gui)
                .setViewer(player)
                .build();
    }

    public Window getWindow() {
        return window;
    }

    private Item createDirectionItem(FileConfiguration lang, Direction dir, int width, int length,
                                      GriefPreventionEasyGUI plugin, Player player, Claim claim, Origin origin) {
        String dirName = switch (dir) {
            case NORTH -> lang.getString("gui.resize.north", "§b§lNorth");
            case SOUTH -> lang.getString("gui.resize.south", "§b§lSouth");
            case EAST -> lang.getString("gui.resize.east", "§b§lEast");
            case WEST -> lang.getString("gui.resize.west", "§b§lWest");
        };

        Material material = switch (dir) {
            case NORTH -> Material.LIGHT_BLUE_WOOL;
            case SOUTH -> Material.ORANGE_WOOL;
            case EAST -> Material.LIME_WOOL;
            case WEST -> Material.YELLOW_WOOL;
        };

        // Highlight the axis this direction changes
        String widthLine = (dir == Direction.EAST || dir == Direction.WEST)
                ? "§a▸ §eWidth (←→): §f" + width
                : "§7  Width (←→): §f" + width;
        String lengthLine = (dir == Direction.NORTH || dir == Direction.SOUTH)
                ? "§a▸ §eLength (↑↓): §f" + length
                : "§7  Length (↑↓): §f" + length;

        return Item.builder()
                .setItemProvider(new ItemBuilder(material)
                        .setName(text(dirName))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§eClaim size:"),
                                text(widthLine),
                                text(lengthLine),
                                text(""),
                                text("§7Click to resize."),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    new ResizeGUI(plugin, player, claim, dir, 0, origin).getWindow().open();
                })
                .build();
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }
}
