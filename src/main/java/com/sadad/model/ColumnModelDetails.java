package com.sadad.model;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class ColumnModelDetails implements Serializable {

    private static final long serialVersionUID = -7955164535072682776L;

    @NonNull
    private String title;
    @NonNull
    private String fieldName;
    @NonNull
    private boolean isFiled;
    private boolean merge = false;
    private int index = -1;
}
