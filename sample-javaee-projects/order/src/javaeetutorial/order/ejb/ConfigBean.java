/**
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaeetutorial.order.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author ian
 */
@Singleton
@Startup
public class ConfigBean {

    @EJB
    private RequestBean request;

    @PostConstruct
    public void createData() {
        request.createPart("1234-5678-01", "ABC PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        request.createPart("9876-4321-02", "DEF PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        request.createPart("5456-6789-03", "GHI PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        request.createPart("ABCD-XYZW-FF", "XYZ PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
        request.createPart("SDFG-ERTY-BN", "BOM PART",
                new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null);

        request.addPartToBillOfMaterial("SDFG-ERTY-BN", 
        		"1234-5678-01");
        request.addPartToBillOfMaterial("SDFG-ERTY-BN",
                "9876-4321-02");
        request.addPartToBillOfMaterial("SDFG-ERTY-BN",
                "5456-6789-03");
        request.addPartToBillOfMaterial("SDFG-ERTY-BN",
                "ABCD-XYZW-FF");

        request.createVendor(100, "WidgetCorp",
                "111 Main St., Anytown, KY 99999", "Mr. Jones",
                "888-777-9999");
        request.createVendor(200, "Gadget, Inc.",
                "123 State St., Sometown, MI 88888", "Mrs. Smith",
                "866-345-6789");
    }

    @PreDestroy
    public void deleteData() {
        
    }
}
