package com.rojel.wesv;

import com.darkblade12.particleeffect.ParticleEffect;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Configuration {
    private JavaPlugin plugin;
    private FileConfiguration config;

    private ParticleEffect particle;
    private double gapBetweenPoints;
    private double verticalGap;
    private int updateParticlesInterval;
    private int updateSelectionInterval;
    private boolean cuboidLines;
    private boolean polygonLines;
    private boolean cylinderLines;
    private boolean ellipsoidLines;
    private boolean checkForAxe;
    private Material selectionItem;
    private int particleDistance;
    private boolean useProtocolLib;
    private int maxSize;

    public Configuration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        config.options().copyDefaults(true);

        particle = getParticleEffect(config.getString("particleEffect"));
        gapBetweenPoints = config.getDouble("gapBetweenPoints");
        verticalGap = config.getDouble("verticalGap");
        updateParticlesInterval = config.getInt("updateParticlesInterval");
        updateSelectionInterval = config.getInt("updateSelectionInterval");

        cuboidLines = config.getBoolean("horizontalLinesForCuboid");
        polygonLines = config.getBoolean("horizontalLinesForPolygon");
        cylinderLines = config.getBoolean("horizontalLinesForCylinder");
        ellipsoidLines = config.getBoolean("horizontalLinesForEllipsoid");

        checkForAxe = config.getBoolean("checkForAxe");
        selectionItem = getSelectionItem(config.getString("selectionItem"));

        particleDistance = config.getInt("particleDistance");
        useProtocolLib = config.getBoolean("useProtocolLib");
        maxSize = config.getInt("maxSize");
    }

    public ParticleEffect getParticleEffect(String name) {
        ParticleEffect effect = ParticleEffect.fromName(name);
        if (effect != null)
            return effect;

        plugin.getLogger().warning("The particle effect set in the configuration file is invalid.");

        return ParticleEffect.REDSTONE;
    }

    public Material getSelectionItem(String name) {
        Material selectionItem = Material.getMaterial(name);
        if (selectionItem != null)
            return selectionItem;

        plugin.getLogger().warning("The selection item set in the configuration file is invalid.");

        return Material.WOOD_AXE;
    }

    public boolean isEnabled(Player player) {
        String path = "players." + player.getUniqueId().toString();
        config.addDefault(path, true);

        return config.getBoolean(path);
    }

    public void setEnabled(Player player, boolean enabled) {
        config.set("players." + player.getUniqueId().toString(), enabled);
        plugin.saveConfig();
    }

    public ParticleEffect particle() {
        return particle;
    }

    public double gapBetweenPoints() {
        return gapBetweenPoints;
    }

    public double verticalGap() {
        return verticalGap;
    }

    public int updateParticlesInterval() {
        return updateParticlesInterval;
    }

    public int updateSelectionInterval() {
        return updateSelectionInterval;
    }

    public boolean cuboidLines() {
        return cuboidLines;
    }

    public boolean polygonLines() {
        return polygonLines;
    }

    public boolean cylinderLines() {
        return cylinderLines;
    }

    public boolean ellipsoidLines() {
        return ellipsoidLines;
    }

    public boolean checkForAxe() {
        return checkForAxe;
    }

    public Material selectionItem() {
        return selectionItem;
    }

    public boolean useProtocolLib() {
        return useProtocolLib;
    }

    public int particleDistance() {
        return particleDistance;
    }

    public int maxSize() {
        return maxSize;
    }
}
