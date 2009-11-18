/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.identity.federation.bindings.tomcat.sp;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;
import org.jboss.identity.federation.api.saml.v2.request.SAML2Request;
import org.jboss.identity.federation.core.exceptions.ConfigurationException;
import org.jboss.identity.federation.core.saml.v2.common.IDGenerator;
import org.jboss.identity.federation.core.saml.v2.common.StatementLocal;
import org.jboss.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.jboss.identity.federation.core.saml.v2.exceptions.AssertionExpiredException;
import org.jboss.identity.federation.core.saml.v2.util.AssertionUtil;
import org.jboss.identity.federation.saml.v2.assertion.AssertionType;
import org.jboss.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.jboss.identity.federation.saml.v2.assertion.AttributeType;
import org.jboss.identity.federation.saml.v2.assertion.NameIDType;
import org.jboss.identity.federation.saml.v2.assertion.SubjectType;
import org.jboss.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.jboss.identity.federation.saml.v2.protocol.ResponseType;
import org.jboss.identity.federation.saml.v2.protocol.StatusType;

/**
 * Common code useful for a SP
 * @author Anil.Saldhana@redhat.com
 * @since Jan 9, 2009
 */
public class SPUtil
{ 
   /**
    * Create a SAML2 auth request
    * @param serviceURL URL of the service
    * @param identityURL URL of the identity provider
    * @return   
    * @throws ConfigurationException 
    */
   public AuthnRequestType createSAMLRequest(String serviceURL, String identityURL) throws ConfigurationException
   {
      if(serviceURL == null)
         throw new IllegalArgumentException("serviceURL is null");
      if(identityURL == null)
         throw new IllegalArgumentException("identityURL is null");
      
      SAML2Request saml2Request = new SAML2Request();
      String id = IDGenerator.create("ID_");
      return saml2Request.createAuthnRequestType(id, serviceURL, identityURL, serviceURL); 
   }
   
   /**
    * Handle the SAMLResponse from the IDP
    * @param request entire request from IDP
    * @param responseType ResponseType that has been generated
    * @param serverEnvironment tomcat,jboss etc
    * @return   
    * @throws AssertionExpiredException 
    */
   @SuppressWarnings("unchecked")
   public Principal handleSAMLResponse(Request request, ResponseType responseType) 
   throws ConfigurationException, AssertionExpiredException
   {
      if(request == null)
         throw new IllegalArgumentException("request is null");
      if(responseType == null)
         throw new IllegalArgumentException("response type is null");
      
      StatusType statusType = responseType.getStatus();
      if(statusType == null)
         throw new IllegalArgumentException("Status Type from the IDP is null");

      String statusValue = statusType.getStatusCode().getValue();
      if(JBossSAMLURIConstants.STATUS_SUCCESS.get().equals(statusValue) == false)
         throw new SecurityException("IDP forbid the user");

      List<Object> assertions = responseType.getAssertionOrEncryptedAssertion();
      if(assertions.size() == 0)
         throw new IllegalStateException("No assertions in reply from IDP"); 
      
      AssertionType assertion = (AssertionType)assertions.get(0);
      //Check for validity of assertion
      boolean expiredAssertion = AssertionUtil.hasExpired(assertion);
      if(expiredAssertion)
         throw new AssertionExpiredException();
      
      SubjectType subject = assertion.getSubject(); 
      JAXBElement<NameIDType> jnameID = (JAXBElement<NameIDType>) subject.getContent().get(0);
      NameIDType nameID = jnameID.getValue();
      String userName = nameID.getValue();
      List<String> roles = new ArrayList<String>();

      //Set it on a thread local for JBID integrators
      StatementLocal.statements.set(assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement());
      
      //Let us get the roles
      AttributeStatementType attributeStatement = (AttributeStatementType) assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement().get(0);
      List<Object> attList = attributeStatement.getAttributeOrEncryptedAttribute();
      for(Object obj:attList)
      {
         AttributeType attr = (AttributeType) obj;
         String roleName = (String) attr.getAttributeValue().get(0);
         roles.add(roleName);
      }
      return this.createGenericPrincipal(request, userName, roles);       
   } 
   
   public Principal createGenericPrincipal(Request request, String username, List<String> roles)
   { 
      Context ctx = request.getContext();
      return new GenericPrincipal(ctx.getRealm(), username, null, roles);
   } 
}