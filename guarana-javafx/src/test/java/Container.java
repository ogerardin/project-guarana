/*
 * Copyright (c) 2017 Olivier Gérardin
 */

import lombok.Data;

/**
 * @author oge
 * @since 06/03/2017
 */
@Data
public class Container {
    public String name;

    public void dump() {
        System.out.println(this.toString());
    }

}
