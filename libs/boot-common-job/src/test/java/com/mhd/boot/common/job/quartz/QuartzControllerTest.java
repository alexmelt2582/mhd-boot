//package com.mhd.boot.common.job.quartz;
//
//import com.mhd.boot.common.job.quartz.core.scheduler.SchedulerManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.quartz.SchedulerException;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.doNothing;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * @author zhao-hao-dong
// **/
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = TestController.class)
//public class QuartzControllerTest {
//    @Mock
//    private SchedulerManager schedulerManager;
//
//    // 模拟的 JobKey 或名称
//    private static final String JOB_KEY = "testJob";
//
//    @InjectMocks
//    private TestController testController;
//
//    private MockMvc mockMvc;
//
//    /**
//     * 在每个测试方法前初始化 MockMvc
//     */
//    @BeforeEach
//    void setUp() throws SchedulerException {
//        // 手动构建 MockMvc，传入被测试的 Controller
//        this.mockMvc = MockMvcBuilders.standaloneSetup(testController).build();
//
//        // 设置宽松模式，避免未定义的 setter 方法报错
//        Mockito.lenient().doNothing().when(schedulerManager).resumeJob(anyString());
//    }
//
//    /**
//     * 测试 /test/hello 接口
//     */
//    @Test
//    void testHello() throws Exception {
//        mockMvc.perform(get("/test/hello"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("hello"));
//    }
//
//    /**
//     * 测试 /test/add 接口
//     */
//    @Test
//    void testAddJob() throws Exception {
//        // 模拟 Service 层行为
//        doNothing().when(schedulerManager).addJob(
//                anyLong(), anyString(), isNull(), anyString(), isNull(), isNull()
//        );
//
//        mockMvc.perform(get("/test/add"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("add"));
//    }
//
//    /**
//     * 测试 /test/start 接口
//     */
//    @Test
//    void testStartJob() throws Exception {
//        doNothing().when(schedulerManager).resumeJob(anyString());
//
//        mockMvc.perform(get("/test/start"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("start"));
//    }
//
//    /**
//     * 测试 /test/pause 接口
//     */
//    @Test
//    void testPauseJob() throws Exception {
//        doNothing().when(schedulerManager).pauseJob(anyString());
//
//        mockMvc.perform(get("/test/pause"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("pause"));
//    }
//
//    /**
//     * 测试 /test/delete 接口
//     */
//    @Test
//    void testDeleteJob() throws Exception {
//        doNothing().when(schedulerManager).deleteJob(anyString());
//
//        mockMvc.perform(get("/test/delete"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("delete"));
//    }
//
//    /**
//     * 测试 /test/trigger 接口
//     */
//    @Test
//    void testTriggerJob() throws Exception {
//        doNothing().when(schedulerManager).triggerJob(anyLong(), anyString(), isNull());
//
//        mockMvc.perform(get("/test/trigger"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("trigger"));
//    }
//
//    /**
//     * 测试 /test/update 接口
//     */
//    @Test
//    void testUpdateJob() throws Exception {
//        doNothing().when(schedulerManager).updateJob(
//                anyString(), isNull(), anyString(), isNull(), isNull()
//        );
//
//        mockMvc.perform(get("/test/update"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("trigger")); // 注意：原代码返回的是 "trigger"
//    }
//}
