package com.caspar.eservicemall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caspar.eservicemall.common.constant.product.CategoryConstant;
import com.caspar.eservicemall.product.service.CategoryBrandRelationService;
import com.caspar.eservicemall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.Query;

import com.caspar.eservicemall.product.dao.CategoryDao;
import com.caspar.eservicemall.product.entity.CategoryEntity;
import com.caspar.eservicemall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;
   // ServiceImpl<M extends BaseMapper<T>, T> implements IService<T>
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }
   /**
    * 型递归流程使用的是stream流来进行处理，filter过滤+map(填充子列表)+sorted（排序）最终使用collect来进行聚合
    * **/
    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查询到所有的分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2、组装成父子的树型结构
        List<CategoryEntity> ans = entities.stream()
                .filter((menu) -> menu.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    if (menu1.getSort() == null || menu2.getSort() == null) return 0;
                    return menu1.getSort() - menu2.getSort();
                })
                .collect(Collectors.toList());


        return ans;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否被其他地方引用

        //2、逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到catelogId的完整路径
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return (Long[])parentPath.toArray(new Long[parentPath.size()]);
    }
/**
 * 级联更新所有关联的数据
 *
 * @CacheEvict:失效模式
 * @CachePut:双写模式，需要有返回值
 * 1、同时进行多种缓存操作：@Caching
 * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category",allEntries = true)
 * 3、存储同一类型的数据，都可以指定为同一分区
 * @param category
 */
// @Caching(evict = {
//         @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
//         @CacheEvict(value = "category",key = "'getCatalogJson'")
// })
    @CacheEvict(value = {"category"}, allEntries = true)
    @Transactional
    @Override
    public void updateCasCade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    /**
     * 每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型分)】
     * 代表当前方法的结果需要缓存，如果缓存中有，方法都不用调用，如果缓存中没有，会调用方法。最后将方法的结果放入缓存
     * 默认行为
     *      如果缓存中有，方法不再调用
     *      key是默认生成的:缓存的名字::SimpleKey::[](自动生成key值)
     *      缓存的value值，默认使用jdk序列化机制，将序列化的数据存到redis中
     *      默认时间是 -1：
     *
     *   自定义操作：key的生成
     *      指定生成缓存的key：key属性指定，接收一个Spel
     *      指定缓存的数据的存活时间:配置文档中修改存活时间
     *      将数据保存为json格式
     *
     *
     * 4、Spring-Cache的不足之处：
     *  1）、读模式
     *      缓存穿透：查询一个null数据。解决方案：缓存空数据   spring.cache.redis.cache-null-values=true
     *      缓存击穿：大量并发进来同时查询一个正好过期的数据。解决方案：加锁 ? 默认是无加锁的;使用sync = true来解决击穿问题
     *      缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间
     *  2)、写模式：（缓存与数据库一致）
     *      1）、读写加锁。
     *      2）、引入Canal,感知到MySQL的更新去更新Redis
     *      3）、读多写多，直接去数据库查询就行
     *
     *  总结：一致性要求不高的数据
     *      常规数据（读多写少，即时性，，完全可以使用Spring-Cache）：写模式(只要缓存的数据有过期时间就足够了)
     *      特殊数据：特殊设计
     *
     *  原理：
     *      CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
     * @return
     */
    @Cacheable(value = {"category"},key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys........");
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(
                new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间："+ (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    /**
     * 查询三级分类并封装成Map返回
     * 使用SpringCache注解方式简化缓存设置
     *
     * 缓存击穿：大量并发进来同时查询一个正好过期的数据。解决方案：加锁 ? 默认是无加锁的;使用sync = true来解决击穿问题
     */
    @Cacheable(value = {"category"}, key = "'getCatalogJson'", sync = true)
    public Map<String, List<Catelog2Vo>> getCatalogJsonWithSpringCache() {
        // 未命中缓存
        // 1.double check，占锁成功需要再次检查缓存（springcache使用本地锁）
        // 查询非空即返回
        String catlogJSON = redisTemplate.opsForValue().get("getCatalogJson");
        if (!StringUtils.isEmpty(catlogJSON)) {
            // 查询成功直接返回不需要查询DB
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catlogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        // 2.查询所有分类，按照parentCid分组
        Map<Long, List<CategoryEntity>> categoryMap = baseMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(key -> key.getParentCid()));

        // 3.获取1级分类
        List<CategoryEntity> level1Categorys = categoryMap.get(0L);

        // 4.封装数据
        Map<String, List<Catelog2Vo>> result = level1Categorys.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), l1Category -> {
            // 5.查询2级分类，并封装成List<Catalog2VO>
            List<Catelog2Vo> catalog2VOS = categoryMap.get(l1Category.getCatId())
                    .stream().map(l2Category -> {
                        // 7.查询3级分类，并封装成List<Catalog3VO>
                        List<Catelog2Vo.Category3Vo> catalog3Vos = categoryMap.get(l2Category.getCatId())
                                .stream().map(l3Category -> {
                                    // 封装3级分类VO
                                    Catelog2Vo.Category3Vo catalog3Vo = new Catelog2Vo.Category3Vo(l2Category.getCatId().toString(), l3Category.getCatId().toString(), l3Category.getName());
                                    return catalog3Vo;
                                }).collect(Collectors.toList());
                        // 封装2级分类VO返回
                        Catelog2Vo catalog2VO = new Catelog2Vo(l1Category.getCatId().toString(), catalog3Vos, l2Category.getCatId().toString(), l2Category.getName());
                        return catalog2VO;
                    }).collect(Collectors.toList());
            return catalog2VOS;
        }));
        return result;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        // return this.baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
    }
    /**
     *
     * // 1. 空结果缓存, 解决缓存穿透
     * // 2. 设置过期时间(加随机值), 解决缓存雪崩
     * // 3. 加锁, 解决缓存击穿
     *
     *
     * 查询三级分类并封装成Map返回
     * 使用redis客户端实现缓存设置
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        // 查询缓存
        String catalogJSON = redisTemplate.opsForValue().get(CategoryConstant.CACHE_KEY_CATALOG_JSON);

        if (StringUtils.isEmpty(catalogJSON)) {
            // 未命中缓存
            // 查询db

            Map<String, List<Catelog2Vo>> map = getCatalogJsonFromDb();
            String jsonString= JSON.toJSONString(map);
            redisTemplate.opsForValue().set(CategoryConstant.CACHE_KEY_CATALOG_JSON,jsonString);
            return map;
        }
        // 命中缓存
        Map<String, List<Catelog2Vo>> result = JSONObject.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    /**
     * 查询三级分类（本地锁版本）
     * 已废弃
     */
    @Deprecated
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        // 本地锁：synchronized，JUC(lock)，在分布式0情况下，需要使用分布式锁
        synchronized (this) {
            // 得到锁以后还要检查一次，double check
            return getCatalogJsonFromDb();
        }
    }
    /**
     * 查询三级分类（redisson分布式锁版本）
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        // 1.抢占分布式锁，同时设置过期时间
        RLock lock = redisson.getLock(CategoryConstant.LOCK_KEY_CATALOG_JSON);
        lock.lock(30, TimeUnit.SECONDS);
        try {
            // 2.查询DB
            Map<String, List<Catelog2Vo>> result = getCatalogJsonFromDb();
            return result;
        } finally {
            // 3.释放锁
            lock.unlock();
        }
    }
    /**
     * 查询三级分类（原生版redis分布式锁版本）
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        // 1.抢占分布式锁，同时设置过期时间
        String uuid = UUID.randomUUID().toString();
        // 使用setnx占锁（setIfAbsent）
        Boolean isLock = redisTemplate.opsForValue().setIfAbsent(CategoryConstant.LOCK_KEY_CATALOG_JSON, uuid, 300, TimeUnit.SECONDS);  //占锁 和设置过期时间必须是原子操作
        if (isLock) {
            // 2.抢占成功
            Map<String, List<Catelog2Vo>> result = null;
            try {
                // 查询DB
                // TODO 业务续期（锁过期）【不应该添加业务续期代码】
                return getCatalogJsonFromDb();
            } finally {
                // 3.查询UUID是否是自己，是自己的lock就删除
                // 封装lua脚本（原子操作解锁）
                // 查询+删除（当前值与目标值是否相等，相等执行删除，不等返回0）
                String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call('del',KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                // 删除锁
                redisTemplate.execute(new DefaultRedisScript<Long>(luaScript, Long.class), Arrays.asList(CategoryConstant.LOCK_KEY_CATALOG_JSON), uuid);
            }
        } else {
            // 4.加锁失败，自旋重试
            // TODO 不应该使用递归，使用while，且固定次数
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }
    }


    /**
     * 查询三级分类（从数据源DB查询）
     * 加入分布式锁版本代码，double check检查
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
        // 1.double check，占锁成功需要再次检查缓存
        // 查询非空即返回
        String catlogJSON = redisTemplate.opsForValue().get("catlogJSON");
        if (!StringUtils.isEmpty(catlogJSON)) {
            // 查询成功直接返回不需要查询DB
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catlogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        // 2.查询所有分类，按照parentCid分组
        Map<Long, List<CategoryEntity>> categoryMap = baseMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(key -> key.getParentCid()));

        // 3.获取1级分类
        List<CategoryEntity> level1Categorys = categoryMap.get(0L);

        // 4.封装数据
        Map<String, List<Catelog2Vo>> result = level1Categorys.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), l1Category -> {
            // 3.查询2级分类，并封装成List<Catalog2VO>
            List<Catelog2Vo> catalog2VOS = categoryMap.get(l1Category.getCatId())
                    .stream().map(l2Category -> {
                        // 4.查询3级分类，并封装成List<Catalog3VO>
                        List<Catelog2Vo.Category3Vo> catalog3Vos = categoryMap.get(l2Category.getCatId())
                                .stream().map(l3Category -> {
                                    // 封装3级分类VO
                                    Catelog2Vo.Category3Vo catalog3Vo = new Catelog2Vo.Category3Vo(l2Category.getCatId().toString(), l3Category.getCatId().toString(), l3Category.getName());
                                    return catalog3Vo;
                                }).collect(Collectors.toList());
                        // 封装2级分类VO返回
                        Catelog2Vo catalog2VO = new Catelog2Vo(l1Category.getCatId().toString(), catalog3Vos, l2Category.getCatId().toString(), l2Category.getName());
                        return catalog2VO;
                    }).collect(Collectors.toList());
            return catalog2VOS;
        }));

        // 5.结果集存入redis
        // 关注锁时序问题，存入redis代码块必须在同步快内执行
        redisTemplate.opsForValue().set(CategoryConstant.CACHE_KEY_CATALOG_JSON,
                JSONObject.toJSONString(result), 1, TimeUnit.DAYS);

        return result;
    }

    /**
     * 递归收集所有父节点
     * @param catelogId
     * @param paths
     * @return
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths){
//1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid()!=0){
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归处理获取子分类
     * @param parent 父分类
     * @param all 所有分类
     * @return 已经获取到子分类的分类
     */
    public List<CategoryEntity> getChildren(CategoryEntity parent, List<CategoryEntity> all) {
        List<CategoryEntity> ans = all.stream()
                .filter((menu) -> menu.getParentCid().equals(parent.getCatId())) //Long类型比较需要进行equals
                .map((menu) -> {
                    //递归查找子菜单
                    menu.setChildren(getChildren(menu, all));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    if (menu1.getSort() == null || menu2.getSort() == null) return 0;
                    return menu1.getSort() - menu2.getSort();
                })
                .collect(Collectors.toList());
        return ans;
    }
}