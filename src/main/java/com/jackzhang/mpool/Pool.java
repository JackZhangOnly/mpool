package com.jackzhang.mpool;

import redis.clients.jedis.Jedis;

/**
 * Created by Jack on 2018/2/23.
 */
public interface Pool {
    /**
     * 初始化：最大的连接数；获取连接的超时时间
     * @param max
     * @param maxWait
     */
    public void init(int max,long maxWait);

    /**
     * 获取连接
     * 1、控制连接的数量（线程安全）
     * 2、控制获了连接的时间
     * @return
     */
    public Jedis getResource();

    /**
     * 释放连接
     * @param jedis
     */
    public void release(Jedis jedis);

}
