/*
 * Copyright (c) 2015. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey, Mario Caccamo @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO.  If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.bbsrc.tgac.miso.core.data.impl.kit;

import uk.ac.bbsrc.tgac.miso.core.data.KitComponentDescriptor;
import uk.ac.bbsrc.tgac.miso.core.data.KitDescriptor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A skeleton implementation of KitComponentDescriptor
 *
 * @author  Michal Zak
 * @since 0.0.2
 */
public class KitComponentDescriptorImpl implements KitComponentDescriptor {
    /** Field UNSAVED_ID */
    public static final Long UNSAVED_ID = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long kitComponentDescriptorId = KitComponentDescriptorImpl.UNSAVED_ID;
    private String name = "";
    private String referenceNumber ="";

    private KitDescriptor kitDescriptor;

    public long getKitComponentDescriptorId() {
        return kitComponentDescriptorId;
    }

    public void setKitComponentDescriptorId(long kitComponentDescriptorId) {
        this.kitComponentDescriptorId = kitComponentDescriptorId;
    }

    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return kitComponentDescriptorId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public KitDescriptor getKitDescriptor() {
        return kitDescriptor;
    }

    public void setKitDescriptor(KitDescriptor kitDescriptor) {
        this.kitDescriptor = kitDescriptor;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getKitComponentDescriptorId());
        sb.append(" : ");
        sb.append(getName());
        sb.append(" : ");
        sb.append(getReferenceNumber());
        sb.append(" : ");
        sb.append(getKitDescriptor().getKitDescriptorId());
        sb.append(" : ");
        return sb.toString();
    }
}
