package com.sadad.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmutableTriple<L, M, R> {
    private L left;
    private M middle;
    private R right;
}
