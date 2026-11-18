# reference/android — Recovered Borogame Android client sources

These Java sources were recovered by decompiling the **official Borogame
Android APK** (`borogame.apk`) with jadx. They belong to the Borogame service
team and are committed here as the behavioral reference for the macOS port.

Only the **Borogame-specific** classes are kept (the rest of the APK is
unmodified upstream Moonlight-Android code, which we do not duplicate).

| File                                                  | Recovers behavior of |
|-------------------------------------------------------|----------------------|
| `com/limelight/AutoPair.java`                          | `api.php?pinPair` relay |
| `com/limelight/Store.java`                             | Store listing + thumbnails + SSO click |
| `com/limelight/TicketsActivity.java`                   | Ticket list, statuses, WHMCS links |
| `com/limelight/ProfileSectionActivity.java`            | License validation + profile |
| `com/limelight/preferences/AddComputerManually.java`   | `fetchHost` + automatic host add |

These are *decompiled* sources: variable names may not be identical to the
original repository that was deleted. Treat them as ground truth for
**protocol behavior**, not as code to copy verbatim.

See [`../../docs/api-contract.md`](../../docs/api-contract.md) for the
distilled contract.
