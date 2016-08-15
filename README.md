
# Running Tests
mvn test
mvn test -P unit-tests
mvn test -P integration-tests
mvn test -P all-tests

# Deploy Binaries to the mvn-repo branch
mvn deploy

# Sign and deploy
mvn clean deploy -Dgpg.keyname=F1033A0A -Dgpg.passphrase=Secr3t


# Deply Site to the gh-pages branch
mvn site-deploy


mvn sprint-boot:run
mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=microservice1”
mvn sonar:sonar -Dsonar.host.url=http://192.168.99.100:9000 -Dsonar.jdbc.url="jdbc:h2:tcp://192.168.99.100/sonar" -Dsonar.jdbc.username=sonar -Dsonar.jdbc.password=sonar -Dsonar.profile=


---

A Servo and InfluxDB Integration.

* [Servo](https://github.com/Netflix/servo) is an Open-Source application monitoring library by [Netflix](https://netflix.github.io/)
* [InfluxDB](https://influxdata.com/) is a scalable datastore for metrics, events, and real-time analytics

## Description

* Publishes exposed application and JVM metrics to an external InfluxDB.
* An implementation of the Servo's [MetricObserver](https://netflix.github.io/servo/current/servo-core/docs/javadoc/com/netflix/servo/publish/MetricObserver.html) to forward metrics data to an InfluxDB database.

## Main Features

* Batch support
* Configurable

Wishlist
* Retries

##

|||
|-|-|
| Continuous Integration | [TravisCI](https://travis-ci.org/getting_started) |
| Test Coverage | [COVERALLS](https://coveralls.io/) |

---
https://docs.influxdata.com/influxdb/v0.13/
---

## Example


```java
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.JvmMetricPoller;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.polymathiccoder.servo.publish.influxdb.InfluxDBMetricObserver;
import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBConnection;
import com.polymathiccoder.servo.publish.influxdb.operations.InfluxDBOperations;
import com.polymathiccoder.servo.publish.influxdb.util.NetflixServoUtils;
import com.polymathiccoder.servo.publish.influxdb.util.SampleData;

public class Main {
    public static void main(String[] args) {
        // Observers
        final InfluxDBConnection influxDBConnection = InfluxDBConnection.create(SampleData.getInfluxDBConfiguration());
        final InfluxDBOperations influxDBOperations = new InfluxDBOperations(influxDBConnection);
        final InfluxDBMetricObserver influxDBMetricObserver = new InfluxDBMetricObserver(
                influxDBOperations,
                NetflixServoUtils.commonTagsDecoration());

        // Pollers
        MetricPoller monitorRegistryMetricPoller = new MonitorRegistryMetricPoller();
        MetricPoller JvmMetricPoller = new JvmMetricPoller();

        // Schedulers
        final PollScheduler pollScheduler = PollScheduler.getInstance();
        pollScheduler.start();

        final PollRunnable jvmMetricsTask = new PollRunnable(
                JvmMetricPoller,
                BasicMetricFilter.MATCH_ALL,
                true,
                Lists.newArrayList(
                        influxDBMetricObserver));
        pollScheduler.addPoller(jvmMetricsTask, 3, TimeUnit.SECONDS);

        final PollRunnable monitorRegistryMetricsTask = new PollRunnable(
                monitorRegistryMetricPoller,
                BasicMetricFilter.MATCH_ALL,
                true,
                Lists.newArrayList(
                        influxDBMetricObserver));
        pollScheduler.addPoller(monitorRegistryMetricsTask, 3, TimeUnit.SECONDS);
    }
}
```
