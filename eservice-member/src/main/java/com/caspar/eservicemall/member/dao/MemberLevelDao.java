package com.caspar.eservicemall.member.dao;

import com.caspar.eservicemall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author casparZheng
 * @email 824394795@qq.com
 * @date 2023-02-27 02:48:25
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();
}
