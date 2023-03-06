package com.caspar.eservicemall.product.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.Query;

import com.caspar.eservicemall.product.dao.CategoryDao;
import com.caspar.eservicemall.product.entity.CategoryEntity;
import com.caspar.eservicemall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
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