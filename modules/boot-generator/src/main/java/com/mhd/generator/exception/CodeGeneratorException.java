package com.mhd.generator.exception;

/**
 * @author zhao-hao-dong
 * @since 2025-04-08
 **/
public class CodeGeneratorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CodeGeneratorException(String message) {
        super(message);
    }

    public CodeGeneratorException(Throwable cause) {
        super(cause);
    }

    public CodeGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
