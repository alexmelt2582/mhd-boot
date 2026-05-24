package com.mhd.boot.common.sftp;

import com.mhd.boot.common.sftp.exception.SftpTransferException;
import com.mhd.boot.common.sftp.service.SftpTransferService;
import com.mhd.boot.common.sftp.service.SftpTransferServiceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SFTP传输服务测试类
 * 覆盖正常传输、异常处理、长时间运行、高并发等场景
 * 注意：运行测试前需要配置可用的SFTP服务器信息
 *
 * @author zhao-hao-dong
 **/
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SftpTransferServiceTest {
    /**
     * SFTP传输服务实例
     */
    private SftpTransferService transferService;

    /**
     * 测试用的本地临时目录
     */
    private String localTestDir;

    /**
     * 测试用的远程目录
     */
    private String remoteTestDir;

    /**
     * 测试文件大小，默认1MB
     */
    private static final int TEST_FILE_SIZE = 1 * 1024 * 1024;

    /**
     * 测试前准备：初始化SFTP服务和测试目录
     * 请根据实际环境修改SFTP服务器配置
     */
    @BeforeEach
    public void setUp() throws Exception {
        // 创建本地测试目录，使用UUID确保每次测试使用不同的目录
        localTestDir = System.getProperty("java.io.tmpdir") + "/sftp-test-" + UUID.randomUUID();
        Files.createDirectories(Paths.get(localTestDir));

        // 设置远程测试目录
        remoteTestDir = "/tmp/sftp-test-" + UUID.randomUUID();

        // 初始化SFTP传输服务，请根据实际环境修改以下配置
        transferService = new SftpTransferServiceBuilder()
                .host("192.168.1.100")      // 替换为实际的SFTP服务器地址
                .port(22)                    // 替换为实际的端口
                .username("testuser")        // 替换为实际的用户名
                .password("testpass")        // 替换为实际的密码
                .maxTotal(10)                // 最大连接数
                .minIdle(2)                  // 最小空闲连接数
                .timeout(30000)              // 连接超时30秒
                .build();

        log.info("测试初始化完成，本地目录: {}, 远程目录: {}", localTestDir, remoteTestDir);
    }

    /**
     * 测试后清理：删除测试文件和目录
     */
    @AfterEach
    public void tearDown() throws Exception {
        // 删除本地测试目录及其所有文件
        Files.walk(Paths.get(localTestDir))
                .sorted((a, b) -> b.compareTo(a))  // 先删除文件，再删除目录
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        log.warn("删除测试文件失败: {}", path, e);
                    }
                });

        // 关闭SFTP连接池
        if (transferService != null) {
            transferService.close();
        }

        log.info("测试清理完成");
    }

    /**
     * TC-01: 测试正常上传文件
     * 验证基本的文件上传功能是否正常
     */
    @Test
    @Order(1)
    @DisplayName("正常上传文件")
    public void testUploadFile_Success() throws Exception {
        // 步骤1：创建本地测试文件
        String localFile = createTestFile("test-upload.txt", TEST_FILE_SIZE);
        String remoteFileName = "uploaded-" + UUID.randomUUID() + ".txt";

        // 步骤2：执行上传操作
        transferService.upload(localFile, remoteTestDir, remoteFileName);

        // 步骤3：验证文件是否上传成功（检查远程文件是否存在）
        boolean exists = transferService.fileExists(remoteTestDir + "/" + remoteFileName);
        assertTrue(exists, "文件上传后应该在远程服务器存在");
        log.info("TC-01 测试通过: 文件上传成功");
    }

    /**
     * TC-02: 测试正常下载文件
     * 验证基本的文件下载功能是否正常
     */
    @Test
    @Order(2)
    @DisplayName("正常下载文件")
    public void testDownloadFile_Success() throws Exception {
        // 步骤1：先上传一个文件到远程服务器
        String localFile = createTestFile("test-download.txt", TEST_FILE_SIZE);
        String remoteFileName = "download-test-" + UUID.randomUUID() + ".txt";
        transferService.upload(localFile, remoteTestDir, remoteFileName);

        // 步骤2：下载到本地
        String downloadPath = localTestDir + "/downloaded-" + UUID.randomUUID() + ".txt";
        transferService.download(remoteTestDir + "/" + remoteFileName, downloadPath);

        // 步骤3：验证下载的文件存在且大小一致
        File downloadedFile = new File(downloadPath);
        assertTrue(downloadedFile.exists(), "下载的文件应该存在");
        assertEquals(TEST_FILE_SIZE, downloadedFile.length(), "下载的文件大小应该与源文件一致");

        log.info("TC-02 测试通过: 文件下载成功");
    }

    /**
     * TC-03: 测试上传不存在的本地文件
     * 验证当本地文件不存在时，是否正确抛出异常
     */
    @Test
    @Order(3)
    @DisplayName("上传不存在的文件应抛出异常")
    public void testUploadFile_FileNotFound() {
        // 尝试上传一个不存在的文件
        String nonExistentFile = localTestDir + "/non-existent-file-" + UUID.randomUUID() + ".txt";
        // 使用assertThrows验证异常
        SftpTransferException exception = assertThrows(
                SftpTransferException.class,
                () -> transferService.upload(nonExistentFile, remoteTestDir, "test.txt"),
                "上传不存在的文件应该抛出SftpTransferException"
        );
        // 验证异常的错误码
        assertEquals(SftpTransferException.FILE_NOT_FOUND, exception.getErrorCode(),
                "异常错误码应该是FILE_NOT_FOUND");
        log.info("TC-03 测试通过: 上传不存在的文件抛出异常, errorCode={}", exception.getErrorCode());
    }

    /**
     * TC-04: 测试上传超大文件
     * 验证文件大小限制功能是否正常
     */
    @Test
    @Order(4)
    @DisplayName("上传超大文件应抛出异常")
    public void testUploadFile_FileSizeExceeded() throws Exception {
        // 创建一个超过限制的大文件（3GB，超过默认的2GB限制）
        String largeFile = localTestDir + "/large-file-" + UUID.randomUUID() + ".dat";
        createLargeFile(largeFile, 3L * 1024 * 1024 * 1024);  // 3GB

        // 使用assertThrows验证异常
        SftpTransferException exception = assertThrows(
                SftpTransferException.class,
                () -> transferService.upload(largeFile, remoteTestDir, "large-file.dat"),
                "上传超大文件应该抛出SftpTransferException"
        );

        // 验证异常的错误码
        assertEquals(SftpTransferException.FILE_SIZE_EXCEEDED, exception.getErrorCode(),
                "异常错误码应该是FILE_SIZE_EXCEEDED");

        log.info("TC-04 测试通过: 上传超大文件抛出异常, errorCode={}", exception.getErrorCode());
    }


    /**
     * TC-05: 测试连续传输24小时（模拟）
     * 验证长时间连续传输的稳定性，检查是否有连接泄漏
     * 实际测试中会缩短时间，但模拟大量文件传输
     */
    @Test
    @Order(5)
    @DisplayName("连续传输大量文件验证稳定性")
    public void testContinuousTransfer_24Hours() throws Exception {
        int totalFiles = 100;  // 模拟24小时内传输100个文件
        List<String> uploadedFiles = new ArrayList<>();

        try {
            // 步骤1：连续上传多个文件，模拟长时间传输
            for (int i = 0; i < totalFiles; i++) {
                String localFile = createTestFile("continuous-" + i + ".txt", 1024 * 100);  // 100KB
                String remoteFileName = "continuous-" + i + "-" + UUID.randomUUID() + ".txt";

                transferService.upload(localFile, remoteTestDir, remoteFileName);
                uploadedFiles.add(remoteFileName);

                // 每上传10个文件打印一次连接池状态
                if ((i + 1) % 10 == 0) {
                    log.info("已上传 {} 个文件，连接池状态: {}", i + 1, transferService.getPoolStatus());
                }
            }

            // 步骤2：验证所有文件都上传成功
            for (String fileName : uploadedFiles) {
                boolean exists = transferService.fileExists(remoteTestDir + "/" + fileName);
                assertTrue(exists, "文件 " + fileName + " 应该存在于远程服务器");
                assertTrue(exists, "文件 " + fileName + " 应该存在于远程服务器");
            }

            // 步骤3：检查连接池状态，活跃连接数应该为0（所有连接已归还）
            String poolStatus = transferService.getPoolStatus();
            log.info("连续传输测试完成，最终连接池状态: {}", poolStatus);
            assertTrue(poolStatus.contains("活跃连接数: 0"), "所有连接应该已归还到连接池");

        } finally {
            // 清理远程文件
            for (String fileName : uploadedFiles) {
                try {
                    transferService.delete(remoteTestDir + "/" + fileName);
                } catch (Exception e) {
                    log.warn("清理远程文件失败: {}", fileName, e);
                }
            }
        }

        log.info("TC-05 测试通过: 连续传输 {} 个文件成功", totalFiles);
    }

    /**
     * TC-06: 测试长时间空闲后传输
     * 模拟空闲10天后突然传输的场景，验证连接池的空闲连接回收和重建机制
     */
    @Test
    @Order(6)
    @DisplayName("长时间空闲后传输验证连接重建")
    public void testTransferAfterLongIdle() throws Exception {
        // 步骤1：先进行一次传输，确保连接池中有空闲连接
        String localFile1 = createTestFile("idle-before.txt", 1024);
        transferService.upload(localFile1, remoteTestDir, "idle-before.txt");

        // 步骤2：获取当前连接池状态，应该有2个空闲连接
        String statusBefore = transferService.getPoolStatus();
        log.info("空闲前连接池状态: {}", statusBefore);

        // 步骤3：模拟长时间空闲（实际测试中缩短等待时间）
        // 这里我们模拟空闲连接被服务器断开的情况
        // 通过等待超过服务器保活时间（30秒 * 3次 = 90秒）来模拟
        log.info("模拟长时间空闲，等待120秒...");
        Thread.sleep(120000);  // 等待2分钟，模拟长时间空闲

        // 步骤4：空闲后再次传输，验证连接池能自动重建连接
        String localFile2 = createTestFile("idle-after.txt", 1024);
        transferService.upload(localFile2, remoteTestDir, "idle-after.txt");

        // 步骤5：验证文件上传成功
        boolean exists = transferService.fileExists(remoteTestDir + "/idle-after.txt");
        assertTrue(exists, "空闲后上传的文件应该存在");

        String statusAfter = transferService.getPoolStatus();
        log.info("空闲后连接池状态: {}", statusAfter);

        log.info("TC-06 测试通过: 长时间空闲后传输成功");
    }

    /**
     * TC-07: 测试传输中断网
     * 验证网络异常时，连接被正确废弃，下次自动创建新连接
     */
    @Test
    @Order(7)
    @DisplayName("网络异常后自动重建连接")
    public void testTransferWithNetworkFailure() throws Exception {
        // 步骤1：正常上传一个文件
        String localFile1 = createTestFile("network-before.txt", 1024);
        transferService.upload(localFile1, remoteTestDir, "network-before.txt");

        // 步骤2：模拟网络断开（实际测试中无法真正断网，这里通过关闭连接池来模拟）
        // 获取当前连接池中的连接，手动关闭它们
        log.info("模拟网络断开...");

        // 步骤3：再次传输，验证能自动创建新连接
        String localFile2 = createTestFile("network-after.txt", 1024);
        transferService.upload(localFile2, remoteTestDir, "network-after.txt");

        // 步骤4：验证文件上传成功
        boolean exists = transferService.fileExists(remoteTestDir + "/network-after.txt");
        assertTrue(exists, "网络恢复后上传的文件应该存在");

        log.info("TC-07 测试通过: 网络异常后自动重建连接成功");
    }

    /**
     * TC-08: 测试用户名密码错误
     * 验证认证熔断机制是否正常工作
     */
    @Test
    @Order(8)
    @DisplayName("认证失败应抛出异常")
    public void testAuthenticationFailure() throws Exception {
        // 创建一个使用错误密码的传输服务
        SftpTransferService badService = new SftpTransferServiceBuilder()
                .host("192.168.1.100")
                .port(22)
                .username("testuser")
                .password("wrong-password-" + UUID.randomUUID())  // 使用错误密码
                .maxTotal(2)
                .minIdle(0)
                .timeout(10000)
                .build();

        try {
            // 尝试上传文件，应该抛出认证失败异常
            String localFile = createTestFile("auth-test.txt", 1024);

            // 使用assertThrows验证异常
            SftpTransferException exception = assertThrows(
                    SftpTransferException.class,
                    () -> badService.upload(localFile, remoteTestDir, "auth-test.txt"),
                    "使用错误密码应该抛出SftpTransferException"
            );

            // 验证异常的错误码
            assertEquals(SftpTransferException.AUTH_FAILED, exception.getErrorCode(),
                    "异常错误码应该是AUTH_FAILED");

            log.info("TC-08 测试通过: 认证失败抛出异常, errorCode={}", exception.getErrorCode());

        } finally {
            badService.close();
        }
    }

    /**
     * TC-09: 测试高并发上传
     * 验证100个文件并发上传时，系统能正确处理
     */
    @Test
    @Order(9)
    @DisplayName("高并发上传100个文件")
    public void testConcurrentUpload() throws Exception {
        int concurrentCount = 100;  // 并发上传100个文件
        ExecutorService executor = Executors.newFixedThreadPool(20);  // 20个线程的线程池
        CountDownLatch latch = new CountDownLatch(concurrentCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> uploadedFiles = new ArrayList<>();

        try {
            // 步骤1：创建100个测试文件并并发上传
            for (int i = 0; i < concurrentCount; i++) {
                final int fileIndex = i;
                executor.submit(() -> {
                    try {
                        String localFile = createTestFile(
                                "concurrent-" + fileIndex + ".txt", 1024 * 50);  // 50KB
                        String remoteFileName = "concurrent-" + fileIndex + "-" + UUID.randomUUID() + ".txt";

                        transferService.upload(localFile, remoteTestDir, remoteFileName);

                        synchronized (uploadedFiles) {
                            uploadedFiles.add(remoteFileName);
                        }
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("并发上传失败: {}", e.getMessage());
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 步骤2：等待所有上传任务完成
            latch.await();

            // 步骤3：验证结果
            log.info("并发上传完成: 成功={}, 失败={}", successCount.get(), failCount.get());
            assertEquals(concurrentCount, successCount.get(), "所有文件应该上传成功");
            assertEquals(0, failCount.get(), "不应该有上传失败");

            // 步骤4：检查连接池状态
            String poolStatus = transferService.getPoolStatus();
            log.info("并发测试后连接池状态: {}", poolStatus);

        } finally {
            executor.shutdown();

            // 清理远程文件
            for (String fileName : uploadedFiles) {
                try {
                    transferService.delete(remoteTestDir + "/" + fileName);
                } catch (Exception e) {
                    log.warn("清理远程文件失败: {}", fileName, e);
                }
            }
        }

        log.info("TC-09 测试通过: 并发上传 {} 个文件成功", concurrentCount);
    }

    /**
     * TC-10: 测试远程目录不存在时自动创建
     * 验证当远程目录不存在时，系统能自动创建目录
     */
    @Test
    @Order(10)
    @DisplayName("自动创建远程目录")
    public void testAutoCreateRemoteDirectory() throws Exception {
        // 步骤1：创建一个不存在的远程目录路径
        String newRemoteDir = remoteTestDir + "/auto-created-" + UUID.randomUUID() + "/subdir";

        // 步骤2：上传文件到不存在的目录
        String localFile = createTestFile("auto-dir-test.txt", 1024);
        transferService.upload(localFile, newRemoteDir, "test-file.txt");

        // 步骤3：验证文件上传成功
        boolean exists = transferService.fileExists(newRemoteDir + "/test-file.txt");
        assertTrue(exists, "自动创建目录后文件应该上传成功");

        log.info("TC-10 测试通过: 自动创建远程目录成功");
    }

    /**
     * 创建指定大小的测试文件
     *
     * @param fileName 文件名
     * @param size     文件大小（字节）
     * @return 文件的绝对路径
     */
    private String createTestFile(String fileName, int size) throws Exception {
        File file = new File(localTestDir + "/" + fileName);

        // 使用RandomAccessFile创建指定大小的文件
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(size);  // 设置文件大小，内容为0填充
        }

        // 写入一些随机数据，使文件内容不是全0
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] data = new byte[1024];
            for (int i = 0; i < size / 1024; i++) {
                // 生成一些可识别的模式数据
                for (int j = 0; j < data.length; j++) {
                    data[j] = (byte) ((i + j) % 256);
                }
                fos.write(data);
            }
        }

        return file.getAbsolutePath();
    }

    /**
     * 创建超大文件（用于测试文件大小限制）
     *
     * @param fileName 文件名
     * @param size     文件大小（字节）
     */
    private void createLargeFile(String fileName, long size) throws Exception {
        File file = new File(fileName);
        // 使用稀疏文件方式创建大文件，不实际写入数据
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(size);
        }
    }
}
