/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey @ TGAC
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO. If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.bbsrc.tgac.miso.core.security;

import static uk.ac.bbsrc.tgac.miso.core.util.LimsUtils.isStringEmptyOrNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager;

import com.eaglegenomics.simlims.core.User;

import uk.ac.bbsrc.tgac.miso.core.security.util.LimsSecurityUtils;
import org.springframework.transaction.annotation.Transactional;

import com.eaglegenomics.simlims.core.manager.AbstractSecurityManager;

/**
 * Extension of the basic LocalSecurityManager, this class adds the ability to save users initially authorised by LDAP into the MISO DB so
 * that the security information can be utilised by the SecurityProfile system
 * 
 * @author Rob Davey
 * @since 0.0.2
 */
@Transactional(rollbackFor = Exception.class)
public class LDAPSecurityManager extends AbstractSecurityManager {
  /** Field log */
  protected static final Logger log = LoggerFactory.getLogger(LDAPSecurityManager.class);

  private boolean propagateLdapPasswords = false;

  @Autowired
  private LdapUserDetailsManager ldapUserManager;

  @Autowired
  private PasswordCodecService passwordCodecService;

  public void setLdapUserManager(LdapUserDetailsManager ldapUserManager) {
    this.ldapUserManager = ldapUserManager;
  }

  public void setPasswordCodecService(PasswordCodecService passwordCodecService) {
    this.passwordCodecService = passwordCodecService;
  }

  /**
   * Saves the User to the MISO database
   *
   * @param user
   *          of type User
   * @throws IOException
   *           when the User cannot be saved
   */
  @Override
  public long saveUser(User user) throws IOException {
    // firstly, save the user as per the superclass
    long id = super.saveUser(user);

    // then update the used password as per LDAP
    if (propagateLdapPasswords) {
      User jdbcUser = super.getUserByLoginName(user.getLoginName());
      if (jdbcUser != null) {
        if (!isStringEmptyOrNull(user.getPassword())) {
          if (SecurityContextHolder.getContext().getAuthentication() != null
                  && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            if (SecurityContextHolder.getContext().getAuthentication().getName().equals(user.getLoginName())) {
              // this should be the case if the user has already logged in and is wishing to update details, rather than create
              Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
              if (p instanceof InetOrgPerson) {
                User ldapUser = LimsSecurityUtils.fromLdapUser((InetOrgPerson) p);
                if (ldapUser.getPassword().equals(user.getPassword())
                        || passwordCodecService.getEncoder().isPasswordValid(ldapUser.getPassword(), user.getPassword(), null)) {
                } else {
                  // if the existing LDAP pass and this pass are different
                  ldapUserManager.changePassword(ldapUser.getPassword(),
                          passwordCodecService.getEncoder().encodePassword(user.getPassword(), null));
                }
              }
            }

            if (user.getUserId() == null) {
              // this will happen if the user auths against LDAP and the user exists in the DB
              // i.e. when they log into the LIMS for the very first time
              user.setUserId(jdbcUser.getUserId());
            }

            if (isStringEmptyOrNull(user.getFullName())) {
              throw new IOException("Cannot save user with no full name / display name.");
            }

            if (isStringEmptyOrNull(user.getEmail())) {
              throw new IOException("Cannot save user with no email.");
            }

            if (isStringEmptyOrNull(user.getLoginName())) {
              throw new IOException("Cannot save user with no login name.");
            }

            if (isStringEmptyOrNull(user.getPassword())) {
              // infer that the password is going to be the same, not set to null
              user.setPassword(jdbcUser.getPassword());
            }

            return super.saveUser(user);
          } else {
            // probably attempting registration, i.e. no auth session and user exists in LDAP and LIMS
            throw new IOException("User with supplied login name already exists");
          }
        }
        return jdbcUser.getUserId();
      }
    }
    return id;
  }

  @Override
  public boolean canCreateNewUser() {
    return false;
  }

  @Override
  public boolean isPasswordMutable() {
    return false;
  }

  public void setPropagateLdapPasswords(boolean propagateLdapPasswords) {
    this.propagateLdapPasswords = propagateLdapPasswords;
  }
}
