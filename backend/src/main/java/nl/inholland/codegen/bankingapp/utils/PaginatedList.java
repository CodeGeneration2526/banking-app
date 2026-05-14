package nl.inholland.codegen.bankingapp.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedList<T> {
    private int pageStart;
    private int pageEnd;
    private int totalCount;
    private List<T> content;
}
