import QtQuick 2.9
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick.Controls.Material 2.2
import BoroApi 1.0

Page {
    id: boroTicketsPage
    objectName: qsTr("Borogame Tickets")

    BoroTheme { id: theme }
    font.family: theme.fontFamily

    property bool loaded: false

    Component.onCompleted: {
        if (!boroTicketsPage.loaded) {
            BoroApi.fetchTickets()
        }
    }

    Connections {
        target: BoroApi

        function onTicketsReady(tickets) {
            boroTicketsPage.loaded = true
            ticketsModel.clear()
            for (var i = 0; i < tickets.length; i++) {
                ticketsModel.append(tickets[i])
            }
        }
    }

    function statusColor(status) {
        switch (status) {
        case "Open":
            return "#4CAF50"
        case "Answered":
            return "#2196F3"
        case "Customer-Reply":
            return "#FF9800"
        case "Closed":
            return "#9E9E9E"
        default:
            return "#757575"
        }
    }

    ListModel {
        id: ticketsModel
    }

    ListView {
        id: ticketsList
        anchors.fill: parent
        anchors.margins: 20
        anchors.bottomMargin: 70
        model: ticketsModel
        spacing: 10
        clip: true

        delegate: Rectangle {
            width: ticketsList.width
            height: ticketContent.implicitHeight + 24
            radius: 10
            color: theme.surface
            border.color: theme.hairline
            border.width: 1

            ColumnLayout {
                id: ticketContent
                anchors.fill: parent
                anchors.margins: 12
                spacing: 6

                Label {
                    Layout.fillWidth: true
                    text: subject
                    font.pointSize: 13
                    wrapMode: Text.Wrap
                }

                RowLayout {
                    Layout.fillWidth: true
                    spacing: 8

                    Label {
                        Layout.fillWidth: true
                        text: deptname
                        color: theme.textSecondary
                        elide: Label.ElideRight
                    }

                    Label {
                        text: status
                        color: "white"
                        font.pointSize: 10
                        padding: 6
                        background: Rectangle {
                            radius: 5
                            color: boroTicketsPage.statusColor(status)
                        }
                    }
                }

                Label {
                    Layout.alignment: Qt.AlignHCenter
                    text: date
                    color: "#757575"
                    font.pointSize: 10
                }
            }

            MouseArea {
                anchors.fill: parent
                hoverEnabled: true
                cursorShape: Qt.PointingHandCursor
                onClicked: BoroApi.openTicket(url)
            }
        }
    }

    Button {
        anchors.bottom: parent.bottom
        anchors.horizontalCenter: parent.horizontalCenter
        anchors.bottomMargin: 20
        highlighted: true
        text: qsTr("New ticket")
        onClicked: BoroApi.openNewTicket()
    }

    BusyIndicator {
        anchors.centerIn: parent
        visible: BoroApi.busy && !boroTicketsPage.loaded
    }

    ColumnLayout {
        anchors.centerIn: parent
        visible: boroTicketsPage.loaded && ticketsModel.count === 0 && !BoroApi.busy
        spacing: 12

        Label {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("No tickets found.")
            color: theme.textSecondary
        }

        Button {
            Layout.alignment: Qt.AlignHCenter
            flat: true
            text: qsTr("Refresh")
            onClicked: {
                boroTicketsPage.loaded = false
                BoroApi.fetchTickets()
            }
        }
    }
}
