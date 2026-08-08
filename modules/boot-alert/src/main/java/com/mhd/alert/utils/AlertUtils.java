package com.mhd.alert.utils;

import com.mhd.boot.common.utils.encrypt.CryptoUtils;

import java.util.Comparator;
import java.util.Map;

/**
 * @author zhao-hao-dong
 **/
public class AlertUtils {
    public static String calculateFingerprint(Map<String, String> fingerPrints) {
        StringBuilder canonicalLabels = new StringBuilder();
        fingerPrints.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(entry -> {
                    appendLengthPrefixed(canonicalLabels, entry.getKey());
                    appendLengthPrefixed(canonicalLabels, entry.getValue());
                });
        return CryptoUtils.sha256Hex(canonicalLabels.toString());
    }

    private static void appendLengthPrefixed(StringBuilder target, String value) {
        if (value == null) {
            target.append("-1:");
            return;
        }
        target.append(value.length()).append(':').append(value);
    }
}
