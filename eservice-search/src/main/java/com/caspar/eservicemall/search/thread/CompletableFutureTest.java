package com.caspar.eservicemall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//异步编排测试


//  1.runXXX都是没有返回结果的,如runAsync，supplyXXX可以获取返回结果，如supplyAsync
//  2.可以传入自定义线程池，否则使用默认线程池


//  whenCompleteAsync和whenComplete的区别：
//  whenComplete ：是执行当前任务的线程执行并继续执行 whenComplete任务
//  whenCompleteAsync: 是执行把whenCompleteAsync这个任务继续提交给线程池来进行执行。
//  方法不以Async结尾，意味着Action使用相同的线程执行，而Async可能会使用其他线程执行（如果是使用相同的线程池，也可能会被同一个线程选中执行）

public class CompletableFutureTest {
    // 线程池
    public static ExecutorService service = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        System.out.println("main...CompletableFuture.runAsync...start.....");
//        // 不需要返回值
//        CompletableFuture.runAsync(()->{
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("当前线程：" + Thread.currentThread().getId()+ " 运行结果：" + i);
//        },service);
//        System.out.println("main..CompletableFuture.runAsync....end.....");


//        System.out.println("main...CompletableFuture.supplyAsync...start.....");
//        // 需要返回值  异步对象
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("当前线程：" + Thread.currentThread().getId() + " 运行结果：" + i);
//            return i;
//        }, service).whenCompleteAsync((res,exception)->{
//            //  void accept(T t, U u);  没有返回值
//            //     //虽然能得到异常信息，但是没法修改返回数据
//                System.out.println("异步任务成功完成了...结果是：" + res + "异常是：" + exception);
//        }).exceptionallyAsync(
//                ////可以感知异常，同时返回默认值
//                throwable -> 10 );
//        Integer futureReturnValue=future.get();
//        System.out.println("CompletableFuture.supplyAsync 返回值 :"+futureReturnValue);
//
//        System.out.println("main..CompletableFuture.supplyAsync....end.....");



        /**
         * 方法执行完后端处理
         */
//         CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//             System.out.println("当前线程：" + Thread.currentThread().getId());
//             int i = 10 / 2;
//             System.out.println("运行结果：" + i);
//             return i;
//         }, service).handle((result,thr) -> {
//             if (result != null) {
//                 return result * 2;
//             }
//             if (thr != null) {
//                 System.out.println("异步任务成功完成了...结果是：" + result + "异常是：" + thr);
//                 return 0;
//             }
//             return 0;
//         });
//         Integer futureReturnValue=future.get();
//        System.out.println("CompletableFuture.supplyAsync handle 返回值 :"+futureReturnValue);
//
//        System.out.println("main..CompletableFuture.supplyAsync.handle...end.....");




        /**
         * 线程串行化
         * 1、thenRunL：不能获取上一步的执行结果
         * 2、thenAcceptAsync：能接受上一步结果，但是无返回值
         * 3、thenApplyAsync：能接受上一步结果，有返回值
         *
         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenApplyAsync(res -> {
//            System.out.println("任务2启动了..." + res);
//            return "Hello" + res;
//        }, service);
//        System.out.println("main......end....." + future.get());


       /**
        * 两任务组合 都要完成
        *
        * 1) runAfterBothAsync
        *  二者都要完成，组合【不获取前两个任务返回值，且自己无返回值】
        * **/
//        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前任务1线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("当前任务1运行结果：" + i);
//            return i;
//        }, service);
//
//        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前任务2线程：" + Thread.currentThread().getId());
//            System.out.println("当前任务2运行结果：" + "hello");
//              return "hello";
//        }, service);
//
//        CompletableFuture<Void> future03 = future01.runAfterBothAsync(future02, () -> {
//            System.out.println("任务3启动了 ...");
//        }, service);

        //  2) thenAcceptBothAsync
        //  二者都要完成，组合【获取前两个任务返回值，自己无返回值】
//        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前任务1线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("当前任务1运行结果：" + i);
//            return i;
//        }, service);
//
//        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前任务2线程：" + Thread.currentThread().getId());
//            System.out.println("当前任务2运行结果：" + "hello");
//              return "hello";
//        }, service);
////
//        future01.thenAcceptBothAsync(future02,(f1,f2)->{
//            //void accept(T t);
//            System.out.println("任务3启动了 ..."+ "任务1返回结果:"+f1+" 任务2返回结果"+f2);
//        },service);


        // 3) thenCombineAsync
        //   二者都要完成，组合【获取前两个任务返回值，自己有返回值】
//        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1执行");
//            return 10 / 2;
//        }, service);
//        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2执行");
//            return "hello";
//        }, service);
//        CompletableFuture<String> future03 = future01.thenCombineAsync(future02,
//                (result1, result2) -> {
//                    System.out.println("任务3执行");
//                    System.out.println("任务1返回值：" + result1);
//                    System.out.println("任务2返回值：" + result2);
//                    return "任务3返回值";
//                }, service);
//        System.out.println(future03.get());


    }

}
