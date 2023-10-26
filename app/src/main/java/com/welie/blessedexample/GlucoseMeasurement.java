package com.welie.blessedexample;

import com.welie.blessed.BluetoothBytesParser;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import static com.welie.blessedexample.GlucoseMeasurementUnit.MiligramPerDeciliter;
import static com.welie.blessedexample.GlucoseMeasurementUnit.MmolPerLiter;

import androidx.annotation.NonNull;

public class GlucoseMeasurement implements Serializable {

    public final GlucoseMeasurementUnit unit;
    public Date timestamp;
    public int sequenceNumber;
    public boolean contextWillFollow;
    public float value;

    public GlucoseMeasurement(byte[] byteArray) {
        BluetoothBytesParser parser = new BluetoothBytesParser(byteArray);

        // Parse flags
        final int flags = parser.getUInt8();
        final boolean timeOffsetPresent = (flags & 0x01) > 0;
        final boolean typeAndLocationPresent = (flags & 0x02) > 0;
        unit = (flags & 0x04) > 0 ? MmolPerLiter : MiligramPerDeciliter;
        contextWillFollow = (flags & 0x10) > 0;

        // Sequence number is used to match the reading with an optional glucose context
        sequenceNumber = parser.getUInt16();

        // Read timestamp
        timestamp = parser.getDateTime();

        if (timeOffsetPresent) {
            int timeOffset = parser.getSInt16();
            timestamp = new Date(timestamp.getTime() + (timeOffset * 60000L));
        }

        if (typeAndLocationPresent) {
            final float glucoseConcentration = parser.getSFloat();
            final int multiplier = unit == MiligramPerDeciliter ? 100000 : 1000;
            value = glucoseConcentration * multiplier;
        }
    }

    @NonNull
    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        String formattedTimestamp = timestamp != null ? df.format(timestamp) : "null";
        return String.format(Locale.ENGLISH,"%.1f %s, at (%s)", value, unit == MmolPerLiter ? "mmol/L" : "mg/dL", formattedTimestamp);
    }
}