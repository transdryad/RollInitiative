package org.hazelv.RollInitiative;

import net.minestom.server.instance.block.Block;

public class WorldBlock {
    Block block;
    int noiseScale;
    boolean noiseEnabled;
    double threshold;
    double minHeight;
    double maxHeight;

    public WorldBlock(Block block, int noiseScale, boolean noiseEnabled, double threshold, double minHeight, double maxHeight) {
        this.block = block;
        this.noiseScale = noiseScale;
        this.noiseEnabled = noiseEnabled;
        this.threshold = threshold;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }
}
