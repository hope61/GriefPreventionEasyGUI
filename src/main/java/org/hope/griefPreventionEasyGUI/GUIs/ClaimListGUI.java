package org.hope.griefPreventionEasyGUI.GUIs;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class ClaimListGUI {
    private final Window window;

    public ClaimListGUI(GriefPreventionEasyGUI plugin, Player player) {

        FileConfiguration lang = plugin.getLang();

        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        List<Claim> claims = new ArrayList<>(playerData.getClaims());
        // Filter out subclaims (subdivisions) - only top-level claims
        claims.removeIf(c -> c.parent != null);

        Item backItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.back", "§7Back"))))
                .addClickHandler(click -> new HelpMenuGUI(plugin, player).getWindow().open())
                .build();

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        if (claims.isEmpty()) {
            // No claims - show empty state
            Item emptyItem = Item.builder()
                    .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                            .setName(text(lang.getString("gui.claimlist.empty", "§7You have no claims."))))
                    .build();

            PagedGui<Item> gui = PagedGui.itemsBuilder()
                    .setStructure(
                            ". . . . . . . . .",
                            ". . . . . . . . .",
                            ". . . . x . . . .",
                            ". . . . . . . . .",
                            ". . . . . . . . .",
                            ". . . . B . . . ."
                    )
                    .addIngredient('x', emptyItem)
                    .addIngredient('B', backItem)
                    .addIngredient('.', filler)
                    .build();

            this.window = Window.builder()
                    .setTitle(text(lang.getString("gui.claimlist.title", "§6§lMy Claims")))
                    .setUpperGui(gui)
                    .setViewer(player)
                    .build();
            return;
        }

        // Build claim items
        List<Item> claimItems = new ArrayList<>();
        for (Claim claim : claims) {
            Location lesser = claim.getLesserBoundaryCorner();
            Location greater = claim.getGreaterBoundaryCorner();
            int xWidth = claim.getWidth();
            int zLength = greater.getBlockZ() - lesser.getBlockZ() + 1;

            Item claimItem = Item.builder()
                    .setItemProvider(new ItemBuilder(Material.PAPER)
                            .setName(text("§e§lClaim (" + lesser.getBlockX() + ", " + lesser.getBlockZ() + ")"))
                            .setLore(List.of(
                                    text("§8§m----------------------------"),
                                    text("§eSize: §f" + xWidth + " × " + zLength),
                                    text("§eArea: §f" + claim.getArea() + " blocks"),
                                    text("§7Click for more info"),
                                    text("§8§m----------------------------"))))
                    .addClickHandler(click -> new ClaimInfoGUI(plugin, player, claim, Origin.LIST).getWindow().open())
                    .build();
            claimItems.add(claimItem);
        }

        Item prevItem = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.previous_page", "§7Previous Page"))))
                .addClickHandler((item, gui, click) -> {
                    if (gui.getPage() > 0) gui.setPage(gui.getPage() - 1);
                })
                .build();

        Item nextItem = BoundItem.pagedBuilder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.next_page", "§7Next Page"))))
                .addClickHandler((item, gui, click) -> {
                    if (gui.getPage() < gui.getPageCount() - 1) gui.setPage(gui.getPage() + 1);
                })
                .build();

        PagedGui<Item> gui = PagedGui.itemsBuilder()
                .setStructure(
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "x x x x x x x x x",
                        "< . . . B . . . >"
                )
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', prevItem)
                .addIngredient('>', nextItem)
                .addIngredient('B', backItem)
                .addIngredient('.', filler)
                .setContent(claimItems)
                .build();

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.claimlist.title", "§6§lMy Claims")))
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
}
