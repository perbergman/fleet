package com.example.fleet.service;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;

import java.util.concurrent.Executor;

public final class MyCallCredentials extends CallCredentials {

    private static Metadata.Key<String> header =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    public static <T extends AbstractStub<T>> T authenticatingStub(T stub, String token) {
        return stub.withCallCredentials(new MyCallCredentials(token));
    }

    private final String token;

    public MyCallCredentials(String token) {
        super();
        this.token = token;
    }

    @Override
    public void applyRequestMetadata(
            CallCredentials.RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        Metadata metadata = new Metadata();
        metadata.put(
                MyCallCredentials.header, token.startsWith("Bearer ") ? token : "Bearer " + token);
        applier.apply(metadata);
    }

    @Override
    public void thisUsesUnstableApi() {
        // No need to implement this, it's used as a warning from upstream
    }
}
