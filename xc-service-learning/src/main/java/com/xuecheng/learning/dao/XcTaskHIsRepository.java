package com.xuecheng.learning.dao;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XcTaskHIsRepository extends JpaRepository<XcTaskHis, String> {

}
