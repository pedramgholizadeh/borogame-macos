import QtQuick 2.9

/**
 * Borogame's design tokens: a restrained, near-black palette with one muted
 * violet accent inherited from the brand. Kept dependency-free so every QML
 * page in the same folder can declare `BoroTheme { id: theme }` and stay in
 * sync with the rest of the app.
 */
QtObject {
    readonly property color canvas: "#0B0B0D"
    readonly property color surface: "#151518"
    readonly property color surfaceRaised: "#1B1B1F"
    readonly property color hairline: "#2B2B31"

    readonly property color textPrimary: "#F4F4F6"
    readonly property color textSecondary: "#9A9AA3"
    readonly property color textMuted: "#6E6E77"

    readonly property color accent: "#B39DDB"
    readonly property color accentStrong: "#8E6CC8"
    readonly property color accentWash: "#241C32"

    readonly property color success: "#81C784"
    readonly property color warning: "#D7AE6B"
    readonly property color danger: "#D98A8A"

    readonly property string fontFamily: "Vazirmatn"

    // Consistent radii and spacing used across Borogame surfaces.
    readonly property int radius: 10
    readonly property int smallRadius: 6
    readonly property int gap: 12
}
