package com.rojel.wesv;

import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

public class CustomMetrics {
    private final JavaPlugin plugin;
    private final Configuration config;

    public CustomMetrics(JavaPlugin plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void initMetrics() {
        try {
            Metrics metrics = new Metrics(plugin);

            Metrics.Graph cuboidGraph = metrics.createGraph("Horizontal lines for cuboid selections");
            cuboidGraph.addPlotter(new Metrics.Plotter(config.cuboidLines() ? "Enabled" : "Disabled") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph polygonGraph = metrics.createGraph("Horizontal lines for polygon selections");
            polygonGraph.addPlotter(new Metrics.Plotter(config.polygonLines() ? "Enabled" : "Disabled") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph cylinderGraph = metrics.createGraph("Horizontal lines for cylinder selections");
            cylinderGraph.addPlotter(new Metrics.Plotter(config.cylinderLines() ? "Enabled" : "Disabled") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph ellipsoidGraph = metrics.createGraph("Horizontal lines for ellipsoid selections");
            ellipsoidGraph.addPlotter(new Metrics.Plotter(config.ellipsoidLines() ? "Enabled" : "Disabled") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph pointGapGraph = metrics.createGraph("Gap between points");
            pointGapGraph.addPlotter(new Metrics.Plotter(config.gapBetweenPoints() + "") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph verticalGapGraph = metrics.createGraph("Vertical gap between horizontal filling lines");
            verticalGapGraph.addPlotter(new Metrics.Plotter(config.verticalGap() + "") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph particleIntervalGraph = metrics.createGraph("Particle update interval");
            particleIntervalGraph.addPlotter(new Metrics.Plotter(config.updateParticlesInterval() + "") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph selectionIntervalGraph = metrics.createGraph("Selection update interval");
            selectionIntervalGraph.addPlotter(new Metrics.Plotter(config.updateSelectionInterval() + "") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph particleEffectGraph = metrics.createGraph("Particle effect");
            particleEffectGraph.addPlotter(new Metrics.Plotter(config.particle().getName()) {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph protocolLibGraph = metrics.createGraph("Use ProtocolLib");
            protocolLibGraph.addPlotter(new Metrics.Plotter(config.useProtocolLib() ? "Enabled" : "Disabled") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            Metrics.Graph particleDistanceGraph = metrics.createGraph("Particle distance");
            particleDistanceGraph.addPlotter(new Metrics.Plotter(config.particleDistance() + "") {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            metrics.start();
        } catch (IOException e) {
            plugin.getLogger().info("Unable to submit statistics to MCStats :(");
        }
    }
}
