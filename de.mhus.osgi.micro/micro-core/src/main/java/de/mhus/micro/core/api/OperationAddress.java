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

import de.mhus.lib.basics.Named;
import de.mhus.lib.basics.Versioned;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.util.Version;

/**
 * The class represents an address to an operation jms://de.xyz.Operation:1.2.3/remote/sop provider:
 * jms group: de.xyz name: Operation version: 1.2.3 path: de.xyz.Operation queue: remote connection:
 * sop
 *
 * @author mikehummel
 */
public class OperationAddress implements Named, Versioned {

    private String provider;
    private String path;
    private Version version;
    private String address;
    private String[] parts;
    private int grpIndex;

    public OperationAddress(String address) {
        super();
        this.address = address;
        // parse address
        int p = address.indexOf("://");
        if (p >= 0) {
            provider = address.substring(0, p);
            address = address.substring(p + 3);
        } else {
            provider = OperationApi.DEFAULT_PROVIDER_NAME;
        }
        p = address.indexOf('/');
        if (p >= 0) {
            path = address.substring(0, p);
            address = address.substring(p + 1);
        } else {
            path = address;
            address = "";
        }

        p = path.indexOf(':');
        if (p >= 0) {
            version = new Version(path.substring(p + 1));
            path = path.substring(0, p);
        } else version = new Version(null);

        grpIndex = path.lastIndexOf('.');

        if (address.length() > 0) {
            parts = address.split("/");
        } else {
            parts = new String[0];
        }
    }

    public String getPath() {
        return path;
    }

    public Version getVersion() {
        return version;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return address;
    }

    public String getPart(int index) {
        return parts[index];
    }

    public int partSize() {
        return parts.length;
    }

    public String getGroup() {
        if (grpIndex >= 0) return path.substring(0, grpIndex);
        return "";
    }

    @Override
    public String getName() {
        if (grpIndex >= 0) return path.substring(grpIndex + 1);
        return path;
    }

    @Override
    public String getVersionString() {
        return version.toString();
    }

    public static OperationAddress create(
            String providerName, OperationDescription desc, String... parts) {
        StringBuilder b =
                new StringBuilder().append(providerName).append("://").append(desc.getPath());
        String v = desc.getVersionString();
        if (v != null) b.append(':').append(v);
        if (parts != null) {
            for (String part : parts) b.append("/").append(part);
        }
        return new OperationAddress(b.toString());
    }
}
