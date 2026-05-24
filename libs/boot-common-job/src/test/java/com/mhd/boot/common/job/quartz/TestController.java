//package com.mhd.boot.common.job.quartz;
//
//import com.mhd.boot.common.job.quartz.core.scheduler.SchedulerManager;
//import jakarta.annotation.Resource;
//import org.quartz.SchedulerException;
//
///**
// * @author zhao-hao-dong
// **/
//@RestController
//@RequestMapping(value = "/test")
//public class TestController {
//
//    @Resource
//    private SchedulerManager schedulerManager;
//
//    @RequestMapping(value = "/hello")
//    @OperateLog(module = "测试类", description = "hello", type = OperateTypeEnum.GET, logResultData = true)
//    public String hello() {
//        return "hello";
//    }
//
//    @RequestMapping(value = "/add")
//    @OperateLog(module = "添加Job", description = "add", type = OperateTypeEnum.GET, logResultData = true)
//    public String add() throws SchedulerException {
//        schedulerManager.addJob(1L, "testJob", null, "0/5 * * * * ?", null, null);
//        return "add";
//    }
//
//    @RequestMapping(value = "/start")
//    @OperateLog(module = "启动Job", description = "start", type = OperateTypeEnum.GET, logResultData = true)
//    public String start() throws SchedulerException {
//        schedulerManager.resumeJob("testJob");
//        return "start";
//    }
//
//    @RequestMapping(value = "/pause")
//    @OperateLog(module = "暂停Job", description = "pause", type = OperateTypeEnum.GET, logResultData = true)
//    public String pause() throws SchedulerException {
//        schedulerManager.pauseJob("testJob");
//        return "pause";
//    }
//
//    @RequestMapping(value = "/delete")
//    @OperateLog(module = "删除Job", description = "delete", type = OperateTypeEnum.GET, logResultData = true)
//    public String delete() throws SchedulerException {
//        schedulerManager.deleteJob("testJob");
//        return "delete";
//    }
//
//    @RequestMapping(value = "/trigger")
//    @OperateLog(module = "立即执行Job", description = "trigger", type = OperateTypeEnum.GET, logResultData = true)
//    public String trigger() throws SchedulerException {
//        schedulerManager.triggerJob(1L, "testJob", null);
//        return "trigger";
//    }
//
//    @RequestMapping(value = "/update")
//    @OperateLog(module = "更新Job", description = "update", type = OperateTypeEnum.GET, logResultData = true)
//    public String update() throws SchedulerException {
//        schedulerManager.updateJob("testJob", null, "0/5 * * * * ?", null, null);
//        return "trigger";
//    }
//}
