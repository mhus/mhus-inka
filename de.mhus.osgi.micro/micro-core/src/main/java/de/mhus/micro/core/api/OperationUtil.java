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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MJson;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.MConfig;
import de.mhus.lib.core.json.JacksonTransformer;
import de.mhus.lib.core.json.TransformHelper;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.lib.core.operation.OperationResult;
import de.mhus.lib.core.operation.OperationToIfcProxy;
import de.mhus.lib.core.util.MCallback2;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.errors.NotFoundException;

public class OperationUtil {

    public static OperationResult doExecute(OperationsSelector selector, IConfig request)
            throws NotFoundException {
        return selector.doExecute(request);
    }

    public static OperationResult doExecute(
            Class<?> filter, IConfig request, String... providedTags)
            throws NotFoundException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        return selector.doExecute(request);
    }

    public static OperationResult doExecute(
            Class<?> filter, IConfig request, Selector selectorAlgo, String... providedTags)
            throws NotFoundException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.addSelector(selectorAlgo);
        return selector.doExecute(request);
    }

    public static List<OperationResult> doExecuteAll(
            Class<?> filter, IConfig request, String... providedTags)
            throws NotFoundException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        return selector.doExecuteAll(request);
    }

    public static List<OperationResult> doExecuteAll(
            Class<?> filter, IConfig request, Selector selectorAlgo, String... providedTags)
            throws NotFoundException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.addSelector(selectorAlgo);
        return selector.doExecuteAll(request);
    }

    public static <T> boolean doExecute(
            Class<T> filter, MCallback2<OperationDescriptor, T> executor, String... providedTags)
            throws MException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        OperationDescriptor desc = selector.doSelect();
        if (desc == null) return false;
        T obj = createOperationProxy(filter, desc);
        executor.event(desc, obj);
        return true;
    }

    public static <T> boolean doExecute(
            Class<T> filter,
            MCallback2<OperationDescriptor, T> executor,
            Selector selectorAlgo,
            String... providedTags)
            throws MException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.addSelector(selectorAlgo);
        OperationDescriptor desc = selector.doSelect();
        if (desc == null) return false;
        T obj = createOperationProxy(filter, desc);
        executor.event(desc, obj);
        return true;
    }

    public static <T> boolean doExecuteAll(
            Class<T> filter, MCallback2<OperationDescriptor, T> executor, String... providedTags)
            throws MException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        List<OperationDescriptor> list = selector.doSelectAll();
        for (OperationDescriptor desc : list) {
            try {
                T obj = createOperationProxy(filter, desc);
                executor.event(desc, obj);
            } catch (Throwable t) {
                MLogUtil.log().d(filter, t);
            }
        }
        return !list.isEmpty();
    }

    public static <T> boolean doExecuteAll(
            Class<T> filter,
            MCallback2<OperationDescriptor, T> executor,
            Selector selectorAlgo,
            String... providedTags)
            throws MException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.addSelector(selectorAlgo);
        List<OperationDescriptor> list = selector.doSelectAll();
        for (OperationDescriptor desc : list) {
            try {
                T obj = createOperationProxy(filter, desc);
                executor.event(desc, obj);
            } catch (Throwable t) {
                MLogUtil.log().d(filter, t);
            }
        }
        return !list.isEmpty();
    }

    public static OperationResult doExecute(
            Class<?> filter, VersionRange range, IConfig request, String... providedTags)
            throws NotFoundException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.setVersion(range);
        return selector.doExecute(request);
    }

    public static OperationResult doExecute(
            Class<?> filter,
            VersionRange range,
            IConfig request,
            Selector selectorAlgo,
            String... providedTags)
            throws NotFoundException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.setVersion(range);
        selector.addSelector(selectorAlgo);
        return selector.doExecute(request);
    }

    public static List<OperationResult> doExecuteAll(
            OperationsSelector selector, IConfig request) throws NotFoundException {
        return selector.doExecuteAll(request);
    }

    public static List<OperationResult> doExecuteAll(
            Class<?> filter, VersionRange range, IConfig request, String... providedTags)
            throws NotFoundException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.setVersion(range);
        return selector.doExecuteAll(request);
    }

    public static List<OperationResult> doExecuteAll(
            Class<?> filter,
            VersionRange range,
            IConfig request,
            Selector selectorAlgo,
            String... providedTags)
            throws NotFoundException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.setVersion(range);
        selector.addSelector(selectorAlgo);
        return selector.doExecuteAll(request);
    }

    public static <T> boolean doExecute(
            Class<T> filter,
            VersionRange range,
            MCallback2<OperationDescriptor, T> executor,
            String... providedTags)
            throws MException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.setVersion(range);
        OperationDescriptor desc = selector.doSelect();
        if (desc == null) return false;
        T obj = createOperationProxy(filter, desc);
        executor.event(desc, obj);
        return true;
    }

    public static <T> boolean doExecute(
            Class<T> filter,
            VersionRange range,
            MCallback2<OperationDescriptor, T> executor,
            Selector selectorAlgo,
            String... providedTags)
            throws MException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.setVersion(range);
        selector.addSelector(selectorAlgo);
        OperationDescriptor desc = selector.doSelect();
        if (desc == null) return false;
        T obj = createOperationProxy(filter, desc);
        executor.event(desc, obj);
        return true;
    }

    public static <T> boolean doExecuteAll(
            Class<T> filter,
            VersionRange range,
            MCallback2<OperationDescriptor, T> executor,
            String... providedTags)
            throws MException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.setVersion(range);
        List<OperationDescriptor> list = selector.doSelectAll();
        for (OperationDescriptor desc : list) {
            try {
                T obj = createOperationProxy(filter, desc);
                executor.event(desc, obj);
            } catch (Throwable t) {
                MLogUtil.log().d(filter, t);
            }
        }
        return !list.isEmpty();
    }

    public static <T> boolean doExecuteAll(
            Class<T> filter,
            VersionRange range,
            MCallback2<OperationDescriptor, T> executor,
            Selector selectorAlgo,
            String... providedTags)
            throws MException {
        OperationsSelector selector = new OperationsSelector();
        selector.setFilter(filter);
        selector.setTags(providedTags);
        selector.setVersion(range);
        selector.addSelector(selectorAlgo);
        List<OperationDescriptor> list = selector.doSelectAll();
        for (OperationDescriptor desc : list) {
            try {
                T obj = createOperationProxy(filter, desc);
                executor.event(desc, obj);
            } catch (Throwable t) {
                MLogUtil.log().d(filter, t);
            }
        }
        return !list.isEmpty();
    }

    public static boolean matches(
            OperationDescriptor desc,
            String filter,
            VersionRange version,
            Collection<String> providedTags) {
        return (filter == null || MString.compareFsLikePattern(desc.getPath(), filter))
                && (version == null || version.includes(desc.getVersion()))
                && (providedTags == null || desc.compareTags(providedTags));
    }

    public static boolean isOption(String[] options, String opt) {
        if (options == null || opt == null) return false;
        for (String o : options) if (opt.equals(o)) return true;
        return false;
    }

    public static int getOption(String[] options, String opt, int def) {
        if (options == null || opt == null) return def;
        opt = opt + "=";
        for (String o : options)
            if (o != null && o.startsWith(opt)) return MCast.toint(o.substring(opt.length()), def);
        return def;
    }

    public static long getOption(String[] options, String opt, long def) {
        if (options == null || opt == null) return def;
        opt = opt + "=";
        for (String o : options)
            if (o != null && o.startsWith(opt)) return MCast.tolong(o.substring(opt.length()), def);
        return def;
    }

    public static String getOption(String[] options, String opt, String def) {
        if (options == null || opt == null) return def;
        opt = opt + "=";
        for (String o : options)
            if (o != null && o.startsWith(opt)) return o.substring(opt.length());
        return def;
    }

    public static String getOption(Collection<String> options, String opt, String def) {
        if (options == null || opt == null) return def;
        opt = opt + "=";
        for (String o : options)
            if (o != null && o.startsWith(opt)) return o.substring(opt.length());
        return def;
    }

    @SuppressWarnings("unchecked")
    public static <T> T createOperationProxy(Class<T> ifc, OperationDescriptor desc)
            throws MException {
        if (!desc.getPath().equals(ifc.getName()))
            throw new MException(
                    "Interface and operation do not match", ifc.getName(), desc.getName());

        return (T)
                Proxy.newProxyInstance(
                        ifc.getClassLoader(),
                        new Class[] {ifc},
                        new OperationInvocationHandler(ifc, desc));
    }

    private static class OperationInvocationHandler implements InvocationHandler {

        private static final TransformHelper HELPER = new TransformHelper() {
            {
                strategy = new JacksonTransformer();
            }
        };

        @SuppressWarnings("unused")
        private Class<?> ifc;

        private OperationDescriptor desc;
        private boolean isJava;

        public OperationInvocationHandler(Class<?> ifc, OperationDescriptor desc) {
            this.ifc = ifc;
            this.desc = desc;
            this.isJava =
                    getOption(desc.getTags(), OperationDescription.TAG_TECH, "")
                            .equals(OperationDescription.TECH_JAVA);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            OperationApi api = M.l(OperationApi.class);

            IConfig request = new MConfig();

            request.setString(OperationToIfcProxy.METHOD, method.getName());
            Parameter[] parameters = method.getParameters();
            
            ArrayNode paramsJ = MJson.createArrayNode();
            for (int i = 0; i < parameters.length; i++) {

                ObjectNode paramJ = MJson.createObjectNode();
                paramsJ.add(paramJ);

                paramJ.put(OperationToIfcProxy.PARAMETERTYPE, parameters[i].getType().getCanonicalName());

                if (args.length >= i && args[i] != null) {
                    paramJ.put(OperationToIfcProxy.TYPE, OperationToIfcProxy.JSON);
                    paramJ.put(OperationToIfcProxy.PARAMETERORGTYPE, args[i].getClass().getCanonicalName());

                    JsonNode argJ = MJson.pojoToJson(args[i], HELPER);
                    paramJ.put(OperationToIfcProxy.VALUE, argJ);
                } else {
                    paramJ.put(OperationToIfcProxy.TYPE, OperationToIfcProxy.NULL);
                }

//                if (args[i] != null) {
//                    if (isJava) {
//                        properties.put(
//                                OperationToIfcProxy.PARAMETER + i,
//                                MCast.serializeToString(args[i]));
//                        //					properties.put(OperationToIfcProxy.TYPE + i,
//                        // method.getParameters()[i].getType().getCanonicalName() );
//                        properties.put(
//                                OperationToIfcProxy.TYPE + i, OperationToIfcProxy.SERIALISED);
//                        properties.put(
//                                OperationToIfcProxy.PARAMETERORGTYPE + i,
//                                args[i].getClass().getCanonicalName());
//                    } else {
//                        String type = toType(args[i]);
//                        properties.put(OperationToIfcProxy.TYPE + i, type);
//                        properties.put(OperationToIfcProxy.PARAMETER + i, toString(type, args[i]));
//                    }
//                } else {
//                    properties.put(OperationToIfcProxy.TYPE + i, OperationToIfcProxy.NULL);
//                }
//                properties.put(
//                        OperationToIfcProxy.PARAMETERTYPE + i,
//                        parameters[i].getType().getCanonicalName());
            }

            OperationResult res = api.doExecute(desc, request);

            if (res == null) throw new NullPointerException();

            if (!res.isSuccessful()) throw new MRuntimeException(res.getMsg());

            IConfig response = res.getResultAsConfig();
            return null; //XXX WRONG!!!!
        }

        private String toString(String type, Object o) {
            switch (type) {
                case "short":
                case "byte":
                case "int":
                    return String.valueOf(MCast.toint(o, 0));
                case "long":
                    return String.valueOf(MCast.tolong(o, 0));
                case "double":
                case "float":
                    return String.valueOf(MCast.todouble(o, 0));
                case "string":
                    return String.valueOf(o);
                case "date":
                    return MDate.toIso8601(MCast.toDate(o, MDate.NULL_DATE));
                default:
                    return String.valueOf(o);
            }
        }

        private String toType(Object o) {
            if (o == null) return OperationToIfcProxy.NULL;
            if (o instanceof Integer) return "int";
            if (o instanceof Long) return "long";
            if (o instanceof Short) return "short";
            if (o instanceof String) return "string";
            if (o instanceof Double) return "double";
            if (o instanceof Float) return "float";
            if (o instanceof Byte) return "byte";
            if (o instanceof Date) return "date";
            if (o instanceof Map) return "map";
            if (o instanceof List) return "list";
            return o.getClass().getCanonicalName();
        }
    }

    public static Map<String, String> getParameters(OperationDescriptor desc) {
        TreeMap<String, String> out = new TreeMap<>();
        for (String key : desc.getParameterKeys()) out.put(key, desc.getParameter(key));
        return out;
    }

    public static <T> T getOperationIfc(Class<T> ifc) throws MException {
        OperationApi api = M.l(OperationApi.class);
        OperationDescriptor desc = api.findOperation(ifc.getCanonicalName(), null, null);
        return createOperationProxy(ifc, desc);
    }
}
