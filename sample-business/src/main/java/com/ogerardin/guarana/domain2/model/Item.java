/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.domain2.model;

import lombok.Data;

import java.util.List;

/**
 * @author oge
 * @since 14/02/2017
 */
@Data
public class Item {

    Category category;

    String name;

    List<FeatureValue> features;
}
