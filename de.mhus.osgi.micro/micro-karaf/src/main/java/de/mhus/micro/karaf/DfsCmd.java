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
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.MException;
import de.mhus.micro.ext.api.dfs.DfsApi;
import de.mhus.micro.ext.api.dfs.FileInfo;
import de.mhus.micro.ext.api.dfs.FileQueueApi;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "sop", name = "dfs", description = "Distributed File System actions")
@Service
public class DfsCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command:\n"
                            + "providers          - list all providers\n"
                            + "list <dir uri>     - list directory content\n"
                            + "info <file uri>    - list file infomation\n"
                            + "export <file uri>  - create a file queue for the file\n"
                            + "print <file uri>   - create file queue entry and print content of file\n"
                            + "import <queue uri> <target uri> - Import a queued file into dfs\n"
                            + "delete <uri>       - delete a dfs file\n"
                            + "mkdir <uri>        - create all missing directorises")
    String cmd;

    @Argument(
            index = 1,
            name = "parameters",
            required = false,
            description = "More Parameters",
            multiValued = true)
    String[] parameters;

    @Option(name = "-f", aliases = "--full", description = "Full output", required = false)
    boolean full = false;

    @Override
    public Object execute2() throws Exception {

        DfsApi api = M.l(DfsApi.class);

        switch (cmd) {
            case "providers":
                {
                    ConsoleTable table = new ConsoleTable(full);
                    table.setHeaderValues("Scheme", "Location", "Uri");
                    for (String uri : api.listProviders()) {
                        MUri u = MUri.toUri(uri);
                        table.addRowValues(u.getScheme(), u.getLocation(), uri);
                    }
                    table.print(System.out);
                }
                break;
            case "list":
                {
                    MUri uri = MUri.toUri(parameters[0]);
                    System.out.println("Directory Content of " + uri);
                    ConsoleTable table = new ConsoleTable(full);
                    table.setHeaderValues("Name", "Uri");
                    for (Entry<String, MUri> file : api.getDirectoryList(uri).entrySet()) {
                        table.addRowValues(file.getKey(), file.getValue());
                    }
                    table.print(System.out);
                }
                break;
            case "info":
                {
                    FileInfo info = api.getFileInfo(parameters[0]);
                    System.out.println("Info for " + parameters[0]);
                    System.out.println("Name    : " + info.getName());
                    System.out.println(
                            "Size    : "
                                    + MString.toByteDisplayString(info.getSize())
                                    + " ("
                                    + info.getSize()
                                    + ")");
                    System.out.println(
                            "Modified: " + MDate.toIso8601(new Date(info.getModified())));
                    System.out.println("URI     : " + info.getUri());
                }
                break;
            case "export":
                {
                    MUri uri = api.exportFile(MUri.toUri(parameters[0]));
                    System.out.println("Exported file: " + parameters[0]);
                    System.out.println("Queue: " + uri);
                }
                break;
            case "print":
                {
                    MUri uri = api.exportFile(MUri.toUri(parameters[0]));
                    System.out.println("Requested file: " + parameters[0]);
                    System.out.println("Queue: " + uri);
                    System.out.println("------------ Content -------------");
                    File file = M.l(FileQueueApi.class).loadFile(uri);
                    System.out.println(MFile.readFile(file));
                    System.out.println("-------------- END ---------------");
                }
                break;
            case "import":
                {
                    MUri queueUri = api.exportFile(MUri.toUri(parameters[0]));
                    MUri target = api.exportFile(MUri.toUri(parameters[1]));
                    api.importFile(queueUri, target);
                    System.out.println("OK");
                }
                break;
            case "delete":
                {
                    MUri uri = api.exportFile(MUri.toUri(parameters[0]));
                    api.deleteFile(uri);
                    System.out.println("OK");
                }
                break;
            case "mkdir":
                {
                    MUri uri = api.exportFile(MUri.toUri(parameters[0]));
                    api.createDirectories(uri);
                    System.out.println("OK");
                }
                break;
            case "test":
                {
                    testProvider(api);
                }
                break;
            default:
                System.out.println("Unknown command");
        }
        return null;
    }

    private void testProvider(DfsApi api) throws IOException, MException {

        FileQueueApi queueApi = M.l(FileQueueApi.class);

        String providerName = parameters[0];
        System.out.println(">>> Root directory index");
        {
            MUri uri = MUri.toUri(providerName + ":/");
            Map<String, MUri> list = api.getDirectoryList(uri);
            for (Entry<String, MUri> entry : list.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
        System.out.println(">>> Create Dir");
        {
            MUri uri = MUri.toUri(providerName + ":/_dfstest/test");
            api.createDirectories(uri);
        }
        System.out.println(">>> Root directory index");
        {
            MUri uri = MUri.toUri(providerName + ":/");
            Map<String, MUri> list = api.getDirectoryList(uri);
            for (Entry<String, MUri> entry : list.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
        System.out.println(">>> Import File");
        {
            // create file in file queue
            UUID id = queueApi.createQueueFile("text.txt", 0);
            queueApi.appendQueueFileContent(id, "This is a test!".getBytes());
            queueApi.closeQueueFile(id);
            MUri queueUri = queueApi.getUri(id);

            // transfer into DFS
            MUri targetUri = MUri.toUri(providerName + ":/_dfstest/test/text.txt");
            api.importFile(queueUri, targetUri);
        }
        System.out.println(">>> Test directory index");
        {
            MUri uri = MUri.toUri(providerName + ":/_dfstest/test");
            Map<String, MUri> list = api.getDirectoryList(uri);
            for (Entry<String, MUri> entry : list.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
        System.out.println(">>> Read File");
        {
            MUri uri = MUri.toUri(providerName + ":/_dfstest/test/text.txt");
            MUri queueUri = api.exportFile(uri);
            File file = queueApi.loadFile(queueUri);
            String content = MFile.readFile(file);
            System.out.println("Content: " + content);
        }
        System.out.println(">>> Update File");
        {
            // create file in file queue
            UUID id = queueApi.createQueueFile("text.txt", 0);
            queueApi.appendQueueFileContent(id, "This is another test!".getBytes());
            queueApi.closeQueueFile(id);
            MUri queueUri = queueApi.getUri(id);

            // transfer into DFS
            MUri targetUri = MUri.toUri(providerName + ":/_dfstest/test/text.txt");
            api.importFile(queueUri, targetUri);
        }
        System.out.println(">>> Read File");
        {
            MUri uri = MUri.toUri(providerName + ":/_dfstest/test/text.txt");
            MUri queueUri = api.exportFile(uri);
            File file = queueApi.loadFile(queueUri);
            String content = MFile.readFile(file);
            System.out.println("Content: " + content);
        }
        System.out.println(">>> Delete File");
        {
            MUri uri = MUri.toUri(providerName + ":/_dfstest/test/text.txt");
            api.deleteFile(uri);
        }
        System.out.println(">>> Delete Test Folder");
        {
            MUri uri = MUri.toUri(providerName + ":/_dfstest/");
            api.deleteFile(uri);
        }
        System.out.println(">>> Root directory index");
        {
            MUri uri = MUri.toUri(providerName + ":/");
            Map<String, MUri> list = api.getDirectoryList(uri);
            for (Entry<String, MUri> entry : list.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}
