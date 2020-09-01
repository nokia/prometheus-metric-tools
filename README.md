# Prometheus Metrics Tools for Java

## Description

This is just a set of tools on top of the [Prometheus JVM Client](https://github.com/prometheus/client_java#labels).

The goal is to simplify the use of the client and add some features.

This project is currently focused on the Gauge metric collectors, particularly on the labels.

### Metric Registry definition

```
MetricRegistry metricRegistry = app.getMetricRegistry();
```

### Gauges and Labels definition

Firstly, the labels definition is now clearer and easier to do.

Then:

```
Gauge gauge = Gauge.build()
    .name("name")
    .help("help")
    .labelNames("label1", "label2", "label3")
    .create();
    
gauge.labels("value1", "value2", "value3");

```

Now:

```
GaugeMetric gaugeMetric = new GaugeMetric(
                        "name",
                        new ArrayList<>(Arrays.asList(
                                new Label("label1", "value1"),
                                new Label("label2", "value2"),
                                new Label("label3", "value3")
                        )),
                        "help");
```

### Labels mutability

A metric label isn't mutable, as it's part of the metric collector itself.

The role of a metric collector is to follow the evolution of a metric, not of the labels.

The disadvantage with this concept is that sometimes it could be great to keep some metadata relative to a metric, without to store it in another database.
So here's a trick to store metadata in a TSDB like Prometheus: the label mutalibity.

If we use this feature in order to set a new value to a label, a copy of the metric collector will be done, with the new label values.
The original remains unchanged and still good to use to follow the metric, and the other one can be used to follow the evolution of mutable labels.

This idea came up when we tried to display changeable metadata in a monitoring system like Grafana without going through anything other than Prometheus.

### Set gauge

```
gaugeMetric.set(2);
```

### Register metric

```
metricRegistry.register(gaugeMetric);
```

### Use example

Here's an example, let's say we cant to follow the status of Jira issues.

The full code example is in the src/main/java/com/nokia/as/main/Main.java file.

Basically, without the mutable labels, the only way to follow and display the status was to give up all data that may change (for example the assignee of the issue).

![Screenshot_from_2020-08-25_17-33-08](https://user-images.githubusercontent.com/10490998/91873044-e0cd4100-ec78-11ea-9115-7ed91e0940f0.png)

But now, we can also follow and display this data.

![Screenshot_from_2020-08-25_17-32-17](https://user-images.githubusercontent.com/10490998/91873070-e62a8b80-ec78-11ea-8f8c-c4530b8707b5.png)

```
jiraIssues.get(0).getGaugeMetric().setLabel("assignee", "aracely");
```

![Screenshot_from_2020-08-25_17-32-04](https://user-images.githubusercontent.com/10490998/91873081-e75bb880-ec78-11ea-8bfd-1c2c7701edb4.png)
