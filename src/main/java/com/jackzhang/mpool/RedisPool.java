package com.jackzhang.mpool;

import redis.clients.jedis.Jedis;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jack on 2018/2/24.
 */
public class RedisPool implements Pool
{
    int max;
    long maxWait;

    /**
     * 保存连接
     */
    LinkedBlockingQueue<Jedis> busy;//繁忙
    LinkedBlockingQueue<Jedis> idle;//空闲

    AtomicInteger activeCounter=new AtomicInteger(0);//连接数

    public void init(int max, long maxWait) {
        this.max=max;
        this.maxWait=maxWait;

        busy=new LinkedBlockingQueue<Jedis>();
        idle=new LinkedBlockingQueue<Jedis>();
    }

    public Jedis  getResource() throws InterruptedException {
        long begin=System.currentTimeMillis();

        //1、取空闲的连接复用
        Jedis connection=idle.poll();
        if (connection!=null){
            busy.offer(connection);
        }
        //2、连接数未满，创建新的连接
        if (activeCounter.get()<max){
            if (activeCounter.incrementAndGet()<=max){

                System.out.println("创建了一个连接，当前连接数："+activeCounter);

                connection=new Jedis("127.0.0.1",6379);
                busy.offer(connection);


                return connection;
            }else {
                activeCounter.decrementAndGet();
            }
        }
        //3、未获取到连接，等待已有连接接释放
        try {
            connection=busy.poll(maxWait-(System.currentTimeMillis()-begin), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (connection==null){
            System.out.println("未获取到连接");
        }else {
            busy.offer(connection);
        }
        return connection;
    }

    public void release(Jedis jedis) {
       if ( busy.remove(jedis)){
           idle.offer(jedis);
           return;
       }
        if (activeCounter.decrementAndGet()>=0){
            activeCounter.decrementAndGet();
        }
    }
}
