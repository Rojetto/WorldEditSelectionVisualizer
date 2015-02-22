package com.rojel.wesv;

import com.darkblade12.particleeffect.ParticleEffect;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class Configuration {
    private WorldEditSelectionVisualizer plugin;
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

    public Configuration(WorldEditSelectionVisualizer plugin) {
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
        selectionItem = Material.getMaterial(config.getString("selectionItem"));
        if (selectionItem == null)
            selectionItem = Material.WOOD_AXE;
    }

    public ParticleEffect getParticleEffect(String name) {
        Class particleEffectClass = ParticleEffect.class;
        Field[] fields = particleEffectClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.getName().replaceAll("[^a-zA-Z0-9]", "").equalsIgnoreCase(name.replaceAll("[^a-zA-Z0-9]", ""))) {
                Object fieldContent = null;
                try {
                    fieldContent = field.get(particleEffectClass);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (fieldContent != null && fieldContent instanceof ParticleEffect) {
                    return (ParticleEffect) fieldContent;
                }
            }
        }

        plugin.getLogger().warning("The particle effect set in the configuration file is invalid.");

        return ParticleEffect.REDSTONE;
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
}
