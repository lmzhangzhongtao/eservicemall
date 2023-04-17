package com.caspar.eservicemall.search.service.impl;

import co.elastic.clients.util.QuadFunction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

public class testFunctionTR {


    /**
     * 对控制台文本进行加密的方法
     * @param signer 传入的加密函数
     * @return 加密之后的密文
     */
    protected static String sign(Function<String, String> signer) {
        System.out.println("请输入要加密的字串:");
        String str = new Scanner(System.in).next();
       // Function<T,R>有一个名为apply的抽象方法，它接收一个泛型T，并返回一个泛型R
        //   R apply(T t);
        return signer.apply(str);
    }


    protected  static Function<Integer, String> test(String testStr){
        return new Function<Integer, String>() {

            @Override
            public String apply(Integer integer) {
                return integer.toString();
            }
        };
    }


    protected  static String getStringTest(Function<Integer, String> fn){
            return fn.apply(4);

    }

    protected static Function<Integer, String> getStringTestFn(Integer i){
       return new Function<Integer, String>() {
           @Override
           public String apply(Integer integer) {
               return i.toString();
           }
       };

    }

    /**
     * md5加密函数
     * @param signStr 要加密的明文
     * @return 加密后的密文
     */
    protected static String md5Sign(String signStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new String(md5.digest(signStr.getBytes()));
    }



    protected static String md5Sign22(String signStr){
        return new String(signStr+"222222222222");
    }
    protected static String sign22(Function<String, String> signer){
        System.out.println("sign22的字串:");
        String str="sign22的字串";
        return signer.apply(str);
    }






    public static void main(String[] args)throws Exception {
        //方法引用是什么？
        //方法引用是只需要使用方法的名字，而具体调用交给函数式接口，需要和Lambda表达式配合使用。

//        String result = sign(testFunctionTR::md5Sign);
//        System.out.println("加密后："+result);
//        String s=testFunctionTR.getStringTest(getStringTestFn(7777));
//        System.out.println(s);


        List<String> list =  Arrays.asList("a","b","c","d");
        list.forEach(s -> System.out.println(s));
        list.forEach(System.out::println);


    }
}
