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
    radius: 10
    color: "#333333"
    border {
        width: 1
        color: "#5E35B1"
    }

    property bool showConfirmation: false

    // Top accent strip, clipped to the rounded corners of the card.
    clip: true
    Rectangle {
        anchors {
            left: parent.left
            right: parent.right
            top: parent.top
        }
        height: 4
        color: "#9575CD"
    }

    ColumnLayout {
        id: contentColumn
        anchors {
            fill: parent
            margins: 16
        }
        spacing: 10

        RowLayout {
            spacing: 12

            Rectangle {
                Layout.preferredWidth: 36
                Layout.preferredHeight: 36
                radius: 18
                color: "#673AB7"

                Image {
                    anchors.centerIn: parent
                    source: "qrc:/res/borogame.svg"
                    sourceSize.width: 22
                    sourceSize.height: 22
                    fillMode: Image.PreserveAspectFit
                }
            }

            ColumnLayout {
                Layout.fillWidth: true
                spacing: 2

                Label {
                    text: qsTr("Recommended for your Mac")
                    font {
                        pointSize: 15
                        bold: true
                    }
                }

                Label {
                    Layout.fillWidth: true
                    text: qsTr("Tuned for Apple Silicon and a 20 Mbps connection. Apply it any time to reset the streaming settings on this Mac.")
                    font.pointSize: 9
                    wrapMode: Text.Wrap
                    color: "#BDBDBD"
                }
            }
        }

        RowLayout {
            spacing: 6
            Layout.fillWidth: true

            Rectangle {
                radius: 4
                color: "#452A75"
                Layout.preferredHeight: 24
                Layout.preferredWidth: profileChip.implicitWidth + 16

                Label {
                    id: profileChip
                    anchors.centerIn: parent
                    text: qsTr("1080p · 60 FPS")
                    font.pointSize: 9
                    color: "#E1BEE7"
                }
            }

            Rectangle {
                radius: 4
                color: "#452A75"
                Layout.preferredHeight: 24
                Layout.preferredWidth: bitrateChip.implicitWidth + 16

                Label {
                    id: bitrateChip
                    anchors.centerIn: parent
                    text: qsTr("10 Mbps, adaptive")
                    font.pointSize: 9
                    color: "#E1BEE7"
                }
            }

            Rectangle {
                radius: 4
                color: "#452A75"
                Layout.preferredHeight: 24
                Layout.preferredWidth: decoderChip.implicitWidth + 16

                Label {
                    id: decoderChip
                    anchors.centerIn: parent
                    text: qsTr("Metal hardware decode")
                    font.pointSize: 9
                    color: "#E1BEE7"
                }
            }

            Item {
                Layout.fillWidth: true
            }
        }

        RowLayout {
            spacing: 12

            Button {
                id: applyButton
                text: qsTr("Apply recommended settings")
                highlighted: true

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
                color: "#81C784"
                font.pointSize: 9
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
