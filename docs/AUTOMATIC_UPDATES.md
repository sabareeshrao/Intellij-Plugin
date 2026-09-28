# Automatic IntelliJ Plugin Updates

Every successful push to `main` builds and publishes a newer development plugin version automatically.

## Custom repository URL

```text
https://raw.githubusercontent.com/sabareeshrao/Intellij-Plugin/plugin-repository/updatePlugins.xml
```

Configure this URL once in IntelliJ:

1. Settings → Plugins.
2. Gear icon → Manage Plugin Repositories.
3. Add the URL above.
4. Check for updates.

## Publication flow

```text
push to main
    ↓
GitHub Actions
    ↓
build + tests
    ↓
PLUGIN_VERSION = 0.1.<workflow run number>
    ↓
buildPlugin
    ↓
updatePlugins.xml + versioned plugin ZIP
    ↓
plugin-repository branch
    ↓
raw.githubusercontent.com HTTPS endpoint
    ↓
IntelliJ detects a newer version
```

The installed plugin ID always remains:

```text
com.sabareeshrao.intellijlesson
```

The stable plugin ID is what lets IntelliJ treat every later development build as an update to the plugin already installed on the laptop.

The update channel intentionally uses a dedicated Git branch rather than GitHub Pages, so publishing requires no one-time Pages enablement or additional repository settings.
