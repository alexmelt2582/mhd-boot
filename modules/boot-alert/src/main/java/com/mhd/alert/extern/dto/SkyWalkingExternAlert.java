package com.mhd.alert.extern.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * SkyWalking Alert Content Entity
 * @see <a href="https://skywalking.apache.org/docs/main/latest/en/setup/backend/backend-alarm/">backend-alarm</a>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkyWalkingExternAlert {

    private Long scopeId;

    private String scope;

    private String name;

    private String id0;

    private String id1;

    private String ruleName;

    private String alarmMessage;

    private Long startTime;

    private List<Tag> tags;

    /**
     * SkyWalking Tag Entity
     */
    @Data
    public static class Tag {

        private String key;

        private String value;
    }
}
