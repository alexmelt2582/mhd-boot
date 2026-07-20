package com.mhd.boot.common.sftp.exception;

/**
 * SFTP传输自定义异常
 * 封装SFTP传输过程中的各种异常，提供更清晰的错误分类
 *
 * @author zhao-hao-dong
 **/
public class SftpTransferException extends Exception {
    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 认证失败错误码
     */
    public static final String AUTH_FAILED = "AUTH_FAILED";

    /**
     * 网络连接失败错误码
     */
    public static final String CONNECTION_FAILED = "CONNECTION_FAILED";

    /**
     * 文件传输失败错误码
     */
    public static final String TRANSFER_FAILED = "TRANSFER_FAILED";

    /**
     * 文件路径错误码
     */
    public static final String PATH_ERROR = "PATH_ERROR";

    /**
     * 权限不足错误码
     */
    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";

    /**
     * 连接池资源耗尽错误码
     */
    public static final String POOL_EXHAUSTED = "POOL_EXHAUSTED";

    /**
     * 文件不存在错误码
     */
    public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";

    /**
     * 文件大小超过限制错误码
     */
    public static final String FILE_SIZE_EXCEEDED = "FILE_SIZE_EXCEEDED";

    /**
     * 构造SFTP传输异常
     *
     * @param errorCode 错误码，用于标识异常类型
     * @param message   错误描述信息
     */
    public SftpTransferException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造SFTP传输异常（带原始异常）
     * 保留原始异常信息，方便排查问题
     *
     * @param errorCode 错误码
     * @param message   错误描述信息
     * @param cause     原始异常
     */
    public SftpTransferException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     *
     * @return 错误码字符串
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 根据JSch异常信息判断错误类型并创建对应的SftpTransferException
     *
     * @param e       原始JSch异常
     * @param context 上下文描述，如"上传文件xxx时"
     * @return 封装后的SftpTransferException
     */
    public static SftpTransferException fromJSchException(Exception e, String context) {
        String message = e.getMessage() != null ? e.getMessage() : "";
        String errorCode;

        if (message.contains("Auth fail")) {
            errorCode = AUTH_FAILED;
        } else if (message.contains("timeout") || message.contains("Timeout")) {
            errorCode = CONNECTION_FAILED;
        } else if (message.contains("No such file") || message.contains("No such directory")) {
            errorCode = PATH_ERROR;
        } else if (message.contains("Permission denied")) {
            errorCode = PERMISSION_DENIED;
        } else if (message.contains("Socket") || message.contains("Connection refused")) {
            errorCode = CONNECTION_FAILED;
        } else {
            errorCode = TRANSFER_FAILED;
        }

        return new SftpTransferException(errorCode, context + " failed: " + message, e);
    }

    @Override
    public String toString() {
        return String.format("SftpTransferException[errorCode=%s] %s", errorCode, getMessage());
    }
}
