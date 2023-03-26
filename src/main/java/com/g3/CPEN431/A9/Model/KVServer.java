package com.g3.CPEN431.A9.Model;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.*;
import ca.NetSysLab.ProtocolBuffers.Message;
import com.g3.CPEN431.A9.Model.Distribution.*;
import com.g3.CPEN431.A9.Model.Store.StoreCache;

public class KVServer {
    public static final int MAX_KEY_LENGTH = 32; // bytes
    public static final int MAX_VALUE_LENGTH = 10000; // bytes
    public static final int PROCESS_ID = (int) ManagementFactory.getRuntimeMXBean().getPid();
    private static final int DEFAULT_CACHE_SIZE = StoreCache.DEFAULT_CACHE_SIZE;
    private long currentCacheSize = DEFAULT_CACHE_SIZE;
    private static final int THREAD_POOL_SIZE = 4;
    private final StoreCache storeCache = StoreCache.getInstance();
    private final DatagramSocket socket;
    private final ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final static KeyTransferManager keyTransferManager = KeyTransferManager.getInstance();
    public static int port;

    Replication replication;


    public KVServer(int port) {
        this.port = port;
        try {
            socket = new DatagramSocket(port);
            keyTransferManager.setSocket(socket);
            replication = new Replication(this.socket);
            NodesCircle nodesCircle = NodesCircle.getInstance();
            Node node = nodesCircle.getNodeFromIp(InetAddress.getLocalHost().getHostAddress(), port);

            // Make sure the current node is in the nodes list
            if (node != null) {
                System.out.println("Server running on port: " + node.getPort());
                nodesCircle.setThisNodeId(node.getId());

                // start epidemic server
                EpidemicServer server = new EpidemicServer(socket, node.getId());
                ScheduledExecutorService epidemicService = Executors.newScheduledThreadPool(1);
                epidemicService.scheduleAtFixedRate(
                        server, 0, 30, TimeUnit.MILLISECONDS);
                NodeStatusChecker checker = new NodeStatusChecker();
                ScheduledExecutorService nodeCheckService = Executors.newScheduledThreadPool(1);
                epidemicService.scheduleAtFixedRate(
                        checker, 0, 1000, TimeUnit.MILLISECONDS);
            } else {
                System.out.println(InetAddress.getLocalHost().getHostAddress());
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("[KVServer]" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void resizeCache() {
        if (storeCache.getCache().estimatedSize() >= currentCacheSize * 2/ 3) {
            storeCache.getCache().policy().eviction().ifPresent(eviction -> {
                eviction.setMaximum(2 * eviction.getMaximum());
                currentCacheSize = eviction.getMaximum();
            });
        } else if (currentCacheSize != DEFAULT_CACHE_SIZE
                && storeCache.getCache().estimatedSize() < DEFAULT_CACHE_SIZE * 2/ 3) {
            storeCache.getCache().policy().eviction().ifPresent(eviction -> {
                eviction.setMaximum(DEFAULT_CACHE_SIZE);
                currentCacheSize = DEFAULT_CACHE_SIZE;
            });
        }
    }

    public void start() {
        // receive request
        while (true) {
            resizeCache();
            byte[] buf = new byte[15000];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
                Message.Msg requestMessage = Message.Msg.parseFrom
                        (Arrays.copyOfRange(buf, 0, packet.getLength()));

                pool.execute(new KVServerHandler(requestMessage,
                        socket, packet.getAddress(), packet.getPort(), replication));
            } catch (IOException e) {
                System.out.println("[ KVServer ]");
                System.out.println("[ KVServer, "+socket.getLocalPort()+", " + Thread.currentThread().getName() + "]: "
                        + e.getLocalizedMessage() + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
