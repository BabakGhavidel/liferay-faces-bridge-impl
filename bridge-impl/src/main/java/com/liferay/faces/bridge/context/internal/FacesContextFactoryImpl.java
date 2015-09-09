/**
 * Copyright (c) 2000-2015 Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.liferay.faces.bridge.context.internal;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.ServletContext;


/**
 * @author  Neil Griffin
 */
public class FacesContextFactoryImpl extends FacesContextFactory {

	// Private Data Members
	private FacesContextFactory wrappedFacesContextFactory;

	public FacesContextFactoryImpl(FacesContextFactory facesContextFactory) {
		wrappedFacesContextFactory = facesContextFactory;
	}

	@Override
	public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle)
		throws FacesException {

		// If the specified context is a ServletContext, then it is possible that the session is expiring.
		if ((context != null) && (context instanceof ServletContext)) {

			// If the session is expiring, then return an instance of FacesContext that can function in a limited
			// manner during session expiration.
			String requestFQCN = "";

			if (request != null) {
				requestFQCN = request.getClass().getName().toLowerCase();
			}

			String responseFQCN = "";

			if (response != null) {
				responseFQCN = response.getClass().getName().toLowerCase();
			}

			// NOTE: BridgeSessionListener creates classes named HttpServletRequestExpirationImpl and
			// HttpServletResponseExpirationImpl.
			if ((requestFQCN.length() == 0) || (responseFQCN.length() == 0) || requestFQCN.contains("expiration") ||
					responseFQCN.contains("expiration")) {

				ExternalContext externalContext = new ExternalContextExpirationImpl((ServletContext) context);

				return new FacesContextExpirationImpl(externalContext);
			}

			// Otherwise, delegate to the wrapped factory.
			else {

				return wrappedFacesContextFactory.getFacesContext(context, request, response, lifecycle);
			}
		}

		// Otherwise, delegate to the wrapped factory.
		else {
			return wrappedFacesContextFactory.getFacesContext(context, request, response, lifecycle);
		}
	}
}
