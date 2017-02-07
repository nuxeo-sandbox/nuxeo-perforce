# About
Browse and Sync Perforce assets from a remote depot

# Build & Install Java Plugin

```
mvn install
$NUXEO/bin/nuxeoctl mp-install nuxeo-perforce-package/target/nuxeo-perforce-package-1.0-SNAPSHOT.zip
```

# Prepare Perforce env

* Download a Helix server: https://www.perforce.com/downloads/helix#server
* Extract the download
* Copy the `p4*` executables to `/usr/local/bin`
* You may follow the tutorial [here](https://www.perforce.com/perforce/doc.current/manuals/p4guide/chapter.tutorial.html) to create a server, streams, and workspace
  * This is *required* before the connector will work

# Initialize Nuxeo with existing assets

Start the Nuxeo server (make sure the package is installed).

A Node script is provided to ease the initialization; it just starts an Operation that ask for all files from Perforce.

Note that the `nuxeo` client must be installed:

```
npm install nuxeo
```

Run the script

```
./perforce-triggers/initialize.js
```

# Install Perforce Triggers

## Prepare triggers

To prepare them; you need to install NPM dependencies:
```
cd perforce-triggers && npm install
```

Then, you must also set some environment variables / or edit the `change-commit.js` script:
```
export P4CLIENT="/usr/local/bin/p4";
export NUXEO_HOST="http://localhost:8080/nuxeo";
export NUXEO_USER="Administrator";
export NUXEO_PWD="Administrator";
```

## Add triggers to Perforce

* Run `p4 triggers`
* Go to the end of the file
* Insert the following line, making sure to replace <PATH> with the path to `change-commit.js`:

```
nuxeo change-commit //... "<PATH>/change-commit.js %changelist% %serverport%"
```
# Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.

More information is available at [www.nuxeo.com](http://www.nuxeo.com).
