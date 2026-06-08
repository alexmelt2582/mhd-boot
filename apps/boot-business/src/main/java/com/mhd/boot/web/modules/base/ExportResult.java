package com.mhd.boot.web.modules.base;

/**
 * 导出结果
 *
 * @author zhao-hao-dong
 */
public record ExportResult(byte[] pdfBytes, String filename) {
}
