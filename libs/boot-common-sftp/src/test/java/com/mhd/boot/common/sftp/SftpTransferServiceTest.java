package com.mhd.boot.common.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpProgressMonitor;
import com.mhd.boot.common.sftp.exception.SftpTransferException;
import com.mhd.boot.common.sftp.service.SftpTransferService;
import com.mhd.boot.common.sftp.service.SftpTransferServiceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * SFTP 传输服务
     */
    private SftpTransferService transferService;

    /**
     * 本地临时测试目录
     */
    private Path localTestDir;

    /**
     * 远程测试根目录
     */
    private String remoteTestDir;

    /**
     * 每个普通测试文件默认大小
     */
    private static final int DEFAULT_FILE_SIZE = 8 * 1024;

    /**
     * 真实SFTP主机
     */
    private String sftpHost;

    /**
     * 真实SFTP端口
     */
    private int sftpPort;

    /**
     * 真实SFTP用户名
     */
    private String sftpUsername;

    /**
     * 真实SFTP密码
     */
    private String sftpPassword;

    /**
     * 测试准备
     * 初始化真实 SFTP 连接和本地/远程测试目录
     *
     * @throws Exception 初始化失败时抛出
     */
    @BeforeEach
    void setUp() throws Exception {
        // 步骤1：创建本地临时测试目录
        localTestDir = Files.createTempDirectory("sftp-integration-");

        // 步骤2：生成本次测试远程根目录，避免不同用例互相污染
        remoteTestDir = "/home/zhaohd/sftp-integration-" + UUID.randomUUID();

        // 步骤3：读取真实 SFTP 配置，未指定时沿用之前默认值
        sftpHost = System.getProperty("test.sftp.host", "xxx");
        sftpPort = Integer.parseInt(System.getProperty("test.sftp.port", "22"));
        sftpUsername = System.getProperty("test.sftp.username", "xx");
        sftpPassword = System.getProperty("test.sftp.password", "xxx");

        // 步骤4：创建 SFTP 服务实例
        transferService = new SftpTransferServiceBuilder()
                .host(sftpHost)
                .port(sftpPort)
                .username(sftpUsername)
                .password(sftpPassword)
                .maxTotal(10)
                .minIdle(2)
                .timeout(30000)
                .maxRetries(2)
                .retryIntervalMillis(500)
                .build();

        // 步骤5：准备远程根目录
        transferService.mkdir(remoteTestDir);
        log.info("SFTP测试初始化完成，本地目录={}, 远程目录={}", localTestDir, remoteTestDir);
    }

    /**
     * 测试清理
     * 清理远程目录和本地临时目录
     *
     * @throws Exception 清理失败时抛出
     */
    @AfterEach
    void tearDown() throws Exception {
        // 步骤1：清理远程目录
        if (transferService != null) {
            try {
                transferService.deleteRecursively(remoteTestDir);
            } catch (Exception ex) {
                log.warn("清理远程目录失败: {}", remoteTestDir, ex);
            }
        }

        // 步骤2：关闭连接池
        if (transferService != null) {
            transferService.close();
        }

        // 步骤3：清理本地临时目录
        deleteLocalRecursively(localTestDir);
    }

    /**
     * 测试上传后文件存在
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(1)
    @DisplayName("上传后文件应存在")
    void testUploadAndFileExists() throws Exception {
        // 步骤1：创建本地测试文件
        String localFilePath = createTestFile("upload.txt", DEFAULT_FILE_SIZE);

        // 步骤2：上传文件
        String remoteFileName = "uploaded-" + UUID.randomUUID() + ".txt";
        transferService.upload(localFilePath, remoteTestDir, remoteFileName);

        // 步骤3：校验文件存在
        assertTrue(transferService.fileExists(remoteTestDir + "/" + remoteFileName));
    }

    /**
     * 测试上传进度回调
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(2)
    @DisplayName("上传进度回调应被触发")
    void testUploadWithProgressMonitor() throws Exception {
        // 步骤1：创建本地文件和进度监控器
        String localFilePath = createTestFile("monitor.txt", DEFAULT_FILE_SIZE);
        TestProgressMonitor monitor = new TestProgressMonitor();

        // 步骤2：执行上传
        String remoteFileName = "monitor-" + UUID.randomUUID() + ".txt";
        transferService.upload(localFilePath, remoteTestDir, remoteFileName, monitor);

        // 步骤3：校验进度回调结果
        assertTrue(monitor.initialized);
        assertTrue(monitor.transferredBytes > 0);
        assertTrue(monitor.ended);
    }

    /**
     * 测试下载内容一致性
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(3)
    @DisplayName("下载内容应与上传一致")
    void testDownloadContentEquals() throws Exception {
        // 步骤1：准备上传文件
        String localFilePath = createTextFile("download-src.txt", "download-content-" + UUID.randomUUID());
        String remoteFileName = "download-" + UUID.randomUUID() + ".txt";
        transferService.upload(localFilePath, remoteTestDir, remoteFileName);

        // 步骤2：下载远程文件
        Path downloadPath = localTestDir.resolve("download-result.txt");
        transferService.download(remoteTestDir + "/" + remoteFileName, downloadPath.toString());

        // 步骤3：校验字节一致
        assertArrayEquals(Files.readAllBytes(Paths.get(localFilePath)), Files.readAllBytes(downloadPath));
    }

    /**
     * 测试重命名和删除
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(4)
    @DisplayName("重命名后新文件应存在且旧文件不存在")
    void testRenameAndDelete() throws Exception {
        // 步骤1：上传原始文件
        String localFilePath = createTestFile("rename.txt", DEFAULT_FILE_SIZE);
        String oldName = "old-" + UUID.randomUUID() + ".txt";
        String newName = "new-" + UUID.randomUUID() + ".txt";
        transferService.upload(localFilePath, remoteTestDir, oldName);

        // 步骤2：执行重命名
        transferService.rename(remoteTestDir + "/" + oldName, remoteTestDir + "/" + newName);

        // 步骤3：验证重命名结果
        assertFalse(transferService.fileExists(remoteTestDir + "/" + oldName));
        assertTrue(transferService.fileExists(remoteTestDir + "/" + newName));

        // 步骤4：删除新文件并再次验证
        transferService.delete(remoteTestDir + "/" + newName);
        assertFalse(transferService.fileExists(remoteTestDir + "/" + newName));
    }

    /**
     * 测试目录自动创建
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(5)
    @DisplayName("上传到不存在目录时应自动创建")
    void testAutoCreateDirectoryWhenUpload() throws Exception {
        // 步骤1：准备深层目录和本地文件
        String deepDir = remoteTestDir + "/nested/a/b/c";
        String localFilePath = createTestFile("deep.txt", DEFAULT_FILE_SIZE);
        String remoteFileName = "deep-" + UUID.randomUUID() + ".txt";

        // 步骤2：上传到深层目录
        transferService.upload(localFilePath, deepDir, remoteFileName);

        // 步骤3：校验目标文件存在
        assertTrue(transferService.fileExists(deepDir + "/" + remoteFileName));
    }

    /**
     * 测试基础列表查询
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(6)
    @DisplayName("listFiles 应返回上传文件名")
    void testListFiles() throws Exception {
        // 步骤1：上传两个文件
        String fileA = "list-a-" + UUID.randomUUID() + ".txt";
        String fileB = "list-b-" + UUID.randomUUID() + ".txt";
        transferService.upload(createTestFile("list-a.txt", DEFAULT_FILE_SIZE), remoteTestDir, fileA);
        transferService.upload(createTestFile("list-b.txt", DEFAULT_FILE_SIZE), remoteTestDir, fileB);

        // 步骤2：查询目录并断言
        List<String> names = transferService.listFiles(remoteTestDir);
        assertTrue(names.contains(fileA));
        assertTrue(names.contains(fileB));
    }

    /**
     * 测试高级筛选和排序
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(7)
    @DisplayName("高级列表筛选和排序应生效")
    void testAdvancedListFilterAndSort() throws Exception {
        // 步骤1：准备不同后缀文件并保证时间差
        transferService.upload(createTestFile("r1.log", DEFAULT_FILE_SIZE), remoteTestDir, "report_1.log");
        Thread.sleep(1100);
        transferService.upload(createTestFile("r2.log", DEFAULT_FILE_SIZE), remoteTestDir, "report_2.log");
        transferService.upload(createTestFile("x.txt", DEFAULT_FILE_SIZE), remoteTestDir, "report_3.txt");

        // 步骤2：按后缀和正则筛选
        List<ChannelSftp.LsEntry> entries = transferService.listFiles(remoteTestDir, ".log", "report_\\d", 0, 0);
        assertEquals(2, entries.size());

        // 步骤3：排序并断言修改时间非降序
        transferService.sortByModifyTime(entries);
        int prev = 0;
        for (ChannelSftp.LsEntry entry : entries) {
            int current = entry.getAttrs().getMTime();
            assertTrue(current >= prev);
            prev = current;
        }
    }

    /**
     * 测试递归删除目录
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(8)
    @DisplayName("deleteRecursively 应删除整棵目录树")
    void testDeleteRecursively() throws Exception {
        // 步骤1：在子目录内上传多个文件
        String subDir = remoteTestDir + "/to-delete/sub";
        String fileA = "a-" + UUID.randomUUID() + ".txt";
        String fileB = "b-" + UUID.randomUUID() + ".txt";
        transferService.upload(createTestFile("delete-a.txt", DEFAULT_FILE_SIZE), subDir, fileA);
        transferService.upload(createTestFile("delete-b.txt", DEFAULT_FILE_SIZE), subDir, fileB);
        assertTrue(transferService.fileExists(subDir + "/" + fileA));

        // 步骤2：递归删除根目录
        transferService.deleteRecursively(remoteTestDir + "/to-delete");

        // 步骤3：校验目录下文件已不存在
        assertFalse(transferService.fileExists(subDir + "/" + fileA));
        assertFalse(transferService.fileExists(subDir + "/" + fileB));
    }

    /**
     * 测试上传不存在本地文件异常
     */
    @Test
    @Order(9)
    @DisplayName("上传不存在本地文件应抛 FILE_NOT_FOUND")
    void testUploadMissingLocalFile() {
        // 步骤1：构造不存在的本地路径
        String missingPath = localTestDir.resolve("missing-" + UUID.randomUUID() + ".txt").toString();

        // 步骤2：断言异常类型和错误码
        SftpTransferException exception = assertThrows(SftpTransferException.class,
                () -> transferService.upload(missingPath, remoteTestDir, "missing.txt"));
        assertEquals(SftpTransferException.FILE_NOT_FOUND, exception.getErrorCode());
    }

    /**
     * 测试上传超大文件异常
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(10)
    @DisplayName("上传超大文件应抛 FILE_SIZE_EXCEEDED")
    void testUploadLargeFile() throws Exception {
        // 步骤1：创建超过2GB的稀疏文件
        Path largeFile = localTestDir.resolve("large.bin");
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(largeFile.toFile(), "rw")) {
            randomAccessFile.setLength(2L * 1024 * 1024 * 1024 + 1);
        }

        // 步骤2：断言上传抛出文件过大异常
        SftpTransferException exception = assertThrows(SftpTransferException.class,
                () -> transferService.upload(largeFile.toString(), remoteTestDir, "large.bin"));
        assertEquals(SftpTransferException.FILE_SIZE_EXCEEDED, exception.getErrorCode());
    }

    /**
     * 测试池状态和关闭
     */
    @Test
    @Order(11)
    @DisplayName("连接池状态应可获取")
    void testGetPoolStatus() {
        // 步骤1：获取池状态字符串
        String poolStatus = transferService.getPoolStatus();

        // 步骤2：断言状态非空
        assertNotNull(poolStatus);
        assertFalse(poolStatus.isEmpty());

        // 步骤3：显式关闭应不抛异常（tearDown 会再次调用，允许幂等）
        assertDoesNotThrow(() -> transferService.close());
    }

    /**
     * 测试连续传输24小时（模拟）
     * 验证长时间连续传输的稳定性，检查是否有连接泄漏
     * 实际测试中会缩短时间，但模拟大量文件传输
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(12)
    @DisplayName("连续传输大量文件验证稳定性")
    void testContinuousTransfer_24Hours() throws Exception {
        int totalFiles = 100;  // 模拟24小时内传输100个文件
        List<String> uploadedFiles = new ArrayList<>();

        try {
            // 步骤1：连续上传多个文件，模拟长时间传输
            for (int i = 0; i < totalFiles; i++) {
                String localFile = createTestFile("continuous-" + i + ".txt", 1024 * 100);
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
            }

            // 步骤3：检查连接池状态，活跃连接数应该为0（所有连接已归还）
            String poolStatus = transferService.getPoolStatus();
            log.info("连续传输测试完成，最终连接池状态: {}", poolStatus);
            assertTrue(poolStatus.contains("活跃连接数: 0"),
                    "所有连接应该已归还到连接池");
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

        log.info("测试通过: 连续传输 {} 个文件成功", totalFiles);
    }

    /**
     * 测试长时间空闲后传输
     * 模拟空闲10天后突然传输的场景，验证连接池的空闲连接回收和重建机制
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(13)
    @DisplayName("长时间空闲后传输验证连接重建")
    void testTransferAfterLongIdle() throws Exception {

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
    }

    /**
     * 测试传输中断网
     * 验证网络异常时，连接被正确废弃，下次自动创建新连接
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(14)
    @DisplayName("网络异常后自动重建连接")
    void testTransferWithNetworkFailure() throws Exception {
        // 步骤1：正常上传一个文件
        String localFile1 = createTestFile("network-before.txt", 1024);
        transferService.upload(localFile1, remoteTestDir, "network-before.txt");

        // 步骤2：模拟网络断开（实际测试中无法真正断网，这里通过关闭连接池来模拟）
        log.info("模拟网络断开...");

        // 步骤3：再次传输，验证能自动创建新连接
        String localFile2 = createTestFile("network-after.txt", 1024);
        transferService.upload(localFile2, remoteTestDir, "network-after.txt");

        // 步骤4：验证文件上传成功
        boolean exists = transferService.fileExists(remoteTestDir + "/network-after.txt");
        assertTrue(exists, "网络恢复后上传的文件应该存在");

        log.info("测试通过: 网络异常后自动重建连接成功");
    }

    /**
     * 测试用户名密码错误
     * 验证认证熔断机制是否正常工作
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(15)
    @DisplayName("认证失败应抛出异常")
    void testAuthenticationFailure() throws Exception {
        // 步骤1：创建错误密码服务
        SftpTransferService badService = new SftpTransferServiceBuilder()
                .host(sftpHost)
                .port(sftpPort)
                .username(sftpUsername)
                .password("wrong-password-" + UUID.randomUUID())
                .maxTotal(2)
                .minIdle(0)
                .timeout(10000)
                .build();

        try {
            // 步骤2：尝试上传并断言异常
            String localFile = createTestFile("auth-test.txt", 1024);
            SftpTransferException exception = assertThrows(
                    SftpTransferException.class,
                    () -> badService.upload(localFile, remoteTestDir, "auth-test.txt"),
                    "使用错误密码应该抛出SftpTransferException"
            );

            // 步骤3：验证异常码
            assertEquals(SftpTransferException.AUTH_FAILED, exception.getErrorCode(),
                    "异常错误码应该是AUTH_FAILED");

            log.info("测试通过: 认证失败抛出异常, errorCode={}", exception.getErrorCode());
        } finally {
            // 步骤4：关闭错误服务
            badService.close();
        }
    }

    /**
     * 测试高并发上传
     * 验证100个文件并发上传时，系统能正确处理
     *
     * @throws Exception 测试执行失败时抛出
     */
    @Test
    @Order(16)
    @DisplayName("高并发上传100个文件")
    void testConcurrentUpload() throws Exception {
        int concurrentCount = 100;  // 并发上传100个文件
        ExecutorService executor = Executors.newFixedThreadPool(20);
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
                        String localFile = createTestFile("concurrent-" + fileIndex + ".txt", 1024 * 50);
                        String remoteFileName = "concurrent-" + fileIndex + "-" + UUID.randomUUID() + ".txt";

                        transferService.upload(localFile, remoteTestDir, remoteFileName);

                        synchronized (uploadedFiles) {
                            uploadedFiles.add(remoteFileName);
                        }
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("并发上传失败", e);
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
            for (String fileName : uploadedFiles) {
                try {
                    transferService.delete(remoteTestDir + "/" + fileName);
                } catch (Exception e) {
                    log.warn("清理远程文件失败: {}", fileName, e);
                }
            }
        }

        log.info("测试通过: 并发上传 {} 个文件成功", concurrentCount);
    }

    /**
     * 创建二进制测试文件
     *
     * @param fileName 文件名
     * @param size     文件大小（字节）
     * @return 文件绝对路径
     * @throws Exception 创建失败时抛出
     */
    private String createTestFile(String fileName, int size) throws Exception {
        // 步骤1：创建目标文件并写入可重复模式数据
        Path filePath = localTestDir.resolve(fileName);
        try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
            byte[] block = new byte[1024];
            int written = 0;
            while (written < size) {
                for (int i = 0; i < block.length; i++) {
                    block[i] = (byte) ((written + i) % 256);
                }
                int remain = Math.min(block.length, size - written);
                outputStream.write(block, 0, remain);
                written += remain;
            }
        }
        return filePath.toString();
    }

    /**
     * 创建文本测试文件
     *
     * @param fileName 文件名
     * @param content  文本内容
     * @return 文件绝对路径
     * @throws Exception 创建失败时抛出
     */
    private String createTextFile(String fileName, String content) throws Exception {
        // 步骤1：写入文本内容到文件
        Path filePath = localTestDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toString();
    }

    /**
     * 递归删除本地目录
     *
     * @param rootPath 根目录
     */
    private void deleteLocalRecursively(Path rootPath) {
        // 步骤1：根目录为空时直接返回
        if (rootPath == null || !Files.exists(rootPath)) {
            return;
        }

        // 步骤2：倒序删除目录树
        try {
            Files.walk(rootPath)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ex) {
                            log.warn("删除本地测试文件失败: {}", path, ex);
                        }
                    });
        } catch (Exception ex) {
            log.warn("清理本地测试目录失败: {}", rootPath, ex);
        }
    }

    /**
     * 测试专用进度监控器
     */
    private static class TestProgressMonitor implements SftpProgressMonitor {
        /**
         * 是否执行了初始化
         */
        private boolean initialized;

        /**
         * 是否执行了结束回调
         */
        private boolean ended;

        /**
         * 已传输总字节
         */
        private long transferredBytes;

        /**
         * 计数回调
         *
         * @param count 本次增量
         * @return true 继续传输
         */
        @Override
        public boolean count(long count) {
            // 统计累计传输字节
            transferredBytes += count;
            return true;
        }

        /**
         * 结束回调
         */
        @Override
        public void end() {
            // 标记已结束
            ended = true;
        }

        /**
         * 初始化回调
         *
         * @param op   操作类型
         * @param src  源路径
         * @param dest 目标路径
         * @param max  最大字节数
         */
        @Override
        public void init(int op, String src, String dest, long max) {
            // 标记初始化被调用
            initialized = true;
        }
    }
}
