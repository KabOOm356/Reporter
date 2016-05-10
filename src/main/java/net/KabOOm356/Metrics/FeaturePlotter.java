package net.KabOOm356.Metrics;

import org.mcstats.Metrics.Plotter;

/**
 * A {@link Plotter} that simply increments if a feature is active.
 */
public class FeaturePlotter extends Plotter {
	public FeaturePlotter(final String featureName) {
		super(featureName);
	}

	@Override
	public int getValue() {
		return 1;
	}
}
