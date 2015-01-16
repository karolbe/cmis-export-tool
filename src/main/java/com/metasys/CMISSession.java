package com.metasys;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by kbryd on 1/16/15.
 */
public class CMISSession {

    private static final Logger logger = Logger.getLogger(CMISSession.class.getName());

    protected Session session;

    // default values...
    private String ENDPOINT_URL = "http://localhost:8080/alfresco/api/-default-/public/cmis/versions/1.1/atom";
    private String ENDPOINT_LOGIN = "admin";
    private String ENDPOINT_PASSWORD = "admin";
    private String ENDPOINT_REPO_ID = null;

    public CMISSession(String login, String password, String url) {
        this.ENDPOINT_LOGIN = login;
        this.ENDPOINT_PASSWORD = password;
        this.ENDPOINT_URL = url;
    }

    public CMISSession(String login, String password, String url, String repositoryName) {
        this.ENDPOINT_LOGIN = login;
        this.ENDPOINT_PASSWORD = password;
        this.ENDPOINT_URL = url;
        this.ENDPOINT_REPO_ID = repositoryName;
    }

    public Session getSession() {
        return session;
    }

    public void connect() {

        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        parameter.put(SessionParameter.USER, ENDPOINT_LOGIN);
        parameter.put(SessionParameter.PASSWORD, ENDPOINT_PASSWORD);

        parameter.put(SessionParameter.ATOMPUB_URL, ENDPOINT_URL);
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        if(ENDPOINT_REPO_ID != null) {
            parameter.put(SessionParameter.REPOSITORY_ID, ENDPOINT_REPO_ID);
        }

        List<Repository> repositories = factory.getRepositories(parameter);

        session = repositories.get(0).createSession();
    }

}
