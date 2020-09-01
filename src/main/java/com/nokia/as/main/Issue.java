package com.nokia.as.main;

import com.nokia.as.main.metricstools.prometheus.GaugeMetric;

public class Issue {

    private String id;
    private String key;
    private String reporter;
    private String assignee;
    private int status;

    private GaugeMetric gaugeMetric;

    public Issue(String id,
                 String key,
                 String reporter,
                 String assignee,
                 int status) {
        this.id = id;
        this.key = key;
        this.reporter = reporter;
        this.assignee = assignee;
        this.status = status;
    }


    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getReporter() {
        return reporter;
    }

    public String getAssignee() {
        return assignee;
    }

    public int getStatus() {
        return status;
    }

    public GaugeMetric getGaugeMetric() {
        return gaugeMetric;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
        if (this.gaugeMetric != null) {
            this.gaugeMetric.set(status);
        }
    }

    public void setStatus(int status) {
        this.status = status;
        this.gaugeMetric.set(status);
    }

    public void setGaugeMetric(GaugeMetric gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }
}
