/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.cakephp.netbeans.github;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.openide.util.Exceptions;

/**
 *
 * @author junichi11
 */
public final class CakePhpGithubTags {

    private static final String GITHUB_API_REPOS_TAGS = "https://api.github.com/repos/cakephp/cakephp/tags"; // NOI18N
    private ArrayList<GithubTag> tags;
    private ArrayList<String> names;
    private boolean isNetworkError = false;
    private static final CakePhpGithubTags INSTANCE = new CakePhpGithubTags();

    public static CakePhpGithubTags getInstance() {
        return INSTANCE;
    }

    private CakePhpGithubTags() {
        init();
    }

    private void init() {
        isNetworkError = false;
        try {
            // JSON -> Object
            Gson gson = new Gson();
            URL tagsJson = new URL(GITHUB_API_REPOS_TAGS);
            BufferedReader reader = new BufferedReader(new InputStreamReader(tagsJson.openStream(), "UTF-8")); // NOI18N
            try {
                JsonReader jsonReader = new JsonReader(reader);
                Type type = new TypeToken<ArrayList<GithubTag>>() {
                }.getType();
                tags = gson.fromJson(jsonReader, type);
            } finally {
                reader.close();
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            isNetworkError = true;
        }

        names = new ArrayList<String>(tags.size());
        for (GithubTag tag : tags) {
            names.add(tag.getName());
        }
    }

    public void reload() {
        init();
    }

    public ArrayList<GithubTag> getTags() {
        if (isNetworkError) {
            reload();
        }
        return tags;
    }

    public String getZipUrl(String name) {
        for (GithubTag tag : tags) {
            if (tag.getName().equals(name)) {
                return tag.getZipballUrl();
            }
        }
        return null;
    }

    public String[] getNames() {
        if (isNetworkError) {
            reload();
        }
        return names.toArray(new String[0]);
    }

    public String getLatestStableVersion() {
        if (isNetworkError) {
            reload();
        }
        for (String name : names) {
            if (name.contains("-")) { // NOI18N
                continue;
            }
            return name;
        }
        return null;
    }

    public boolean isNetworkError() {
        return isNetworkError;
    }

}
