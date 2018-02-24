package com.jackzhang.mpool;

import redis.clients.jedis.Jedis;

import javax.sql.rowset.JdbcRowSet;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Jack on 2018/2/24.
 */
public class PoolTest {

    private static final int COUNT=20;
    private static final CountDownLatch downLatch=new CountDownLatch(COUNT);

    public static void main(String[] args) {
        final Pool pool=new RedisPool();
        pool.init(10,20000);

        for (int i=0;i<COUNT;i++){
            new Thread(new Runnable() {
                public void run() {
                    downLatch.countDown();
                    try {
                        downLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //并发获取
                    Jedis connection=null;
                    try {
                        connection=pool.getResource();
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        pool.release(connection);
                    }
                }
            }).start();
        }
    }
}
