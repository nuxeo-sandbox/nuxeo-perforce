# nuxeo-perforce
Browse and Sync Perforce assets from a remote depot

## Perforce Triggers

To install them, add the folllowing trigger line using `p4 triggers`:
```
nuxeo change-submit //... "/<PATH>/change-commit.js %changelist% %serverport%"
```

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).
