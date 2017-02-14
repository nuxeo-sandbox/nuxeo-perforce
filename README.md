# About
Browse and Sync Perforce assets from a remote depot

# Build & Install Nuxeo Package

```
mvn install
$NUXEO/bin/nuxeoctl mp-install nuxeo-perforce-package/target/nuxeo-perforce-package-1.0-SNAPSHOT.zip
```

# Configure Plugin

By default documents are created at `/default-domain/workspaces` and only files whose MIME-type starts with `audio`, `video`, and `image` are imported. You may change these settings via the ConfigurationService:

```
<component name="com.nuxeo.perforce.configuration" version="1.0">
  <extension point="configuration" target="org.nuxeo.runtime.ConfigurationService">
    <property name="com.nuxeo.perforce.fileTypes">audio,video,image</property>
    <property name="com.nuxeo.perforce.contentRoot">/default-domain/workspaces</property>
  </extension>
</component>
```

# Prepare Perforce env

* Download a Helix server: https://www.perforce.com/downloads/helix#server
* Extract the download
* On Unix Systems Copy the `p4*` executables to `/usr/local/bin`
* You may follow the tutorial [here](https://www.perforce.com/perforce/doc.current/manuals/p4guide/chapter.tutorial.html) to create a server, streams, and workspace
  * This is *required* before the connector will work

# Initialize Nuxeo with existing assets

Start the Nuxeo server (make sure the package is installed).

A Node script is provided to ease the initialization; it just starts an Operation that ask for all files from Perforce.

Note that the `nuxeo` client must be installed:

```
cd perforce-triggers && npm install
```

Run the script

```
node initialize.js
```

# Install Perforce Triggers

## Prepare triggers

To prepare them; you need to install NPM dependencies:
```
cd perforce-triggers && npm install
```

Then, you must also set some environment variables / or edit the `change-commit.js` script:
```
export P4CLIENT="<PATH TO p4 EXECUTABLE>";
export NUXEO_HOST="http://localhost:8080/nuxeo";
export NUXEO_USER="Administrator";
export NUXEO_PWD="Administrator";
```

## Add triggers to Perforce

* Run `p4 triggers`
* Go to the end of the file
* Insert the following line, making sure to replace <PATH> with the path to `change-commit.js`:

(IMPORTANT: a leading tab character is *required*)

```
nuxeo change-commit //... "node <PATH>/change-commit.js %changelist% %serverport%"
```
# Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.

More information is available at [www.nuxeo.com](http://www.nuxeo.com).
