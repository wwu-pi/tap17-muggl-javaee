/**
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.dukestutoring.entity;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author ievans
 */
@Entity
public class Administrator extends Person implements Serializable {
    private static final long serialVersionUID = 8896939024924790415L;


    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Administrator)) {
            return false;
        }
        Administrator other = (Administrator) object;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "dukestutoring.entity.Administrator[ id=" + id + " ]";
    }
    
}
