package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompany;
import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcCompanyRepository extends JpaRepository<XcCompanyUser, String> {

    //根据用户id 查询公司信息
    XcCompanyUser findXcCompanyUserByUserId(String userId);

}
