package uk.ac.bbsrc.tgac.miso.core.data.impl;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.eaglegenomics.simlims.core.User;

import uk.ac.bbsrc.tgac.miso.core.data.Pool;
import uk.ac.bbsrc.tgac.miso.core.data.Poolable;

@Entity
@Table(name = "PoolChangeLog")
@Inheritance(strategy = InheritanceType.JOINED)
public class PoolChangeLog implements Serializable {

  private static final long serialVersionUID = -9114624240361182071L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
  @ManyToOne
  private PoolImpl<Poolable<?, ?>> pool;
  private String columnsChanged;

  @Transient // TODO: unable to serialize
  private User user;
  private String message;
  private Date changeTime;

  public Pool getPool() {
    return pool;
  }

  public void setPool(PoolImpl pool) {
    this.pool = pool;
  }

  public String getColumnsChanged() {
    return columnsChanged;
  }

  public void setColumnsChanged(String columnsChanged) {
    this.columnsChanged = columnsChanged;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Date getChangeTime() {
    return changeTime;
  }

  public void setChangeTime(Date changeTime) {
    this.changeTime = changeTime;
  }

}
