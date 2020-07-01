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
package de.mhus.micro.karaf;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.MConfig;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.MException;
import de.mhus.lib.form.ModelUtil;
import de.mhus.micro.core.api.JmsApi;
import de.mhus.micro.core.api.OperationAddress;
import de.mhus.micro.core.api.OperationApi;
import de.mhus.micro.core.api.OperationDescriptor;
import de.mhus.micro.core.api.OperationUtil;
import de.mhus.micro.osgi.PingOperation;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "micro", description = "Micro Service Commands")
@Service
public class MicroServiceCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command:\n"
                            + " list [path]\n"
                            + " action\n"
                            + " info <path> [tag filter]\n"
                            + " search <regex address> [tag filter]\n"
                            + " execute <path> [key=value]*\n"
                            + " ping [ident]\n"
                            + " request - service discovery request",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "path",
            required = false,
            description = "Path to Operation",
            multiValued = false)
    String path;

    @Argument(
            index = 2,
            name = "parameters",
            required = false,
            description = "More Parameters",
            multiValued = true)
    String[] parameters;

    //	@Option(name="-c", aliases="--connection", description="JMS Connection Name",required=false)
    //	String conName = null;

    //	@Option(name="-q", aliases="--queue", description="JMS Connection Queue
    // OperationChannel",required=false)
    //	String queueName = null;

    @Option(
            name = "-v",
            aliases = "--version",
            description = "Version Range [1.2.3,2.0.0)",
            required = false)
    String version = null;

    @Option(
            name = "-o",
            aliases = "--options",
            description = "Execute Options",
            required = false,
            multiValued = true)
    String[] options = null;

    @Option(name = "-p", aliases = "--print", description = "Print File Content", required = false)
    boolean print = false;

    @Override
    public Object execute2() throws Exception {

        //		if (conName == null)
        //			conName = M.l(JmsApi.class).getDefaultConnectionName();

        OperationApi api = M.l(OperationApi.class);

        if (cmd.equals("search")) {
            ConsoleTable out = new ConsoleTable(tblOpt);
            out.setHeaderValues("address", "title", "tags", "parameters", "uuid");
            List<String> tags = null;
            if (MCollection.isSet(parameters))
                tags = MCollection.toList(parameters);

            for (OperationDescriptor desc :
                    api.findOperations(
                            (String)null, version == null ? null : new VersionRange(version), tags)) {
                
                if (desc.getAddress().toString().matches(path))
                    out.addRowValues(
                            desc.getAddress(),
                            desc.getTitle(),
                            desc.getTags(),
                            OperationUtil.getParameters(desc),
                            desc.getUuid());
            }
            out.print(System.out);
        } else if (cmd.equals("ping")) {
            LinkedList<String> tags = new LinkedList<>();
            if (parameters != null) tags.add(OperationDescriptor.TAG_IDENT + "=" + parameters[0]);
            OperationDescriptor desc =
                    api.findOperation(PingOperation.class.getCanonicalName(), null, tags);
            long start = System.currentTimeMillis();
            OperationResult res = api.doExecute(desc, new MConfig());
            long after = System.currentTimeMillis();
            if (!res.isSuccessful()) throw new MException("Ping not successful");
            IProperties map = res.getResultAsMap();
            long other = map.getLong("time", 0);
            if (other <= 0) throw new MException("No time sent");
            System.out.println("Time difference: " + (other - start));
            System.out.println("Duration: " + (after - start));
            System.out.println("Corrected difference: " + ( (other - start) - (after - start)/2 - 4 ) ); // 4 is empiric
        } else if (cmd.equals("list")) {

            List<String> tags = null;
            if (MCollection.isSet(parameters))
                tags = MCollection.toList(parameters);
            
            ConsoleTable out = new ConsoleTable(tblOpt);
            out.setHeaderValues("address", "title", "tags", "parameters", "uuid");
            for (OperationDescriptor desc :
                    api.findOperations(
                            path, version == null ? null : new VersionRange(version), tags)) {
                out.addRowValues(
                        desc.getAddress(),
                        desc.getTitle(),
                        desc.getTags(),
                        OperationUtil.getParameters(desc),
                        desc.getUuid());
            }
            out.print(System.out);
        } else if (cmd.equals("info")) {
            OperationDescriptor desc = null;
            if (path.indexOf("://") >= 0) {
                OperationAddress addr = new OperationAddress(path);
                desc = api.getOperation(addr);
            } else
                desc =
                        api.findOperation(
                                path, version == null ? null : new VersionRange(version), null);
            System.out.println("Title  : " + desc.getTitle());
            String xml = null;
            try {
                xml = MXml.toString(ModelUtil.toXml(desc.getForm()), true);
            } catch (Throwable t) {
            }
            System.out.println("Form   : " + xml);
        } else if (cmd.equals("execute")) {

            IConfig properties = IConfig.readConfigFromString(parameters);
            OperationResult res = null;
            if (path.indexOf("://") >= 0) {
                OperationAddress addr = new OperationAddress(path);
                OperationDescriptor desc = api.getOperation(addr);
                res = api.doExecute(desc, properties, options);
            } else
                res =
                        api.doExecute(
                                path,
                                version == null ? null : new VersionRange(version),
                                null,
                                properties,
                                options);
            System.out.println("Result: " + res);
            if (res != null) {
                System.out.println("MSG: " + res.getMsg());
                System.out.println("RC : " + res.getReturnCode());
                System.out.println("RES: " + res.getResult());
                if (print && res.getResult() instanceof File) {
                    System.out.println("--- Start File Content ---");
                    FileInputStream is = new FileInputStream((File) res.getResult());
                    MFile.copyFile(is, System.out);
                    System.out.println("\n--- End File Content ---");
                    is.close();
                }
            }
        } else if (cmd.equals("request")) {
            M.l(JmsApi.class).requestOperationRegistry();
            System.out.println("ok");
        } else if (cmd.equals("send")) {
            M.l(JmsApi.class).sendLocalOperations();
            System.out.println("ok");
        } else if (cmd.equals("sync")) {
            api.synchronize();
            System.out.println("ok");
        } else if (cmd.equals("providers")) {
            for (String p : api.getProviderNames()) {
                System.out.println(p);
            }
        } else if (cmd.equals("reset")) {
            api.reset();
            System.out.println("OK");
        } else {
            System.out.println("Command not found");
        }
        return null;
    }
}
