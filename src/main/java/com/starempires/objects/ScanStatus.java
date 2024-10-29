package com.starempires.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.annotation.JsonValue;

@RequiredArgsConstructor
@Getter
public enum ScanStatus {

    UNKNOWN(0),
    STALE(1),
    SCANNED(2),
    VISIBLE(3);

    private final int visibility_;

    public boolean isMoreVisible(final ScanStatus status) {
        return visibility_ > status.visibility_;
    }

    public boolean isLessVisible(final ScanStatus status) {
        return visibility_ < status.visibility_;
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
