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
package de.mhus.lib.core.directory.fs;

import java.io.File;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.directory.ResourceNode;

public class FileResourceRoot extends FileResource {

    private static final long serialVersionUID = 1L;

    public FileResourceRoot(File documentRoot) {
        super(null, null, documentRoot);
    }

    @SuppressWarnings("rawtypes")
    public ResourceNode getResource(String target) {

        return getResource(this, target);
    }

    @SuppressWarnings("rawtypes")
    private ResourceNode getResource(FileResource parent, String target) {
        if (parent == null || target == null) return null;
        if (target.length() == 0) return parent;

        String next = null;
        if (MString.isIndex(target, '/')) {
            next = MString.beforeIndex(target, '/');
            target = MString.afterIndex(target, '/');
        } else {
            next = target;
            target = "";
        }
        if (next.length() == 0) return getResource(parent, target);

        ResourceNode n = parent.getNode(next);
        if (n == null) return null;

        return getResource((FileResource) n, target);
    }
}
