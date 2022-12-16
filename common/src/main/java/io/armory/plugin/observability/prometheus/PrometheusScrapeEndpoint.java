/*
 * Copyright 2020 Armory, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.armory.plugin.observability.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * Port of PrometheusScrapeEndpoint but rather than being an web endpoint that won't work with
 * plugins, it will be an endpoint
 *
 * <p>See:
 * https://github.com/spring-projects/spring-boot/blob/cd1baf18fe9ec71c11d7d131d6f1a417ec0c00e2/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/metrics/export/prometheus/PrometheusScrapeEndpoint.java
 */
// If you use WebEndpoint instead of Endpoint, the plugin throws class def not found error with PF4j
// ¯\_(ツ)_/¯
@Endpoint(id = "x-prometheus")
public class PrometheusScrapeEndpoint {

  private final CollectorRegistry collectorRegistry;
  private final String PROMETHEUS_CONTENT_TYPE = "text/plain";

  public PrometheusScrapeEndpoint(CollectorRegistry collectorRegistry) {
    this.collectorRegistry = collectorRegistry;
  }

  @ReadOperation(produces = PROMETHEUS_CONTENT_TYPE)
  public String scrape() {
    try {
      Writer writer = new StringWriter();
      Enumeration<Collector.MetricFamilySamples> samples =
          this.collectorRegistry.metricFamilySamples();
      TextFormat.write004(writer, samples);
      return writer.toString();
    } catch (IOException ex) {
      // This actually never happens since StringWriter::write() doesn't throw any
      // IOException
      throw new RuntimeException("Writing metrics failed", ex);
    }
  }
}
