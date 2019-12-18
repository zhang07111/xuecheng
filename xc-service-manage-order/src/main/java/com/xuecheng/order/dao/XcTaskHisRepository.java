package com.xuecheng.order.dao;


import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;

public interface XcTaskHisRepository extends JpaRepository<XcTaskHis, String> {

    //查询某个时间之间的前n条任务
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);

    //更新updateTime
    @Modifying
    @Query("update XcTask t set t.updateTime = :updateTime where t.id = :id")
    int updateTaskTime(@PathVariable("id") String id, @PathVariable("updateTime") Date updateTime);

    @Modifying
    @Query("update XcTask t set t.version = :version+1 where t.id = :id and  t.version = :version")
    int updateTaskVersion(@PathVariable("id") String id, @PathVariable("version") int version);

}
