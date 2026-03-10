/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.test.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author oge
 * @since 12/01/2017
 */
@Data
@AllArgsConstructor
public class Thing {

    private final String name;

    private final Person owner;

}
