import QtQuick 2.9
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick.Controls.Material 2.2

import StreamingPreferences 1.0

/**
 * Borogame's one-click streaming profile.
 *
 * Kept in overlay/ so the upstream settings page only needs a two-line hook.
 * The values mirror the tuned defaults in StreamingPreferences and can be
 * reapplied at any time (also fixes machines that already have old settings).
 */
Rectangle {
    id: profileCard

    implicitHeight: contentColumn.implicitHeight + 2 * contentColumn.anchors.margins
    radius: theme.radius
    color: theme.surface
    border {
        width: 1
        color: theme.hairline
    }

    BoroTheme { id: theme }

    property bool showConfirmation: false

    // A single quiet accent rail instead of a loud header banner.
    Rectangle {
        anchors {
            left: parent.left
            top: parent.top
            bottom: parent.bottom
        }
        width: 2
        radius: 1
        color: theme.accent
        opacity: 0.75
    }

    ColumnLayout {
        id: contentColumn
        anchors {
            fill: parent
            margins: 18
            leftMargin: 24
        }
        spacing: 12

        RowLayout {
            spacing: 14

            Rectangle {
                Layout.preferredWidth: 32
                Layout.preferredHeight: 32
                radius: 16
                color: theme.accentWash

                Image {
                    anchors.centerIn: parent
                    source: "qrc:/res/borogame.svg"
                    sourceSize.width: 20
                    sourceSize.height: 20
                    fillMode: Image.PreserveAspectFit
                }
            }

            ColumnLayout {
                Layout.fillWidth: true
                spacing: 3

                Label {
                    text: qsTr("Recommended for your Mac")
                    font {
                        family: theme.fontFamily
                        pointSize: 15
                        weight: Font.Medium
                    }
                    color: theme.textPrimary
                }

                Label {
                    Layout.fillWidth: true
                    text: qsTr("Tuned for Apple Silicon and a 20 Mbps connection. Apply it any time to reset the streaming settings on this Mac.")
                    font {
                        family: theme.fontFamily
                        pointSize: 9
                    }
                    wrapMode: Text.Wrap
                    color: theme.textSecondary
                }
            }
        }

        RowLayout {
            spacing: 6
            Layout.fillWidth: true

            Rectangle {
                Layout.preferredHeight: 22
                Layout.preferredWidth: profileChip.implicitWidth + 14
                radius: theme.smallRadius
                color: theme.accentWash

                Label {
                    id: profileChip
                    anchors.centerIn: parent
                    text: qsTr("1080p · 60 FPS")
                    font {
                        family: theme.fontFamily
                        pointSize: 9
                    }
                    color: theme.accent
                }
            }

            Rectangle {
                Layout.preferredHeight: 22
                Layout.preferredWidth: bitrateChip.implicitWidth + 14
                radius: theme.smallRadius
                color: theme.accentWash

                Label {
                    id: bitrateChip
                    anchors.centerIn: parent
                    text: qsTr("10 Mbps, adaptive")
                    font {
                        family: theme.fontFamily
                        pointSize: 9
                    }
                    color: theme.accent
                }
            }

            Rectangle {
                Layout.preferredHeight: 22
                Layout.preferredWidth: decoderChip.implicitWidth + 14
                radius: theme.smallRadius
                color: theme.accentWash

                Label {
                    id: decoderChip
                    anchors.centerIn: parent
                    text: qsTr("Metal hardware decode")
                    font {
                        family: theme.fontFamily
                        pointSize: 9
                    }
                    color: theme.accent
                }
            }

            Item { Layout.fillWidth: true }
        }

        RowLayout {
            spacing: 14

            Button {
                id: applyButton
                text: qsTr("Apply recommended settings")
                flat: true
                font {
                    family: theme.fontFamily
                    pointSize: 10
                }
                Material.foreground: theme.accent
                background: Rectangle {
                    radius: theme.smallRadius
                    color: applyButton.hovered ? theme.accentWash : "transparent"
                    border {
                        width: 1
                        color: theme.accent
                    }
                }

                ToolTip.delay: 800
                ToolTip.timeout: 5000
                ToolTip.visible: hovered
                ToolTip.text: qsTr("Sets 1080p60, a 10 Mbps adaptive bitrate, hardware decoding, frame pacing and low-latency display settings.")

                onClicked: {
                    StreamingPreferences.width = 1920
                    StreamingPreferences.height = 1080
                    StreamingPreferences.fps = 60
                    StreamingPreferences.bitrateKbps = StreamingPreferences.getDefaultBitrate(1920, 1080, 60, false)
                    StreamingPreferences.autoAdjustBitrate = true
                    StreamingPreferences.enableVsync = true
                    StreamingPreferences.framePacing = true
                    StreamingPreferences.videoDecoderSelection = StreamingPreferences.VDS_AUTO
                    StreamingPreferences.videoCodecConfig = StreamingPreferences.VCC_AUTO
                    StreamingPreferences.rendererSelection = StreamingPreferences.RS_METAL
                    StreamingPreferences.enableYUV444 = false
                    StreamingPreferences.unlockBitrate = false
                    StreamingPreferences.keepAwake = true
                    StreamingPreferences.save()

                    profileCard.showConfirmation = true
                    confirmationTimer.start()
                }
            }

            Label {
                visible: profileCard.showConfirmation
                text: qsTr("Applied. Start a new stream to use it.")
                font {
                    family: theme.fontFamily
                    pointSize: 9
                }
                color: theme.success
                wrapMode: Text.Wrap
            }
        }
    }

    Timer {
        id: confirmationTimer
        interval: 4000
        onTriggered: profileCard.showConfirmation = false
    }
}
