package com.example.entity.vo.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PrivacySaveVO {
    @Pattern(regexp = "(phone|emial|qq|wx|gender)")
    String type;
    boolean status;
}
