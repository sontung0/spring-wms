# Spring Cloud Stream Resources

## Learning Path (Recommended Order)

This section defines a logical progression from basic to advanced. Follow the sections in order.

### Phase 1: Foundations & Core Concepts
- [A Brief History of Spring’s Data Integration Journey](https://docs.spring.io/spring-cloud-stream/reference/preface.html)
  Historical context on the evolution of Spring's data integration journey and the origins of Spring Cloud Stream.
- [Introduction](https://docs.spring.io/spring-cloud-stream/reference/index.html)
  High-level orientation and entry point to the reference documentation.
- [Spring Cloud Stream Reference Documentation](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream.html)
  High-level introduction, quick start, main concepts (publish-subscribe, consumer groups, partitions), architecture overview, and core features.
- [Spring Cloud Stream’s application model](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-application-model.html)
  Understanding the overall application model and how Spring Cloud Stream applications are structured.
- [The Binder Abstraction](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-binder-abstraction.html)
  Core concept of how binders abstract message brokers.
- [Persistent publish-subscribe support](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-persistent-publish-subscribe-support.html)
  Foundational publish-subscribe semantics.
- [Consumer group support](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/consumer-groups.html)
  Understanding consumer groups for scaling and load balancing.
- [Partitioning support](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-partitioning.html)
  Introduction to partitioning concepts.

### Phase 2: Programming Model
- [Programming Model](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/programming-model.html)
  The functional programming model using Supplier, Function, and Consumer.
- [Destination Binders](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/destination-binders.html)
  How destination binders work in the programming model.
- [Bindings](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/bindings.html)
  Core concept of bindings between functions and destinations.
- [Binding and Binding names](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/binding-names.html)
  How bindings are named and configured.
- [Functional binding names](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/functional-binding-names.html)
  Using functional interfaces to define binding names.
- [Explicit binding creation](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/explicit-binding-creation.html)
  Advanced techniques for creating bindings explicitly.
- [Binding visualization and control](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/binding_visualization_control.html)
  Tools and techniques to visualize and control bindings at runtime.
- [Producing and Consuming Messages](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/producing-and-consuming-messages.html)
  Core patterns for producing and consuming messages.
- [Event Routing](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/event-routing.html)
  Routing messages based on event type or content.
- [Post processing (after sending message)](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/post-processing-after-sending-message.html)
  Post-processing hooks after message production.

### Phase 3: Binder Abstraction (Deep Dive)
- [Binder abstraction](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/binders.html)
  Deep dive into the Binder SPI and pluggable architecture.
- [A pluggable Binder SPI](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-binder-api.html)
  Details of the Binder Service Provider Interface.
- [Binder Detection](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/binder-detection.html)
  How binders are automatically detected on the classpath.
- [Multiple Binders on the Classpath](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/multiple-binders.html)
  Working with multiple binders simultaneously.
- [Connecting to Multiple Systems](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/multiple-systems.html)
  Patterns for connecting to heterogeneous messaging systems.
- [Customizing binders in multi binder applications](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/binder-customizer.html)
  Advanced customization of binder behavior.

### Phase 4: Essential Features
- [Error Handling](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-error-handling.html)
  Error handling strategies, retry mechanisms, dead-letter queues, and error channel configuration.
- [Observability](https://docs.spring.io/spring-cloud-stream/reference/observability.html)
  Metrics, tracing, and observability features for stream applications.
- [Configuration Options](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/configuration-options.html)
  Complete reference of all configuration properties for Spring Cloud Stream and its binders.
  - [Binding Service Properties](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/binding-service-properties.html)
  - [Binding Properties](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/binding-properties.html)
- [Content Type Negotiation](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/content-type.html)
  Message conversion, serialization, and content type negotiation between producers and consumers.
  - [Mechanics](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/mechanics.html)
  - [Provided MessageConverters](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/provided-messageconverters.html)
  - [User-defined Message Converters](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-user-defined-message-converters.html)
- [Inter-Application Communication](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/application-communication.html)
  Patterns and best practices for communication between multiple Spring Cloud Stream applications.
  - [Connecting Multiple Application Instances](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-connecting-multiple-application-instances.html)
  - [Instance Index and Instance Count](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-instance-index-instance-count.html)
- [Partitioning](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-partitioning.html)
  Partitioning strategies for scaling, ordering guarantees, and key-based routing in message processing.
- [Testing](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/spring_integration_test_binder.html)
  Testing strategies using the test binder and Spring Integration test support.
- [Health Indicator](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/health-indicator.html)
  Health checks, actuator integration, and monitoring for stream applications.
- [Samples](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/samples.html)
  Curated collection of sample applications demonstrating various Spring Cloud Stream features.

### Phase 5: Apache Kafka Binder (Core)
- [Usage (Kafka Binder)](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/usage.html)
  Getting started and basic usage of the Kafka Binder.
- [Overview (Kafka Binder)](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/overview.html)
  Architecture and key features of the Kafka Binder.
- [Configuration Options (Kafka Binder)](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/config-options.html)
  Complete configuration reference for the Kafka Binder.
- [Resetting Offsets](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/reset-offsets.html)
  Techniques for resetting consumer offsets.
- [Consuming Batches](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/consume-batches.html)
  Batch consumption patterns with Kafka.
- [Manual Acknowledgement](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/manual-ack.html)
  Manual acknowledgment strategies.
- [Security Configuration](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/security-config.html)
  Securing Kafka connections (SSL, SASL, etc.).
- [Pausing and Resuming the Consumer](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/pause_resume.html)
  Runtime control of consumer lifecycle.
- [Transactional Binder](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/transactional.html)
  Transactional message processing with Kafka.
- [Error Channels](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/error-channels.html)
  Error channel integration specific to Kafka.
- [Kafka Metrics](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/metrics.html)
  Exposing and consuming Kafka-related metrics.
- [Tombstone Records](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/tombstone.html)
  Handling tombstone records in compacted topics.
- [KafkaBindingRebalanceListener](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/rebalance_listener.html)
  Custom rebalance listener implementation.
- [Retry and Dead Letter Processing](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/retry-dlq.html)
  Retry and DLQ patterns specific to the Kafka Binder.
- [Kafka Binder Listener Container Customizers](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/container-cust-kafka-binder.html)
  Customizing the underlying Kafka listener container.
- [Customizing Consumer and Producer configuration](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/cons-prod-config-cust.html)
  Fine-grained control over consumer and producer configs.
- [Customizing AdminClient Configuration](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/admin-client-config-cust.html)
  Customizing Kafka AdminClient behavior.
- [Custom Kafka Binder Health Indicator](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/custom-health-ind.html)
  Building custom health indicators for Kafka binders.
- [Dead-Letter Topic Processing](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/dlq.html)
  Advanced dead-letter topic handling.
- [Dead-Letter Topic Partition Selection](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/dlq-partition.html)
  Controlling partition assignment for dead-letter messages.
- [Partitioning with the Kafka Binder](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-binder/partitions.html)
  Kafka-specific partitioning strategies.

### Phase 6: Reactive Kafka Binder
- [Overview (Reactive)](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/overview.html)
  Introduction to the Reactive Kafka Binder.
- [Maven Coordinates](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/usage.html)
  Adding the Reactive Kafka Binder to a project.
- [Basic Example using the Reactive Kafka Binder](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/examples.html)
  Simple reactive producer/consumer examples.
- [Consuming Records](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/consuming.html)
  Reactive consumption patterns.
- [Concurrency](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/concurrency.html)
  Managing concurrency in reactive Kafka applications.
- [Multiplex](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/multiplex.html)
  Multiplexing multiple topics reactively.
- [Destination is Pattern](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/pattern.html)
  Using topic patterns in reactive binders.
- [Sender Result Channel](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/sender_result.html)
  Handling send results reactively.
- [Reactor Kafka Binder Health Indicator](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/health_indicator.html)
  Health monitoring for reactive Kafka binders.
- [Observability in Reactive Kafka Binder](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/reactive_observability.html)
  Observability and tracing in reactive Kafka applications.

### Phase 7: Kafka Streams Binder (Stateful Processing)
- [Usage](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/usage.html)
  Getting started with the Kafka Streams Binder.
- [Overview](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/overview.html)
  Architecture and capabilities of the Kafka Streams Binder.
- [Programming Model](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/programming-model.html)
  Functional and imperative programming models for Kafka Streams.
- [Ancillaries to the programming model](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/ancillaries-to-the-programming-model.html)
  Supporting utilities and helpers.
- [Record serialization and deserialization](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/record-serialization-and-deserialization.html)
  Serde configuration and custom serializers.
- [Error Handling](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/error-handling.html)
  Error handling patterns specific to Kafka Streams.
- [Retrying critical business logic](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/retrying-critical-business-logic.html)
  Retry strategies within streams.
- [State Store](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/state-store.html)
  Working with state stores.
- [Interactive Queries](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/interactive-queries.html)
  Querying state stores from outside the streams application.
- [Health Indicator](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/health-indicator.html)
  Health checks for Kafka Streams applications.
- [Accessing Kafka Streams Metrics](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/accessing-metrics.html)
  Exposing and consuming Kafka Streams metrics.
- [Mixing high level DSL and low level Processor API](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/mixing-high-level-dsl-and-low-level-processor-api.html)
  Combining DSL and Processor API.
- [Partition support on the outbound](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/partition-support-on-the-outbound.html)
  Partitioning strategies in Kafka Streams.
- [StreamsBuilderFactoryBean configurer](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/streamsbuilderfactorybean-customizer.html)
  Customizing the StreamsBuilderFactoryBean.
- [Timestamp extractor](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/timestamp-extractor.html)
  Custom timestamp extraction logic.
- [Multi binders with Kafka Streams based binders and regular Kafka Binder](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/multi-binders-with-based-binders-and-regular-binder.html)
  Combining Kafka Streams binders with regular Kafka binders.
- [State Cleanup](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/state-cleanup.html)
  Managing state store cleanup.
- [Kafka Streams topology visualization](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/topology-visualization.html)
  Visualizing Kafka Streams topologies.
- [Event type based routing in Kafka Streams applications](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/event-type-based-routing-in-applications.html)
  Routing events by type within streams.
- [Binding visualization and control in Kafka Streams binder](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/binding-visualization-and-control-in-binder.html)
  Runtime control and visualization for Kafka Streams bindings.
- [Manually starting processors](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/manually-starting-processors.html)
  Controlling when Kafka Streams processors start.
- [Manually starting processors selectively](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/manually-starting-processors-selectively.html)
  Selective startup of individual processors.
- [Tracing using Spring Cloud Sleuth](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/tracing-using-spring-cloud-sleuth.html)
  Distributed tracing in Kafka Streams applications.
- [Configuration Options](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-streams-binder/configuration-options.html)
  Complete configuration reference for the Kafka Streams Binder.

### Phase 8: RabbitMQ Binder
- [RabbitMQ Binder Reference Guide](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview.html)
  Complete reference for the RabbitMQ binder.
  - [RabbitMQ Binder Properties](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/binder-properties.html)
  - [RabbitMQ Consumer Properties](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/rabbitmq-consumer-properties.html)
  - [RabbitMQ Producer Properties](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/prod-props.html)
  - [Advanced Listener Container Configuration](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/advanced-listener-container-configuration.html)
  - [Advanced Configuration](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/advanced-binding-configuration.html)
  - [Receiving Batched Messages](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/receiving-batch.html)
  - [Publisher Confirms](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/publisher-confirms.html)
  - [Initial Consumer Support for the RabbitMQ Stream Plugin](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/rabbitmq-stream-consumer.html)
  - [Initial Producer Support for the RabbitMQ Stream Plugin](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/rabbitmq-stream-producer.html)
- [Using Existing Queues/Exchanges](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/existing-destinations.html)
- [Retry With the RabbitMQ Binder](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/rabbitmq-retry.html)
  - [Putting it All Together](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/putting-it-all-together.html)
- [Error Channels](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/error-channels.html)
- [Partitioning with the RabbitMQ Binder](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_partitions.html)
- [Rabbit Binder Health Indicator](https://docs.spring.io/spring-cloud-stream/reference/rabbit/rabbit_overview/health-indicator.html)

### Phase 9: Other Binders
- [Spring Cloud Stream Binder for Apache Pulsar](https://docs.spring.io/spring-cloud-stream/reference/pulsar/pulsar_binder.html)
  Complete reference for the Apache Pulsar binder.
- [Solace](https://github.com/SolaceProducts/solace-spring-cloud/tree/master/solace-spring-cloud-starters/solace-spring-cloud-stream-starter#spring-cloud-stream-binder-for-solace-pubsub)
  Partner-maintained binder reference for Solace PubSub+.
- [Amazon Kinesis](https://github.com/spring-cloud/spring-cloud-stream-binder-aws-kinesis/blob/main/spring-cloud-stream-binder-kinesis-docs/src/main/asciidoc/overview.adoc)
  Partner-maintained binder reference for Amazon Kinesis.

### Phase 10: Schema Registry & Advanced Topics
- [Spring Cloud Stream Schema Registry](https://docs.spring.io/spring-cloud-stream/reference/schema-registry/spring-cloud-stream-schema-registry.html)
  Schema registry integration for message serialization, evolution, and compatibility.

## External Resources
- [Spring Cloud Stream Samples (GitHub)](https://github.com/spring-cloud/spring-cloud-stream-samples)
  Official curated collection of repeatable sample applications demonstrating features and patterns.

## Wisdom (Communities)
- [Stack Overflow: `spring-cloud-stream` tag](https://stackoverflow.com/questions/tagged/spring-cloud-stream)
  Active community for troubleshooting. Use for: debugging specific errors, configuration issues, and integration questions.
- [Spring Community Gitter / Discord](https://gitter.im/spring-cloud/spring-cloud-stream)
  Direct chat with developers and other users. Use for: architecture questions, best practices, and nuanced discussions not easily searchable on Stack Overflow.
