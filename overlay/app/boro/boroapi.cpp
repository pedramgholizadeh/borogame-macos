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

void BoroApi::applyProfile(const QJsonObject& obj)
{
    // The backend is inconsistent about capitalisation for the full name and
    // sends numbers as JSON strings. Read both spellings and coerce manually.
    m_Nic = obj.value("nic").toString();
    QString fullName = obj.value("fullName").toString();
    if (fullName.isEmpty()) {
        fullName = obj.value("fullname").toString();
    }
    if (fullName.isEmpty()) {
        fullName = obj.value("fName").toString() + QLatin1Char(' ') +
                   obj.value("lName").toString();
        fullName = fullName.trimmed();
    }
    m_FullName = fullName;
    m_Email = obj.value("email").toString();

    QJsonValue cards = obj.value("cards");
    m_Cards = cards.isString() ? cards.toString().toInt() : cards.toInt();

    emit profileChanged();
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
            m_License = trimmed;
            applyProfile(obj);
            QSettings().setValue(kSettingsKey, trimmed);
            emit loggedInChanged();
            emit licenseValidated(true, QString());
        },
        [this](const QString& error) {
            setBusy(false);
            emit licenseValidated(false, tr("Connection error: %1").arg(error));
        });
}

void BoroApi::refreshProfile()
{
    if (m_License.isEmpty()) {
        return;
    }

    // Deliberately does not touch m_Busy: this runs on a timer like the
    // Windows client's fetchUserProfileTimer and must not flicker the UI.
    get(QString("license=%1").arg(m_License),
        [this](const QByteArray& data) {
            QJsonDocument doc = QJsonDocument::fromJson(data);
            if (!doc.isObject()) {
                return;
            }

            QJsonObject obj = doc.object();
            QJsonValue validation = obj.value("validation");
            bool valid = validation.toBool() ||
                    validation.toString().compare(QLatin1String("true"), Qt::CaseInsensitive) == 0;
            if (!valid) {
                return;
            }

            applyProfile(obj);
            emit profileRefreshed();
        },
        [](const QString&) {});
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

            // Current Windows/backend shape (verified against the live API):
            //   [[title], [thumbnailUrlWithoutExtension], ["games.php/<Slug>"]]
            // Older Android-era payloads used flat strings or a full game URL
            // in column 2; both shapes are handled here.
            auto cleanCell = [](const QJsonValue& value) {
                // The live endpoint wraps every cell in its own single-element
                // array: [[title], [thumbnail], [path]]. Older payloads used
                // plain strings, so accept both.
                QJsonValue cell = value;
                if (cell.isArray()) {
                    QJsonArray inner = cell.toArray();
                    if (!inner.isEmpty()) {
                        cell = inner.at(0);
                    }
                }
                return cell.toString().remove('[').remove(']').remove('"').trimmed();
            };

            QJsonArray rows = doc.array();
            for (const QJsonValue& rowValue : rows) {
                QJsonArray row = rowValue.toArray();
                if (row.size() < 3) {
                    continue;
                }

                QString title = cleanCell(row.at(0));
                QString imageUrl = cleanCell(row.at(1));
                QString gamePath = cleanCell(row.at(2));
                if (title.isEmpty() || gamePath.isEmpty()) {
                    continue;
                }

                if (!imageUrl.isEmpty()) {
                    if (imageUrl.startsWith(QLatin1String("//"))) {
                        imageUrl.prepend(QLatin1String("https:"));
                    }
                    bool hasExtension = imageUrl.endsWith(QLatin1String(".jpg"), Qt::CaseInsensitive) ||
                            imageUrl.endsWith(QLatin1String(".jpeg"), Qt::CaseInsensitive) ||
                            imageUrl.endsWith(QLatin1String(".png"), Qt::CaseInsensitive) ||
                            imageUrl.endsWith(QLatin1String(".webp"), Qt::CaseInsensitive) ||
                            imageUrl.contains(QLatin1Char('?'));
                    if (!hasExtension) {
                        imageUrl += QLatin1String(".jpg");
                    }
                }
                else {
                    QString slug = title;
                    slug.replace(QLatin1Char(' '), QLatin1Char('_'));
                    imageUrl = QString(kThumbUrl) + slug + QLatin1String(".jpg");
                }

                // Accept the legacy absolute form too and reduce it to the
                // relative path expected by the SSO redirect.
                if (gamePath.startsWith(QLatin1String("http://")) ||
                        gamePath.startsWith(QLatin1String("https://"))) {
                    gamePath = QUrl(gamePath).path();
                    while (gamePath.startsWith(QLatin1Char('/'))) {
                        gamePath.remove(0, 1);
                    }
                }

                QVariantMap game;
                game["title"] = title;
                game["imageUrl"] = imageUrl;
                game["gameUrl"] = gamePath;
                game["gamePath"] = gamePath;
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

void BoroApi::openGame(const QString& path)
{
    QString gamePath = path.trimmed();
    if (gamePath.isEmpty()) {
        return;
    }

    // Store rows now carry the exact relative path. Keep accepting a bare
    // title from older call sites so nothing regresses during the transition.
    if (!gamePath.contains(QLatin1Char('/')) && !gamePath.contains(QLatin1Char('.'))) {
        gamePath = QLatin1String("games.php/") + gamePath;
        gamePath.replace(QLatin1Char(' '), QLatin1Char('_'));
    }

    QUrl target;
    if (m_License.isEmpty()) {
        target = QUrl(QString("https://borogame.com/%1").arg(gamePath));
    }
    else {
        target = QUrl(ssoUrl(gamePath));
    }
    QDesktopServices::openUrl(target);
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
