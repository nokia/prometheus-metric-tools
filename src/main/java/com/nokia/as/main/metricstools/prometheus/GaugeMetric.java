package com.nokia.as.main.metricstools.prometheus;

import io.prometheus.client.Gauge;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GaugeMetric {
    private Gauge gauge;
    private String name;
    private List<Label> labels;
    private List<String> labelNames;
    private List<String> labelValues;
    private String help;
    private Set<MetricRegistry> metricRegistries;

    private boolean mutable;
    private Gauge tsGauge;
    private List<Label> mutableLabels;
    private List<String> mutableLabelNames;
    private List<String> mutableLabelValues;
    private List<Label> noMutableLabels;
    private List<String> noMutableLabelNames;
    private List<String> noMutableLabelValues;

    public GaugeMetric(String name, List<Label> labels) {
        this.name = name;
        this.labels = labels;
        this.mutable = isOneLabelMutable();
        Label mutableLabel = this.mutable ? new Label("mutable", "true")
                : new Label("mutable", "false");
        this.labels.add(mutableLabel);
        this.labelNames = labels.stream().map(Label::getName).collect(Collectors.toList());
        this.labelValues = labels.stream().map(Label::getValue).collect(Collectors.toList());
        this.help = "Gauge metric: " + this.name;
        this.gauge = Gauge.build()
                .name(toValidName(this.name))
                .help(this.help)
                .labelNames(this.labelNames.toArray(String[]::new))
                .create();
        this.metricRegistries = new HashSet<>();

        this.mutableLabels = new ArrayList<>();
        this.noMutableLabels = this.labels.stream()
                .filter(label -> !label.isMutable() && !label.getName().equals("mutable"))
                .collect(Collectors.toList());
        this.noMutableLabels.add(new Label("mutable", "false"));
        this.noMutableLabelNames = this.noMutableLabels.stream().map(Label::getName).collect(Collectors.toList());
        this.noMutableLabelValues = this.noMutableLabels.stream().map(Label::getValue).collect(Collectors.toList());
        if (this.mutable) {
            this.mutableLabels = this.labels.stream()
                    .filter(label -> label.isMutable() && !label.getName()
                            .equals("mutable")).collect(Collectors.toList());
            this.mutableLabelNames = this.mutableLabels.stream().map(Label::getName).collect(Collectors.toList());
            this.mutableLabelNames = this.mutableLabels.stream().map(Label::getValue).collect(Collectors.toList());
            this.tsGauge = Gauge.build()
                    .name(toValidName(this.name + "_ts"))
                    .help(this.help + " --> No mutable replica of gauge metric " + this.name)
                    .labelNames(this.noMutableLabelNames.toArray(String[]::new))
                    .create();
        }
    }

    public GaugeMetric(String name, List<Label> labels, String help) {
        this(name, labels);
        this.help = help;
    }

    public Gauge getGauge() {
        return gauge;
    }

    public List<Label> getLabels() {
        return labels;
    }

    private void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public String getHelp() {
        return help;
    }

    public String getName() {
        return name;
    }

    public List<String> getLabelNames() {
        return labelNames;
    }

    public List<String> getLabelValues() {
        return labelValues;
    }

    private void setLabelNames(List<String> labelNames) {
        this.labelNames = labelNames;
    }

    private void setLabelValues(List<String> labelValues) {
        this.labelValues = labelValues;
    }

    public List<Label> getNoMutableLabels() {
        return noMutableLabels;
    }

    public List<String> getNoMutableLabelNames() {
        return noMutableLabelNames;
    }

    public List<String> getNoMutableLabelValues() {
        return noMutableLabelValues;
    }

    public List<Label> getMutableLabels() {
        return mutableLabels;
    }

    public List<String> getMutableLabelNames() {
        return mutableLabelNames;
    }

    public List<String> getMutableLabelValues() {
        return mutableLabelValues;
    }

    public Gauge getTsGauge() {
        return tsGauge;
    }

    public Set<MetricRegistry> getMetricRegistries() {
        return metricRegistries;
    }

    public void setGauge(Gauge gauge) {
        this.gauge = gauge;
    }

    public void set(Double value) {
        gauge.labels(labelValues.toArray(String[]::new)).set(value);
        if (mutable) {
            tsGauge.labels(noMutableLabelValues.toArray(String[]::new)).set(value);
        }
    }

    public void set(Integer value) {
        gauge.labels(labelValues.toArray(String[]::new)).set(value);
        if (mutable) {
            tsGauge.labels(noMutableLabelValues.toArray(String[]::new)).set(value);
        }
    }

    public Label getLabel(String labelName) {
        return labels.stream().filter(label -> labelName.equals(label.getName()))
                .findFirst()
                .orElse(null);
    }

    public void addMetricRegistry(MetricRegistry metricRegistry) {
        metricRegistries.add(metricRegistry);
    }

    public void removeMetricRegistry(MetricRegistry metricRegistry) {
        metricRegistries.remove(metricRegistry);
    }

    public static String toValidName(String name) {
        name = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        name = name.toLowerCase();
        name = name.replaceAll("-", "_")
                .replaceAll("\\.", "_")
                .replaceAll(",", "_")
                .replaceAll(";", "_")
                .replaceAll("/", "_")
                .replaceAll("\\s+", "");
        return name;
    }

    private void updateLabel(String labelName, String labelValue) {
        List<Label> labels = this.getLabels().stream()
                .filter(l -> !l.getName().equals(labelName)).collect(Collectors.toList());
        labels.add(new Label(labelName, labelValue, true));

        List<String> labelNames = labels.stream().map(Label::getName).collect(Collectors.toList());
        Gauge gauge = Gauge.build()
                .name(toValidName(this.name))
                .help(this.help)
                .labelNames(labelNames.toArray(String[]::new))
                .create();

        Gauge previousGauge = this.gauge;
        this.gauge = gauge;
        this.labels = labels;
        this.labelNames = labels.stream().map(Label::getName).collect(Collectors.toList());
        this.labelValues = labels.stream().map(Label::getValue).collect(Collectors.toList());

        Set<MetricRegistry> metricRegistries = this.metricRegistries;
        for (MetricRegistry metricRegistry : metricRegistries) {
            metricRegistry.update(previousGauge, this);
        }
    }

    public void setLabel(String labelName, String labelValue) {
        Label label = this.getLabel(labelName);
        if (label != null && !label.getValue().equals(labelValue) && label.isMutable()) {
            this.updateLabel(label.getName(), labelValue);
        }
    }

    public boolean isOneLabelMutable() {
        for (Label label : labels) {
            if (label.isMutable()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMutable() {
        return mutable;
    }
}
