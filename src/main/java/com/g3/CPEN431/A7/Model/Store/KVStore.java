package com.g3.CPEN431.A7.Model.Store;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class KVStore {
    private ConcurrentHashMap<ByteBuffer, ValueV> store;
    private final static KVStore instance = new KVStore();

    private KVStore() {
        store = new ConcurrentHashMap<>();
    }

    public static KVStore getInstance() {
        return instance;
    }

    public ConcurrentHashMap<ByteBuffer, ValueV> getStore() {
        return store;
    }

    public  void clearStore () {
        this.store.clear();
        this.store = new ConcurrentHashMap<>();
    }
}