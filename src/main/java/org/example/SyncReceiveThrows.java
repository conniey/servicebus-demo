package org.example;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncReceiveThrows {
    private static final int NUMBER_TO_RECEIVE = 10;

    public static void main(String[] args) {
        final String connectionString = Objects.requireNonNull(Environment.getNamespaceConnectionString(), "Connection string is not set.");
        final String queueName = Objects.requireNonNull(Environment.getQueueName(), "Queue name is not set.");
        final AtomicInteger counter = new AtomicInteger();

        final ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .receiver()
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .queueName(queueName)
                .buildClient();

        int rounds = 100;
        for (int i = 0; i < rounds; i++) {
            final Thread thread = createNewThread(i, receiver, counter);
            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.printf("-- Error happened while joining: %s%n", e);
                break;
            }
        }
    }

    private static Thread createNewThread(int round, ServiceBusReceiverClient receiver, AtomicInteger counter) {
        return new Thread(() -> {
            System.out.println("---- CREATING " + round + " ----");
            for (ServiceBusReceivedMessage message : receiver.receiveMessages(NUMBER_TO_RECEIVE)) {
                final int i = counter.incrementAndGet();
                System.out.printf("round[%d] #[%d] messageId[%s] %n", round, i, message.getMessageId());

                if (i % 15 == 0) {
                    throw new IllegalStateException("Test error occurs. index: " + i);
                }

                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    System.out.println("---> Unable to sleep. Error: " + e);
                } finally {
                    receiver.complete(message);
                }
            }
            System.out.println("---- ENDING " + round + " ----");
        });
    }
}
