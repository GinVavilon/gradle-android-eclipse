package com.github.ginvavilon.android_eclipse.example;

import com.google.common.escape.Escapers;

public class Executer {

    public static void main(String[] args) {
        String result = Escapers.builder().addEscape('\n', "<br/>").build().escape("line1\nline2");
        System.out.println(result);
    }

}
