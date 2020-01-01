package com.lc.core.dto;

import lombok.Data;

import java.io.Serializable;


/**
 * @author l5990
 */
@Data
public class User implements Serializable {

    private Long id;

    private String account;

    private String uName;
}
