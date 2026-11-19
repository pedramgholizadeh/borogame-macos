import QtQuick 2.9
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3
import QtQuick.Controls.Material 2.2
import BoroApi 1.0

Page {
    id: boroStorePage
    objectName: qsTr("Borogame Store")

    BoroTheme { id: theme }
    font.family: theme.fontFamily

    property bool loaded: false

    Component.onCompleted: {
        if (!boroStorePage.loaded) {
            BoroApi.fetchStore()
        }
    }

    Connections {
        target: BoroApi

        function onStoreReady(games) {
            boroStorePage.loaded = true
            storeModel.clear()
            for (var i = 0; i < games.length; i++) {
                storeModel.append(games[i])
            }
        }
    }

    ListModel {
        id: storeModel
    }

    GridView {
        id: storeGrid
        anchors.fill: parent
        anchors.margins: 20
        cellWidth: 240
        cellHeight: 330
        model: storeModel
        clip: true

        delegate: Item {
            width: storeGrid.cellWidth - 16
            height: storeGrid.cellHeight - 16

            Rectangle {
                id: card
                anchors.fill: parent
                radius: 10
                color: theme.surface
                border.color: theme.hairline
                border.width: 1

                Image {
                    id: thumb
                    anchors.top: parent.top
                    anchors.left: parent.left
                    anchors.right: parent.right
                    height: 200
                    fillMode: Image.PreserveAspectCrop
                    clip: true
                    source: imageUrl
                    asynchronous: true

                    BusyIndicator {
                        anchors.centerIn: parent
                        visible: thumb.status === Image.Loading
                    }
                }

                Label {
                    anchors.top: thumb.bottom
                    anchors.topMargin: 10
                    anchors.left: parent.left
                    anchors.right: parent.right
                    anchors.margins: 10
                    text: title
                    font.pointSize: 12
                    elide: Label.ElideRight
                    maximumLineCount: 2
                    wrapMode: Text.Wrap
                }

                MouseArea {
                    anchors.fill: parent
                    hoverEnabled: true
                    cursorShape: Qt.PointingHandCursor
                    onClicked: BoroApi.openGame(title)
                }
            }
        }
    }

    BusyIndicator {
        anchors.centerIn: parent
        visible: BoroApi.busy && !boroStorePage.loaded
    }

    ColumnLayout {
        anchors.centerIn: parent
        visible: boroStorePage.loaded && storeModel.count === 0 && !BoroApi.busy
        spacing: 12

        Label {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("The store is empty right now.")
            color: theme.textSecondary
        }

        Button {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Retry")
            onClicked: {
                boroStorePage.loaded = false
                BoroApi.fetchStore()
            }
        }
    }
}
