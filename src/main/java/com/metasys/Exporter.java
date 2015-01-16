package com.metasys;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by kbryd on 1/16/15.
 */
public class Exporter {

    private static Logger logger = Logger.getLogger(Exporter.class.getName());

    public static void exportSingleDocument(Session session, Document document, String destinationPath) throws IOException {
        String sep = "";

        if(!destinationPath.endsWith(File.separator)) {
            sep = File.separator;
        }

        ContentStream contentStream = document.getContentStream();
        if(contentStream != null) {
            for(String path : document.getPaths()) {
                path = path.substring(0, path.lastIndexOf('/'));
                String exportPath = destinationPath + sep + path + sep + document.getName();

                logger.log(Level.INFO, String.format("Exporting document '%s' (%d bytes) from '%s' to '%s'", document.getName(), document.getContentStreamLength(), path, exportPath));

                InputStream inStream = contentStream.getStream();
                FileOutputStream outStream = new FileOutputStream(exportPath);
                IOUtils.copy(inStream, outStream);
                outStream.close();
                inStream.close();
            }
        } else {
            // case for empty documents
            for(String path : document.getPaths()) {
                path = path.substring(0, path.lastIndexOf('/'));
                String exportPath = destinationPath + sep + path + sep + document.getName();

                logger.log(Level.INFO, String.format("Exporting document '%s' from '%s' to '%s'", document.getName(), path, exportPath));
                File file = new File(destinationPath + sep + document.getName());
                file.createNewFile();
            }
        }
    }

    public static void exportSingleFolder(Folder folder, String destinationPath) {
        String sep = "";

        if(!destinationPath.endsWith(File.separator)) {
            sep = File.separator;
        }

        String exportPath = destinationPath + sep + folder.getPath();
        logger.log(Level.INFO, String.format("Exporting folder '%s' from '%s' to '%s'", folder.getName(), folder.getPath(), exportPath));

        File file = new File(exportPath);
        file.mkdirs();
    }


    public static void export(CMISSession cmisSession, String startingPath, String destinationPath, Integer maxLevels) throws IOException {
        Session session = cmisSession.getSession();

        ObjectType type = session.getTypeDefinition("cmis:document");
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        Folder startingFolder = (Folder) session.getObjectByPath(startingPath);


        exportFolders(destinationPath, session, objectIdQueryName, startingFolder);

        exportDocuments(destinationPath, session, objectIdQueryName, startingFolder);
    }

    private static void exportFolders(String destinationPath, Session session, String objectIdQueryName, Folder startingFolder) {
        String queryString = "SELECT "+objectIdQueryName+" FROM cmis:folder F WHERE IN_TREE(F, '" + startingFolder.getId() + "')";
        ItemIterable<QueryResult> results = session.query(queryString, false);

        for (QueryResult qResult : results) {
            String objectId = qResult.getPropertyValueByQueryName("F.cmis:objectId");
            Folder folder = (Folder) session.getObject(session.createObjectId(objectId));

            exportSingleFolder(folder, destinationPath);
        }
    }

    private static void exportDocuments(String destinationPath, Session session, String objectIdQueryName, Folder startingFolder) throws IOException {
        String queryString = "SELECT "+objectIdQueryName+" FROM cmis:document F WHERE IN_TREE(F, '" + startingFolder.getId() + "')";
        ItemIterable<QueryResult> results = session.query(queryString, false);

        for (QueryResult qResult : results) {
            String objectId = qResult.getPropertyValueByQueryName("F.cmis:objectId");
            Document doc = (Document) session.getObject(session.createObjectId(objectId));

            exportSingleDocument(session, doc, destinationPath);
        }
    }
}
