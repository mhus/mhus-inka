/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.lib.cao.action;

import de.mhus.lib.cao.CaoList;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.form.definition.FmText;

public class DeleteRenditionConfiguration extends CaoConfiguration {

    public DeleteRenditionConfiguration(CaoConfiguration con, CaoList list, IConfig model) {
        super(con, list, model);
    }

    public static final String RENDITION = "rendition";

    @Override
    protected IConfig createDefaultModel() {
        return new DefRoot(
                new FmText(
                        RENDITION,
                        "rendition.name=Rendition",
                        "rendition.description=Name of the rendition to delete"));
    }

    public void setRendition(String name) {
        properties.setString(RENDITION, name);
    }
}
