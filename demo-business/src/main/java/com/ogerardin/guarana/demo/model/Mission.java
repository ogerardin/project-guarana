/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.model;

import lombok.Data;

import java.util.Date;

/**
 * @author oge
 * @since 22/12/2016
 */
@Data
public class Mission {

    private final Person person;

    private Date startDate;

    private Date endDate;

}
