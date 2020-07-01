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
package de.mhus.micro.core.api;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.config.DefaultConfigFactory;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.MConfig;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.errors.NotFoundException;

public class OperationAnswer {

    private String description;

    public OperationAnswer() {}

    public OperationAnswer(String path, Version version, IConfig properties) {
        try {
            description =
                    path + (version == null ? "" : ":" + version) + "?" + IConfig.toCompactJsonString(properties);
        } catch (MException e) {
            throw new MRuntimeException(e);
        }
    }

    public OperationAnswer(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public String getPath() {
        int p = description.indexOf('?');
        if (p < 0) return description;
        String out = description.substring(0, p);
        p = out.indexOf(':');
        if (p < 0) return out;
        return out.substring(0, p);
    }

    public VersionRange getVersionRange() {
        String out = description;
        int p = out.indexOf('?');
        if (p >= 0) out = out.substring(0, p);
        p = out.indexOf(':');
        if (p < 0) return null;
        return new VersionRange(out.substring(p + 1));
    }

    public IConfig getContent() {
        int p = description.indexOf('?');
        if (p < 0) return new MConfig();
        try {
            return IConfig.readConfigFromString(description.substring(p + 1));
        } catch (MException e) {
            throw new MRuntimeException(description, e);
        }
    }

    public OperationResult send(IConfig request, String... executeOptions)
            throws NotFoundException {
        OperationApi api = M.l(OperationApi.class);
        IConfig prop = getContent();
        if (request != null) prop.putAll(request);
        OperationResult res =
                api.doExecute(getPath(), getVersionRange(), null, prop, executeOptions);
        return res;
    }
}
