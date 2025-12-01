package com.policyadmin.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client")
public class ClientIdTypeProperties {

    private List<IdType> idTypes = List.of();

    public List<IdType> getIdTypes() {
        return idTypes;
    }

    public void setIdTypes(List<IdType> idTypes) {
        this.idTypes = idTypes;
    }

    public static class IdType {
        private String code;
        private String description;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
