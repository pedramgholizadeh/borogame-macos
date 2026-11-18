import QtQuick 2.9
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick.Controls.Material 2.2
import BoroApi 1.0

Page {
    id: boroProfilePage
    objectName: qsTr("Borogame Account")

    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 40
        spacing: 12

        Item { Layout.fillHeight: true }

        Rectangle {
            id: avatar
            Layout.alignment: Qt.AlignHCenter
            width: 84
            height: 84
            radius: 42
            color: "#673AB7"

            Label {
                anchors.centerIn: parent
                text: BoroApi.nic.length > 0 ? BoroApi.nic : "?"
                font.pointSize: 24
                font.bold: true
                color: "white"
            }
        }

        Label {
            Layout.alignment: Qt.AlignHCenter
            text: BoroApi.fullName
            font.pointSize: 18
        }

        Label {
            Layout.alignment: Qt.AlignHCenter
            text: BoroApi.email
            color: "#BDBDBD"
        }

        Label {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Cards: %1").arg(BoroApi.cards)
            color: "#BDBDBD"
        }

        Item { height: 12 }

        Button {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Refresh profile")
            enabled: !BoroApi.busy
            onClicked: BoroApi.validateLicense(BoroApi.licenseKey)
        }

        Button {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Check my host")
            enabled: !BoroApi.busy

            onClicked: {
                BoroApi.fetchHost()
            }
        }

        Button {
            Layout.alignment: Qt.AlignHCenter
            flat: true
            text: qsTr("Frequently Asked Questions")
            onClicked: BoroApi.openFaq()
        }

        Button {
            Layout.alignment: Qt.AlignHCenter
            flat: true
            text: qsTr("Sign out")
            Material.foreground: "#EF5350"

            onClicked: signOutDialog.open()
        }

        Item { Layout.fillHeight: true }
    }

    NavigableMessageDialog {
        id: signOutDialog
        standardButtons: Dialog.Yes | Dialog.No
        text: qsTr("Sign out of Borogame? You will need your license key to sign in again.")
        onAccepted: {
            BoroApi.logout()
            stackView.replace("qrc:/gui/BoroLoginPage.qml")
        }
    }
}
