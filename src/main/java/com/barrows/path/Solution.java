package com.barrows.path;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class Solution {
    @Getter
    private final List<Door> doors;
    private boolean valid;

    public static final Solution EMPTY = new Solution(Collections.emptyList(), false);

    public Solution(List<Door> doors) {
        this(doors, true);
    }

    public Solution(List<Door> doors, boolean valid)
    {
        this.doors = doors;
        this.valid = valid;
    }

    public boolean isInvalid(){
        return !this.valid;
    }

    public void invalidate()
    {
        this.valid = false;
    }
}
