import QtQuick 2.9

import ComputerManager 1.0
import BoroApi 1.0

/*
 * BoroServiceController — keeps the macOS client on the Windows-client flow.
 *
 * Borogame hosts are assigned by the service, never discovered on the LAN.
 * This controller polls api.php?fetchHost=<license>, adds the assigned host to
 * ComputerManager, and refreshes the profile/card balance on the same cadence
 * as the Windows client. It is invisible and owns no UI.
 */
Item {
    id: boroServiceController
    visible: false

    property string assignedHost: ""

    function requestHost() {
        if (BoroApi.loggedIn && !BoroApi.busy) {
            BoroApi.fetchHost()
        }
    }

    function requestProfile() {
        if (BoroApi.loggedIn) {
            BoroApi.refreshProfile()
        }
    }

    Component.onCompleted: {
        if (BoroApi.loggedIn) {
            Qt.callLater(requestHost)
            Qt.callLater(requestProfile)
        }
    }

    Connections {
        target: BoroApi

        function onLoggedInChanged() {
            if (BoroApi.loggedIn) {
                Qt.callLater(requestHost)
                Qt.callLater(requestProfile)
            }
            else {
                boroServiceController.assignedHost = ""
            }
        }

        function onHostFetched(ip) {
            var address = String(ip || "").trim()
            if (address.length === 0 || address === "0") {
                boroServiceController.assignedHost = ""
                return
            }

            // addNewHostManually() folds duplicate UUIDs into the existing
            // entry, but avoiding redundant calls keeps the network quiet.
            if (address === boroServiceController.assignedHost) {
                return
            }

            boroServiceController.assignedHost = address
            ComputerManager.addNewHostManually(address)
        }
    }

    Timer {
        id: hostTimer
        interval: 15000
        repeat: true
        running: BoroApi.loggedIn
        onTriggered: requestHost()
    }

    Timer {
        id: profileTimer
        interval: 30000
        repeat: true
        running: BoroApi.loggedIn
        onTriggered: requestProfile()
    }
}
