package de.mhus.micro.core.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.mhus.lib.core.util.SoftHashMap;

public class SelectorRoundRobin implements Selector {

    protected String ident;

    protected static SoftHashMap<String, Set<UUID>> executedLists =
            new SoftHashMap<>(); // list of already executed UUIDs

    public SelectorRoundRobin(OperationsSelector selector) {
        Collection<String> tags = selector.getProvidedTags();
        LinkedList<String> tagsList = null;
        if (tags != null) {
            tagsList = new LinkedList<String>(tags);
            Collections.sort(tagsList);
        }
        ident = selector.getFilter() + "|" + selector.getVersion() + "|" + tagsList;
    }

    @Override
    public void select(List<OperationDescriptor> list) {

        if (list.isEmpty()) return; // do not round robin if not found

        Set<UUID> executed = getExecutedList(ident);
        OperationDescriptor first = list.get(0); // remember the first one
        synchronized (executed) {
            if (!executed.isEmpty()) {
                list.removeIf(i -> executed.contains(i.getUuid()));
            }
            if (!list.isEmpty()) {
                first = list.get(0);
            } else {
                // reset RR list
                executed.clear();
            }

            // remove all others
            final UUID firstId = first.getUuid();
            list.removeIf(i -> !i.getUuid().equals(firstId));

            // remember as executed
            executed.add(firstId);
        }
    }

    protected Set<UUID> getExecutedList(String ident) {

        synchronized (executedLists) {
            Set<UUID> list = executedLists.get(ident);
            if (list == null) {
                list = new HashSet<>();
                executedLists.put(ident, list);
            }
            return list;
        }
    }
}
