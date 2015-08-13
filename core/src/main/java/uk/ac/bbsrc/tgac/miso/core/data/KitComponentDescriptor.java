/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
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

package uk.ac.bbsrc.tgac.miso.core.data;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import uk.ac.bbsrc.tgac.miso.core.data.KitDescriptor;
import uk.ac.bbsrc.tgac.miso.core.data.Nameable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A KitComponentDescriptor handles information about a consumable element that is part of a kit. Every element of that type has a name and reference number and uses KitDescriptor. KitComponents use
 *  KitComponentDescriptors, which in turn use KitDescriptors, to represent a real-world manifestation of a consumable kit.
 *
 * @author  Michal Zak
 * @since 0.0.2
 */
@JsonSerialize(typing = JsonSerialize.Typing.STATIC, include = JsonSerialize.Inclusion.NON_NULL)
//@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public interface KitComponentDescriptor extends Nameable{

    long getKitComponentDescriptorId();

    void setKitComponentDescriptorId(long kitComponentDescriptorId);

    String getName();

    void setName(String name);

    String getReferenceNumber();

    void setReferenceNumber(String referenceNumber);

    KitDescriptor getKitDescriptor();

    void setKitDescriptor(KitDescriptor kitDescriptor);

    @Override
    String toString();
}
