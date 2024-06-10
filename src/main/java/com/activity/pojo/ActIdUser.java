package com.activity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActIdUser {
    private static final long serialVersionUID = 1L;
    private String id;
    private Integer rev;
    private String first;
    private String last;
    private String email;
    private String pwd;
    private String pictureId;
}