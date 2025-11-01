package com.example.workflowdemo.process;

public enum ProcessState {
    APPROVAL_PENDING,
    MANUAL_PENDING,
    COMPLETED,
    REJECTED;

    public static ProcessState fromValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof ProcessState state) {
            return state;
        }
        return ProcessState.valueOf(value.toString());
    }
}
