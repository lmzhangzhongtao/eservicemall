package com.caspar.eservicemall.member.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caspar.eservicemall.common.utils.PageUtils;
import com.caspar.eservicemall.common.utils.Query;

import com.caspar.eservicemall.member.dao.MemberReceiveAddressDao;
import com.caspar.eservicemall.member.entity.MemberReceiveAddressEntity;
import com.caspar.eservicemall.member.service.MemberReceiveAddressService;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据会员ID查询会员收货地址列表
     */
    @Override
    public List<MemberReceiveAddressEntity> getAddress(Long memberId) {
        return baseMapper.selectList(new QueryWrapper<MemberReceiveAddressEntity>()
                .eq("member_id", memberId));
    }

}