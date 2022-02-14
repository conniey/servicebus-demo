package org.example;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

public class PublishMessages {
    public static void main(String[] args) {
        final String connectionString = Objects.requireNonNull(Environment.getNamespaceConnectionString(), "Connection string is not set.");
        final String queueName = Objects.requireNonNull(Environment.getQueueName(), "Queue name is not set.");

        final Flux<Long> sendMono = Flux.usingWhen(Mono.fromCallable(() -> {
                    return new ServiceBusClientBuilder()
                            .connectionString(connectionString)
                            .sender()
                            .queueName(queueName)
                            .buildAsyncClient();
                }),
                sender -> {
                    return Flux.interval(Duration.ofSeconds(2))
                            .flatMap(index -> {
                                final ServiceBusMessage message = new ServiceBusMessage("Contents: " + index)
                                        .setMessageId(index.toString());
                                return sender.sendMessage(message)
                                        .thenReturn(index);
                            });
                },
                sender -> Mono.fromRunnable(() -> sender.close()));

        final Disposable subscription = sendMono.subscribe(
                index -> System.out.println("Sent message: " + index),
                error -> System.err.println("Error occurred: " + error),
                () -> System.out.println("Completed"));

        System.out.println("Starting... press any key to exit.");
        try {
            final int read = System.in.read();
            System.out.println("-- Exit pressed. Closing. " + read);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            subscription.dispose();
        }
    }
}
