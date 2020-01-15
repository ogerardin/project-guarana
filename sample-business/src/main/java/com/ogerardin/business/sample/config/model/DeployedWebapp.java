package com.ogerardin.business.sample.config.model;

import lombok.Data;

@Data
public class DeployedWebapp {

    String name;

    TomcatInstance tomcatInstance;
}
