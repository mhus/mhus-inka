package de.mhus.micro.core.api;

import java.util.List;

public class SelectorProvider implements Selector {

    public static final SelectorProvider NOT_LOCAL_SELECTOR = new SelectorProvider("local");

    private String name;

    public SelectorProvider(String name) {
        this.name = name;
    }

    @Override
    public void select(List<OperationDescriptor> list) {
        list.removeIf(d -> d.getAddress().getProvider().equals(name));
    }
}
