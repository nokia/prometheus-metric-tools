package com.nokia.as.main;

import com.nokia.as.main.jetty.App;
import com.nokia.as.main.metricstools.prometheus.GaugeMetric;
import com.nokia.as.main.metricstools.prometheus.Label;
import com.nokia.as.main.metricstools.prometheus.MetricRegistry;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        try {
            // Start Jetty server
            App app = new App();
            Thread thread = new Thread(app);
            thread.start();

            // Jira issues data as metric examples
            // In this example, we're gonna set the metadata ad labels
            // and the status as metric value to watch
            ArrayList<Issue> jiraIssues = new ArrayList<>();
            jiraIssues.add(new Issue(
                    "0001",
                    "ISSUE-0001",
                    "seb",
                    "thibaud",
                    0));
            jiraIssues.add(new Issue(
                    "0002",
                    "ISSUE-0002",
                    "thibaud",
                    "seb",
                    1));

            /* ************************************ EXAMPLE ***************************************** */

            MetricRegistry metricRegistry = app.getMetricRegistry();

            for(Issue issue : jiraIssues) {
                // Create gauge metric + set labels
                GaugeMetric gaugeMetric = new GaugeMetric(
                        "issue_" + issue.getId(),
                        new ArrayList<>(Arrays.asList(
                                new Label("id", issue.getId()),
                                new Label("key", issue.getKey()),
                                new Label("reporter", issue.getReporter()),
                                new Label("assignee", issue.getAssignee())
                        )));

                issue.setGaugeMetric(gaugeMetric);

                // Gauge metric set value
                gaugeMetric.set(issue.getStatus());

                // Register gauge metric
                metricRegistry.register(gaugeMetric);
            }

            /* Current exposed metrics:

                issue_0001{id="0001",key="ISSUE-0001",reporter="seb",assignee="thibaud",mutable="false",} 0.0
                issue_0002{id="0002",key="ISSUE-0002",reporter="thibaud",assignee="seb",mutable="false",} 1.0
             */

            jiraIssues.get(1).setStatus(2); // Into setStatus --> this.gaugeMetric.set(status);

            /* Current exposed metrics:

                issue_0001{id="0001",key="ISSUE-0001",reporter="seb",assignee="thibaud",mutable="false",} 0.0
                issue_0002{id="0002",key="ISSUE-0002",reporter="thibaud",assignee="seb",mutable="false",} 2.0
             */

            jiraIssues.get(0).getGaugeMetric().setLabel("assignee", "aracely");

            /* Current exposed metrics:

                issue_0001{id="0001",key="ISSUE-0001",reporter="seb",assignee="thibaud",mutable="false",} 0.0
                issue_0002{id="0002",key="ISSUE-0002",reporter="thibaud",assignee="seb",mutable="false",} 2.0
             */
            // assignee isn't a mutable label, so the label value don't change

            metricRegistry.clear();

            for(Issue issue : jiraIssues) {
                // Create gauge metric + set labels
                GaugeMetric gaugeMetric = new GaugeMetric(
                        "issue_" + issue.getId(),
                        new ArrayList<>(Arrays.asList(
                                new Label("id", issue.getId()),
                                new Label("key", issue.getKey()),
                                new Label("reporter", issue.getReporter()),
                                new Label("assignee", issue.getAssignee(), true) // Mutable label
                        )));

                issue.setGaugeMetric(gaugeMetric);

                // Gauge metric set value
                gaugeMetric.set(issue.getStatus());

                // Register gauge metric
                metricRegistry.register(gaugeMetric);
            }

            /* Current exposed metrics:

                issue_0001{id="0001",key="ISSUE-0001",reporter="seb",assignee="thibaud",mutable="true",} 0.0
                issue_0002{id="0002",key="ISSUE-0002",reporter="thibaud",assignee="seb",mutable="true",} 2.0
                issue_0002_ts{id="0002",key="ISSUE-0002",reporter="thibaud",mutable="false",} 2.0
                issue_0001_ts{id="0001",key="ISSUE-0001",reporter="seb",mutable="false",} 0.0
             */
            // As these gauge metrics contain at least one mutable label, they're duplicated.
            // The other one is mutable, so it's possible to change the mutable label value, but it's not
            // possible anymore to use it to track the metric time series. That's why there's now a duplicate.
            // The <metric>_ts gauge is a duplicate of the metric that will never be erased. It only
            // has the no mutable labels, this one is to track the metric time series.

            jiraIssues.get(0).getGaugeMetric().setLabel("assignee", "aracely");

            /* Current exposed metrics:

                issue_0001{id="0001",key="ISSUE-0001",reporter="seb",mutable="true",assignee="aracely",} 0.0
                issue_0002{id="0002",key="ISSUE-0002",reporter="thibaud",assignee="seb",mutable="true",} 2.0
                issue_0002_ts{id="0002",key="ISSUE-0002",reporter="thibaud",mutable="false",} 2.0
                issue_0001_ts{id="0001",key="ISSUE-0001",reporter="seb",mutable="false",} 0.0
             */
            // The assignee has changed

            /* ******************************************************************************* */

        } catch (Exception e) {
            e.printStackTrace();        }

    }

}
