package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    XcCompanyRepository xcCompanyRepository;

    @Autowired
    XcUserRepository xcUserRepository;

    @Autowired
    XcMenuMapper xcMenuMapper;

    //根据username 查询用户信息
    public XcUser findXcUserByUserName(String username) {
        return xcUserRepository.findByUsername(username);
    }

    //根据账号查询用户信息
    public XcUserExt getUserExt(String username) {
        //查询 基本信息
        XcUser xcUser = this.findXcUserByUserName(username);
        if (xcUser == null) {
            return null;
        }
        String userId = xcUser.getId();
        //查询用户所有权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        //查询公司信息
        XcCompanyUser xcCompanyUser = xcCompanyRepository.findXcCompanyUserByUserId(userId);
        String companyId = null;
        if (xcCompanyUser != null) {
            companyId = xcCompanyUser.getCompanyId();
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        xcUserExt.setCompanyId(companyId);
        xcUserExt.setPermissions(xcMenus);
        return xcUserExt;
    }


}
