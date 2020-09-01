package com.nokia.as.main.metricstools.prometheus;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

public class MetricRegistry {
    private CollectorRegistry collectorRegistry;

    public MetricRegistry() {
        this.collectorRegistry = new CollectorRegistry();
    }

    public CollectorRegistry getCollectorRegistry() {
        return collectorRegistry;
    }

    public void register(GaugeMetric gaugeMetric) {
        Gauge gauge = gaugeMetric.getGauge();

        collectorRegistry.register(gauge);
        gaugeMetric.addMetricRegistry(this);

        gauge.labels(gaugeMetric.getLabelValues().toArray(String[]::new));

        if (gaugeMetric.isMutable()) {
            Gauge tsGauge = gaugeMetric.getTsGauge();
            collectorRegistry.register(tsGauge);
            tsGauge.labels(gaugeMetric.getNoMutableLabelValues().toArray(String[]::new));
        }
    }

    public void unregister(GaugeMetric gaugeMetric) {
        Gauge gauge = gaugeMetric.getGauge();

        collectorRegistry.unregister(gauge);
        gaugeMetric.removeMetricRegistry(this);

        if (gaugeMetric.isMutable()) {
            Gauge tsGauge = gaugeMetric.getTsGauge();
            collectorRegistry.unregister(tsGauge);
        }
    }

    public void update(Gauge previousGauge, GaugeMetric gaugeMetric) {
        Gauge gauge = gaugeMetric.getGauge();
        collectorRegistry.unregister(previousGauge);
        collectorRegistry.register(gauge);
        gauge.labels(gaugeMetric.getLabelValues().toArray(String[]::new));
    }

    public void clear() {
        collectorRegistry.clear();
    }
}
