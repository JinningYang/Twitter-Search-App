package com.company;

import java.util.Set;

public interface SearchAble<VALUE_TYPE> {
    Set<VALUE_TYPE> searchFor(String K);
}
