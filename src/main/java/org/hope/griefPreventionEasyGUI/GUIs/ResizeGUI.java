package org.hope.griefPreventionEasyGUI.GUIs;

import me.ryanhamshire.GriefPrevention.Claim;
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
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class ResizeGUI {
    private final GriefPreventionEasyGUI plugin;
    private final Player player;
    private final Claim claim;
    private final Direction direction;
    private final int offset;
    private final Origin origin;
    private final Window window;

    public ResizeGUI(GriefPreventionEasyGUI plugin, Player player, Claim claim,
                     Direction direction, int offset, Origin origin) {
        this.plugin = plugin;
        this.player = player;
        this.claim = claim;
        this.direction = direction;
        this.offset = offset;
        this.origin = origin;

        FileConfiguration lang = plugin.getLang();

        Location lesser = claim.getLesserBoundaryCorner();
        Location greater = claim.getGreaterBoundaryCorner();
        int currentWidth = greater.getBlockX() - lesser.getBlockX() + 1;
        int currentLength = greater.getBlockZ() - lesser.getBlockZ() + 1;
        int currentArea = currentWidth * currentLength;

        int maxExpand = calcMaxExpand(currentWidth, currentLength);
        int maxShrink = calcMaxShrink(lesser, greater, currentWidth, currentLength);
        int clampedOffset = Math.max(-maxShrink, Math.min(maxExpand, this.offset));

        int newWidth = currentWidth;
        int newLength = currentLength;
        switch (direction) {
            case NORTH, SOUTH -> newLength += clampedOffset;
            case EAST, WEST -> newWidth += clampedOffset;
        }
        int blockDelta = (newWidth * newLength) - currentArea;

        // Stepper buttons
        Item shrinkMax = createMaxItem(Material.RED_CONCRETE,
                lang.getString("gui.resize.shrink_max", "§c§l-MAX"), -maxShrink, maxExpand, maxShrink);
        Item shrink5 = createStepItem(Material.RED_STAINED_GLASS_PANE,
                lang.getString("gui.resize.shrink_5", "§c-5"), -5, maxExpand, maxShrink);
        Item shrink1 = createStepItem(Material.PINK_STAINED_GLASS_PANE,
                lang.getString("gui.resize.shrink_1", "§c-1"), -1, maxExpand, maxShrink);
        Item expand1 = createStepItem(Material.LIME_STAINED_GLASS_PANE,
                lang.getString("gui.resize.expand_1", "§a+1"), 1, maxExpand, maxShrink);
        Item expand5 = createStepItem(Material.GREEN_STAINED_GLASS_PANE,
                lang.getString("gui.resize.expand_5", "§a+5"), 5, maxExpand, maxShrink);
        Item expandMax = createMaxItem(Material.GREEN_CONCRETE,
                lang.getString("gui.resize.expand_max", "§a§l+MAX"), maxExpand, maxExpand, maxShrink);

        // Preview
        String directionName = getDirectionName(lang);
        String offsetStr = clampedOffset >= 0 ? "+" + clampedOffset : String.valueOf(clampedOffset);
        String costLine;
        if (blockDelta > 0) {
            costLine = lang.getString("gui.resize.preview_cost", "§cCost: §f%blocks% blocks")
                    .replace("%blocks%", String.valueOf(blockDelta));
        } else if (blockDelta < 0) {
            costLine = lang.getString("gui.resize.preview_refund", "§aRefund: §f%blocks% blocks")
                    .replace("%blocks%", String.valueOf(Math.abs(blockDelta)));
        } else {
            costLine = lang.getString("gui.resize.no_changes", "§7No changes");
        }

        Item previewItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.BOOK)
                        .setName(text(lang.getString("gui.resize.preview_name", "§e§lPreview")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text(lang.getString("gui.resize.preview_direction", "§eDirection: §f%dir% %offset%")
                                        .replace("%dir%", directionName)
                                        .replace("%offset%", offsetStr)),
                                text(lang.getString("gui.resize.preview_dimensions", "§eNew size: §f%width% × %length%")
                                        .replace("%width%", String.valueOf(newWidth))
                                        .replace("%length%", String.valueOf(newLength))),
                                text(costLine),
                                text("§8§m----------------------------"))))
                .build();

        // Confirm button — disabled when no change
        Item confirmItem;
        if (clampedOffset == 0) {
            confirmItem = Item.builder()
                    .setItemProvider(new ItemBuilder(Material.GRAY_WOOL)
                            .setName(text(lang.getString("gui.resize.no_changes", "§7No changes"))))
                    .build();
        } else {
            confirmItem = Item.builder()
                    .setItemProvider(new ItemBuilder(Material.LIME_WOOL)
                            .setName(text(lang.getString("gui.resize.confirm_name", "§a§lConfirm")))
                            .setLore(List.of(
                                    text("§8§m----------------------------"),
                                    text("§7Click to apply."),
                                    text("§8§m----------------------------"))))
                    .addClickHandler(click -> confirmResize(clampedOffset))
                    .build();
        }

        Item backItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.back", "§7Back"))))
                .addClickHandler(click -> new DirectionGUI(plugin, player, claim, origin).getWindow().open())
                .build();

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        Gui gui = Gui.builder()
                .setStructure(
                        "# a b c i d e f #",
                        "# # # # # # # # #",
                        "# # # C # # # B #"
                )
                .addIngredient('#', filler)
                .addIngredient('a', shrinkMax)
                .addIngredient('b', shrink5)
                .addIngredient('c', shrink1)
                .addIngredient('i', previewItem)
                .addIngredient('d', expand1)
                .addIngredient('e', expand5)
                .addIngredient('f', expandMax)
                .addIngredient('C', confirmItem)
                .addIngredient('B', backItem)
                .build();

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.resize.title_resize", "§6§lResize — %dir%")
                        .replace("%dir%", directionName)))
                .setUpperGui(gui)
                .setViewer(player)
                .build();
    }

    public Window getWindow() {
        return window;
    }

    private Item createStepItem(Material material, String name,
                                int step, int maxExpand, int maxShrink) {
        return Item.builder()
                .setItemProvider(new ItemBuilder(material).setName(text(name)))
                .addClickHandler(click -> {
                    int newOffset = Math.max(-maxShrink, Math.min(maxExpand, offset + step));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    new ResizeGUI(plugin, player, claim, direction, newOffset, origin).getWindow().open();
                })
                .build();
    }

    private Item createMaxItem(Material material, String name,
                               int targetOffset, int maxExpand, int maxShrink) {
        return Item.builder()
                .setItemProvider(new ItemBuilder(material).setName(text(name)))
                .addClickHandler(click -> {
                    int newOffset = Math.max(-maxShrink, Math.min(maxExpand, targetOffset));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    new ResizeGUI(plugin, player, claim, direction, newOffset, origin).getWindow().open();
                })
                .build();
    }

    private int calcMaxExpand(int width, int length) {
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        int remainingBlocks = playerData.getRemainingClaimBlocks();

        int oppositeAxisLength = switch (direction) {
            case NORTH, SOUTH -> width;
            case EAST, WEST -> length;
        };

        if (oppositeAxisLength <= 0) return 0;
        return remainingBlocks / oppositeAxisLength;
    }

    private int calcMaxShrink(Location lesser, Location greater, int width, int length) {
        int minWidth = GriefPrevention.instance.config_claims_minWidth;
        int minArea = GriefPrevention.instance.config_claims_minArea;

        boolean isNorthSouth = (direction == Direction.NORTH || direction == Direction.SOUTH);
        int axisLength = isNorthSouth ? length : width;
        int otherAxisLength = isNorthSouth ? width : length;

        int maxShrinkFromWidth = axisLength - minWidth;
        int minAxisForArea = (otherAxisLength > 0)
                ? (int) Math.ceil((double) minArea / otherAxisLength) : axisLength;
        int maxShrink = Math.min(maxShrinkFromWidth, axisLength - minAxisForArea);

        // Can't shrink past subclaims
        List<Claim> children = claim.children;
        if (children != null) {
            for (Claim child : children) {
                Location childLesser = child.getLesserBoundaryCorner();
                Location childGreater = child.getGreaterBoundaryCorner();
                int limit = calcSubclaimLimit(lesser, greater, childLesser, childGreater);
                maxShrink = Math.min(maxShrink, limit);
            }
        }

        return Math.max(0, maxShrink);
    }

    private int calcSubclaimLimit(Location lesser, Location greater,
                                  Location childLesser, Location childGreater) {
        return switch (direction) {
            case NORTH -> childLesser.getBlockZ() - lesser.getBlockZ();
            case SOUTH -> greater.getBlockZ() - childGreater.getBlockZ();
            case WEST -> childLesser.getBlockX() - lesser.getBlockX();
            case EAST -> greater.getBlockX() - childGreater.getBlockX();
        };
    }

    private void confirmResize(int finalOffset) {
        FileConfiguration lang = plugin.getLang();

        if (!player.getUniqueId().equals(claim.ownerID)) {
            sendError(lang.getString("gui.resize.error_not_owner", "§cThis claim is not yours!"));
            player.closeInventory();
            return;
        }

        // Verify claim still exists
        Claim currentClaim = GriefPrevention.instance.dataStore.getClaimAt(
                claim.getLesserBoundaryCorner(), false, null);
        if (currentClaim == null || !currentClaim.getID().equals(claim.getID())) {
            sendError(lang.getString("gui.resize.error_not_owner", "§cThis claim is not yours!"));
            player.closeInventory();
            return;
        }

        Location lesser = claim.getLesserBoundaryCorner();
        Location greater = claim.getGreaterBoundaryCorner();

        int newX1 = lesser.getBlockX();
        int newX2 = greater.getBlockX();
        int newZ1 = lesser.getBlockZ();
        int newZ2 = greater.getBlockZ();

        switch (direction) {
            case NORTH -> newZ1 -= finalOffset;
            case SOUTH -> newZ2 += finalOffset;
            case WEST -> newX1 -= finalOffset;
            case EAST -> newX2 += finalOffset;
        }

        int newWidth = newX2 - newX1 + 1;
        int newLength = newZ2 - newZ1 + 1;
        int minWidth = GriefPrevention.instance.config_claims_minWidth;
        int minArea = GriefPrevention.instance.config_claims_minArea;

        if (newWidth < minWidth || newLength < minWidth || newWidth * newLength < minArea) {
            sendError(lang.getString("gui.resize.error_too_small", "§cThe claim would be too small!"));
            return;
        }

        int blockCost = (newWidth * newLength) - claim.getArea();
        if (blockCost > 0) {
            PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
            int remaining = playerData.getRemainingClaimBlocks();
            if (remaining < blockCost) {
                sendError(lang.getString("gui.resize.error_blocks",
                                "§cNot enough blocks! Needed: %needed%, Available: %available%")
                        .replace("%needed%", String.valueOf(blockCost))
                        .replace("%available%", String.valueOf(remaining)));
                return;
            }
        }

        CreateClaimResult result = GriefPrevention.instance.dataStore.resizeClaim(
                claim, newX1, newX2,
                lesser.getWorld().getMinHeight(), lesser.getWorld().getMaxHeight(),
                newZ1, newZ2,
                player
        );

        if (result.succeeded) {
            player.sendMessage(text(lang.getString("gui.resize.success",
                            "§aClaim resized! New size: %width% × %length%")
                    .replace("%width%", String.valueOf(newWidth))
                    .replace("%length%", String.valueOf(newLength))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
            Claim resizedClaim = result.claim != null ? result.claim : claim;
            new ClaimInfoGUI(plugin, player, resizedClaim, origin).getWindow().open();
        } else {
            sendError(lang.getString("gui.resize.error_overlap",
                    "§cCannot resize — overlaps with another claim!"));
        }
    }

    private void sendError(String message) {
        player.sendMessage(text(message));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    private String getDirectionName(FileConfiguration lang) {
        return switch (direction) {
            case NORTH -> lang.getString("gui.resize.north", "§b§lNorth");
            case SOUTH -> lang.getString("gui.resize.south", "§b§lSouth");
            case EAST -> lang.getString("gui.resize.east", "§b§lEast");
            case WEST -> lang.getString("gui.resize.west", "§b§lWest");
        };
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }
}
