package com.caspar.eservicemall.common.utils;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanMap;

import java.util.HashMap;

import java.util.List;

import java.util.Map;


public class BeanMapUtils<T> {
    public Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map =new HashMap<String, Object>();
        BeanMap beanMap = new BeanMap(bean);
        for (Object key : beanMap.keySet()) {
            map.put(key+"", beanMap.get(key));
        }
        return map;
    }
    public T mapToBean(Map<String, Object> map,T bean) {
        BeanMap beanMap = new BeanMap(bean);
        beanMap.putAll(map);
        return bean;
    }

}
