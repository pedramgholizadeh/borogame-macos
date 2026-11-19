// api-smoke.cpp — headless integration test for BoroApi against the live
// Borogame backend. Exercises the exact production code path:
//
//     moc boroapi.h + compile boroapi.cpp + this file, then run.
//
// Usage: api-smoke <license-key>
// Prints PASS/FAIL per checked endpoint. Exit code 0 = all passed.
#include <QCoreApplication>
#include <QTimer>
#include <cstdio>

#include "boroapi.h"

int main(int argc, char* argv[])
{
    QCoreApplication app(argc, argv);
    if (app.arguments().size() < 2) {
        fprintf(stderr, "Usage: api-smoke <license-key>\n");
        return 2;
    }
    const QString key = app.arguments().at(1);

    BoroApi api;

    int failures = 0;
    bool licenseDone = false;
    bool hostDone = false;

    QObject::connect(&api, &BoroApi::licenseValidated,
                     [&](bool ok, const QString& message) {
        licenseDone = true;
        if (ok) {
            printf("PASS license: validation=true, profile nic='%s' name='%s' email='%s' cards=%d\n",
                   api.nic().toUtf8().constData(),
                   api.fullName().toUtf8().constData(),
                   api.email().toUtf8().constData(),
                   api.cards());
        }
        else {
            printf("FAIL license: %s\n", message.toUtf8().constData());
            failures++;
        }
        api.fetchHost();
    });

    QObject::connect(&api, &BoroApi::hostFetched, [&](const QString& ip) {
        hostDone = true;
        if (ip.isNull()) {
            printf("FAIL fetchHost: no response\n");
            failures++;
        }
        else if (ip == "0") {
            printf("PASS fetchHost: no host assigned (\"0\")\n");
        }
        else {
            printf("PASS fetchHost: assigned host %s\n", ip.toUtf8().constData());
        }
        app.quit();
    });

    api.validateLicense(key);

    // Hard timeout: the backend must answer within 30s overall
    QTimer::singleShot(30000, [&] {
        printf("FAIL: timeout (licenseDone=%d hostDone=%d)\n", licenseDone, hostDone);
        failures++;
        app.quit();
    });

    app.exec();
    return failures == 0 ? 0 : 1;
}
