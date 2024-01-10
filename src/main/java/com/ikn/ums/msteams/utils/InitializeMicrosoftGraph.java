package com.ikn.ums.msteams.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;

import okhttp3.Request;

@Component
public class InitializeMicrosoftGraph {
	
	private ClientSecretCredential clientSecretCredential;
	@SuppressWarnings("unused")
	private GraphServiceClient<Request> graphServiceClient;
	
	@Autowired
	private Environment environment;
	
	// initialize Microsoft graph API and get access token
		//@Override
		public AccessToken initializeMicrosoftGraph() {
			if (clientSecretCredential == null) {
				final String clientId = environment.getProperty("app.clientId");
				final String clientSecret = environment.getProperty("app.clientSecret");
				final String tenantId = environment.getProperty("app.tenantId");
				this.clientSecretCredential = new ClientSecretCredentialBuilder().clientId(clientId).tenantId(tenantId)
						.clientSecret(clientSecret).build();
			}
			final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
					List.of("https://graph.microsoft.com/.default"), clientSecretCredential);
			this.graphServiceClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
			return getAccessToken();
		}

		// helper method
		private AccessToken getAccessToken() {
			final String[] graphscopes = new String[] { "https://graph.microsoft.com/.default" };
			final TokenRequestContext context = new TokenRequestContext();
			context.addScopes(graphscopes);
			return this.clientSecretCredential.getToken(context).block();
			
		}

}
