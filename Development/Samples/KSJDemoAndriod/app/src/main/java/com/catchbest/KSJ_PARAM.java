package com.catchbest;

/**
 * Created by terry on 17-12-29.
 */

public enum KSJ_PARAM {
    KSJ_EXPOSURE,        // Exposure Time (ms)
    KSJ_RED,                 // Red Gain, for line scan sensor map to seg0, for ccd map to KSJ_VGAGAIN
    KSJ_GREEN,               // Green Gain, for line scan sensor map to seg1, for ccd map to KSJ_LAMPLEVEL
    KSJ_BLUE,                // Blue Gain, for CCD map to KSJ_CDSGAIN
    KSJ_GAMMA,               // Gamma
    KSJ_PREVIEW_COLUMNSTART, // Preview Col Start
    KSJ_PREVIEW_ROWSTART,    // Preview Row Start
    KSJ_CAPTURE_COLUMNSTART, // Capture Col Start
    KSJ_CAPTURE_ROWSTART,    // Capture Row Start
    KSJ_HORIZONTALBLANK,     // Horizontal Blank
    KSJ_VERTICALBLANK,       // Vertical Blank
    KSJ_FLIP,                // Flip
    KSJ_BIN,                 // Binning
    KSJ_MIRROR,              // Mirror
    KSJ_CONTRAST,            // Contrast
    KSJ_BRIGHTNESS,          // Brightness
    KSJ_VGAGAIN,             // VGA Gain(CCD)
    KSJ_CLAMPLEVEL,          // Clamp Level(CCD)
    KSJ_CDSGAIN,             // CDS Gain(CCD)
    KSJ_RED_SHIFT,           // Not Use
    KSJ_GREEN_SHIFT,         // Not Use
    KSJ_BLUE_SHIFT,          // Not Use
    KSJ_COMPANDING,          // Companding
    KSJ_EXPOSURE_LINES,      // Exposure Lines
    KSJ_SATURATION,          // Saturation
    KSJ_TRIGGERDELAY,              // Trigger Delay Step = 100uS
    KSJ_STROBEDELAY,               // Not Use
    KSJ_TRIGGER_MODE,              // Trigger Mode
    KSJ_TRIGGER_METHOD,            // Trigger Method
    KSJ_BLACKLEVEL,                // Black Level
    KSJ_BLACKLEVEL_THRESHOLD_AUTO, // Black Level Threshold Auto
    KSJ_BLACKLEVEL_THRESHOLD_LO,   // Black Level Low Threshold
    KSJ_BLACKLEVEL_THRESHOLD_HI    // Black Level High Threshold


}
