package com.caspar.eservicemall.search.config;

import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class TestJava8 {
    //Lambda表达式的使用前提：
    //
    //有一个接口
    //接口中有且仅有一个抽象方法
    //函数式接口：有且仅有一个抽象方法的接口
    static void Lambda_function(LambdaFunction function, String str) {
        function.func(str);
    }
    static String getString(Supplier<String> supplier) {
        return supplier.get();
    }
    static Integer getInteger(Supplier<Integer> supplier) {
        return supplier.get();
    }


    /**
     * 定义一个方法
     *
     * @param money    传递一个int类型的 money
     * @param consumer
     */
    static void consumer_accept(Consumer<Integer> consumer,Integer money) {
        consumer.accept(money);
    }
    static void consumer_andThen(String str, Consumer<String> consumer1, Consumer<String> consumer2) {
        consumer1.andThen(consumer2).accept(str);
    }

    void printInfo(String[] arr, Consumer<String> consumer1, Consumer<String> consumer2) {
        for (String s : arr) {
            consumer1.andThen(consumer2).accept(s);
        }
    }
    void printInfoTest() {
        String[] array = {"张三,女", "李四,女", "王五,男"};
        printInfo(array,
                str -> System.out.print("姓名: " + str.split(",")[0]),
                str -> System.out.println(" 性别: " + str.split(",")[1])
        );
    }




    /**
     * 吧字符串类型转为 int 类型
     * @param str
     * @param fun
     * @return
     * //R apply(T t)	根据类型 T 的参数获取类型 R 的结果
     */
    static int function_apply(String str, Function<String, Integer> fun) {
        Integer apply = fun.apply(str);
        return apply;
    }
    static void function_test() {
        int i = function_apply("1234", x -> Integer.valueOf(x));
        System.out.println(i);

        Integer i2=function_apply("12",x->{
            return 6;
        });
        System.out.println(i2);
    }







//Lambda 表达式的格式：
//
//格式：（形式参数）-> { 代码块 }
//形式参数：如果有多个参数，参数之间用逗号隔开；如果没有参数，留空即可
//->：由英文中画线和大于符号组成，固定写法，代表指向动作
//代码块：是我们具体要做的事情，也就是以前我们写的方法体内容
    public static void main(String[] args) {
     //   String string = getString(() -> "String");

        Lambda_function((String str) -> {
            System.out.println(str);
        }, "一个形参");

        consumer_accept((money)->{
            System.out.println("本次消费:"+money);
        },100);

        function_test();



        Integer a=2;
        Integer b=2;
        Integer c=2000;
        Integer d=2000;
        System.out.println(a==b);
        System.out.println(c);
        System.out.println(c==d);
    }








}
