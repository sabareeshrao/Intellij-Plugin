# Automatic IntelliJ Plugin Updates

The development plugin is published automatically after every successful push to `main`.

## Custom repository URL

```text
https://sabareeshrao.github.io/Intellij-Plugin/updatePlugins.xml
```

Configure this URL once in IntelliJ:

1. Settings → Plugins.
2. Gear icon → Manage Plugin Repositories.
3. Add the URL above.
4. Check for plugin updates.

Every successful `main` build gets a monotonically increasing version in the form `0.1.<GitHub run number>`. The same workflow builds the plugin ZIP, generates `updatePlugins.xml`, and deploys both to GitHub Pages.

The installed plugin ID never changes:

```text
com.sabareeshrao.intellijlesson
```

Keeping the plugin ID stable is what lets IntelliJ recognize later builds as updates to the existing installation.
