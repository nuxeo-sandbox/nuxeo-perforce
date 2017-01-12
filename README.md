# nuxeo-perforce
Browse and Sync Perforce assets from a remote depot

## Build & Install Java Plugin

```
mvn install
$NUXEO/bin/nuxeoctl mp-install nuxeo-perforce-package/target/nuxeo-perforce-package-1.0-SNAPSHOT.zip
```

## Prepare Perforce env

Download a Helix server: https://www.perforce.com/downloads/helix#server

## Initialize Nuxeo with existing assets

A Node script has been write to ease the initialize; it just starts an Operation that ask all files from Perforce

```
./perforce-triggers/initialize.js
```

## Install Perforce Triggers

### Prepare triggers

To prepare them; you need to install NPM dependencies:
```
cd perforce-triggers && npm install
```

Then, you must also set some env variables / or edit script:
```
export P4CLIENT="/Users/arnaud/Nuxeo/tmp/perforce/p4";
export NUXEO_HOST="http://localhost:8080/nuxeo";
export NUXEO_USER="Administrator";
export NUXEO_PWD="Administrator";
```

### Add triggers to Perforce
To install them, use the p4 client, and add the folllowing trigger line using `p4 triggers`:
```
nuxeo change-commit //... "//<PATH>/change-commit.js %changelist% %serverport%"
```

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).
