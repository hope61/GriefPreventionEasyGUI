package org.hope.griefPreventionEasyGUI.GUIs;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;
import org.hope.griefPreventionEasyGUI.listeners.TeleportManager;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class ClaimInfoGUI {
    private final GriefPreventionEasyGUI plugin;
    private final Player player;
    private final Claim claim;
    private final Window window;

    public ClaimInfoGUI(GriefPreventionEasyGUI plugin, Player player, Claim claim, Origin origin) {
        this.plugin = plugin;
        this.player = player;
        this.claim = claim;

        FileConfiguration lang = plugin.getLang();

        Location lesser = claim.getLesserBoundaryCorner();
        Location greater = claim.getGreaterBoundaryCorner();
        int xWidth = claim.getWidth();
        int zLength = greater.getBlockZ() - lesser.getBlockZ() + 1;
        int area = claim.getArea();

        // Claim info item
        Item infoItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.BOOK)
                        .setName(text(lang.getString("gui.claiminfo.info_name", "§e§lClaim Info")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§eSize: §f" + xWidth + " × " + zLength),
                                text("§eArea: §f" + area + " blocks"),
                                text("§eCoords: §f(" + lesser.getBlockX() + ", " + lesser.getBlockZ() + ")"),
                                text("§8§m----------------------------"))))
                .build();

        // Trusted players info
        ArrayList<String> buildList = new ArrayList<>(), containerList = new ArrayList<>(),
                accessList = new ArrayList<>(), manageList = new ArrayList<>();
        claim.getPermissions(buildList, containerList, accessList, manageList);
        int totalTrusted = buildList.size() + containerList.size() + accessList.size() + manageList.size();

        Item trustedInfoItem = Item.builder()
                .setItemProvider(new ItemBuilder(Material.PLAYER_HEAD)
                        .setName(text(lang.getString("gui.claiminfo.trusted_name", "§b§lTrusted Players")))
                        .setLore(List.of(
                                text("§8§m----------------------------"),
                                text("§eTrusted: §f" + totalTrusted),
                                text("§7Click to manage"),
                                text("§8§m----------------------------"))))
                .addClickHandler(click -> {
                    if (!isOwner()) return;
                    new TrustGUI(plugin, player, claim, origin).getWindow().open();
                })
                .build();

        FileConfiguration config = plugin.getConfig();

        // Action row — dynamically built based on enabled features
        List<Item> actionItems = new ArrayList<>();
        char[] actionChars = {'A', 'B', 'D', 'E', 'F'};

        // Teleport button
        if (config.getBoolean("teleport-enabled", true)) {
            int delay = config.getInt("teleport-delay-seconds", 3);
            actionItems.add(Item.builder()
                    .setItemProvider(new ItemBuilder(Material.ENDER_PEARL)
                            .setName(text(lang.getString("gui.claiminfo.teleport_name", "§d§lTeleport")))
                            .setLore(List.of(
                                    text("§8§m----------------------------"),
                                    text("§7Teleport to the claim."),
                                    text("§eDelay: §f" + delay + "s"),
                                    text("§8§m----------------------------"))))
                    .addClickHandler(click -> {
                        if (!hasTrust()) return;
                        startTeleport(lesser, greater, config.getInt("teleport-delay-seconds", 3));
                    })
                    .build());
        }

        // Resize button (owner-only)
        if (config.getBoolean("resize-enabled", true)) {
            actionItems.add(Item.builder()
                    .setItemProvider(new ItemBuilder(Material.GOLDEN_SHOVEL)
                            .setName(text(lang.getString("gui.claiminfo.resize_name", "§6§lResize Claim")))
                            .setLore(List.of(
                                    text("§8§m----------------------------"),
                                    text("§7Expand or shrink the claim."),
                                    text("§8§m----------------------------"))))
                    .addClickHandler(click -> {
                        if (!isOwner()) return;
                        new DirectionGUI(plugin, player, claim, origin).getWindow().open();
                    })
                    .build());
        }

        // Trusted players
        actionItems.add(trustedInfoItem);

        // Bottom row
        List<Item> bottomItems = new ArrayList<>();
        char[] bottomChars = {'x', 'y', 'z'};

        // Abandon button
        if (config.getBoolean("abandon-enabled", true)) {
            bottomItems.add(Item.builder()
                    .setItemProvider(new ItemBuilder(Material.BARRIER)
                            .setName(text(lang.getString("gui.claiminfo.abandon_name", "§c§lAbandon Claim")))
                            .setLore(List.of(
                                    text("§8§m----------------------------"),
                                    text("§7Deletes this claim permanently."),
                                    text("§8§m----------------------------"))))
                    .addClickHandler(click -> {
                        if (!isOwner()) return;
                        new ConfirmAbandonGUI(plugin, player, claim, ConfirmAbandonOrigin.CLAIM_INFO, origin)
                                .getWindow().open();
                    })
                    .build());
        }

        // Back button
        bottomItems.add(Item.builder()
                .setItemProvider(new ItemBuilder(Material.ARROW)
                        .setName(text(lang.getString("gui.back", "§7Back"))))
                .addClickHandler(click -> {
                    if (origin == Origin.LIST) {
                        new ClaimListGUI(plugin, player).getWindow().open();
                    } else {
                        new HelpMenuGUI(plugin, player).getWindow().open();
                    }
                })
                .build());

        Item filler = Item.builder()
                .setItemProvider(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName(Component.text(" ")))
                .build();

        String actionRow = centeredRow(actionItems.size(), actionChars);
        String bottomRow = centeredRow(bottomItems.size(), bottomChars);

        Gui.Builder<?, ?> guiBuilder = Gui.builder()
                .setStructure(
                        "# # # # i # # # #",
                        actionRow,
                        "# # # # # # # # #",
                        bottomRow
                )
                .addIngredient('#', filler)
                .addIngredient('i', infoItem);

        for (int idx = 0; idx < actionItems.size(); idx++) guiBuilder.addIngredient(actionChars[idx], actionItems.get(idx));
        for (int idx = 0; idx < bottomItems.size(); idx++) guiBuilder.addIngredient(bottomChars[idx], bottomItems.get(idx));

        Gui gui = guiBuilder.build();

        this.window = Window.builder()
                .setTitle(text(lang.getString("gui.claiminfo.title", "§6§lClaim Info")))
                .setUpperGui(gui)
                .setViewer(player)
                .build();
    }

    public Window getWindow() {
        return window;
    }

    private void startTeleport(Location lesser, Location greater, int delay) {
        FileConfiguration lang = plugin.getLang();
        player.closeInventory();

        // Check for stored teleport location (saved when claim was created via AutoClaim)
        String claimId = String.valueOf(claim.getID());
        String storedWorld = plugin.getCooldowns().getString("claim-tp." + claimId + ".world");
        Location destination;

        if (storedWorld != null) {
            World world = plugin.getServer().getWorld(storedWorld);
            if (world != null) {
                double x = plugin.getCooldowns().getDouble("claim-tp." + claimId + ".x");
                double y = plugin.getCooldowns().getDouble("claim-tp." + claimId + ".y");
                double z = plugin.getCooldowns().getDouble("claim-tp." + claimId + ".z");
                destination = new Location(world, x, y, z);
            } else {
                player.sendMessage(text(lang.getString("gui.messages.teleport_unsafe",
                        "§cCannot teleport there!")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
        } else {
            // Fallback: teleport to surface at claim center
            int centerX = (lesser.getBlockX() + greater.getBlockX()) / 2;
            int centerZ = (lesser.getBlockZ() + greater.getBlockZ()) / 2;
            World world = lesser.getWorld();
            int surfaceY = world.getHighestBlockYAt(centerX, centerZ);

            if (surfaceY < world.getMinHeight()) {
                player.sendMessage(text(lang.getString("gui.messages.teleport_unsafe",
                        "§cCannot teleport there!")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            destination = new Location(world, centerX + 0.5, surfaceY + 1, centerZ + 0.5);
        }

        player.sendMessage(text(lang.getString("gui.messages.teleport_wait",
                        "§eTeleporting in %delay% seconds. Don't move!")
                .replace("%delay%", String.valueOf(delay))));

        new TeleportManager(plugin, player, destination, delay,
                lang.getString("gui.messages.teleport_cancelled", "§cTeleport cancelled!"),
                lang.getString("gui.messages.teleport_success", "§aTeleported!"));
    }

    private boolean isOwner() {
        if (!player.getUniqueId().equals(claim.ownerID)) {
            player.sendMessage(text(plugin.getLang().getString("gui.messages.not_your_claim", "§cThis claim is not yours!")));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return false;
        }
        return true;
    }

    private boolean hasTrust() {
        if (player.getUniqueId().equals(claim.ownerID)) return true;
        if (claim.hasExplicitPermission(player, ClaimPermission.Access)) return true;
        player.sendMessage(text(plugin.getLang().getString("gui.messages.not_your_claim", "§cThis claim is not yours!")));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        return false;
    }

    private String centeredRow(int count, char[] chars) {
        return switch (count) {
            case 1 -> "# # # # " + chars[0] + " # # # #";
            case 2 -> "# # # " + chars[0] + " # " + chars[1] + " # # #";
            case 3 -> "# # " + chars[0] + " # " + chars[1] + " # " + chars[2] + " # #";
            case 4 -> "# " + chars[0] + " # " + chars[1] + " # " + chars[2] + " # " + chars[3] + " #";
            default -> "# # # # # # # # #";
        };
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s);
    }
}
