package com.chenwut.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    HttpServletRequest request;

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充(insert)...");
        log.info(metaObject.toString());

        Long currentId = BaseContext.getCurrentId();
        log.info("通过ThreadLocal获取到的currentId: {}", currentId);

        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

//        metaObject.setValue("createUser", request.getSession().getAttribute("employee"));
//        metaObject.setValue("updateUser", request.getSession().getAttribute("employee"));

        metaObject.setValue("createUser", currentId);
        metaObject.setValue("updateUser", currentId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充(update)...");
        log.info(metaObject.toString());

        Long currentId = BaseContext.getCurrentId();
        log.info("通过ThreadLocal获取到的currentId: {}", currentId);

        //在这里获取一下线程id
        long id = Thread.currentThread().getId();
        log.info("doFilter的线程id为：{}", id);

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", currentId);

//        metaObject.setValue("updateUser", request.getSession().getAttribute("employee"));
    }
}