import QtQuick 2.9
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick.Controls.Material 2.2

import ComputerManager 1.0
import BoroApi 1.0

Page {
    id: boroLoginPage
    objectName: qsTr("Borogame Login")

    property string pendingIp: ""

    BoroTheme { id: theme }
    font.family: theme.fontFamily

    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 40
        spacing: 16

        Item { Layout.fillHeight: true; Layout.fillWidth: true }

        Image {
            id: logo
            Layout.alignment: Qt.AlignHCenter
            source: "qrc:/res/borogame.svg"
            sourceSize.width: 132
            sourceSize.height: 132
            fillMode: Image.PreserveAspectFit
        }

        Label {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Welcome to Borogame")
            font.pointSize: 22
            color: theme.textPrimary
        }

        Label {
            Layout.alignment: Qt.AlignHCenter
            Layout.maximumWidth: 480
            horizontalAlignment: Text.AlignHCenter
            wrapMode: Text.Wrap
            color: theme.textSecondary
            text: qsTr("Sign in with your Borogame license key to reach your cloud gaming host, the game store, and support tickets.")
        }

        TextField {
            id: licenseInput
            Layout.alignment: Qt.AlignHCenter
            Layout.preferredWidth: 380
            placeholderText: qsTr("License key")
            enabled: !BoroApi.busy
            echoMode: TextInput.Password
            onAccepted: validateButton.clicked()
        }

        Label {
            id: messageLabel
            Layout.alignment: Qt.AlignHCenter
            Layout.maximumWidth: 480
            horizontalAlignment: Text.AlignHCenter
            wrapMode: Text.Wrap
            visible: text.length > 0
            color: theme.danger
        }

        Button {
            id: validateButton
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Sign in")
            enabled: !BoroApi.busy && licenseInput.text.trim().length > 0
            highlighted: true

            onClicked: {
                messageLabel.text = ""
                BoroApi.validateLicense(licenseInput.text)
            }
        }

        BusyIndicator {
            visible: BoroApi.busy
            Layout.alignment: Qt.AlignHCenter
        }

        Button {
            Layout.alignment: Qt.AlignHCenter
            flat: true
            text: qsTr("Need help? Read the FAQ")
            onClicked: BoroApi.openFaq()
        }

        Item { Layout.fillHeight: true; Layout.fillWidth: true }
    }

    Connections {
        target: BoroApi

        function onLicenseValidated(success, message) {
            if (!success) {
                messageLabel.text = message
                return
            }

            messageLabel.color = theme.success
            messageLabel.text = qsTr("License accepted. Looking for your host…")
            boroLoginPage.pendingIp = ""
            BoroApi.fetchHost()
        }

        function onHostFetched(ip) {
            if (ip && ip !== "0") {
                // Add the service-assigned host so it appears in the PC grid
                ComputerManager.addNewHostManually(ip)
                messageLabel.text = qsTr("Host %1 added. Enjoy your games!").arg(ip)
                popTimer.start()
            }
            else {
                messageLabel.color = theme.warning
                messageLabel.text = qsTr("No host is assigned to your account yet. Open the Borogame store to reserve one.")
                boroLoginPage.pendingIp = "0"
            }
        }
    }

    // Give the success message a moment before returning to the PC grid
    Timer {
        id: popTimer
        interval: 1200
        onTriggered: stackView.pop()
    }
}
