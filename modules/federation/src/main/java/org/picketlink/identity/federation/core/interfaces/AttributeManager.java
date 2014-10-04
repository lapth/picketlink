/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.core.interfaces;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Manages the attributes for an identity
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 31, 2009
 */
public interface AttributeManager {

    /**
     * Given a set of keys, get back attributes
     *
     * @param userPrincipal Principal for whom attributes need to be retrieved
     * @param attributeKeys
     *
     * @return
     *
     * @deprecated Prefer using {@link org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2AttributeManager#getAttributes(org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType, java.security.Principal)}.
     */
    @Deprecated()
    Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys);
}