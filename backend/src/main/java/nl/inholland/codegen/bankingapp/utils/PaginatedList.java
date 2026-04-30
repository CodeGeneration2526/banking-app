package nl.inholland.codegen.bankingapp.utils;

import java.util.List;

public class PaginatedList<T> {
    public int pageStart;
    public int pageEnd;
    public int totalCount;

    public List<T> content;
}
