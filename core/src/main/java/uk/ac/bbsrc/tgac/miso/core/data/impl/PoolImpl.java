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

package uk.ac.bbsrc.tgac.miso.core.data.impl;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.eaglegenomics.simlims.core.SecurityProfile;
import com.eaglegenomics.simlims.core.User;

import uk.ac.bbsrc.tgac.miso.core.data.AbstractPool;
import uk.ac.bbsrc.tgac.miso.core.data.Poolable;

/**
 * Info
 *
 * @author Rob Davey
 * @since 0.0.2
 */
@Entity
@Table(name = "Pool")
public class PoolImpl<P extends Poolable<?, ?>> extends AbstractPool<P> implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -7918556470415723005L;

  public static final String PREFIX = "MPO";

  @Transient
  private String units = "";

  public PoolImpl() {
    setSecurityProfile(new SecurityProfile());
  }

  public PoolImpl(User user) {
    setSecurityProfile(new SecurityProfile(user));
  }

  public String getUnits() {
    return this.units;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(getName());
    sb.append(" : ");
    sb.append(getAlias());
    sb.append(" : ");
    sb.append(getCreationDate());
    sb.append(" : ");
    sb.append(getConcentration());
    return sb.toString();
  }
}
