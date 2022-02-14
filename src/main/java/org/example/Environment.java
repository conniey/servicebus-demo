package org.example;

class Environment {
    public static String getNamespaceConnectionString() {
        return System.getenv("SERVICE_BUS_NAMESPACE_CONNECTION_STRING");
    }

    public static String getQueueName() {
        return System.getenv("SERVICE_BUS_QUEUE_NAME");
    }
}
