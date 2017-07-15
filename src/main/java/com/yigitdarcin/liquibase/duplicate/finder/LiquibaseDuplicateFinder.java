package com.yigitdarcin.liquibase.duplicate.finder;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "find-duplicate-ids")
public class LiquibaseDuplicateFinder extends AbstractMojo {

    @Parameter(property = "find-duplicate-ids.location")
    private String location;

    public void execute() throws MojoExecutionException {
        if (location == null || location == "") {
            throw new MojoExecutionException("location parameter should be filled");
        }

        final Path path = Paths.get(location);
        final File changelogDirectory = path.toFile();
        if (!changelogDirectory.exists()) {
            throw new MojoExecutionException("Cannot find the changlog directory");
        }

        List<String> filePaths = new ArrayList<>();
        listFilesForFolder(changelogDirectory, filePaths);
        chechFileValidations(filePaths);
    }

    private void chechFileValidations(List<String> xmlFilePaths) throws MojoExecutionException {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            for (String path : xmlFilePaths) {
                List<String> changesetNames = new ArrayList<>();
                Document doc = dBuilder.parse(path);
                final Node rootNode = doc.getChildNodes().item(0);
                if ("databaseChangeLog".equals(rootNode.getNodeName())) {
                    for (int i = 0; i < rootNode.getChildNodes().getLength(); i++) {
                        Node node = rootNode.getChildNodes().item(i);
                        if ("changeSet".equals(node.getNodeName())) {
                            String changesetId = node.getAttributes().getNamedItem("id").getNodeValue();
                            String changesetAuthor = node.getAttributes().getNamedItem("author").getNodeValue();
                            String changesetIdAndAuthor = changesetId + "::" + changesetAuthor;
                            if (changesetNames.contains(changesetIdAndAuthor)) {
                                final String message = "Gotcha! shame on you " + changesetAuthor + " for c/p " + changesetId;
                                getLog().error("****************************************************");
                                getLog().error("Liquibase Validation Error");
                                getLog().error(message);
                                getLog().error("****************************************************");
                                throw new MojoExecutionException(message);
                            } else {
                                changesetNames.add(changesetIdAndAuthor);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Cannot read files");
        }

    }

    public List<String> listFilesForFolder(final File folder, List<String> filePaths) {

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, filePaths);
            } else if (isXMLFile(fileEntry.getName())) {
                filePaths.add(fileEntry.getAbsolutePath());
            }
        }
        return filePaths;
    }

    private boolean isXMLFile(String name) {
        return name.toLowerCase().endsWith(".xml");
    }


}
