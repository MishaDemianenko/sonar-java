/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.findbugs;

import com.thoughtworks.xstream.XStream;
import org.sonar.api.profiles.ProfileExporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Java;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.findbugs.xml.Bug;
import org.sonar.plugins.findbugs.xml.FindBugsFilter;
import org.sonar.plugins.findbugs.xml.Match;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class FindbugsProfileExporter extends ProfileExporter {

  public FindbugsProfileExporter() {
    super(FindbugsConstants.REPOSITORY_KEY, FindbugsConstants.PLUGIN_NAME);
    setSupportedLanguages(Java.KEY);
    setMimeType("application/xml");
  }

  @Override
  public void exportProfile(RulesProfile profile, Writer writer) {
    try {
      FindBugsFilter filter = buildFindbugsFilter(profile.getActiveRulesByRepository(FindbugsConstants.REPOSITORY_KEY));
      XStream xstream = FindBugsFilter.createXStream();
      writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- Generated by SonarQube -->\n".concat(xstream.toXML(filter)));
    } catch (IOException e) {
      throw new SonarException("Fail to export the Findbugs profile : " + profile, e);
    }
  }

  protected static FindBugsFilter buildFindbugsFilter(List<ActiveRule> activeRules) {
    FindBugsFilter root = new FindBugsFilter();
    for (ActiveRule activeRule : activeRules) {
      if (FindbugsConstants.REPOSITORY_KEY.equals(activeRule.getRepositoryKey())) {
        Match child = new Match();
        child.setBug(new Bug(activeRule.getConfigKey()));
        root.addMatch(child);
      }
    }
    return root;
  }

}
