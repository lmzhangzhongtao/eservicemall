package com.caspar.eservicemall.product.web;
import com.caspar.eservicemall.product.entity.CategoryEntity;
import com.caspar.eservicemall.product.service.CategoryService;
import com.caspar.eservicemall.product.vo.Catelog2Vo;
import jakarta.annotation.Resource;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @Autowired
    RedissonClient redisson;


    @Autowired
    StringRedisTemplate redisTemplate;
    @GetMapping(value = {"/","index.html"})
    private String indexPage(Model model) {

        //1、查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categories",categoryEntities);
        return "index";
    }
    //index/json/catalog.json
    @GetMapping(value = "/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;

    }
    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        //1.获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");
        //2.加锁 解锁
        try {
            //lock.lock();//阻塞式等待
            //1)、锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s，不用担心业务时间长，锁自动过期被删掉（看门狗）
            //2)、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s后自动删除
            lock.lock(10, TimeUnit.SECONDS);//10秒自动解锁 注：此时不会触发看门狗原理，故自动解锁时间一定要大于业务的执行时间
            //问题：lock.lock(10, TimeUnit.SECONDS);在锁时间到了以后，不会自动续期
            //1.如果我们传递了锁 的超时时间，就会发送给Redis执行脚本，进行占锁，默认超时就是我们指定的时间
            //2.如果我们未指定锁的超时时间，就使用30*1000【LockWatchdogTimeout看门狗的默认时间】
            //只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】
            //每10秒进行续期 30s
            //最佳实战
            //使用lock.lock(30, TimeUnit.SECONDS);省掉了整个续期操作
            System.out.println("加锁成功，执行业务……"+Thread.currentThread().getId());
            Thread.sleep(30000);
        }catch (Exception e){

        }finally {
            System.out.println("释放锁……"+Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }
    //保证一定能读到最新数据，修改期间，写锁是排他锁（互斥锁），读锁是一个共享锁
    //写锁没释放读就必须等待
    //读+读：相当于无锁，并发读，只会在Redis中记录好，所有当前的读锁，他们都会同时加锁成功
    //写+读：等待写锁释放
    //写+写：阻塞方式
    //读+写：有读锁，写也需要等待
    //只要有写的存在，都必须等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){
        String s ="";
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        try {
            //1.改数据加写锁，读数据加读锁
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue",s);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }


        return s;
    }
    @GetMapping("/read")
    @ResponseBody
    public String readValue(){
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s ="";
        //加读锁
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            s= redisTemplate.opsForValue().get("writeValue");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }


        return s;
    }

    /**
     * 闭锁
     * 放假，锁门
     * 5个班全部没人了，可以锁大门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();//等待闭锁都完成
        return "放假了……";
    }

    /**
     * 车库停车
     *3车位
     */

    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        //park.acquire();//获取一个信号，获取一个值，占一个车位
        boolean b = park.tryAcquire();//尝试获取获取一个信号，获取一个值，占一个车位
        return "ok";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();//释放一个车位
        return "ok";
    }
}
