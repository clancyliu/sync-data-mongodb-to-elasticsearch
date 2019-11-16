package com.clancy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author liugang
 * @date 2019/11/1 20:50
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 4836586804948092355L;

    private Long id;

    private String action;

    private String mac;

    private String sn;

}
