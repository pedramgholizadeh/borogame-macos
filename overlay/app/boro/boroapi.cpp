#include "boroapi.h"

#include <QDesktopServices>
#include <QJsonArray>
#include <QJsonDocument>
#include <QJsonObject>
#include <QNetworkReply>
#include <QNetworkRequest>
#include <QSettings>
#include <QUrl>
#include <QUrlQuery>

namespace {
const char* kBaseUrl = "https://borogame.com/api.php";
const char* kSettingsKey = "userLicense";
const char* kThumbUrl = "https://borogame.com/templates/borogame/dist/images/thumb/";
}

BoroApi::BoroApi(QObject* parent)
    : QObject(parent), m_Cards(0), m_Busy(false)
{
    m_License = QSettings().value(kSettingsKey).toString();
}

bool BoroApi::loggedIn() const
{
    return !m_License.isEmpty();
}

QString BoroApi::licenseKey() const
{
    return m_License;
}

QString BoroApi::nic() const
{
    return m_Nic;
}

QString BoroApi::fullName() const
{
    return m_FullName;
}

QString BoroApi::email() const
{
    return m_Email;
}

int BoroApi::cards() const
{
    return m_Cards;
}

bool BoroApi::busy() const
{
    return m_Busy;
}

void BoroApi::setBusy(bool busy)
{
    if (m_Busy != busy) {
        m_Busy = busy;
        emit busyChanged();
    }
}

QString BoroApi::stripPort(const QString& address) const
{
    // NvAddress::toString() yields "host:port". The Android client takes
    // everything before the first colon; mirror that behavior.
    int colon = address.indexOf(':');
    if (colon > 0) {
        return address.left(colon);
    }
    return address;
}

void BoroApi::get(const QString& path,
                  const std::function<void(const QByteArray&)>& onSuccess,
                  const std::function<void(const QString&)>& onError)
{
    QUrlQuery query;
    QUrl url(kBaseUrl);
    // path already contains "key=value" pairs separated by '&' from callers;
    // set them one by one so values are percent-encoded correctly.
    QStringList pairs = path.split('&');
    for (const QString& pair : pairs) {
        int eq = pair.indexOf('=');
        if (eq > 0) {
            query.addQueryItem(pair.left(eq), pair.mid(eq + 1));
        }
        else {
            query.addQueryItem(pair, QString());
        }
    }
    url.setQuery(query);

    QNetworkRequest request(url);
    request.setAttribute(QNetworkRequest::RedirectPolicyAttribute,
                         QNetworkRequest::NoLessSafeRedirectPolicy);
    request.setTransferTimeout(15000);

    QNetworkReply* reply = m_Nam.get(request);
    connect(reply, &QNetworkReply::finished, this, [reply, onSuccess, onError] {
        reply->deleteLater();
        if (reply->error() != QNetworkReply::NoError) {
            onError(reply->errorString());
            return;
        }
        onSuccess(reply->readAll());
    });
}

void BoroApi::validateLicense(const QString& key)
{
    QString trimmed = key.trimmed();
    if (trimmed.isEmpty()) {
        emit licenseValidated(false, tr("Please enter your Borogame license key."));
        return;
    }

    setBusy(true);
    get(QString("license=%1").arg(trimmed),
        [this, trimmed](const QByteArray& data) {
            setBusy(false);
            QJsonDocument doc = QJsonDocument::fromJson(data);
            if (!doc.isObject()) {
                emit licenseValidated(false, tr("Invalid response from Borogame."));
                return;
            }

            QJsonObject obj = doc.object();

            // The backend sends validation as the STRING "true"/"false"
            // (org.json's getBoolean() coerces it on Android; Qt does not).
            QJsonValue validation = obj.value("validation");
            bool valid = validation.toBool() ||
                    validation.toString().compare(QLatin1String("true"), Qt::CaseInsensitive) == 0;
            if (!valid) {
                emit licenseValidated(false, tr("Invalid license key."));
                return;
            }

            // The same response carries the profile (docs/api-contract.md §1).
            // Fields arrive as JSON strings ("cards": "0"), so read them via
            // toString() with numeric fallbacks.
            m_License = trimmed;
            m_Nic = obj.value("nic").toString();
            m_FullName = obj.value("fullName").toString();
            m_Email = obj.value("email").toString();
            QJsonValue cards = obj.value("cards");
            if (cards.isString()) {
                m_Cards = cards.toString().toInt();
            }
            else {
                m_Cards = cards.toInt();
            }

            QSettings().setValue(kSettingsKey, trimmed);
            emit profileChanged();
            emit loggedInChanged();
            emit licenseValidated(true, QString());
        },
        [this](const QString& error) {
            setBusy(false);
            emit licenseValidated(false, tr("Connection error: %1").arg(error));
        });
}

void BoroApi::fetchHost()
{
    setBusy(true);
    get(QString("fetchHost=%1").arg(m_License),
        [this](const QByteArray& data) {
            setBusy(false);
            QJsonDocument doc = QJsonDocument::fromJson(data);
            QString ip = doc.isObject() ? doc.object().value("ip").toString() : QString();
            if (ip.isEmpty()) {
                emit hostFetched(QString());
            }
            else {
                emit hostFetched(ip);
            }
        },
        [this](const QString& error) {
            setBusy(false);
            emit hostFetched(QString());
        });
}

void BoroApi::fetchStore()
{
    setBusy(true);
    get("store=true",
        [this](const QByteArray& data) {
            setBusy(false);
            QVariantList games;
            QJsonDocument doc = QJsonDocument::fromJson(data);
            if (!doc.isArray()) {
                emit storeReady(games);
                return;
            }

            // Response is an array of arrays: [title, unused, gameUrl]
            QJsonArray rows = doc.array();
            for (const QJsonValue& rowValue : rows) {
                QJsonArray row = rowValue.toArray();
                if (row.size() < 3) {
                    continue;
                }

                // The Android client strips literal '[', ']' and '"' from
                // both the title and URL columns.
                QString title = row.at(0).toString()
                        .remove('[').remove(']').remove('"');
                QString gameUrl = row.at(2).toString()
                        .remove('[').remove(']').remove('"');

                QVariantMap game;
                game["title"] = title;
                game["imageUrl"] = QString(kThumbUrl) +
                        title.replace(' ', '_') + ".jpg";
                game["gameUrl"] = gameUrl;
                games.append(game);
            }
            emit storeReady(games);
        },
        [this](const QString& error) {
            setBusy(false);
            emit storeReady(QVariantList());
        });
}

void BoroApi::fetchTickets()
{
    setBusy(true);
    get(QString("tickets=%1").arg(m_License),
        [this](const QByteArray& data) {
            setBusy(false);
            QVariantList tickets;
            QJsonDocument doc = QJsonDocument::fromJson(data);
            if (!doc.isArray()) {
                emit ticketsReady(tickets);
                return;
            }

            for (const QJsonValue& value : doc.array()) {
                QJsonObject ticket = value.toObject();
                QVariantMap map;
                map["subject"] = ticket.value("subject").toString();
                map["deptname"] = ticket.value("deptname").toString();
                map["status"] = ticket.value("status").toString();
                map["date"] = ticket.value("date").toString();
                map["url"] = ticket.value("url").toString();
                tickets.append(map);
            }
            emit ticketsReady(tickets);
        },
        [this](const QString& error) {
            setBusy(false);
            emit ticketsReady(QVariantList());
        });
}

void BoroApi::sendAutoPairPin(const QString& pin, const QString& hostAddress)
{
    if (m_License.isEmpty() || pin.isEmpty() || hostAddress.isEmpty()) {
        return;
    }

    // Fire-and-forget relay, exactly like the Android client: the backend
    // enters the PIN on the designated host so pairing is zero-touch.
    get(QString("pinPair=%1&ip=%2").arg(pin, stripPort(hostAddress)),
        [](const QByteArray&) {},
        [](const QString&) {});
}

void BoroApi::logout()
{
    m_License.clear();
    m_Nic.clear();
    m_FullName.clear();
    m_Email.clear();
    m_Cards = 0;
    QSettings().remove(kSettingsKey);
    emit profileChanged();
    emit loggedInChanged();
}

QString BoroApi::ssoUrl(const QString& path) const
{
    return QString("%1?sso=%2&path=%3")
            .arg(kBaseUrl, m_License, path);
}

void BoroApi::openGame(const QString& title)
{
    QString underscored = title;
    underscored.replace(' ', '_');
    if (m_License.isEmpty()) {
        QDesktopServices::openUrl(QString("https://borogame.com/games.php/%1").arg(underscored));
    }
    else {
        QDesktopServices::openUrl(ssoUrl("games.php/" + underscored));
    }
}

void BoroApi::openNewTicket()
{
    if (m_License.isEmpty()) {
        return;
    }
    QDesktopServices::openUrl(ssoUrl("submitticket.php"));
}

void BoroApi::openTicket(const QString& ticketUrl)
{
    if (m_License.isEmpty() || ticketUrl.isEmpty()) {
        return;
    }
    // Matches the Android client: path=api.php?<ticket url>
    QDesktopServices::openUrl(ssoUrl("api.php?" + ticketUrl));
}

void BoroApi::openFaq()
{
    QDesktopServices::openUrl(QUrl("https://borogame.com/faq.php"));
}

void BoroApi::openNoHostStore()
{
    if (m_License.isEmpty()) {
        return;
    }
    QDesktopServices::openUrl(ssoUrl("games.php"));
}
