package org.hope.griefPreventionEasyGUI.listeners;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;

import java.util.Collection;

public class ClaimVisualizer {
    private static final Particle.DustOptions CORNER_DUST =
            new Particle.DustOptions(Color.fromRGB(255, 170, 0), 1.2f);
    private static final Particle.DustOptions LINE_DUST =
            new Particle.DustOptions(Color.fromRGB(0, 200, 0), 0.8f);

    private final GriefPreventionEasyGUI plugin;

    public ClaimVisualizer(GriefPreventionEasyGUI plugin) {
        this.plugin = plugin;
    }

    public void renderForPlayer(Player player) {
        Location eyeLocation = player.getEyeLocation();
        World world = eyeLocation.getWorld();
        int playerX = eyeLocation.getBlockX();
        int playerZ = eyeLocation.getBlockZ();
        double playerY = eyeLocation.getY();
        int radius = plugin.getConfig().getInt("visualizer-radius", 60);
        int maxParticles = plugin.getConfig().getInt("visualizer-max-particles", 1000);
        int wallHeight = plugin.getConfig().getInt("visualizer-wall-height", 2);
        int radiusSq = radius * radius;

        Collection<Claim> allClaims = GriefPrevention.instance.dataStore.getClaims();
        if (allClaims == null || allClaims.isEmpty()) return;

        int particleCount = 0;

        for (Claim claim : allClaims) {
            if (particleCount >= maxParticles) break;
            if (claim.parent != null) continue;

            Location lesser = claim.getLesserBoundaryCorner();
            Location greater = claim.getGreaterBoundaryCorner();
            if (lesser == null || greater == null) continue;
            if (!world.equals(lesser.getWorld())) continue;

            int x1 = lesser.getBlockX();
            int z1 = lesser.getBlockZ();
            int x2 = greater.getBlockX();
            int z2 = greater.getBlockZ();

            // Skip if no part of the claim is within radius
            int nearestX = Math.max(x1, Math.min(playerX, x2));
            int nearestZ = Math.max(z1, Math.min(playerZ, z2));
            int dx = playerX - nearestX;
            int dz = playerZ - nearestZ;
            if (dx * dx + dz * dz > radiusSq) continue;

            // Corners
            particleCount = spawnCorner(player, x1, z1, playerX, playerZ, playerY, radiusSq, maxParticles, wallHeight, particleCount);
            particleCount = spawnCorner(player, x2, z1, playerX, playerZ, playerY, radiusSq, maxParticles, wallHeight, particleCount);
            particleCount = spawnCorner(player, x1, z2, playerX, playerZ, playerY, radiusSq, maxParticles, wallHeight, particleCount);
            particleCount = spawnCorner(player, x2, z2, playerX, playerZ, playerY, radiusSq, maxParticles, wallHeight, particleCount);
            if (particleCount >= maxParticles) break;

            // Edges
            particleCount = spawnEdge(player, x1, z1, x2, z1, playerX, playerZ, playerY, radiusSq, maxParticles, wallHeight, particleCount);
            particleCount = spawnEdge(player, x1, z2, x2, z2, playerX, playerZ, playerY, radiusSq, maxParticles, wallHeight, particleCount);
            particleCount = spawnEdge(player, x1, z1, x1, z2, playerX, playerZ, playerY, radiusSq, maxParticles, wallHeight, particleCount);
            particleCount = spawnEdge(player, x2, z1, x2, z2, playerX, playerZ, playerY, radiusSq, maxParticles, wallHeight, particleCount);
        }
    }

    private int spawnCorner(Player player, int x, int z,
                            int playerX, int playerZ, double playerY,
                            int radiusSq, int maxParticles, int wallHeight, int particleCount) {
        if (particleCount >= maxParticles) return particleCount;
        if (!isWithinRadius(x, z, playerX, playerZ, radiusSq)) return particleCount;
        if (!player.getWorld().isChunkLoaded(x >> 4, z >> 4)) return particleCount;

        for (int i = -1; i <= wallHeight && particleCount < maxParticles; i++) {
            player.spawnParticle(Particle.DUST, x + 0.5, playerY + i, z + 0.5, 1, CORNER_DUST);
            particleCount++;
        }
        return particleCount;
    }

    private int spawnEdge(Player player, int x1, int z1, int x2, int z2,
                          int playerX, int playerZ, double playerY,
                          int radiusSq, int maxParticles, int wallHeight, int particleCount) {
        boolean isXEdge = (z1 == z2);
        int start = isXEdge ? Math.min(x1, x2) : Math.min(z1, z2);
        int end = isXEdge ? Math.max(x1, x2) : Math.max(z1, z2);

        if (end - start <= 1) return particleCount;

        for (int i = start + 1; i < end && particleCount < maxParticles; i++) {
            int px = isXEdge ? i : x1;
            int pz = isXEdge ? z1 : i;

            if (!isWithinRadius(px, pz, playerX, playerZ, radiusSq)) continue;
            if (!player.getWorld().isChunkLoaded(px >> 4, pz >> 4)) continue;

            for (int h = 0; h < wallHeight && particleCount < maxParticles; h++) {
                player.spawnParticle(Particle.DUST, px + 0.5, playerY + h, pz + 0.5, 1, LINE_DUST);
                particleCount++;
            }
        }
        return particleCount;
    }

    private boolean isWithinRadius(int x, int z, int playerX, int playerZ, int radiusSq) {
        int dx = x - playerX;
        int dz = z - playerZ;
        return dx * dx + dz * dz <= radiusSq;
    }
}
