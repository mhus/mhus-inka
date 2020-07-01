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

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import de.mhus.lib.basics.Named;
import de.mhus.lib.basics.Versioned;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.MNls;
import de.mhus.lib.core.util.MNlsProvider;
import de.mhus.lib.core.util.Nls;
import de.mhus.lib.core.util.ParameterDefinitions;
import de.mhus.lib.core.util.Version;

public class OperationDescriptor implements MNlsProvider, Nls, Named, Versioned {

    public static final String TAG_DEFAULT_ACL = "acl";
    public static final String TAG_METRIC = "metric";
    public static final String TAG_PRIORITY = "pri";
    public static final String TAG_REMOTE = "remote";
    public static final String TAG_HOST = "host";
    public static final String TAG_IDENT = "ident";

    private Collection<String> tags;
    private OperationAddress address;
    private OperationDescription description;
    private UUID uuid;

    public OperationDescriptor(
            UUID uuid,
            String address,
            OperationDescription description,
            Collection<String> tags) {
        this(uuid, new OperationAddress(address), description, tags);
    }

    public OperationDescriptor(
            UUID uuid,
            OperationAddress address,
            OperationDescription description,
            Collection<String> tags) {
        this.uuid = uuid;
        this.address = address;
        this.description = description;
        this.tags = tags;

        // tags from description
        Object tagsStr =
                description.getParameters() == null
                        ? null
                        : description.getParameters().get(OperationDescription.TAGS);
        if (tagsStr != null) for (String item : String.valueOf(tagsStr).split(";")) tags.add(item);
    }

    public boolean compareTags(Collection<String> providedTags) {
        if (providedTags == null) return false;
        // negative check
        for (String t : tags) {
            if (t.startsWith("*")) {
                if (!providedTags.contains(t.substring(1))) return false;
            } else if (t.startsWith("!")) {
                if (providedTags.contains(t.substring(1))) return false;
            }
        }
        // positive check
        for (String t : providedTags) {
            if (t.startsWith("!")) {
                if (tags.contains(t.substring(1))) return false;
            } else if (!tags.contains(t)) return false;
        }
        return true;
    }

    public Collection<String> getTags() {
        return tags;
    }

    /**
     * Every action should have a parameter definition. If parameter definitions are not supported,
     * the method will return null;
     *
     * @return Definition
     */
    public ParameterDefinitions getParameterDefinitions() {
        return description.getParameterDefinitions();
    }

    /**
     * An action can provide a form component but it's not necessary. If parameter definitions are
     * not supported, the method will return null;
     *
     * @return Form
     */
    public DefRoot getForm() {
        return description.getForm();
    }

    @Override
    public String nls(String text) {
        return description.nls(text);
    }

    @Override
    public MNls getNls() {
        return description.getNls();
    }

    public String getCaption() {
        return description.getCaption();
    }

    public String getTitle() {
        return description.getTitle();
    }

    @Override
    public String getName() {
        return address.getName();
    }

    public String getPath() {
        return address.getPath();
    }

    public Version getVersion() {
        return address.getVersion();
    }

    public OperationAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, address, tags);
    }

    @Override
    public String getVersionString() {
        return address.getVersionString();
    }

    public String getProvider() {
        return address.getProvider();
    }

    @SuppressWarnings("unchecked")
    public <T> T adaptTo(Class<T> ifc) {
        if (ifc == OperationAddress.class) return (T) address;
        if (ifc == OperationDescription.class) return (T) description;
        return null;
    }

    public String[] getParameterKeys() {
        HashMap<String, String> p = description.getParameters();
        if (p == null) return new String[0];
        return p.keySet().toArray(new String[p.size()]);
    }

    public String getParameter(String key) {
        HashMap<String, String> p = description.getParameters();
        if (p == null) return null;
        return p.get(key);
    }

    public UUID getUuid() {
        return uuid;
    }
}
