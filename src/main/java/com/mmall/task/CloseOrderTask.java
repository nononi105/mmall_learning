package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Created by NoNo_Ni on 2020/5/2.
 */
@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

//    @Scheduled(cron="0 */1 * * * ?")//每一分钟（每个一分钟的整数倍）
//    public void closeOrderTaskV1(){
//        log.info("关闭订单定时任务启动");
//        int hour =Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
////        iOrderService.CloseOrder(hour);
//        log.info("关闭订单定时任务结束");
//
//    }

    @PreDestroy
    private  void delLock(){
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }

//    @Scheduled(cron="0 */1 * * * ?")//每一分钟（每个一分钟的整数倍）
    public void closeOrderTaskV2(){
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
        if(setnxResult !=null && setnxResult.intValue()==1){
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else {
            log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");

    }


    @Scheduled(cron="0 */1 * * * ?")//每一分钟（每个一分钟的整数倍）
    public void closeOrderTaskV3(){
        log.info("关闭订单定时任务启动");
        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout","5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));

        if(setnxResult !=null && setnxResult.intValue()==1){
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else {
            //未获得锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if(lockValueStr!=null && System.currentTimeMillis()>Long.parseLong(lockValueStr)){
                String getSetResult = RedisShardedPoolUtil.getset(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
                //再次用当前时间戳
                //返回给定的key的旧值,->旧值判断，是否可以获取锁
                //当key没有旧值时，即key不存在时，返回nil -> 获取锁
                //这里我们set了一个新的value值，获取旧的值
                if(getSetResult == null || (getSetResult!=null && StringUtils.equals(lockValueStr,getSetResult))){
                    //真正获取到锁
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

                }else{
                    log.info("没有获取到分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else{
                log.info("没有获取到分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
            log.info("没有获得分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务结束");

    }

    private void closeOrder(String lockName){
        RedisShardedPoolUtil.expire(lockName,5);
        log.info("获取:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour =Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour","2"));
//        iOrderService.CloseOrder(hour);
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        log.info("释放:{},ThreadName:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        log.info("================================");
    }
}
