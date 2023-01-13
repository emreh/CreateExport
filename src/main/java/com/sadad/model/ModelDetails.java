package com.sadad.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelDetails implements Serializable {

    private static final long serialVersionUID = -7955164535072682776L;

    private String title;
    private String fieldName;
    private boolean isFiled;
}
