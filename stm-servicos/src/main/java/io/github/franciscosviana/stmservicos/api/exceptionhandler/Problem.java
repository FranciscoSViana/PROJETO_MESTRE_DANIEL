package io.github.franciscosviana.stmservicos.api.exceptionhandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.franciscosviana.stmservicos.api.model.input.CampoErro;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Problem {

    private Integer status;
    private OffsetDateTime timestamp;
    private String type;
    private String title;
    private String detail;
    private String userMessage;
    private List<Object> objects;
    private List<CampoErro> fields;

    @Getter
    @Builder
    public static class Object {
        private String name;
        private String userMessage;
    }
}
