package com.rojel.wesv;

import org.mcstats.Metrics;

import java.io.IOException;

public class CustomMetrics {
    private WorldEditSelectionVisualizer plugin;

    public CustomMetrics(WorldEditSelectionVisualizer plugin) {
        this.plugin = plugin;
    }

    public void initMetrics() {
        final Configuration config = plugin.config();
        try {
            Metrics metrics = new Metrics(plugin);

            Metrics.Graph cuboidGraph = metrics.createGraph("Horizontal lines for cuboid selections");
            cuboidGraph.addPlotter(new Metrics.Plotter("Enabled") {
                @Override
                public int getValue() {
                    if (config.cuboidLines())
                        return 1;
                    else
                        return 0;
                }
            });
            cuboidGraph.addPlotter(new Metrics.Plotter("Disabled") {
                @Override
                public int getValue() {
                    if (!config.cuboidLines())
                        return 1;
                    else
                        return 0;
                }
            });

            Metrics.Graph polygonGraph = metrics.createGraph("Horizontal lines for polygon selections");
            polygonGraph.addPlotter(new Metrics.Plotter("Enabled") {
                @Override
                public int getValue() {
                    if (config.polygonLines())
                        return 1;
                    else
                        return 0;
                }
            });
            polygonGraph.addPlotter(new Metrics.Plotter("Disabled") {
                @Override
                public int getValue() {
                    if (!config.polygonLines())
                        return 1;
                    else
                        return 0;
                }
            });

            Metrics.Graph cylinderGraph = metrics.createGraph("Horizontal lines for cylinder selections");
            cylinderGraph.addPlotter(new Metrics.Plotter("Enabled") {
                @Override
                public int getValue() {
                    if (config.cylinderLines())
                        return 1;
                    else
                        return 0;
                }
            });
            cylinderGraph.addPlotter(new Metrics.Plotter("Disabled") {
                @Override
                public int getValue() {
                    if (!config.cylinderLines())
                        return 1;
                    else
                        return 0;
                }
            });

            Metrics.Graph ellipsoidGraph = metrics.createGraph("Horizontal lines for ellipsoid selections");
            ellipsoidGraph.addPlotter(new Metrics.Plotter("Enabled") {
                @Override
                public int getValue() {
                    if (config.ellipsoidLines())
                        return 1;
                    else
                        return 0;
                }
            });
            ellipsoidGraph.addPlotter(new Metrics.Plotter("Disabled") {
                @Override
                public int getValue() {
                    if (!config.ellipsoidLines())
                        return 1;
                    else
                        return 0;
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

            metrics.start();
        } catch (IOException e) {
            plugin.getLogger().info("Unable to submit statistics to MCStats :(");
        }
    }
}
