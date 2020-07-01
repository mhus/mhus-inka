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
package de.mhus.micro.osgi;

import org.osgi.service.component.annotations.Component;

import de.mhus.lib.annotations.strategy.OperationService;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.base.service.IdentUtil;
import de.mhus.lib.core.operation.AbstractOperation;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.Successful;
import de.mhus.lib.core.operation.TaskContext;
import de.mhus.lib.core.shiro.AccessUtil;

@Component(service = Operation.class, property = "tags=acl=*")
@OperationService(title = "Ping")
public class PingOperation extends AbstractOperation {

    @Override
    protected OperationResult doExecute2(TaskContext context) throws Exception {
        log().d("PING PONG", context.getParameters());
        String user = AccessUtil.getPrincipal();
        String ident = IdentUtil.getServerIdent();
        String pid = MSystem.getPid();
        String host = MSystem.getHostname();
        String free = MSystem.freeMemoryAsString();
        long time = System.currentTimeMillis();

        return new Successful(
                this, "ok", "user", user, "ident", ident, "pid", pid, "host",
                host, "free", free, "time", "" + time);
    }

}
