package com.xiaot.rpc.registry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;


/**
 * Created by xiaotian on 2018/1/15.
 * zk服务注册
 */
public class ServiceRegistry {

    //日志
    private  static Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
    //注册服务地址
    private  String registryAddress;
    //等待线程
    private CountDownLatch latch = new CountDownLatch(1);

    public  ServiceRegistry(String registryAddress){
        this.registryAddress = registryAddress;
    }

    /**
     * 注册服务
     * @param data
     */
    public void  register(String data){
        if (data!=null){
            ZooKeeper zk = connectServer();
            if (zk!=null){
                createNode(zk,data);
            }
        }
    }

    /**
     * 连接zk服务
     * @return
     * @throws Exception
     */
    private ZooKeeper connectServer(){

        ZooKeeper zk = null;
        try {
            zk =  new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        }
        return zk;

    }

    /**
     * 创建节点
     * @param zk  zookeeper
     * @param data 节点数据
     */
    private  void  createNode(ZooKeeper zk,String data) {
        byte[] bytes = data.getBytes();
        try {
            if (zk.exists(Constant.ZK_REGISTRY_PATH,null)==null){
                zk.create(Constant.ZK_REGISTRY_PATH,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            String path = zk.create(Constant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} ==> {} )",path,bytes);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        }
    }


}
