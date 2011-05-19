/**
 * Copyright 2011 Alexandre Dutra
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package fr.dutra.tools.maven.deptree.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Parent class of line-based parsers, i.e., parsers that scan the input line by line.
 * @author Alexandre Dutra
 *
 */
public abstract class AbstractLineBasedParser extends AbstractParser {

    protected int lineIndex = 0;

    protected List<String> lines;

    protected List<String> splitLines(final Reader reader) throws IOException {
        String line = null;
        final BufferedReader br;
        if(reader instanceof BufferedReader) {
            br = (BufferedReader) reader;
        } else {
            br = new BufferedReader(reader);
        }
        final List<String> lines = new ArrayList<String>();
        while((line = br.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    /**
     * When doing an install at the same time on a multi-module project, one can get this kind of output:
     * <pre>
     * +- active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = active project artifact:
     *     artifact = com.acme.org:foobar:jar:1.0.41-SNAPSHOT:compile;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml;
     *     project: MavenProject: com.acme.org:foobar:1.0.41-SNAPSHOT @ /opt/jenkins/home/jobs/foobar/workspace/trunk/foobar/pom.xml
     * </pre>
     */
    protected String extractActiveProjectArtifact() {
        String artifact = null;
        //start at next line and consume all lines containing "artifact =" or "project: "; record the last line containing "artifact =".
        boolean artifactFound = false;
        while(this.lineIndex < this.lines.size() - 1) {
            String tempLine = this.lines.get(this.lineIndex + 1);
            boolean artifactLine = !artifactFound && tempLine.contains("artifact = ");
            boolean projectLine = artifactFound && tempLine.contains("project: ");
            if(artifactLine || projectLine) {
                if(tempLine.contains("artifact = ") && ! tempLine.contains("active project artifact:")) {
                    artifact = StringUtils.substringBefore(StringUtils.substringAfter(tempLine, "artifact = "), ";");
                    artifactFound = true;
                }
                this.lineIndex++;
            } else {
                break;
            }
        }
        return artifact;
    }

}