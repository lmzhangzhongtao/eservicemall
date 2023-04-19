package com.caspar.eservicemall.search.thread;

import java.util.concurrent.*;

//    1）、继承Thread
//    2）、实现 Runnable接口
//    3）、实现 Callable接口+FutureTask（可以拿到返回结果，可以处理异常）
//         FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
//        System.out.println(futureTask.get());
//    4）、线程池
//
//    区别：
//     1、2不能得到返回值。3可以获取返回值
//	   1、2、3都不能控制资源（无法控制线程数【高并发时线程数耗尽资源】）
//     4可以控制资源，性能稳定，不会一下子所有线程一起运行
//
//    结论：
//    实际开发中，只用线程池【高并发状态开启了n个线程，会耗尽资源】


//    创建原生线程池ThreadPoolExecutor
// new ThreadPoolExecutor(5,
//        200,
//        10,
//        TimeUnit.SECONDS,
//        new LinkedBlockingDeque<>(100000),   //  new LinkedBlockingDeque<>();// 默认大小是Integer.Max会导致内存不足，所以要做压力测试给出适当的队列大小
//        Executors.defaultThreadFactory(),
//        new ThreadPoolExecutor.AbortPolicy());
           // 拒绝策略
           //DiscardOldestPolicy：丢弃最老的任务
          //AbortPolicy：丢弃当前任务，抛出异常【默认策略】
          //CallerRunsPolicy：同步执行run方法
          //DiscardPolicy：丢弃当前任务，不抛出异常
//7个参数：
//    corePoolSize：	核心线程数，不会被回收，接收异步任务时才会创建
//    maximumPoolSize： 最大线程数量，控制资源
//	keepAliveime： 释放的线程数量(maximumPoolSize-corePoolSize) 无任务存活超过空闲时间则线程被释放
//	TimeUnitunit：	时间单位
//	workQueue：		阻塞队列，任务被执行之前保存在任务队列中，只要有线程空闲，就会从队列取出任务执行
//	threadFactory：	线程的创建工厂【可以自定义】
//	RejectedExecutionHandler handler：队列满后执行的拒绝策略

// 线程池任务执行流程
//    当线程池小于corePoolSize时，新提交任务将创建一个新线程执行任务，即使此时线程池中存在空闲线程。
//    当线程池达到corePoolSize时，新提交任务将被放入workQueue中，等待线程池中任务调度执行
//    当workQueue已满，且maximumPoolSize>corePoolSize时，新提交任务会创建新线程执行任务
//    当提交任务数超过maximumPoolSize时，新提交任务由RejectedExecutionHandler处理（默认策略抛出异常）
//    当线程池中超过corePoolSize线程，空闲时间达到keepAliveTime时，释放空闲线程
//    当设置allowCoreThreadTimeOut(true)时，该参数默认false，线程池中corePoolSize线程空闲时间达到keepAliveTime也将关闭


//  面试题：    一个线程池 core 7,max 20, queue 50, 100并发进来怎么分配的
//  答案:    7个会立即得到执行，再开13个进行执行达到最大线程数20， 50个进入等待队列，其他30个则根据拒绝策略进行处理。


// 常用的四种线程池
//Executors.newCachedThreadPool();
//Executors.newFixedThreadPool(10);
//Executors.newScheduledThreadPool(10);
//Executors.newSingleThreadExecutor();

//    回收线程 = maximumPoolSize - corePoolSize
//
//    可缓冲线程池【CachedThreadPool】：corePoolSize=0, maximumPoolSize=Integer.MAX_VALUE
//    定长线程池【FixedThreadPool】：corePoolSize=maximumPoolSize
//    周期线程池【ScheduledThreadPool】：指定核心线程数,maximumPoolSize=Integer.MAX_VALUE,支持定时及周期性任务执行（一段时间之后再执行）
//    单任务线程池【SingleThreadPool】：corePoolSize=maximumPoolSize=1，从队列中获取任务（一个核心线程）

//   为什么使用线程池？
//1.降低资源的消耗【减少创建销毁操作】
//        通过重复利用已经创建好的线程降低线程的创建和销毁带来的损耗
//        高并发状态下过多创建线程可能将资源耗尽
//        2.提高响应速度【控制线程个数】
//        因为线程池中的线程数没有超过线程池的最大上限时,有的线程处于等待分配任务的状态，当任务来时无需创建新的线程就能执行（线程个数过多导致CPU调度慢）
//        3、提高线程的可管理性【例如系统中可以创建两个线程池，核心线程池、非核心线程池【例如发送短信】，显存告警时关闭非核心线程池释放内存资源】
//        线程池会根据当前系统特点对池内的线程进行优化处理，减少创建和销毁线程带来的系统开销。无限的创建和销毁线程不仅消耗系统资源，还降低系统的稳定性，使用线程池进行统一分配

public class ThreadTest {
    public static ExecutorService service = Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//    一： 继承Thread
//         System.out.println("main......start.....");
//         Thread thread = new Thread01();
//         thread.start();
//         System.out.println("main......end.....");
//    二： 实现 Runnable接口
//        System.out.println("main......start.....");
//        Runnable01 runable01 = new Runnable01();
//        new Thread(runable01).start();
//        System.out.println("main......end.....");
//    三： 实现 Callable接口+FutureTask（可以拿到返回结果，可以处理异常）
//        System.out.println("main......start.....");
//         FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//         new Thread(futureTask).start();
//         System.out.println(futureTask.get());    //阻塞等待
//        System.out.println("main......end.....");
        //运行结果
//        main......start.....
//        当前线程：16
//        运行结果：5
//        5
//        main......end.....
        // 四  线程池   ExecutorService
        //  给线程池提交任务，让其自己执行即可
        //
        //  execute和submit区别
        //  作用：都是提交异步任务的
        //  execute：只能提交Runnable任务，没有返回值
        //  submit：可以提交Runnable、Callable，返回值是FutureTask。
        System.out.println("main......start.....");
        service.submit(new Runnable01());  // 提交runnable接口
        Future<Integer> submit = service.submit(new Callable01());  //提交callable接口，方法会有返回值
        Integer integer = submit.get();
        System.out.println(integer);
        System.out.println("main......end.....");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("当前线程：" + Thread.currentThread().getId()+ " 运行结果：" + i);
        }

    }

    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("当前线程：" + Thread.currentThread().getId()+ " 运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer>{

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("当前线程：" + Thread.currentThread().getId()+ " 运行结果：" + i);
            return i;
        }
    }
}
