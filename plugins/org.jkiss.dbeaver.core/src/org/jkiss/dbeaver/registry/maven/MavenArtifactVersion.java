/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.registry.maven;

import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.utils.xml.SAXListener;
import org.jkiss.utils.xml.SAXReader;
import org.jkiss.utils.xml.XMLException;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maven artifact version descriptor (POM).
 */
public class MavenArtifactVersion
{
    static final Log log = Log.getLog(MavenArtifactVersion.class);

    private MavenLocalVersion localVersion;
    private String name;
    private String version;
    private String description;
    private String url;
    private List<MavenArtifactLicense> licenses;
    private List<MavenArtifactDependency> dependencies;

    MavenArtifactVersion(MavenLocalVersion localVersion) throws IOException {
        this.localVersion = localVersion;
        loadPOM();
    }

    MavenArtifactVersion(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public MavenLocalVersion getLocalVersion() {
        return localVersion;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public List<MavenArtifactLicense> getLicenses() {
        return licenses;
    }

    public List<MavenArtifactDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return localVersion.toString();
    }

    private enum ParserState {
        ROOT,
        LICENSE,
        DEPENDENCIES,
        DEPENDENCY
    }

    private void loadPOM() throws IOException {
        String pomURL = localVersion.getArtifact().getFileURL(localVersion.getVersion(), MavenArtifact.FILE_POM);
        InputStream mdStream = RuntimeUtils.openConnectionStream(pomURL);
        try {
            SAXReader reader = new SAXReader(mdStream);
            reader.parse(new SAXListener() {
                private ParserState state = ParserState.ROOT;
                private String lastTag;
                private Map<String, String> attributes = new HashMap<String, String>();

                @Override
                public void saxStartElement(SAXReader reader, String namespaceURI, String localName, Attributes atts) throws XMLException {
                    lastTag = localName;
                    if ("license".equals(localName)) {
                        state = ParserState.LICENSE;
                    } else if ("dependencies".equals(localName)) {
                        state = ParserState.DEPENDENCIES;
                    } else if ("dependency".equals(localName) && state == ParserState.DEPENDENCIES) {
                        state = ParserState.DEPENDENCY;
                    }
                }

                @Override
                public void saxText(SAXReader reader, String data) throws XMLException {
                    switch (state) {
                        case ROOT:
                            if ("name".equals(lastTag)) {
                                name = data;
                            } else if ("version".equals(lastTag)) {
                                version = data;
                            } else if ("description".equals(lastTag)) {
                                description = data;
                            } else if ("url".equals(lastTag)) {
                                url = data;
                            }
                            break;
                        case LICENSE:
                        case DEPENDENCY:
                            attributes.put(lastTag, data);
                            break;
                    }
                }

                @Override
                public void saxEndElement(SAXReader reader, String namespaceURI, String localName) throws XMLException {
                    lastTag = null;
                    if ("license".equals(localName) && state == ParserState.LICENSE) {
                        state = ParserState.ROOT;
                        licenses.add(new MavenArtifactLicense(
                            attributes.get("name"),
                            attributes.get("url")
                        ));
                        attributes.clear();
                    } else if ("dependencies".equals(localName) && state == ParserState.DEPENDENCIES) {
                        state = ParserState.ROOT;
                        dependencies.add(new MavenArtifactDependency(
                            new MavenArtifactReference(
                                attributes.get("groupId"),
                                attributes.get("artifactId"),
                                attributes.get("version")
                            ),
                            attributes.get("type"),
                            Boolean.valueOf(attributes.get("optional"))
                        ));
                        attributes.clear();
                    } else if ("dependency".equals(localName) && state == ParserState.DEPENDENCY) {
                        state = ParserState.DEPENDENCIES;
                        attributes.clear();
                    }
                }
            });
        } catch (XMLException e) {
            log.warn("Error parsing POM", e);
        } finally {
            mdStream.close();
        }
    }

}
