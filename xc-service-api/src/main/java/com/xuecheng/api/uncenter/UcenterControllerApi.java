package com.xuecheng.api.uncenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;

@Api(value = "用户中心", description = "用户管理中心")
public interface UcenterControllerApi {

    XcUserExt gutUserExt(String username);

}
