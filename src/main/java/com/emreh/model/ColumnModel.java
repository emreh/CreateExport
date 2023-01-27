package com.emreh.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnModel implements Serializable {

    private List<ColumnModelDetails> columnModelDetailsList = new ArrayList<>();
    private boolean filter = false;
}
