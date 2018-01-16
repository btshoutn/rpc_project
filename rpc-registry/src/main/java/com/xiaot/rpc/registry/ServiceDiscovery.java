package com.xiaot.rpc.registry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by xiaotian on 2018/1/15.
 * 发现服务
 */
public class ServiceDiscovery {

    private static Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);
    //等待线程
    private CountDownLatch latch = new CountDownLatch(1);
    //地址列表
    private volatile  List<String> dataList = new ArrayList<String>();

    //服务器注册地址
    private  String registryAddress;

    public ServiceDiscovery(String registryAddress){
        this.registryAddress = registryAddress;
        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    /**
     * 发现服务
     * 可以设置负载均衡算法等
     * @return
     */
    public  String discover(){
        String data =null;
        int size = dataList.size();
        if (size>0){

            if (size==1){
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            }else {
                data =dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }

        }

        return  data;
    }


    /**
     * 服务器连接
     * @return
     */
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
            zk  = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState()==Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return zk;
    }

    /**
     * 监听
     * @param zk
     */
  public   void  watchNode(final  ZooKeeper zk){
      List<String> addrList = new ArrayList<String>();
      try {
          List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
              public void process(WatchedEvent watchedEvent) {
                  if (watchedEvent.getType()==Event.EventType.NodeChildrenChanged){
                      watchNode(zk);
                  }
              }
          });

          for (String node : nodeList) {
              try {
                  byte[] data = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                  addrList.add(new String(data));
              } catch (KeeperException e) {
                  e.printStackTrace();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
          LOGGER.debug("node data: {}", addrList);
          this.dataList = addrList;
      } catch (KeeperException e) {
          e.printStackTrace();
      } catch (InterruptedException e) {
          e.printStackTrace();
      } finally {
          this.dataList = addrList;
      }


  }

}
