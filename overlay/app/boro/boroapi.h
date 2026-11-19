#ifndef BORO_API_H
#define BORO_API_H

#include <QObject>
#include <QJsonObject>
#include <QNetworkAccessManager>
#include <QString>
#include <QVariantList>

/*
 * BoroApi — Borogame service integration layer.
 *
 * Implements the HTTP contract documented in docs/api-contract.md against
 * https://borogame.com/api.php. Exposed to QML as the singleton "BoroApi".
 *
 * All network access for the Borogame service lives here; QML pages must
 * never perform HTTP requests directly.
 */
class BoroApi : public QObject
{
    Q_OBJECT

    Q_PROPERTY(bool loggedIn READ loggedIn NOTIFY loggedInChanged)
    Q_PROPERTY(QString licenseKey READ licenseKey NOTIFY loggedInChanged)
    Q_PROPERTY(QString nic READ nic NOTIFY profileChanged)
    Q_PROPERTY(QString fullName READ fullName NOTIFY profileChanged)
    Q_PROPERTY(QString email READ email NOTIFY profileChanged)
    Q_PROPERTY(int cards READ cards NOTIFY profileChanged)
    Q_PROPERTY(bool busy READ busy NOTIFY busyChanged)

public:
    explicit BoroApi(QObject* parent = nullptr);

    bool loggedIn() const;
    QString licenseKey() const;
    QString nic() const;
    QString fullName() const;
    QString email() const;
    int cards() const;
    bool busy() const;

    // Build the SSO redirect URL for opening Borogame web pages in a browser
    // (api.php?sso=<license>&path=<path>).
    Q_INVOKABLE QString ssoUrl(const QString& path) const;

    // Open a Borogame game page. `path` is the relative path returned by the
    // store endpoint (e.g. "games.php/EA_Sports_FC_27"). A bare legacy title
    // is also accepted and converted to that path.
    Q_INVOKABLE void openGame(const QString& path);
    Q_INVOKABLE void openNewTicket();
    Q_INVOKABLE void openTicket(const QString& ticketUrl);
    Q_INVOKABLE void openFaq();
    Q_INVOKABLE void openNoHostStore();

public slots:
    // api.php?license=<KEY> — validates and returns the profile in one call
    void validateLicense(const QString& key);

    // api.php?license=<KEY> — silently refresh the profile/card balance.
    // Used by the Windows client's periodic fetchUserProfileTimer.
    void refreshProfile();

    // api.php?fetchHost=<KEY> — service-assigned host ("0" means none)
    void fetchHost();

    // api.php?store=true — store listing
    void fetchStore();

    // api.php?tickets=<KEY> — WHMCS support tickets
    void fetchTickets();

    // api.php?pinPair=<PIN>&ip=<IP> — fire-and-forget pairing relay.
    // Mirrors com.limelight.AutoPair from the Android client.
    void sendAutoPairPin(const QString& pin, const QString& hostAddress);

    // Forget the stored license
    void logout();

signals:
    void loggedInChanged();
    void profileChanged();
    void busyChanged();

    void licenseValidated(bool success, QString message);
    void profileRefreshed();
    void hostFetched(QString ip);
    void storeReady(QVariantList games);
    void ticketsReady(QVariantList tickets);

private:
    void setBusy(bool busy);
    void applyProfile(const QJsonObject& obj);
    QString stripPort(const QString& address) const;
    void get(const QString& path,
             const std::function<void(const QByteArray&)>& onSuccess,
             const std::function<void(const QString&)>& onError);

    QNetworkAccessManager m_Nam;
    QString m_License;
    QString m_Nic;
    QString m_FullName;
    QString m_Email;
    int m_Cards;
    bool m_Busy;
};

#endif  // BORO_API_H
