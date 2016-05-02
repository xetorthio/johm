package redis.clients.johm.sentinel;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.johm.JOhm;
import redis.clients.johm.JOhmTestBase;
import redis.embedded.RedisCluster;
import redis.embedded.RedisServer;
import redis.embedded.util.JedisUtil;

public class JOhmTestSentinelBase extends JOhmTestBase {
    
    private static RedisCluster cluster;
    private Set<String> jedisSentinelHosts;
    
    @Override
    protected void startJedisEngine() {
        

        
        //retrieve ports on which sentinels have been started, using a simple Jedis utility class
        jedisSentinelHosts = JedisUtil.sentinelHosts(cluster);
        if (benchmarkMode) {
            jedisPool = new JedisSentinelPool("master1", jedisSentinelHosts, new GenericObjectPoolConfig(), 2000);
        } else {
            jedisPool = new JedisSentinelPool("master1", jedisSentinelHosts, new GenericObjectPoolConfig());
        }
        JOhm.setPool(jedisPool);
        purgeRedis();
    }
    
    @BeforeClass
    public static void setup() throws IOException {
        startEmbeddedRedis();
    }
    
    
    protected static void startEmbeddedRedis() throws IOException {
        cluster = RedisCluster.builder().ephemeral().sentinelCount(3).quorumSize(2)
                .replicationGroup("master1", 1)
                .replicationGroup("master2", 1)
                .replicationGroup("master3", 1)
                .sentinelStartingPort(Protocol.DEFAULT_PORT)
                .build();
        cluster.start();
        
    }
    
    @AfterClass
    public static void tearDown() {
        cluster.stop();
    }

}
