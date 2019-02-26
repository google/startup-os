package com.google.startupos.tools.reviewer.local_server.service;

import com.google.auth.Credentials;
import com.google.auth.http.AuthHttpConstants;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.io.IOException;

public class AuthServiceCredentials extends Credentials {
    private AuthService authService;

    AuthServiceCredentials(AuthService service) {
        this.authService = service;
    }

    @Override
    public String getAuthenticationType() {
        return "Acce";
    }

    @Override
    public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
        HashMap<String, List<String>> metadata = new HashMap<>();
        ArrayList<String> values = new ArrayList();
        values.add(AuthHttpConstants.BEARER + " " + this.authService.getAccessToken());
        metadata.put(AuthHttpConstants.AUTHORIZATION, values);
        return metadata;
    }

    @Override
    public boolean hasRequestMetadata() {
        return true;
    }

    @Override
    public boolean hasRequestMetadataOnly() {
        return true;
    }

    @Override
    public void refresh() throws IOException {
        // TODO: we don't have a proper refresh token for Google Access Token
    }

    public String token() {
        return this.authService.getToken();
    }

    public String projectId() {
        return this.authService.getProjectId();
    }
}
